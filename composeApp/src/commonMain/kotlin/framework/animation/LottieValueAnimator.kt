package framework.animation

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * This is a slightly modified [ValueAnimator] that allows us to update start and end values
 * easily optimizing for the fact that we know that it's a value animator with 2 floats.
 */
class LottieValueAnimator : BaseLottieAnimator(), FrameCallback {
    /**
     * Returns the current speed. This will be affected by repeat mode REVERSE.
     */
    var speed = 1f
    private var speedReversedForRepeatMode = false
    private var lastFrameTimeNs: Long = 0
    private var frameRaw = 0f
    private var frame = 0f
    private var repeatCount = 0
    private var minFrame = Int.MIN_VALUE.toFloat()
    private var maxFrame = Int.MAX_VALUE.toFloat()
    private var composition: LottieComposition? = null

    @androidx.annotation.VisibleForTesting
    var isRunning = false
        protected set
    private var useCompositionFrameRate = false
    val animatedValue: Any
        /**
         * Returns a float representing the current value of the animation from 0 to 1
         * regardless of the animation speed, direction, or min and max frames.
         */
        get() = animatedValueAbsolute

    @get:FloatRange(from = 0f, to = 1f)
    val animatedValueAbsolute: Float
        /**
         * Returns the current value of the animation from 0 to 1 regardless
         * of the animation speed, direction, or min and max frames.
         */
        get() = if (composition == null) {
            0
        } else (frame - composition!!.startFrame) / (composition!!.endFrame - composition!!.startFrame)

    @get:FloatRange(from = 0f, to = 1f)
    val animatedFraction: Float
        /**
         * Returns the current value of the currently playing animation taking into
         * account direction, min and max frames.
         */
        get() {
            if (composition == null) {
                return 0
            }
            return if (isReversed) {
                (getMaxFrame() - frame) / (getMaxFrame() - getMinFrame())
            } else {
                (frame - getMinFrame()) / (getMaxFrame() - getMinFrame())
            }
        }
    val duration: Long
        get() = if (composition == null) 0 else composition!!.duration.toLong()

    fun getFrame(): Float {
        return frame
    }

    fun setUseCompositionFrameRate(useCompositionFrameRate: Boolean) {
        this.useCompositionFrameRate = useCompositionFrameRate
    }

    override fun doFrame(frameTimeNanos: Long) {
        postFrameCallback()
        if (composition == null || !isRunning) {
            return
        }
        com.airbnb.lottie.L.beginSection("LottieValueAnimator#doFrame")
        val timeSinceFrame = if (lastFrameTimeNs == 0L) 0 else frameTimeNanos - lastFrameTimeNs
        val frameDuration = frameDurationNs
        val dFrames = timeSinceFrame / frameDuration
        val newFrameRaw = frameRaw + if (isReversed) -dFrames else dFrames
        val ended: Boolean = !MiscUtils.contains(newFrameRaw, getMinFrame(), getMaxFrame())
        val previousFrameRaw = frameRaw
        frameRaw = MiscUtils.clamp(newFrameRaw, getMinFrame(), getMaxFrame())
        frame = if (useCompositionFrameRate) floor(frameRaw.toDouble()).toFloat() else frameRaw
        lastFrameTimeNs = frameTimeNanos
        if (!useCompositionFrameRate || frameRaw != previousFrameRaw) {
            notifyUpdate()
        }
        if (ended) {
            if (getRepeatCount() != INFINITE && repeatCount >= getRepeatCount()) {
                frameRaw = if (speed < 0) getMinFrame() else getMaxFrame()
                frame = frameRaw
                removeFrameCallback()
                notifyEnd(isReversed)
            } else {
                notifyRepeat()
                repeatCount++
                if (getRepeatMode() == REVERSE) {
                    speedReversedForRepeatMode = !speedReversedForRepeatMode
                    reverseAnimationSpeed()
                } else {
                    frameRaw = if (isReversed) getMaxFrame() else getMinFrame()
                    frame = frameRaw
                }
                lastFrameTimeNs = frameTimeNanos
            }
        }
        verifyFrame()
        com.airbnb.lottie.L.endSection("LottieValueAnimator#doFrame")
    }

    private val frameDurationNs: Float
        private get() {
            return if (composition == null) {
                Float.MAX_VALUE
            } else com.airbnb.lottie.utils.Utils.SECOND_IN_NANOS / composition.getFrameRate() / abs(
                speed.toDouble()
            )
        }

    fun clearComposition() {
        composition = null
        minFrame = Int.MIN_VALUE.toFloat()
        maxFrame = Int.MAX_VALUE.toFloat()
    }

    fun setComposition(composition: LottieComposition) {
        // Because the initial composition is loaded async, the first min/max frame may be set
        val keepMinAndMaxFrames = this.composition == null
        this.composition = composition
        if (keepMinAndMaxFrames) {
            setMinAndMaxFrames(
                max(minFrame.toDouble(), composition.getStartFrame().toDouble())
                    .toFloat(),
                min(maxFrame.toDouble(), composition.getEndFrame().toDouble()).toFloat()
            )
        } else {
            setMinAndMaxFrames(
                composition.getStartFrame().toInt().toFloat(), composition.getEndFrame().toInt()
                    .toFloat()
            )
        }
        val frame = frame
        this.frame = 0f
        frameRaw = 0f
        setFrame(frame.toInt().toFloat())
        notifyUpdate()
    }

    fun setFrame(frame: Float) {
        if (frameRaw == frame) {
            return
        }
        frameRaw = MiscUtils.clamp(frame, getMinFrame(), getMaxFrame())
        this.frame = if (useCompositionFrameRate) floor(frameRaw.toDouble()).toFloat() else frameRaw
        lastFrameTimeNs = 0
        notifyUpdate()
    }

    fun setMinFrame(minFrame: Int) {
        setMinAndMaxFrames(minFrame.toFloat(), maxFrame.toInt().toFloat())
    }

    fun setMaxFrame(maxFrame: Float) {
        setMinAndMaxFrames(minFrame, maxFrame)
    }

    fun setMinAndMaxFrames(minFrame: Float, maxFrame: Float) {
        if (minFrame > maxFrame) {
            throw java.lang.IllegalArgumentException(
                String.format(
                    "minFrame (%s) must be <= maxFrame (%s)",
                    minFrame,
                    maxFrame
                )
            )
        }
        val compositionMinFrame =
            if (composition == null) -Float.MAX_VALUE else composition.getStartFrame()
        val compositionMaxFrame =
            if (composition == null) Float.MAX_VALUE else composition.getEndFrame()
        val newMinFrame: Float = MiscUtils.clamp(minFrame, compositionMinFrame, compositionMaxFrame)
        val newMaxFrame: Float = MiscUtils.clamp(maxFrame, compositionMinFrame, compositionMaxFrame)
        if (newMinFrame != this.minFrame || newMaxFrame != this.maxFrame) {
            this.minFrame = newMinFrame
            this.maxFrame = newMaxFrame
            setFrame(MiscUtils.clamp(frame, newMinFrame, newMaxFrame).toInt().toFloat())
        }
    }

    fun reverseAnimationSpeed() {
        speed = -speed
    }

    override fun setRepeatMode(value: Int) {
        super.setRepeatMode(value)
        if (value != ValueAnimator.REVERSE && speedReversedForRepeatMode) {
            speedReversedForRepeatMode = false
            reverseAnimationSpeed()
        }
    }

    @MainThread
    fun playAnimation() {
        isRunning = true
        notifyStart(isReversed)
        setFrame((if (isReversed) getMaxFrame() else getMinFrame()).toInt().toFloat())
        lastFrameTimeNs = 0
        repeatCount = 0
        postFrameCallback()
    }

    @MainThread
    fun endAnimation() {
        removeFrameCallback()
        notifyEnd(isReversed)
    }

    @MainThread
    fun pauseAnimation() {
        removeFrameCallback()
        notifyPause()
    }

    @MainThread
    fun resumeAnimation() {
        isRunning = true
        postFrameCallback()
        lastFrameTimeNs = 0
        if (isReversed && getFrame() == getMinFrame()) {
            setFrame(getMaxFrame())
        } else if (!isReversed && getFrame() == getMaxFrame()) {
            setFrame(getMinFrame())
        }
        notifyResume()
    }

    @MainThread
    override fun cancel() {
        notifyCancel()
        removeFrameCallback()
    }

    private val isReversed: Boolean
        private get() = speed < 0

    fun getMinFrame(): Float {
        if (composition == null) {
            return 0
        }
        return if (minFrame == Int.MIN_VALUE) composition.getStartFrame() else minFrame
    }

    fun getMaxFrame(): Float {
        if (composition == null) {
            return 0
        }
        return if (maxFrame == Int.MAX_VALUE) composition.getEndFrame() else maxFrame
    }

    override fun notifyCancel() {
        super.notifyCancel()
        notifyEnd(isReversed)
    }

    protected fun postFrameCallback() {
        if (isRunning) {
            removeFrameCallback(false)
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    protected fun removeFrameCallback() {
        this.removeFrameCallback(true)
    }

    protected fun removeFrameCallback(stopRunning: Boolean) {
        Choreographer.getInstance().removeFrameCallback(this)
        if (stopRunning) {
            isRunning = false
        }
    }

    private fun verifyFrame() {
        if (composition == null) {
            return
        }
        if (frame < minFrame || frame > maxFrame) {
            throw IllegalStateException(
                String.format(
                    "Frame must be [%f,%f]. It is %f",
                    minFrame,
                    maxFrame,
                    frame
                )
            )
        }
    }
}

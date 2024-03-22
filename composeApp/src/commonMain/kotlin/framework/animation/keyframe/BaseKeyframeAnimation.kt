package framework.animation.keyframe

import framework.animation.FloatRange
import framework.animation.L
import framework.animation.value.Keyframe
import framework.animation.value.LottieValueCallback

/**
 * @param <K> Keyframe type
 * @param <A> Animation type
</A></K> */
abstract class BaseKeyframeAnimation<K, A> internal constructor(keyframes: List<Keyframe<K>>) {
    interface AnimationListener {
        fun onValueChanged()
    }

    // This is not a Set because we don't want to create an iterator object on every setProgress.
    val listeners: MutableList<AnimationListener> =
        ArrayList(1)
    private var isDiscrete = false
    private val keyframesWrapper: KeyframesWrapper<K>
    protected var progress = 0f
    protected var valueCallback: LottieValueCallback<A>? = null
    private var cachedGetValue: A? = null
    private var cachedStartDelayProgress = -1f
    private var cachedEndProgress = -1f

    init {
        keyframesWrapper = wrap(keyframes)
    }

    fun setIsDiscrete() {
        isDiscrete = true
    }

    fun addUpdateListener(listener: AnimationListener) {
        listeners.add(listener)
    }

    open fun setProgress(@FloatRange(from = 0f, to = 1f) progress: Float) {
        var progress = progress
        L.beginSection("BaseKeyframeAnimation#setProgress")
        if (keyframesWrapper.isEmpty) {
            L.endSection("BaseKeyframeAnimation#setProgress")
            return
        }
        if (progress < startDelayProgress) {
            progress = startDelayProgress
        } else if (progress > endProgress) {
            progress = endProgress
        }
        if (progress == this.progress) {
            L.endSection("BaseKeyframeAnimation#setProgress")
            return
        }
        this.progress = progress
        if (keyframesWrapper.isValueChanged(progress)) {
            notifyListeners()
        }
        L.endSection("BaseKeyframeAnimation#setProgress")
    }

    open fun notifyListeners() {
        L.beginSection("BaseKeyframeAnimation#notifyListeners")
        for (i in listeners.indices) {
            listeners[i].onValueChanged()
        }
        L.endSection("BaseKeyframeAnimation#notifyListeners")
    }

    protected val currentKeyframe: Keyframe<K>
        protected get() {
            L.beginSection("BaseKeyframeAnimation#getCurrentKeyframe")
            val keyframe: Keyframe<K> = keyframesWrapper.currentKeyframe
            L.endSection("BaseKeyframeAnimation#getCurrentKeyframe")
            return keyframe
        }
    val linearCurrentKeyframeProgress: Float
        /**
         * Returns the progress into the current keyframe between 0 and 1. This does not take into account
         * any interpolation that the keyframe may have.
         */
        get() {
            if (isDiscrete) {
                return 0f
            }
            val keyframe: Keyframe<K> = currentKeyframe
            if (keyframe.isStatic) {
                return 0f
            }
            val progressIntoFrame: Float = progress - keyframe.startProgress
            val keyframeProgress: Float = keyframe.endProgress - keyframe.startProgress
            return progressIntoFrame / keyframeProgress
        }
    protected val interpolatedCurrentKeyframeProgress: Float
        /**
         * Takes the value of [.getLinearCurrentKeyframeProgress] and interpolates it with
         * the current keyframe's interpolator.
         */
        protected get() {
            val keyframe: Keyframe<K> = currentKeyframe
            // Keyframe should not be null here but there seems to be a Xiaomi Android 10 specific crash.
            // https://github.com/airbnb/lottie-android/issues/2050
            return if (keyframe == null || keyframe.isStatic) {
                0f
            } else keyframe.interpolator.getInterpolation(linearCurrentKeyframeProgress)
        }

    @get:FloatRange(from = 0f, to = 1f)
    private val startDelayProgress: Float
        private get() {
            if (cachedStartDelayProgress == -1f) {
                cachedStartDelayProgress = keyframesWrapper.startDelayProgress
            }
            return cachedStartDelayProgress
        }

    @get:FloatRange(from = 0f, to = 1f)
    open val endProgress: Float
        get() {
            if (cachedEndProgress == -1f) {
                cachedEndProgress = keyframesWrapper.endProgress
            }
            return cachedEndProgress
        }
    open val value: A?
        get() {
            val value: A
            val linearProgress = linearCurrentKeyframeProgress
            if (valueCallback == null && keyframesWrapper.isCachedValueEnabled(linearProgress)) {
                return cachedGetValue
            }
            val keyframe: Keyframe<K> = currentKeyframe
            value = if (keyframe.xInterpolator != null && keyframe.yInterpolator != null) {
                val xProgress: Float = keyframe.xInterpolator.getInterpolation(linearProgress)
                val yProgress: Float = keyframe.yInterpolator.getInterpolation(linearProgress)
                getValue(keyframe, linearProgress, xProgress, yProgress)
            } else {
                val progress = interpolatedCurrentKeyframeProgress
                getValue(keyframe, progress)
            }
            cachedGetValue = value
            return value
        }

    fun getProgress(): Float {
        return progress
    }

    fun setValueCallback(valueCallback: LottieValueCallback<A>?) {
        if (this.valueCallback != null) {
            this.valueCallback.setAnimation(null)
        }
        this.valueCallback = valueCallback
        if (valueCallback != null) {
            valueCallback.setAnimation(this)
        }
    }

    fun hasValueCallback(): Boolean {
        return valueCallback != null
    }

    /**
     * keyframeProgress will be [0, 1] unless the interpolator has overshoot in which case, this
     * should be able to handle values outside of that range.
     */
    abstract fun getValue(
        keyframe: Keyframe<K>,
        keyframeProgress: Float
    ): A

    /**
     * Similar to [.getValue] but used when an animation has separate interpolators for the X and Y axis.
     */
    protected fun getValue(
        keyframe: Keyframe<K>?,
        linearKeyframeProgress: Float,
        xKeyframeProgress: Float,
        yKeyframeProgress: Float
    ): A {
        throw UnsupportedOperationException("This animation does not support split dimensions!")
    }

    private interface KeyframesWrapper<T> {
        val isEmpty: Boolean

        fun isValueChanged(progress: Float): Boolean
        val currentKeyframe: Keyframe<T>

        @FloatRange(from = 0f, to = 1f)
        val startDelayProgress: Float

        @FloatRange(from = 0f, to = 1f)
        val endProgress: Float

        fun isCachedValueEnabled(progress: Float): Boolean
    }

    private class EmptyKeyframeWrapper<T> :KeyframesWrapper<T> {
        override val isEmpty: Boolean
            get() = true

        override fun isValueChanged(progress: Float): Boolean {
            return false
        }

        override val currentKeyframe: Keyframe<T>
            get() {
                throw IllegalStateException("not implemented")
            }
        override val startDelayProgress: Float
            get() = 0f
        override val endProgress: Float
            get() = 1f

        override fun isCachedValueEnabled(progress: Float): Boolean {
            throw IllegalStateException("not implemented")
        }
    }

    private class SingleKeyframeWrapper<T> (keyframes: List<Keyframe<T>>) :
        KeyframesWrapper<T> {
        private val keyframe: Keyframe<T>
        private var cachedInterpolatedProgress = -1f

        init {
            keyframe = keyframes[0]
        }

        override val isEmpty: Boolean
            get() = false

        override fun isValueChanged(progress: Float): Boolean {
            return !keyframe.isStatic
        }

        override val currentKeyframe: Keyframe<T>
            get() = keyframe
        override val startDelayProgress: Float
            get() = keyframe.startProgress
        override val endProgress: Float
            get() = keyframe.endProgress

        override fun isCachedValueEnabled(progress: Float): Boolean {
            if (cachedInterpolatedProgress == progress) {
                return true
            }
            cachedInterpolatedProgress = progress
            return false
        }
    }

    private class KeyframesWrapperImpl<T> (keyframes: List<Keyframe<T>>) : KeyframesWrapper<T> {
        private val keyframes: List<Keyframe<T>>
        override var currentKeyframe: Keyframe<T>
        private var cachedCurrentKeyframe: Keyframe<T>? = null
        private var cachedInterpolatedProgress = -1f

        init {
            this.keyframes = keyframes
            currentKeyframe = findKeyframe(0f)
        }

        override val isEmpty: Boolean
            get() = false

        override fun isValueChanged(progress: Float): Boolean {
            if (currentKeyframe.containsProgress(progress)) {
                return !currentKeyframe.isStatic
            }
            currentKeyframe = findKeyframe(progress)
            return true
        }

        private fun findKeyframe(progress: Float): Keyframe<T> {
            var keyframe: Keyframe<T> = keyframes[keyframes.size - 1]
            if (progress >= keyframe.startProgress) {
                return keyframe
            }
            for (i in keyframes.size - 2 downTo 1) {
                keyframe = keyframes[i]
                if (currentKeyframe === keyframe) {
                    continue
                }
                if (keyframe.containsProgress(progress)) {
                    return keyframe
                }
            }
            return keyframes[0]
        }

        fun getCurrentKeyframe(): Keyframe<T> {
            return currentKeyframe
        }

        override val startDelayProgress: Float
            get() = keyframes[0].startProgress
        override val endProgress: Float
            get() = keyframes[keyframes.size - 1].endProgress

        override fun isCachedValueEnabled(progress: Float): Boolean {
            if (cachedCurrentKeyframe === currentKeyframe
                && cachedInterpolatedProgress == progress
            ) {
                return true
            }
            cachedCurrentKeyframe = currentKeyframe
            cachedInterpolatedProgress = progress
            return false
        }
    }

    companion object {
        private fun <T> wrap(keyframes: List<Keyframe<T>>): KeyframesWrapper<T> {
            if (keyframes.isEmpty()) {
                return EmptyKeyframeWrapper()
            }
            return if (keyframes.size == 1) {
                SingleKeyframeWrapper(
                    keyframes
                )
            } else KeyframesWrapperImpl(
                keyframes
            )
        }
    }
}

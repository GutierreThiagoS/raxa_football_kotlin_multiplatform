package framework.animation.value

import framework.animation.FloatRange
import framework.animation.LottieComposition
import framework.animation.PointF

class Keyframe<T> {
    private val composition: LottieComposition?
    val startValue: T?
    var endValue: T?
    val interpolator: android.view.animation.Interpolator?
    val xInterpolator: android.view.animation.Interpolator?
    val yInterpolator: android.view.animation.Interpolator?
    val startFrame: Float
    var endFrame: Float?
    var startValueFloat: Float = UNSET_FLOAT
        /**
         * Optimization to avoid autoboxing.
         */
        get() {
            if (field == UNSET_FLOAT) {
                field = startValue as Float? as Float
            }
            return field
        }
        private set
    var endValueFloat: Float = UNSET_FLOAT
        /**
         * Optimization to avoid autoboxing.
         */
        get() {
            if (field == UNSET_FLOAT) {
                field = endValue as Float? as Float
            }
            return field
        }
        private set
    var startValueInt: Int = UNSET_INT
        /**
         * Optimization to avoid autoboxing.
         */
        get() {
            if (field == UNSET_INT) {
                field = startValue as Int? as Int
            }
            return field
        }
        private set
    var endValueInt: Int = UNSET_INT
        /**
         * Optimization to avoid autoboxing.
         */
        get() {
            if (field == UNSET_INT) {
                field = endValue as Int? as Int
            }
            return field
        }
        private set
    var startProgress = Float.MIN_VALUE
        get() {
            if (composition == null) {
                return 0f
            }
            if (field == Float.MIN_VALUE) {
                field = (startFrame - composition.startFrame) / composition.getDurationFrames()
            }
            return field
        }
        private set
    var endProgress = Float.MIN_VALUE
        get() {
            if (composition == null) {
                return 1f
            }
            if (field == Float.MIN_VALUE) {
                field = if (endFrame == null) {
                    1f
                } else {
                    val startProgress = startProgress
                    val durationFrames = endFrame!! - startFrame
                    val durationProgress: Float = durationFrames / composition.getDurationFrames()
                    startProgress + durationProgress
                }
            }
            return field
        }
        private set

    // Used by PathKeyframe but it has to be parsed by KeyFrame because we use a JsonReader to
    // deserialzie the data so we have to parse everything in order
    var pathCp1: PointF? = null
    var pathCp2: PointF? = null

    constructor(
        composition: LottieComposition?,
        startValue: T?, endValue: T?,
        interpolator: android.view.animation.Interpolator?, startFrame: Float, endFrame: Float?
    ) {
        this.composition = composition
        this.startValue = startValue
        this.endValue = endValue
        this.interpolator = interpolator
        xInterpolator = null
        yInterpolator = null
        this.startFrame = startFrame
        this.endFrame = endFrame
    }

    constructor(
        composition: LottieComposition?,
        startValue: T?,
        endValue: T?,
        xInterpolator: android.view.animation.Interpolator?,
        yInterpolator: android.view.animation.Interpolator?,
        startFrame: Float,
        endFrame: Float?
    ) {
        this.composition = composition
        this.startValue = startValue
        this.endValue = endValue
        interpolator = null
        this.xInterpolator = xInterpolator
        this.yInterpolator = yInterpolator
        this.startFrame = startFrame
        this.endFrame = endFrame
    }

    protected constructor(
        composition: LottieComposition?,
        startValue: T?,
        endValue: T?,
        interpolator: android.view.animation.Interpolator?,
        xInterpolator: android.view.animation.Interpolator?,
        yInterpolator: android.view.animation.Interpolator?,
        startFrame: Float,
        endFrame: Float?
    ) {
        this.composition = composition
        this.startValue = startValue
        this.endValue = endValue
        this.interpolator = interpolator
        this.xInterpolator = xInterpolator
        this.yInterpolator = yInterpolator
        this.startFrame = startFrame
        this.endFrame = endFrame
    }

    /**
     * Non-animated value.
     */
    constructor(value: T) {
        composition = null
        startValue = value
        endValue = value
        interpolator = null
        xInterpolator = null
        yInterpolator = null
        startFrame = Float.MIN_VALUE
        endFrame = Float.MAX_VALUE
    }

    private constructor(startValue: T, endValue: T) {
        composition = null
        this.startValue = startValue
        this.endValue = endValue
        interpolator = null
        xInterpolator = null
        yInterpolator = null
        startFrame = Float.MIN_VALUE
        endFrame = Float.MAX_VALUE
    }

    fun copyWith(startValue: T, endValue: T): Keyframe<T> {
        return Keyframe<T>(startValue, endValue)
    }

    val isStatic: Boolean
        get() = interpolator == null && xInterpolator == null && yInterpolator == null

    fun containsProgress(
        @FloatRange(
            from = 0f,
            to = 1f
        ) progress: Float
    ): Boolean {
        return progress >= startProgress && progress < endProgress
    }

    override fun toString(): String {
        return "Keyframe{" + "startValue=" + startValue +
                ", endValue=" + endValue +
                ", startFrame=" + startFrame +
                ", endFrame=" + endFrame +
                ", interpolator=" + interpolator +
                '}'
    }

    companion object {
        private const val UNSET_FLOAT = -3987645.78543923f
        private const val UNSET_INT = 784923401
    }
}

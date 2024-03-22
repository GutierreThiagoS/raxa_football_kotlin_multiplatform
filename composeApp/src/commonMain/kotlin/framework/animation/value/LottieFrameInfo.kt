package framework.animation.value

import framework.animation.annotation.RestrictTo

/**
 * Data class for use with [LottieValueCallback].
 * You should *not* hold a reference to the frame info parameter passed to your callback. It will be reused.
 */
class LottieFrameInfo<T>() {
    var startFrame = 0f
        private set
    var endFrame = 0f
        private set
    var startValue: T? = null
        private set
    var endValue: T? = null
        private set
    var linearKeyframeProgress = 0f
        private set
    var interpolatedKeyframeProgress = 0f
        private set
    var overallProgress = 0f
        private set

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    operator fun set(
        startFrame: Float,
        endFrame: Float,
        startValue: T,
        endValue: T,
        linearKeyframeProgress: Float,
        interpolatedKeyframeProgress: Float,
        overallProgress: Float
    ): LottieFrameInfo<T> {
        this.startFrame = startFrame
        this.endFrame = endFrame
        this.startValue = startValue
        this.endValue = endValue
        this.linearKeyframeProgress = linearKeyframeProgress
        this.interpolatedKeyframeProgress = interpolatedKeyframeProgress
        this.overallProgress = overallProgress
        return this
    }
}

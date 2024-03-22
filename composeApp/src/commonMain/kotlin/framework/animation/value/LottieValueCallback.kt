package framework.animation.value

import framework.animation.annotation.RestrictTo
import framework.animation.keyframe.BaseKeyframeAnimation

/**
 * Allows you to set a callback on a resolved [com.airbnb.lottie.model.KeyPath] to modify
 * its animation values at runtime.
 *
 * If your dynamic property does the following, you must call [LottieAnimationView.invalidate] or
 * [LottieDrawable.invalidateSelf] each time you want to update this value.
 * 1. Use [com.airbnb.lottie.RenderMode.SOFTWARE]
 * 2. Rendering a static image (the animation is either paused or there are no values
 * changing within the animation itself)
 * When using software rendering, Lottie caches the internal rendering bitmap. Whenever the animation changes
 * internally, Lottie knows to invalidate the bitmap and re-render it on the next frame. If the animation
 * never changes but your dynamic property does outside of Lottie, Lottie must be notified that it changed
 * in order to set the bitmap as dirty and re-render it on the next frame.
 */
class LottieValueCallback<T> {
    private val frameInfo: LottieFrameInfo<T> = LottieFrameInfo<T>()
    private var animation: BaseKeyframeAnimation<*, *>? = null

    /**
     * This can be set with [.setValue] to use a value instead of deferring
     * to the callback.
     */
    protected var value: T? = null

    constructor()
    constructor(staticValue: T?) {
        value = staticValue
    }

    /**
     * Override this if you haven't set a static value in the constructor or with setValue.
     *
     *
     * Return null to resort to the default value.
     *
     * Refer to the javadoc for this class for a special case that requires manual invalidation
     * each time you want to return something different from this method.
     */
    fun getValue(frameInfo: LottieFrameInfo<T>?): T? {
        return value
    }

    fun setValue(value: T?) {
        this.value = value
        if (animation != null) {
            animation!!.notifyListeners()
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun getValueInternal(
        startFrame: Float,
        endFrame: Float,
        startValue: T,
        endValue: T,
        linearKeyframeProgress: Float,
        interpolatedKeyframeProgress: Float,
        overallProgress: Float
    ): T? {
        return getValue(
            frameInfo.set(
                startFrame,
                endFrame,
                startValue,
                endValue,
                linearKeyframeProgress,
                interpolatedKeyframeProgress,
                overallProgress
            )
        )
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun setAnimation(animation: BaseKeyframeAnimation<*, *>?) {
        this.animation = animation
    }
}

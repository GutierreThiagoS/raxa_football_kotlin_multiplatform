package framework.animation.keyframe

import framework.animation.value.Keyframe

class IntegerKeyframeAnimation(
    keyframes: List<Keyframe<Int>>
) : KeyframeAnimation<Int>(keyframes) {
    override fun getValue(
        keyframe: Keyframe<Int>,
        keyframeProgress: Float
    ): Int {
        return getIntValue(keyframe, keyframeProgress)
    }

    /**
     * Optimization to avoid autoboxing.
     */
    fun getIntValue(keyframe: Keyframe<Int>, keyframeProgress: Float): Int {
        if (keyframe.startValue == null || keyframe.endValue == null) {
            throw IllegalStateException("Missing values for keyframe.")
        }
        if (valueCallback != null) {
            val value: Int = valueCallback.getValueInternal(
                keyframe.startFrame, keyframe.endFrame,
                keyframe.startValue, keyframe.endValue,
                keyframeProgress, linearCurrentKeyframeProgress, getProgress()
            )
            if (value != null) {
                return value
            }
        }
        return MiscUtils.lerp(
            keyframe.startValueInt,
            keyframe.endValueInt,
            keyframeProgress
        )
    }

    val intValue: Int
        /**
         * Optimization to avoid autoboxing.
         */
        get() = getIntValue(getCurrentKeyframe(), getInterpolatedCurrentKeyframeProgress())
}

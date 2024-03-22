package framework.animation.model.animatable

import framework.animation.keyframe.BaseKeyframeAnimation
import framework.animation.keyframe.IntegerKeyframeAnimation
import framework.animation.value.Keyframe


abstract class AnimatableIntegerValue(keyframes: List<Keyframe<Int>>) :
    BaseAnimatableValue<Int, Int>(keyframes) {
    override fun createAnimation(): BaseKeyframeAnimation<Int, Int> {
        return IntegerKeyframeAnimation(keyframes)
    }
}

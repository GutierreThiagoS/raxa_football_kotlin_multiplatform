package framework.animation.model.animatable

import framework.animation.keyframe.BaseKeyframeAnimation
import framework.animation.value.Keyframe

interface AnimatableValue<K, A> {
    fun getKeyframes(): List<Keyframe<K>>

    fun isStatic(): Boolean

    fun createAnimation(): BaseKeyframeAnimation<K, A>
}
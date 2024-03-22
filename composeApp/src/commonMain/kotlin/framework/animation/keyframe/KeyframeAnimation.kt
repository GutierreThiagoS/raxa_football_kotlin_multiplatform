package framework.animation.keyframe

import framework.animation.value.Keyframe


abstract class KeyframeAnimation<T>(keyframes: List<Keyframe<T>>) : BaseKeyframeAnimation<T, T>(keyframes)


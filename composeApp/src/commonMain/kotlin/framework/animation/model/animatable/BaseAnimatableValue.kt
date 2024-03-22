package framework.animation.model.animatable

import framework.animation.value.Keyframe

abstract class BaseAnimatableValue<V, O>(keyframes: List<Keyframe<V>>) :
    AnimatableValue<V, O> {
    val keyframes: List<Keyframe<V>>

    /**
     * Create a default static animatable path.
     */
    constructor(value: V) : this(
        listOf<Keyframe<V>>(
            Keyframe<V>(
                value
            )
        )
    )

    init {
        this.keyframes = keyframes
    }

    override fun getKeyframes(): List<Keyframe<V>> {
        return keyframes
    }

    val isStatic: Boolean
        get() = keyframes.isEmpty() || keyframes.size == 1 && keyframes[0].isStatic

    override fun toString(): String {
        val sb = StringBuilder()
        if (keyframes.isNotEmpty()) {
            sb.append("values=").append(keyframes.toTypedArray().contentToString())
        }
        return sb.toString()
    }
}

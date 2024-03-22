package framework.animation.model

import framework.animation.model.animatable.AnimatableIntegerValue
import framework.animation.model.animatable.AnimatableShapeValue

class Mask(
    maskMode: MaskMode,
    maskPath: AnimatableShapeValue,
    opacity: AnimatableIntegerValue,
    inverted: Boolean
) {
    enum class MaskMode {
        MASK_MODE_ADD,
        MASK_MODE_SUBTRACT,
        MASK_MODE_INTERSECT,
        MASK_MODE_NONE
    }

    private val maskMode: MaskMode
    private val maskPath: AnimatableShapeValue
    private val opacity: AnimatableIntegerValue
    val isInverted: Boolean

    init {
        this.maskMode = maskMode
        this.maskPath = maskPath
        this.opacity = opacity
        isInverted = inverted
    }

    fun getMaskMode(): MaskMode {
        return maskMode
    }

    fun getMaskPath(): AnimatableShapeValue {
        return maskPath
    }

    fun getOpacity(): AnimatableIntegerValue {
        return opacity
    }
}

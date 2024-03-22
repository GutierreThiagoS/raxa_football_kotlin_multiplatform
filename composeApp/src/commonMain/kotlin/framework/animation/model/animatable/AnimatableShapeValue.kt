package framework.animation.model.animatable

import androidx.compose.ui.graphics.Path
import framework.animation.model.animatable.BaseAnimatableValue
import framework.animation.model.content.ShapeData
import framework.animation.value.Keyframe


class AnimatableShapeValue(keyframes: List<Keyframe<ShapeData>>) : BaseAnimatableValue<ShapeData, Path>(keyframes) {
    override fun createAnimation(): ShapeKeyframeAnimation {
        return ShapeKeyframeAnimation(keyframes)
    }
}

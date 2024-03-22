package framework.animation

import androidx.compose.ui.text.intl.Locale
import framework.animation.model.Mask
import framework.animation.value.Keyframe

class Layer(
    shapes: List<ContentModel>,
    composition: LottieComposition,
    layerName: String,
    layerId: Long,
    layerType: LayerType,
    parentId: Long,
    refId: String?,
    masks: List<Mask>,
    transform: AnimatableTransform,
    solidWidth: Int,
    solidHeight: Int,
    solidColor: Int,
    timeStretch: Float,
    startFrame: Float,
    preCompWidth: Float,
    preCompHeight: Float,
    text: AnimatableTextFrame?,
    textProperties: AnimatableTextProperties?,
    inOutKeyframes: List<Keyframe<Float?>?>,
    matteType: Layer.MatteType,
    timeRemapping: AnimatableFloatValue?,
    hidden: Boolean,
    blurEffect: com.airbnb.lottie.model.content.BlurEffect?,
    dropShadowEffect: com.airbnb.lottie.parser.DropShadowEffect?,
    blendMode: LBlendMode
) {
    enum class LayerType {
        PRE_COMP,
        SOLID,
        IMAGE,
        NULL,
        SHAPE,
        TEXT,
        UNKNOWN
    }

    enum class MatteType {
        NONE,
        ADD,
        INVERT,
        LUMA,
        LUMA_INVERTED,
        UNKNOWN
    }

    private val shapes: List<ContentModel>
    private val composition: LottieComposition
    val name: String
    val id: Long
    private val layerType: LayerType
    val parentId: Long
    val refId: String?
    private val masks: List<Mask>
    private val transform: AnimatableTransform
    val solidWidth: Int
    val solidHeight: Int
    val solidColor: Int
    val timeStretch: Float
    private val startFrame: Float
    val preCompWidth: Float
    val preCompHeight: Float
    private val text: AnimatableTextFrame?
    private val textProperties: AnimatableTextProperties?
    private val timeRemapping: AnimatableFloatValue?
    private val inOutKeyframes: List<Keyframe<Float>>
    private val matteType: MatteType
    val isHidden: Boolean
    private val blurEffect: com.airbnb.lottie.model.content.BlurEffect?
    private val dropShadowEffect: com.airbnb.lottie.parser.DropShadowEffect?
    private val blendMode: LBlendMode

    init {
        this.shapes = shapes
        this.composition = composition
        name = layerName
        id = layerId
        this.layerType = layerType
        this.parentId = parentId
        this.refId = refId
        this.masks = masks
        this.transform = transform
        this.solidWidth = solidWidth
        this.solidHeight = solidHeight
        this.solidColor = solidColor
        this.timeStretch = timeStretch
        this.startFrame = startFrame
        this.preCompWidth = preCompWidth
        this.preCompHeight = preCompHeight
        this.text = text
        this.textProperties = textProperties
        this.inOutKeyframes = inOutKeyframes
        this.matteType = matteType
        this.timeRemapping = timeRemapping
        isHidden = hidden
        this.blurEffect = blurEffect
        this.dropShadowEffect = dropShadowEffect
        this.blendMode = blendMode
    }

    fun getComposition(): LottieComposition {
        return composition
    }

    val startProgress: Float
        get() = startFrame / composition.durationFrames

    fun getInOutKeyframes(): List<Keyframe<Float>> {
        return inOutKeyframes
    }

    fun getMasks(): List<Mask> {
        return masks
    }

    fun getLayerType(): LayerType {
        return layerType
    }

    fun getMatteType(): MatteType {
        return matteType
    }

    fun getShapes(): List<ContentModel> {
        return shapes
    }

    fun getTransform(): AnimatableTransform {
        return transform
    }

    fun getText(): AnimatableTextFrame? {
        return text
    }

    fun getTextProperties(): AnimatableTextProperties? {
        return textProperties
    }

    fun getTimeRemapping(): AnimatableFloatValue? {
        return timeRemapping
    }

    override fun toString(): String {
        return toString("")
    }

    fun getBlendMode(): LBlendMode? {
        return blendMode
    }

    fun getBlurEffect(): com.airbnb.lottie.model.content.BlurEffect? {
        return blurEffect
    }

    fun getDropShadowEffect(): com.airbnb.lottie.parser.DropShadowEffect? {
        return dropShadowEffect
    }

    fun toString(prefix: String?): String {
        val sb = StringBuilder()
        sb.append(prefix).append(name).append("\n")
        var parent: Layer = composition.layerModelForId(parentId)
        if (parent != null) {
            sb.append("\t\tParents: ").append(parent.name)
            parent = composition.layerModelForId(parent.parentId)
            while (parent != null) {
                sb.append("->").append(parent.name)
                parent = composition.layerModelForId(parent.parentId)
            }
            sb.append(prefix).append("\n")
        }
        if (!getMasks().isEmpty()) {
            sb.append(prefix).append("\tMasks: ").append(getMasks().size).append("\n")
        }
        if (solidWidth != 0 && solidHeight != 0) {
            sb.append(prefix).append("\tBackground: ").append(
                String.format(
                    Locale.US, "%dx%d %X\n",
                    solidWidth,
                    solidHeight,
                    solidColor
                )
            )
        }
        if (!shapes.isEmpty()) {
            sb.append(prefix).append("\tShapes:\n")
            for (shape in shapes) {
                sb.append(prefix).append("\t\t").append(shape).append("\n")
            }
        }
        return sb.toString()
    }
}

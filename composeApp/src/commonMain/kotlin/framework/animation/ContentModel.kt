package framework.animation

class ContentModel {
    fun toContent(
        drawable: LottieDrawable?,
        composition: LottieComposition?,
        layer: BaseLayer?
    ): com.airbnb.lottie.animation.content.Content?

}
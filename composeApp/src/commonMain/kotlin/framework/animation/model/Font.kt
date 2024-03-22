package framework.animation.model

import framework.animation.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY)
class Font(
    val family: String? = null,
    val name: String? = null,
    val style: String? = null,
    val ascent: Float = 0f
) {
/*
    private val family: String? = null
    private val name: String? = null
    private val style: String? = null
    private val ascent = 0f*/

    private var typeface: Typeface? = null

    @Suppress("unused")
    fun getFamily(): String? {
        return family
    }

    fun getName(): String? {
        return name
    }

    fun getStyle(): String? {
        return style
    }

    @Suppress("unused")
    fun getAscent(): Float {
        return ascent
    }

    fun getTypeface(): Typeface? {
        return typeface
    }

    fun setTypeface(typeface: Typeface?) {
        this.typeface = typeface
    }
}
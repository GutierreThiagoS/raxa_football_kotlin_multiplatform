package framework.animation.utils

import androidx.compose.ui.graphics.Path
import framework.animation.FloatRange
import framework.animation.PointF
import framework.animation.model.CubicCurveData
import framework.animation.model.KeyPath
import framework.animation.model.content.ShapeData
import kotlin.math.max
import kotlin.math.min

object MiscUtils {
    private val pathFromDataCurrentPoint: PointF = PointF()
    fun addPoints(
        p1: PointF,
        p2: PointF
    ): PointF {
        return PointF(p1.x + p2.x, p1.y + p2.y)
    }

    fun getPathFromData(
        shapeData: ShapeData,
        outPath: Path
    ) {
        outPath.reset()
        val initialPoint: PointF = shapeData.getInitialPoint()!!
        outPath.moveTo(initialPoint.x, initialPoint.y)
        pathFromDataCurrentPoint.set(
            initialPoint.x,
            initialPoint.y
        )
        for (i in shapeData.getCurves().indices) {
            val curveData: CubicCurveData = shapeData.getCurves().get(i)
            val cp1: PointF = curveData.getControlPoint1()
            val cp2: PointF = curveData.getControlPoint2()
            val vertex: PointF = curveData.getVertex()
            if (cp1 == pathFromDataCurrentPoint && cp2 == vertex) {
                // On some phones like Samsung phones, zero valued control points can cause artifacting.
                // https://github.com/airbnb/lottie-android/issues/275
                //
                // This does its best to add a tiny value to the vertex without affecting the final
                // animation as much as possible.
                // outPath.rMoveTo(0.01f, 0.01f);
                outPath.lineTo(vertex.x, vertex.y)
            } else {
                outPath.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, vertex.x, vertex.y)
            }
            pathFromDataCurrentPoint.set(vertex.x, vertex.y)
        }
        if (shapeData.isClosed) {
            outPath.close()
        }
    }

    fun lerp(
        a: Float,
        b: Float,
        @FloatRange(from = 0f, to = 1f) percentage: Float
    ): Float {
        return a + percentage * (b - a)
    }

    fun lerp(
        a: Double,
        b: Double,
        @FloatRange(from = 0f, to = 1f) percentage: Double
    ): Double {
        return a + percentage * (b - a)
    }

    fun lerp(
        a: Int,
        b: Int,
        @FloatRange(from = 0f, to = 1f) percentage: Float
    ): Int {
        return (a + percentage * (b - a)).toInt()
    }

    fun floorMod(x: Float, y: Float): Int {
        return floorMod(x.toInt(), y.toInt())
    }

    private fun floorMod(x: Int, y: Int): Int {
        return x - y * floorDiv(x, y)
    }

    private fun floorDiv(x: Int, y: Int): Int {
        var r = x / y
        val sameSign = x xor y >= 0
        val mod = x % y
        if (!sameSign && mod != 0) {
            r--
        }
        return r
    }

    fun clamp(number: Int, min: Int, max: Int): Int {
        return max(min.toDouble(), min(max.toDouble(), number.toDouble())).toInt()
    }

    fun clamp(number: Float, min: Float, max: Float): Float {
        return max(min.toDouble(), min(max.toDouble(), number.toDouble())).toFloat()
    }

    fun clamp(number: Double, min: Double, max: Double): Double {
        return max(min, min(max, number))
    }

    fun contains(number: Float, rangeMin: Float, rangeMax: Float): Boolean {
        return number >= rangeMin && number <= rangeMax
    }

    /**
     * Helper method for any [KeyPathElementContent] that will check if the content
     * fully matches the keypath then will add itself as the final key, resolve it, and add
     * it to the accumulator list.
     *
     *
     * Any [KeyPathElementContent] should call through to this as its implementation of
     * [KeyPathElementContent.resolveKeyPath].
     */
    fun resolveKeyPath(
        keyPath: KeyPath, depth: Int, accumulator: MutableList<KeyPath?>,
        currentPartialKeyPath: KeyPath, content: KeyPathElementContent
    ) {
        var currentPartialKeyPath: KeyPath = currentPartialKeyPath
        if (keyPath.fullyResolvesTo(content.getName(), depth)) {
            currentPartialKeyPath = currentPartialKeyPath.addKey(content.getName())
            accumulator.add(currentPartialKeyPath.resolve(content))
        }
    }
}

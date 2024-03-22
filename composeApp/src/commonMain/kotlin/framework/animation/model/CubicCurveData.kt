package framework.animation.model

import framework.animation.PointF
import framework.animation.annotation.RestrictTo

/**
 * One cubic path operation. CubicCurveData is structured such that it is easy to iterate through
 * it and build a path. However, it is modeled differently than most path operations.
 *
 * CubicCurveData
 * |                     - vertex
 * |                   /
 * |    cp1          cp2
 * |   /
 * |  |
 * | /
 * --------------------------
 *
 * When incrementally building a path, it will already have a "current point" so that is
 * not captured in this data structure.
 * The control points here represent [android.graphics.Path.cubicTo].
 *
 * Most path operations are centered around a vertex and its in control point and out control point like this:
 * |           outCp
 * |          /
 * |         |
 * |         v
 * |        /
 * |      inCp
 * --------------------------
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class CubicCurveData {
    private val controlPoint1: PointF
    private val controlPoint2: PointF
    private val vertex: PointF

    constructor() {
        controlPoint1 = PointF()
        controlPoint2 = PointF()
        vertex = PointF()
    }

    constructor(
        controlPoint1: PointF,
        controlPoint2: PointF,
        vertex: PointF
    ) {
        this.controlPoint1 = controlPoint1
        this.controlPoint2 = controlPoint2
        this.vertex = vertex
    }

    fun setControlPoint1(x: Float, y: Float) {
        controlPoint1.set(x, y)
    }

    fun getControlPoint1(): PointF {
        return controlPoint1
    }

    fun setControlPoint2(x: Float, y: Float) {
        controlPoint2.set(x, y)
    }

    fun getControlPoint2(): PointF {
        return controlPoint2
    }

    fun setVertex(x: Float, y: Float) {
        vertex.set(x, y)
    }

    fun setFrom(curveData: CubicCurveData) {
        setVertex(curveData.vertex.x, curveData.vertex.y)
        setControlPoint1(curveData.controlPoint1.x, curveData.controlPoint1.y)
        setControlPoint2(curveData.controlPoint2.x, curveData.controlPoint2.y)
    }

    fun getVertex(): PointF {
        return vertex
    }

    /*@android.annotation.SuppressLint("DefaultLocale")
    override fun toString(): String {
        return String.format(
            "v=%.2f,%.2f cp1=%.2f,%.2f cp2=%.2f,%.2f",
            vertex.x, vertex.y, controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y
        )
    }*/
}

package framework.animation.model.content

import framework.animation.FloatRange
import framework.animation.PointF
import framework.animation.model.CubicCurveData
import framework.animation.utils.MiscUtils
import kotlin.math.min

class ShapeData {
    private val curves: ArrayList<CubicCurveData>
    private var initialPoint: PointF? = null
    var isClosed = false

    constructor(initialPoint: PointF?, closed: Boolean, curves: List<CubicCurveData>) {
        this.initialPoint = initialPoint
        isClosed = closed
        this.curves = ArrayList(curves)
    }

    constructor() {
        curves = ArrayList()
    }

    fun setInitialPoint(x: Float, y: Float) {
        if (initialPoint == null) {
            initialPoint = PointF()
        }
        initialPoint!!.set(x, y)
    }

    fun getInitialPoint(): PointF? {
        return initialPoint
    }

    fun getCurves(): List<CubicCurveData> {
        return curves
    }

    fun interpolateBetween(
        shapeData1: ShapeData,
        shapeData2: ShapeData,
        @FloatRange(from = 0f, to = 1f) percentage: Float
    ) {
        if (initialPoint == null) {
            initialPoint = PointF()
        }
        isClosed = shapeData1.isClosed || shapeData2.isClosed
        if (shapeData1.getCurves().size != shapeData2.getCurves().size) {
            com.airbnb.lottie.utils.Logger.warning(
                "Curves must have the same number of control points. Shape 1: " +
                        shapeData1.getCurves().size + "\tShape 2: " + shapeData2.getCurves().size
            )
        }
        val points =
            min(shapeData1.getCurves().size.toDouble(), shapeData2.getCurves().size.toDouble())
                .toInt()
        if (curves.size < points) {
            for (i in curves.size until points) {
                curves.add(CubicCurveData())
            }
        } else if (curves.size > points) {
            for (i in curves.size - 1 downTo points) {
                curves.removeAt(curves.size - 1)
            }
        }
        val initialPoint1: PointF = shapeData1.initialPoint
        val initialPoint2: PointF = shapeData2.initialPoint
        setInitialPoint(
            MiscUtils.lerp(initialPoint1.x, initialPoint2.x, percentage),
            MiscUtils.lerp(initialPoint1.y, initialPoint2.y, percentage)
        )
        for (i in curves.indices.reversed()) {
            val curve1: CubicCurveData = shapeData1.getCurves().get(i)
            val curve2: CubicCurveData = shapeData2.getCurves().get(i)
            val cp11: PointF = curve1.getControlPoint1()
            val cp21: PointF = curve1.getControlPoint2()
            val vertex1: PointF = curve1.getVertex()
            val cp12: PointF = curve2.getControlPoint1()
            val cp22: PointF = curve2.getControlPoint2()
            val vertex2: PointF = curve2.getVertex()
            curves[i].setControlPoint1(
                MiscUtils.lerp(cp11.x, cp12.x, percentage), MiscUtils.lerp(
                    cp11.y, cp12.y,
                    percentage
                )
            )
            curves[i].setControlPoint2(
                MiscUtils.lerp(cp21.x, cp22.x, percentage), MiscUtils.lerp(
                    cp21.y, cp22.y,
                    percentage
                )
            )
            curves[i].setVertex(
                MiscUtils.lerp(vertex1.x, vertex2.x, percentage), MiscUtils.lerp(
                    vertex1.y, vertex2.y,
                    percentage
                )
            )
        }
    }

    override fun toString(): String {
        return ("ShapeData{" + "numCurves=" + curves.size +
                "closed=" + isClosed +
                '}')
    }
}

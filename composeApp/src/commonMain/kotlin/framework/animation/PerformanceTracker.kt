package framework.animation

class PerformanceTracker {

    private val TAG = "LOTTIE"
    interface FrameListener {
        fun onFrameRendered(renderTimeMs: Float)
    }

    private var enabled = false
    private val frameListeners: MutableList<FrameListener> =
        mutableListOf()
    private val layerRenderTimes: MutableMap<String, MeanCalculator> = HashMap()
    private val floatComparator: Comparator<Pair<String, Float>> by lazy {
        object : Comparator<Pair<String, Float>> {
            override fun compare(
                a: Pair<String, Float>,
                b: Pair<String, Float>
            ): Int {
                val r1: Float = a.second
                val r2: Float = b.second
                if (r2 > r1) {
                    return 1
                } else if (r1 > r2) {
                    return -1
                }
                return 0
            }

        }
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    fun recordRenderTime(layerName: String, millis: Float) {
        if (!enabled) {
            return
        }
        var meanCalculator: MeanCalculator? = layerRenderTimes[layerName]
        if (meanCalculator == null) {
            meanCalculator = MeanCalculator()
            layerRenderTimes[layerName] = meanCalculator
        }
        meanCalculator.add(millis)
        if (layerName == "__container") {
            for (listener in frameListeners) {
                listener.onFrameRendered(millis)
            }
        }
    }

    fun addFrameListener(frameListener: FrameListener) {
        frameListeners.add(frameListener)
    }

    @Suppress("unused")
    fun removeFrameListener(frameListener: FrameListener) {
        frameListeners.remove(frameListener)
    }

    fun clearRenderTimes() {
        layerRenderTimes.clear()
    }

    fun logRenderTimes() {
        if (!enabled) {
            return
        }
        val sortedRenderTimes: List<Pair<String, Float>> = sortedRenderTimes

        println("$TAG: Render times:")
        for (i in sortedRenderTimes.indices) {
            val layer: Pair<String, Float> = sortedRenderTimes[i]
            /*println(
                com.airbnb.lottie.L.TAG + ": " +
                String.format("\t\t%30s:%.2f", layer.first, layer.second)
            )*/
        }
    }

    val sortedRenderTimes: List<Pair<String, Float>>
        get() {
            if (!enabled) {
                return emptyList()
            }
            val sortedRenderTimes: MutableList<Pair<String, Float>> =
                ArrayList(layerRenderTimes.size)
            for ((key, value) in layerRenderTimes) {
                sortedRenderTimes.add(Pair(key, value.getMean()))
            }
            Collections.sort(
                sortedRenderTimes,
                floatComparator
            )
            return sortedRenderTimes
        }
}

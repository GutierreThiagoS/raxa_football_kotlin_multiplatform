package framework.animation

import framework.animation.annotation.RestrictTo
import framework.animation.model.Font
import framework.animation.model.Marker
import framework.animation.model.animatable.WorkerThread
import framework.animation.model.graphics.Rect
import framework.animation.utils.MiscUtils
import kotlinx.serialization.json.JsonObject

/**
 * After Effects/Bodymovin composition model. This is the serialized model from which the
 * animation will be created. It is designed to be stateless, cacheable, and shareable.
 *
 *
 * To create one, use [LottieCompositionFactory].
 *
 *
 * It can be used with a [com.airbnb.lottie.LottieAnimationView] or
 * [com.airbnb.lottie.LottieDrawable].
 */
class LottieComposition {
    private val performanceTracker: PerformanceTracker = PerformanceTracker()
    private val warnings: HashSet<String> = HashSet()
    private var precomps: Map<String, List<Layer>>? = null
    private var images: MutableMap<String, LottieImageAsset>? = null
    private var imagesDpScale = 0f

    /**
     * Map of font names to fonts
     */
    private var fonts: Map<String, Font>? = null
    private var markers: List<Marker>? = null
    private var characters: androidx.collection.SparseArrayCompat<FontCharacter>? = null
    private var layerMap: androidx.collection.LongSparseArray<Layer>? =
        null
    private var layers: List<Layer> = emptyList()

    // This is stored as a set to avoid duplicates.
    private var bounds: Rect? = null
    var startFrame = 0f
        private set
    var endFrame = 0f
        private set
    var frameRate = 0f
        private set

    /**
     * Used to determine if an animation can be drawn with hardware acceleration.
     */
    private var hasDashPattern = false
    /**
     * Used to determine if an animation can be drawn with hardware acceleration.
     */
    /**
     * Counts the number of mattes and masks. Before Android switched to SKIA
     * for drawing in Oreo (API 28), using hardware acceleration with mattes and masks
     * was only faster until you had ~4 masks after which it would actually become slower.
     */
    @get:RestrictTo(RestrictTo.Scope.LIBRARY)
    var maskAndMatteCount = 0
        private set

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun init(
        bounds: Rect?,
        startFrame: Float,
        endFrame: Float,
        frameRate: Float,
        layers: List<Layer>,
        layerMap: androidx.collection.LongSparseArray<Layer?>,
        precomps: Map<String, List<Layer>>?,
        images: MutableMap<String, LottieImageAsset>?,
        imagesDpScale: Float,
        characters: androidx.collection.SparseArrayCompat<FontCharacter?>,
        fonts: Map<String, Font>?,
        markers: List<Marker>?
    ) {
        this.bounds = bounds
        this.startFrame = startFrame
        this.endFrame = endFrame
        this.frameRate = frameRate
        this.layers = layers
        this.layerMap = layerMap
        this.precomps = precomps
        this.images = images
        this.imagesDpScale = imagesDpScale
        this.characters = characters
        this.fonts = fonts
        this.markers = markers
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun addWarning(warning: String) {
//        com.airbnb.lottie.utils.Logger.warning(warning)
        warnings.add(warning)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun setHasDashPattern(hasDashPattern: Boolean) {
        this.hasDashPattern = hasDashPattern
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun incrementMatteOrMaskCount(amount: Int) {
        maskAndMatteCount += amount
    }

    /**
     * Used to determine if an animation can be drawn with hardware acceleration.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun hasDashPattern(): Boolean {
        return hasDashPattern
    }

    fun getWarnings(): ArrayList<String> {
        return arrayListOf(*warnings.toTypedArray<String>())
    }

    fun setPerformanceTrackingEnabled(enabled: Boolean) {
        performanceTracker.setEnabled(enabled)
    }

    fun getPerformanceTracker(): PerformanceTracker {
        return performanceTracker
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun layerModelForId(id: Long): Layer {
        return layerMap.get(id)
    }

    fun getBounds(): Rect? {
        return bounds
    }

    val duration: Float
        get() = (durationFrames / frameRate * 1000).toLong().toFloat()

    fun getFrameForProgress(progress: Float): Float {
        return MiscUtils.lerp(startFrame, endFrame, progress)
    }

    fun getProgressForFrame(frame: Float): Float {
        val framesSinceStart = frame - startFrame
        val frameRange = endFrame - startFrame
        return framesSinceStart / frameRange
    }

    fun getLayers(): List<Layer>? {
        return layers
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun getPrecomps(id: String): List<Layer>? {
        return precomps!![id]
    }

    fun getCharacters(): androidx.collection.SparseArrayCompat<FontCharacter>? {
        return characters
    }

    fun getFonts(): Map<String, Font>? {
        return fonts
    }

    fun getMarkers(): List<Marker>? {
        return markers
    }

    fun getMarker(markerName: String?): Marker? {
        val size = markers!!.size
        for (i in 0 until size) {
            val marker: Marker = markers!![i]
            if (marker.matchesName(markerName)) {
                return marker
            }
        }
        return null
    }

    fun hasImages(): Boolean {
        return !images!!.isEmpty()
    }

    /**
     * Returns a map of image asset id to [LottieImageAsset]. These assets contain image metadata exported
     * from After Effects or other design tool. The resulting Bitmaps can be set directly on the image asset so
     * they can be loaded once and reused across compositions.
     *
     * If the context dp scale has changed since the last time images were retrieved, images will be rescaled.
     */
    fun getImages(): Map<String, LottieImageAsset>? {
        val dpScale: Float = com.airbnb.lottie.utils.Utils.dpScale()
        if (dpScale != imagesDpScale) {
            imagesDpScale = dpScale
            val entries: Set<Map.Entry<String, LottieImageAsset>> = images!!.entries
            for ((key, value) in entries) {
                images!![key] = value.copyWithScale(imagesDpScale / dpScale)
            }
        }
        return images
    }

    val durationFrames: Float
        get() = endFrame - startFrame

    override fun toString(): String {
        val sb = StringBuilder("LottieComposition:\n")
        for (layer in layers) {
            sb.append(layer.toString("\t"))
        }
        return sb.toString()
    }

    /**
     * This will be removed in the next version of Lottie. [LottieCompositionFactory] has improved
     * API names, failure handlers, and will return in-progress tasks so you will never parse the same
     * animation twice in parallel.
     *
     * @see LottieCompositionFactory
     */
    @Deprecated("")
    object Factory {
        /**
         * @see LottieCompositionFactory.fromAsset
         */
        @Suppress("deprecation")
        @Deprecated("")
        fun fromAssetFileName(
            context: android.content.Context?,
            fileName: String?,
            l: OnCompositionLoadedListener?
        ): Cancellable {
            val listener = ListenerAdapter(l)
            LottieCompositionFactory.fromAsset(context, fileName).addListener(listener)
            return listener
        }

        /**
         * @see LottieCompositionFactory.fromRawRes
         */
        @Suppress("deprecation")
        @Deprecated("")
        fun fromRawFile(
            context: android.content.Context?,
            @androidx.annotation.RawRes resId: Int,
            l: OnCompositionLoadedListener?
        ): Cancellable {
            val listener: LottieComposition.Factory.ListenerAdapter =
                LottieComposition.Factory.ListenerAdapter(l)
            LottieCompositionFactory.fromRawRes(context, resId).addListener(listener)
            return listener
        }

        /**
         * @see LottieCompositionFactory.fromJsonInputStream
         */
        @Suppress("deprecation")
        @Deprecated("")
        fun fromInputStream(
            stream: java.io.InputStream?,
            l: OnCompositionLoadedListener?
        ): Cancellable {
            val listener: LottieComposition.Factory.ListenerAdapter =
                LottieComposition.Factory.ListenerAdapter(l)
            LottieCompositionFactory.fromJsonInputStream(stream, null).addListener(listener)
            return listener
        }

        /**
         * @see LottieCompositionFactory.fromJsonString
         */
        @Suppress("deprecation")
        @Deprecated("")
        fun fromJsonString(
            jsonString: String?,
            l: OnCompositionLoadedListener?
        ): Cancellable {
            val listener: LottieComposition.Factory.ListenerAdapter =
                LottieComposition.Factory.ListenerAdapter(l)
            LottieCompositionFactory.fromJsonString(jsonString, null).addListener(listener)
            return listener
        }

        /**
         * @see LottieCompositionFactory.fromJsonReader
         */
        @Suppress("deprecation")
        @Deprecated("")
        fun fromJsonReader(
            reader: com.airbnb.lottie.parser.moshi.JsonReader?,
            l: OnCompositionLoadedListener?
        ): Cancellable {
            val listener: LottieComposition.Factory.ListenerAdapter =
                LottieComposition.Factory.ListenerAdapter(l)
            LottieCompositionFactory.fromJsonReader(reader, null).addListener(listener)
            return listener
        }

        /**
         * @see LottieCompositionFactory.fromAssetSync
         */
        /*@WorkerThread
        @Deprecated("")
        fun fromFileSync(
            context: android.content.Context?,
            fileName: String?
        ): LottieComposition? {
            return LottieCompositionFactory.fromAssetSync(context, fileName).getValue()
        }

        *//**
         * @see LottieCompositionFactory.fromJsonInputStreamSync
         *//*
        @WorkerThread
        @Deprecated("")
        fun fromInputStreamSync(stream: java.io.InputStream?): LottieComposition? {
            return LottieCompositionFactory.fromJsonInputStreamSync(stream, null).getValue()
        }

        *//**
         * This will now auto-close the input stream!
         *
         * @see LottieCompositionFactory.fromJsonInputStreamSync
         *//*
        @WorkerThread
        @Deprecated("")
        fun fromInputStreamSync(
            stream: java.io.InputStream?,
            close: Boolean
        ): LottieComposition? {
            if (close) {
                com.airbnb.lottie.utils.Logger.warning("Lottie now auto-closes input stream!")
            }
            return LottieCompositionFactory.fromJsonInputStreamSync(stream, null).getValue()
        }

        *//**
         * @see LottieCompositionFactory.fromJsonSync
         *//*
        @WorkerThread
        @Deprecated("")
        fun fromJsonSync(
            @Suppress("unused") res: android.content.res.Resources?,
            json: JsonObject?
        ): LottieComposition? {
            return LottieCompositionFactory.fromJsonSync(json, null).getValue()
        }*/

        /**
         * @see LottieCompositionFactory.fromJsonStringSync
         */
        @WorkerThread
        @Deprecated("")
        fun fromJsonSync(json: String): LottieComposition? {
            return LottieCompositionFactory.fromJsonStringSync(json, null).getValue()
        }

        /**
         * @see LottieCompositionFactory.fromJsonReaderSync
         */
        @WorkerThread
        @Deprecated("")
        fun fromJsonSync(reader: com.airbnb.lottie.parser.moshi.JsonReader?): LottieComposition? {
            return LottieCompositionFactory.fromJsonReaderSync(reader, null).getValue()
        }

        @Suppress("deprecation")
        private class ListenerAdapter(listener: OnCompositionLoadedListener) :
            LottieListener<LottieComposition?>, Cancellable {
            private val listener: OnCompositionLoadedListener
            private var cancelled = false

            init {
                this.listener = listener
            }

            override fun onResult(composition: LottieComposition?) {
                if (cancelled) {
                    return
                }
                listener.onCompositionLoaded(composition)
            }

            override fun cancel() {
                cancelled = true
            }
        }
    }
}

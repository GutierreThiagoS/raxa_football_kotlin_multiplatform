package framework.animation

import framework.animation.model.KeyPath
import framework.animation.model.Typeface
import framework.animation.value.LottieValueCallback
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
/**
 * This can be used to show an lottie animation in any place that would normally take a drawable.
 *
 * @see [Full Documentation](http://airbnb.io/lottie)
 */
class LottieDrawable : Drawable(),
    android.graphics.drawable.Drawable.Callback,
    android.graphics.drawable.Animatable {
    private interface LazyCompositionTask {
        fun run(composition: LottieComposition?)
    }

    /**
     * Internal record keeping of the desired play state when [.isVisible] transitions to or is false.
     *
     *
     * If the animation was playing when it becomes invisible or play/pause is called on it while it is invisible, it will
     * store the state and then take the appropriate action when the drawable becomes visible again.
     */
    private enum class OnVisibleAction {
        NONE,
        PLAY,
        RESUME
    }

    private var composition: LottieComposition? = null
    private val animator: LottieValueAnimator = LottieValueAnimator()

    // Call animationsEnabled() instead of using these fields directly.
    private var systemAnimationsEnabled = true
    private var ignoreSystemAnimationsDisabled = false
    private var safeMode = false
    private var onVisibleAction = OnVisibleAction.NONE
    private val lazyCompositionTasks: ArrayList<LazyCompositionTask> =
        ArrayList<LazyCompositionTask>()

    /**
     * ImageAssetManager created automatically by Lottie for views.
     */
    private var imageAssetManager: ImageAssetManager? = null
    var imageAssetsFolder: String? = null
        private set
    private var imageAssetDelegate: ImageAssetDelegate? = null
    private var fontAssetManager: FontAssetManager? = null
    private var fontMap: Map<String, Typeface>? = null

    /**
     * Will be set if manually overridden by [.setDefaultFontFileExtension].
     * This must be stored as a field in case it is set before the font asset delegate
     * has been created.
     */
    var defaultFontFileExtension: String? = null
    var fontAssetDelegate: FontAssetDelegate? = null
    var textDelegate: com.airbnb.lottie.TextDelegate? = null
    var isMergePathsEnabledForKitKatAndAbove = false
        private set
    /**
     * When true, dynamically set bitmaps will be drawn with the exact bounds of the original animation, regardless of the bitmap size.
     * When false, dynamically set bitmaps will be drawn at the top left of the original image but with its own bounds.
     *
     *
     * Defaults to false.
     */
    /**
     * When true, dynamically set bitmaps will be drawn with the exact bounds of the original animation, regardless of the bitmap size.
     * When false, dynamically set bitmaps will be drawn at the top left of the original image but with its own bounds.
     *
     *
     * Defaults to false.
     */
    var maintainOriginalImageBounds = false
    private var clipToCompositionBounds = true
    private var compositionLayer: CompositionLayer? = null
    private var alpha = 255
    private var performanceTrackingEnabled = false
    private var outlineMasksAndMattes = false

    /**
     * Sets whether to apply opacity to the each layer instead of shape.
     *
     *
     * Opacity is normally applied directly to a shape. In cases where translucent shapes overlap, applying opacity to a layer will be more accurate
     * at the expense of performance.
     *
     *
     * The default value is false.
     *
     *
     * Note: This process is very expensive. The performance impact will be reduced when hardware acceleration is enabled.
     *
     * @see android.view.View.setLayerType
     * @see LottieAnimationView.setRenderMode
     */
    var isApplyingOpacityToLayersEnabled = false

    /**
     * @see .setClipTextToBoundingBox
     */
    var clipTextToBoundingBox = false
        /**
         * When true, if there is a bounding box set on a text layer (paragraph text), any text
         * that overflows past its height will not be drawn.
         */
        set(clipTextToBoundingBox) {
            if (clipTextToBoundingBox != this.clipTextToBoundingBox) {
                field = clipTextToBoundingBox
                invalidateSelf()
            }
        }
    private var renderMode: com.airbnb.lottie.RenderMode = com.airbnb.lottie.RenderMode.AUTOMATIC

    /**
     * The actual render mode derived from [.renderMode].
     */
    private var useSoftwareRendering = false
    private val renderingMatrix: android.graphics.Matrix = android.graphics.Matrix()
    private var softwareRenderingBitmap: android.graphics.Bitmap? = null
    private var softwareRenderingCanvas: android.graphics.Canvas? = null
    private var canvasClipBounds: android.graphics.Rect? = null
    private var canvasClipBoundsRectF: RectF? = null
    private var softwareRenderingPaint: android.graphics.Paint? = null
    private var softwareRenderingSrcBoundsRect: android.graphics.Rect? = null
    private var softwareRenderingDstBoundsRect: android.graphics.Rect? = null
    private var softwareRenderingDstBoundsRectF: RectF? = null
    private var softwareRenderingTransformedBounds: RectF? = null
    private var softwareRenderingOriginalCanvasMatrix: android.graphics.Matrix? = null
    private var softwareRenderingOriginalCanvasMatrixInverse: android.graphics.Matrix? = null

    /**
     * True if the drawable has not been drawn since the last invalidateSelf.
     * We can do this to prevent things like bounds from getting recalculated
     * many times.
     */
    private var isDirty = false
    /**
     * **Note: this API is experimental and may changed.**
     *
     *
     * Sets the current value for [AsyncUpdates]. Refer to the docs for [AsyncUpdates] for more info.
     */
    /** Use the getter so that it can fall back to [L.getDefaultAsyncUpdates].  */
    var asyncUpdates: AsyncUpdates? = null
        /**
         * Returns the current value of [AsyncUpdates]. Refer to the docs for [AsyncUpdates] for more info.
         */
        get() {
            val asyncUpdates: AsyncUpdates? = field
            return if (asyncUpdates != null) {
                asyncUpdates
            } else com.airbnb.lottie.L.getDefaultAsyncUpdates()
        }
    private val progressUpdateListener: AnimatorUpdateListener =
        AnimatorUpdateListener { animation: ValueAnimator? ->
            if (asyncUpdatesEnabled) {
                // Render a new frame.
                // If draw is called while lastDrawnProgress is still recent enough, it will
                // draw straight away and then enqueue a background setProgress immediately after draw
                // finishes.
                invalidateSelf()
            } else if (compositionLayer != null) {
                compositionLayer.setProgress(animator.getAnimatedValueAbsolute())
            }
        }

    /**
     * Ensures that setProgress and draw will never happen at the same time on different threads.
     * If that were to happen, parts of the animation may be on one frame while other parts would
     * be on another.
     */
    private val setProgressDrawLock: java.util.concurrent.Semaphore =
        java.util.concurrent.Semaphore(1)
    private var mainThreadHandler: android.os.Handler? = null
    private var invalidateSelfRunnable: java.lang.Runnable? = null
    private val updateProgressRunnable: java.lang.Runnable = java.lang.Runnable {
        val compositionLayer: CompositionLayer? = compositionLayer
        if (compositionLayer == null) {
            return@Runnable
        }
        try {
            setProgressDrawLock.acquire()
            compositionLayer.setProgress(animator.getAnimatedValueAbsolute())
            // Refer to invalidateSelfOnMainThread for more info.
            if (com.airbnb.lottie.LottieDrawable.Companion.invalidateSelfOnMainThread && isDirty) {
                if (mainThreadHandler == null) {
                    mainThreadHandler = android.os.Handler(Looper.getMainLooper())
                    invalidateSelfRunnable = java.lang.Runnable {
                        val callback: android.graphics.drawable.Drawable.Callback? = getCallback()
                        if (callback != null) {
                            callback.invalidateDrawable(this)
                        }
                    }
                }
                mainThreadHandler.post(invalidateSelfRunnable)
            }
        } catch (e: java.lang.InterruptedException) {
            // Do nothing.
        } finally {
            setProgressDrawLock.release()
        }
    }
    private var lastDrawnProgress = -Float.MAX_VALUE

    @androidx.annotation.IntDef([com.airbnb.lottie.LottieDrawable.Companion.RESTART, com.airbnb.lottie.LottieDrawable.Companion.REVERSE])
    @Retention(
        AnnotationRetention.SOURCE
    )
    annotation class RepeatMode

    init {
        animator.addUpdateListener(progressUpdateListener)
    }

    /**
     * Returns whether or not any layers in this composition has masks.
     */
    fun hasMasks(): Boolean {
        return compositionLayer != null && compositionLayer.hasMasks()
    }

    /**
     * Returns whether or not any layers in this composition has a matte layer.
     */
    fun hasMatte(): Boolean {
        return compositionLayer != null && compositionLayer.hasMatte()
    }

    fun enableMergePathsForKitKatAndAbove(): Boolean {
        return isMergePathsEnabledForKitKatAndAbove
    }

    /**
     * Enable this to get merge path support for devices running KitKat (19) and above.
     *
     *
     * Merge paths currently don't work if the the operand shape is entirely contained within the
     * first shape. If you need to cut out one shape from another shape, use an even-odd fill type
     * instead of using merge paths.
     */
    fun enableMergePathsForKitKatAndAbove(enable: Boolean) {
        if (isMergePathsEnabledForKitKatAndAbove == enable) {
            return
        }
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
            com.airbnb.lottie.utils.Logger.warning("Merge paths are not supported pre-Kit Kat.")
            return
        }
        isMergePathsEnabledForKitKatAndAbove = enable
        if (composition != null) {
            buildCompositionLayer()
        }
    }

    /**
     * Sets whether or not Lottie should clip to the original animation composition bounds.
     *
     *
     * Defaults to true.
     */
    fun setClipToCompositionBounds(clipToCompositionBounds: Boolean) {
        if (clipToCompositionBounds != this.clipToCompositionBounds) {
            this.clipToCompositionBounds = clipToCompositionBounds
            val compositionLayer: CompositionLayer? = compositionLayer
            if (compositionLayer != null) {
                compositionLayer.setClipToCompositionBounds(clipToCompositionBounds)
            }
            invalidateSelf()
        }
    }

    /**
     * Gets whether or not Lottie should clip to the original animation composition bounds.
     *
     *
     * Defaults to true.
     */
    fun getClipToCompositionBounds(): Boolean {
        return clipToCompositionBounds
    }

    /**
     * If you use image assets, you must explicitly specify the folder in assets/ in which they are
     * located because bodymovin uses the name filenames across all compositions (img_#).
     * Do NOT rename the images themselves.
     *
     *
     * If your images are located in src/main/assets/airbnb_loader/ then call
     * `setImageAssetsFolder("airbnb_loader/");`.
     *
     *
     *
     *
     * Be wary if you are using many images, however. Lottie is designed to work with vector shapes
     * from After Effects. If your images look like they could be represented with vector shapes,
     * see if it is possible to convert them to shape layers and re-export your animation. Check
     * the documentation at [airbnb.io/lottie](http://airbnb.io/lottie) for more information about importing shapes from
     * Sketch or Illustrator to avoid this.
     */
    fun setImagesAssetsFolder(imageAssetsFolder: String?) {
        this.imageAssetsFolder = imageAssetsFolder
    }

    /**
     * Create a composition with [LottieCompositionFactory]
     *
     * @return True if the composition is different from the previously set composition, false otherwise.
     */
    fun setComposition(composition: LottieComposition): Boolean {
        if (this.composition === composition) {
            return false
        }
        isDirty = true
        clearComposition()
        this.composition = composition
        buildCompositionLayer()
        animator.setComposition(composition)
        progress = animator.getAnimatedFraction()

        // We copy the tasks to a new ArrayList so that if this method is called from multiple threads,
        // then there won't be two iterators iterating and removing at the same time.
        val it: MutableIterator<LazyCompositionTask> =
            java.util.ArrayList<LazyCompositionTask>(lazyCompositionTasks).iterator()
        while (it.hasNext()) {
            val t: LazyCompositionTask = it.next()
            // The task should never be null but it appears to happen in rare cases. Maybe it's an oem-specific or ART bug.
            // https://github.com/airbnb/lottie-android/issues/1702
            if (t != null) {
                t.run(composition)
            }
            it.remove()
        }
        lazyCompositionTasks.clear()
        composition.setPerformanceTrackingEnabled(performanceTrackingEnabled)
        computeRenderMode()

        // Ensure that ImageView updates the drawable width/height so it can
        // properly calculate its drawable matrix.
        val callback: android.graphics.drawable.Drawable.Callback = getCallback()
        if (callback is android.widget.ImageView) {
            (callback as android.widget.ImageView).setImageDrawable(null)
            (callback as android.widget.ImageView).setImageDrawable(this)
        }
        return true
    }

    /**
     * Call this to set whether or not to render with hardware or software acceleration.
     * Lottie defaults to Automatic which will use hardware acceleration unless:
     * 1) There are dash paths and the device is pre-Pie.
     * 2) There are more than 4 masks and mattes and the device is pre-Pie.
     * Hardware acceleration is generally faster for those devices unless
     * there are many large mattes and masks in which case there is a lot
     * of GPU uploadTexture thrashing which makes it much slower.
     *
     *
     * In most cases, hardware rendering will be faster, even if you have mattes and masks.
     * However, if you have multiple mattes and masks (especially large ones), you
     * should test both render modes. You should also test on pre-Pie and Pie+ devices
     * because the underlying rendering engine changed significantly.
     *
     * @see [Android Hardware Acceleration](https://developer.android.com/guide/topics/graphics/hardware-accel.unsupported)
     */
    fun setRenderMode(renderMode: com.airbnb.lottie.RenderMode) {
        this.renderMode = renderMode
        computeRenderMode()
    }

    val asyncUpdatesEnabled: Boolean
        /**
         * Similar to [.getAsyncUpdates] except it returns the actual
         * boolean value for whether async updates are enabled or not.
         * This is useful when the mode is automatic and you want to know
         * whether automatic is defaulting to enabled or not.
         */
        get() = asyncUpdates == AsyncUpdates.ENABLED

    /**
     * Returns the actual render mode being used. It will always be [RenderMode.HARDWARE] or [RenderMode.SOFTWARE].
     * When the render mode is set to AUTOMATIC, the value will be derived from [RenderMode.useSoftwareRendering].
     */
    fun getRenderMode(): com.airbnb.lottie.RenderMode {
        return if (useSoftwareRendering) com.airbnb.lottie.RenderMode.SOFTWARE else com.airbnb.lottie.RenderMode.HARDWARE
    }

    private fun computeRenderMode() {
        val composition: LottieComposition? = composition ?: return
        useSoftwareRendering = renderMode.useSoftwareRendering(
            android.os.Build.VERSION.SDK_INT,
            composition.hasDashPattern(),
            composition.getMaskAndMatteCount()
        )
    }

    fun setPerformanceTrackingEnabled(enabled: Boolean) {
        performanceTrackingEnabled = enabled
        if (composition != null) {
            composition.setPerformanceTrackingEnabled(enabled)
        }
    }

    /**
     * Enable this to debug slow animations by outlining masks and mattes. The performance overhead of the masks and mattes will
     * be proportional to the surface area of all of the masks/mattes combined.
     *
     *
     * DO NOT leave this enabled in production.
     */
    fun setOutlineMasksAndMattes(outline: Boolean) {
        if (outlineMasksAndMattes == outline) {
            return
        }
        outlineMasksAndMattes = outline
        if (compositionLayer != null) {
            compositionLayer.setOutlineMasksAndMattes(outline)
        }
    }

    val performanceTracker: com.airbnb.lottie.PerformanceTracker?
        get() = if (composition != null) {
            composition.getPerformanceTracker()
        } else null

    /**
     * This API no longer has any effect.
     */
    @Deprecated("")
    fun disableExtraScaleModeInFitXY() {
    }

    private fun buildCompositionLayer() {
        val composition: LottieComposition? = composition ?: return
        compositionLayer = CompositionLayer(
            this, LayerParser.parse(composition), composition.getLayers(), composition
        )
        if (outlineMasksAndMattes) {
            compositionLayer.setOutlineMasksAndMattes(true)
        }
        compositionLayer.setClipToCompositionBounds(clipToCompositionBounds)
    }

    fun clearComposition() {
        if (animator.isRunning()) {
            animator.cancel()
            if (!isVisible()) {
                onVisibleAction = OnVisibleAction.NONE
            }
        }
        composition = null
        compositionLayer = null
        imageAssetManager = null
        lastDrawnProgress = -Float.MAX_VALUE
        animator.clearComposition()
        invalidateSelf()
    }

    /**
     * If you are experiencing a device specific crash that happens during drawing, you can set this to true
     * for those devices. If set to true, draw will be wrapped with a try/catch which will cause Lottie to
     * render an empty frame rather than crash your app.
     *
     *
     * Ideally, you will never need this and the vast majority of apps and animations won't. However, you may use
     * this for very specific cases if absolutely necessary.
     */
    fun setSafeMode(safeMode: Boolean) {
        this.safeMode = safeMode
    }

    override fun invalidateSelf() {
        if (isDirty) {
            return
        }
        isDirty = true

        // Refer to invalidateSelfOnMainThread for more info.
        if (com.airbnb.lottie.LottieDrawable.Companion.invalidateSelfOnMainThread && Looper.getMainLooper() != Looper.myLooper()) {
            return
        }
        val callback: android.graphics.drawable.Drawable.Callback = getCallback()
        if (callback != null) {
            callback.invalidateDrawable(this)
        }
    }

    override fun setAlpha(@androidx.annotation.IntRange(from = 0, to = 255) alpha: Int) {
        this.alpha = alpha
        invalidateSelf()
    }

    override fun getAlpha(): Int {
        return alpha
    }

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        com.airbnb.lottie.utils.Logger.warning("Use addColorFilter instead.")
    }

    val opacity: Int
        get() = PixelFormat.TRANSLUCENT

    /**
     * Helper for the async execution path to potentially call setProgress
     * before drawing if the current progress has drifted sufficiently far
     * from the last set progress.
     *
     * @see AsyncUpdates
     *
     * @see .setAsyncUpdates
     */
    private fun shouldSetProgressBeforeDrawing(): Boolean {
        val composition: LottieComposition? = composition ?: return false
        val lastDrawnProgress = lastDrawnProgress
        val currentProgress: Float = animator.getAnimatedValueAbsolute()
        this.lastDrawnProgress = currentProgress
        val duration: Float = composition.getDuration()
        val deltaProgress =
            abs((currentProgress - lastDrawnProgress).toDouble()).toFloat()
        val deltaMs = deltaProgress * duration
        return deltaMs >= com.airbnb.lottie.LottieDrawable.Companion.MAX_DELTA_MS_ASYNC_SET_PROGRESS
    }

    override fun draw(canvas: android.graphics.Canvas) {
        val compositionLayer: CompositionLayer? = compositionLayer ?: return
        val asyncUpdatesEnabled = asyncUpdatesEnabled
        try {
            if (asyncUpdatesEnabled) {
                setProgressDrawLock.acquire()
            }
            com.airbnb.lottie.L.beginSection("Drawable#draw")
            if (asyncUpdatesEnabled && shouldSetProgressBeforeDrawing()) {
                progress = animator.getAnimatedValueAbsolute()
            }
            if (safeMode) {
                try {
                    if (useSoftwareRendering) {
                        renderAndDrawAsBitmap(canvas, compositionLayer)
                    } else {
                        drawDirectlyToCanvas(canvas)
                    }
                } catch (e: Throwable) {
                    com.airbnb.lottie.utils.Logger.error("Lottie crashed in draw!", e)
                }
            } else {
                if (useSoftwareRendering) {
                    renderAndDrawAsBitmap(canvas, compositionLayer)
                } else {
                    drawDirectlyToCanvas(canvas)
                }
            }
            isDirty = false
        } catch (e: java.lang.InterruptedException) {
            // Do nothing.
        } finally {
            com.airbnb.lottie.L.endSection("Drawable#draw")
            if (asyncUpdatesEnabled) {
                setProgressDrawLock.release()
                if (compositionLayer.getProgress() != animator.getAnimatedValueAbsolute()) {
                    com.airbnb.lottie.LottieDrawable.Companion.setProgressExecutor.execute(
                        updateProgressRunnable
                    )
                }
            }
        }
    }

    /**
     * To be used by lottie-compose only.
     */
    @androidx.annotation.RestrictTo(androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP)
    fun draw(canvas: android.graphics.Canvas, matrix: android.graphics.Matrix?) {
        val compositionLayer: CompositionLayer? = compositionLayer
        val composition: LottieComposition? = composition
        if (compositionLayer == null || composition == null) {
            return
        }
        val asyncUpdatesEnabled = asyncUpdatesEnabled
        try {
            if (asyncUpdatesEnabled) {
                setProgressDrawLock.acquire()
                if (shouldSetProgressBeforeDrawing()) {
                    progress = animator.getAnimatedValueAbsolute()
                }
            }
            if (useSoftwareRendering) {
                canvas.save()
                canvas.concat(matrix)
                renderAndDrawAsBitmap(canvas, compositionLayer)
                canvas.restore()
            } else {
                compositionLayer.draw(canvas, matrix, alpha)
            }
            isDirty = false
        } catch (e: java.lang.InterruptedException) {
            // Do nothing.
        } finally {
            if (asyncUpdatesEnabled) {
                setProgressDrawLock.release()
                if (compositionLayer.getProgress() != animator.getAnimatedValueAbsolute()) {
                    com.airbnb.lottie.LottieDrawable.Companion.setProgressExecutor.execute(
                        updateProgressRunnable
                    )
                }
            }
        }
    }

    // <editor-fold desc="animator">
    @MainThread
    override fun start() {
        val callback: android.graphics.drawable.Drawable.Callback = getCallback()
        if (callback is android.view.View && (callback as android.view.View).isInEditMode()) {
            // Don't auto play when in edit mode.
            return
        }
        playAnimation()
    }

    @MainThread
    override fun stop() {
        endAnimation()
    }

    val isRunning: Boolean
        get() = isAnimating

    /**
     * Plays the animation from the beginning. If speed is &lt; 0, it will start at the end
     * and play towards the beginning
     */
    @MainThread
    fun playAnimation() {
        if (compositionLayer == null) {
            lazyCompositionTasks.add(LazyCompositionTask { c: LottieComposition? -> playAnimation() })
            return
        }
        computeRenderMode()
        if (animationsEnabled() || repeatCount == 0) {
            onVisibleAction = if (isVisible()) {
                animator.playAnimation()
                OnVisibleAction.NONE
            } else {
                OnVisibleAction.PLAY
            }
        }
        if (!animationsEnabled()) {
            frame = (if (speed < 0) minFrame else maxFrame).toInt()
            animator.endAnimation()
            if (!isVisible()) {
                onVisibleAction = OnVisibleAction.NONE
            }
        }
    }

    @MainThread
    fun endAnimation() {
        lazyCompositionTasks.clear()
        animator.endAnimation()
        if (!isVisible()) {
            onVisibleAction = OnVisibleAction.NONE
        }
    }

    /**
     * Continues playing the animation from its current position. If speed &lt; 0, it will play backwards
     * from the current position.
     */
    @MainThread
    fun resumeAnimation() {
        if (compositionLayer == null) {
            lazyCompositionTasks.add(LazyCompositionTask { c: LottieComposition? -> resumeAnimation() })
            return
        }
        computeRenderMode()
        if (animationsEnabled() || repeatCount == 0) {
            onVisibleAction = if (isVisible()) {
                animator.resumeAnimation()
                OnVisibleAction.NONE
            } else {
                OnVisibleAction.RESUME
            }
        }
        if (!animationsEnabled()) {
            frame = (if (speed < 0) minFrame else maxFrame).toInt()
            animator.endAnimation()
            if (!isVisible()) {
                onVisibleAction = OnVisibleAction.NONE
            }
        }
    }

    /**
     * Sets the minimum frame that the animation will start from when playing or looping.
     */
    fun setMinFrame(minFrame: Int) {
        if (composition == null) {
            lazyCompositionTasks.add(LazyCompositionTask { c: LottieComposition? ->
                setMinFrame(
                    minFrame
                )
            })
            return
        }
        animator.setMinFrame(minFrame)
    }

    val minFrame: Float
        /**
         * Returns the minimum frame set by [.setMinFrame] or [.setMinProgress]
         */
        get() = animator.getMinFrame()

    /**
     * Sets the minimum progress that the animation will start from when playing or looping.
     */
    fun setMinProgress(minProgress: Float) {
        if (composition == null) {
            lazyCompositionTasks.add(LazyCompositionTask { c: LottieComposition? ->
                setMinProgress(
                    minProgress
                )
            })
            return
        }
        setMinFrame(
            MiscUtils.lerp(
                composition.getStartFrame(),
                composition.getEndFrame(),
                minProgress
            ).toInt()
        )
    }

    /**
     * Sets the maximum frame that the animation will end at when playing or looping.
     *
     *
     * The value will be clamped to the composition bounds. For example, setting Integer.MAX_VALUE would result in the same
     * thing as composition.endFrame.
     */
    fun setMaxFrame(maxFrame: Int) {
        if (composition == null) {
            lazyCompositionTasks.add(LazyCompositionTask { c: LottieComposition? ->
                setMaxFrame(
                    maxFrame
                )
            })
            return
        }
        animator.setMaxFrame(maxFrame + 0.99f)
    }

    val maxFrame: Float
        /**
         * Returns the maximum frame set by [.setMaxFrame] or [.setMaxProgress]
         */
        get() = animator.getMaxFrame()

    /**
     * Sets the maximum progress that the animation will end at when playing or looping.
     */
    fun setMaxProgress(@androidx.annotation.FloatRange(from = 0f, to = 1f) maxProgress: Float) {
        if (composition == null) {
            lazyCompositionTasks.add(LazyCompositionTask { c: LottieComposition? ->
                setMaxProgress(
                    maxProgress
                )
            })
            return
        }
        animator.setMaxFrame(
            MiscUtils.lerp(
                composition.getStartFrame(),
                composition.getEndFrame(),
                maxProgress
            )
        )
    }

    /**
     * Sets the minimum frame to the start time of the specified marker.
     *
     * @throws IllegalArgumentException if the marker is not found.
     */
    fun setMinFrame(markerName: String) {
        if (composition == null) {
            lazyCompositionTasks.add(LazyCompositionTask { c: LottieComposition? ->
                setMinFrame(
                    markerName
                )
            })
            return
        }
        val marker: com.airbnb.lottie.model.Marker = composition.getMarker(markerName)
            ?: throw java.lang.IllegalArgumentException("Cannot find marker with name $markerName.")
        setMinFrame(marker.startFrame.toInt())
    }

    /**
     * Sets the maximum frame to the start time + duration of the specified marker.
     *
     * @throws IllegalArgumentException if the marker is not found.
     */
    fun setMaxFrame(markerName: String) {
        if (composition == null) {
            lazyCompositionTasks.add(LazyCompositionTask { c: LottieComposition? ->
                setMaxFrame(
                    markerName
                )
            })
            return
        }
        val marker: com.airbnb.lottie.model.Marker = composition.getMarker(markerName)
            ?: throw java.lang.IllegalArgumentException("Cannot find marker with name $markerName.")
        setMaxFrame((marker.startFrame + marker.durationFrames).toInt())
    }

    /**
     * Sets the minimum and maximum frame to the start time and start time + duration
     * of the specified marker.
     *
     * @throws IllegalArgumentException if the marker is not found.
     */
    fun setMinAndMaxFrame(markerName: String) {
        if (composition == null) {
            lazyCompositionTasks.add(LazyCompositionTask { c: LottieComposition? ->
                setMinAndMaxFrame(
                    markerName
                )
            })
            return
        }
        val marker: com.airbnb.lottie.model.Marker = composition.getMarker(markerName)
            ?: throw java.lang.IllegalArgumentException("Cannot find marker with name $markerName.")
        val startFrame: Int = marker.startFrame.toInt()
        setMinAndMaxFrame(startFrame, startFrame + marker.durationFrames.toInt())
    }

    /**
     * Sets the minimum and maximum frame to the start marker start and the maximum frame to the end marker start.
     * playEndMarkerStartFrame determines whether or not to play the frame that the end marker is on. If the end marker
     * represents the end of the section that you want, it should be true. If the marker represents the beginning of the
     * next section, it should be false.
     *
     * @throws IllegalArgumentException if either marker is not found.
     */
    fun setMinAndMaxFrame(
        startMarkerName: String,
        endMarkerName: String,
        playEndMarkerStartFrame: Boolean
    ) {
        if (composition == null) {
            lazyCompositionTasks.add(LazyCompositionTask { c: LottieComposition? ->
                setMinAndMaxFrame(
                    startMarkerName,
                    endMarkerName,
                    playEndMarkerStartFrame
                )
            })
            return
        }
        val startMarker: com.airbnb.lottie.model.Marker = composition.getMarker(startMarkerName)
            ?: throw java.lang.IllegalArgumentException("Cannot find marker with name $startMarkerName.")
        val startFrame: Int = startMarker.startFrame.toInt()
        val endMarker: com.airbnb.lottie.model.Marker = composition.getMarker(endMarkerName)
            ?: throw java.lang.IllegalArgumentException("Cannot find marker with name $endMarkerName.")
        val endFrame: Int = (endMarker.startFrame + if (playEndMarkerStartFrame) 1f else 0f).toInt()
        setMinAndMaxFrame(startFrame, endFrame)
    }

    /**
     * @see .setMinFrame
     * @see .setMaxFrame
     */
    fun setMinAndMaxFrame(minFrame: Int, maxFrame: Int) {
        if (composition == null) {
            lazyCompositionTasks.add(LazyCompositionTask { c: LottieComposition? ->
                setMinAndMaxFrame(
                    minFrame,
                    maxFrame
                )
            })
            return
        }
        // Adding 0.99 ensures that the maxFrame itself gets played.
        animator.setMinAndMaxFrames(minFrame.toFloat(), maxFrame + 0.99f)
    }

    /**
     * @see .setMinProgress
     * @see .setMaxProgress
     */
    fun setMinAndMaxProgress(
        @androidx.annotation.FloatRange(from = 0f, to = 1f) minProgress: Float,
        @androidx.annotation.FloatRange(from = 0f, to = 1f) maxProgress: Float
    ) {
        if (composition == null) {
            lazyCompositionTasks.add(LazyCompositionTask { c: LottieComposition? ->
                setMinAndMaxProgress(
                    minProgress,
                    maxProgress
                )
            })
            return
        }
        setMinAndMaxFrame(
            MiscUtils.lerp(
                composition.getStartFrame(),
                composition.getEndFrame(),
                minProgress
            ).toInt(),
            MiscUtils.lerp(composition.getStartFrame(), composition.getEndFrame(), maxProgress)
                .toInt()
        )
    }

    /**
     * Reverses the current animation speed. This does NOT play the animation.
     *
     * @see .setSpeed
     * @see .playAnimation
     * @see .resumeAnimation
     */
    fun reverseAnimationSpeed() {
        animator.reverseAnimationSpeed()
    }

    var speed: Float
        /**
         * Returns the current playback speed. This will be &lt; 0 if the animation is playing backwards.
         */
        get() = animator.getSpeed()
        /**
         * Sets the playback speed. If speed &lt; 0, the animation will play backwards.
         */
        set(speed) {
            animator.setSpeed(speed)
        }

    fun addAnimatorUpdateListener(updateListener: AnimatorUpdateListener?) {
        animator.addUpdateListener(updateListener)
    }

    fun removeAnimatorUpdateListener(updateListener: AnimatorUpdateListener?) {
        animator.removeUpdateListener(updateListener)
    }

    fun removeAllUpdateListeners() {
        animator.removeAllUpdateListeners()
        animator.addUpdateListener(progressUpdateListener)
    }

    fun addAnimatorListener(listener: AnimatorListener?) {
        animator.addListener(listener)
    }

    fun removeAnimatorListener(listener: AnimatorListener?) {
        animator.removeListener(listener)
    }

    fun removeAllAnimatorListeners() {
        animator.removeAllListeners()
    }

    @RequiresApi(api = android.os.Build.VERSION_CODES.KITKAT)
    fun addAnimatorPauseListener(listener: AnimatorPauseListener?) {
        animator.addPauseListener(listener)
    }

    @RequiresApi(api = android.os.Build.VERSION_CODES.KITKAT)
    fun removeAnimatorPauseListener(listener: AnimatorPauseListener?) {
        animator.removePauseListener(listener)
    }

    var frame: Int
        /**
         * Get the currently rendered frame.
         */
        get() = animator.getFrame().toInt()
        /**
         * Sets the progress to the specified frame.
         * If the composition isn't set yet, the progress will be set to the frame when
         * it is.
         */
        set(frame) {
            if (composition == null) {
                lazyCompositionTasks.add(LazyCompositionTask { c: LottieComposition? ->
                    setFrame(
                        frame
                    )
                })
                return
            }
            animator.setFrame(frame.toFloat())
        }

    /**
     * @see .setRepeatCount
     */
    @Deprecated("")
    fun loop(loop: Boolean) {
        animator.setRepeatCount(if (loop) ValueAnimator.INFINITE else 0)
    }

    @get:com.airbnb.lottie.LottieDrawable.RepeatMode
    @get:android.annotation.SuppressLint("WrongConstant")
    var repeatMode: Int
        /**
         * Defines what this animation should do when it reaches the end.
         *
         * @return either one of [.REVERSE] or [.RESTART]
         */
        get() = animator.getRepeatMode()
        /**
         * Defines what this animation should do when it reaches the end. This
         * setting is applied only when the repeat count is either greater than
         * 0 or [.INFINITE]. Defaults to [.RESTART].
         *
         * @param mode [.RESTART] or [.REVERSE]
         */
        set(mode) {
            animator.setRepeatMode(mode)
        }
    var repeatCount: Int
        /**
         * Defines how many times the animation should repeat. The default value
         * is 0.
         *
         * @return the number of times the animation should repeat, or [.INFINITE]
         */
        get() = animator.getRepeatCount()
        /**
         * Sets how many times the animation should be repeated. If the repeat
         * count is 0, the animation is never repeated. If the repeat count is
         * greater than 0 or [.INFINITE], the repeat mode will be taken
         * into account. The repeat count is 0 by default.
         *
         * @param count the number of times the animation should be repeated
         */
        set(count) {
            animator.setRepeatCount(count)
        }

    @get:Suppress("unused")
    val isLooping: Boolean
        get() = animator.getRepeatCount() == ValueAnimator.INFINITE
    val isAnimating: Boolean
        get() {
            // On some versions of Android, this is called from the LottieAnimationView constructor, before animator was created.
            // https://github.com/airbnb/lottie-android/issues/1430
            return if (animator == null) {
                false
            } else animator.isRunning()
        }
    val isAnimatingOrWillAnimateOnVisible: Boolean
        get() {
            return if (isVisible()) {
                animator.isRunning()
            } else {
                onVisibleAction == OnVisibleAction.PLAY || onVisibleAction == OnVisibleAction.RESUME
            }
        }

    private fun animationsEnabled(): Boolean {
        return systemAnimationsEnabled || ignoreSystemAnimationsDisabled
    }

    /**
     * Tell Lottie that system animations are disabled. When using [LottieAnimationView] or Compose `LottieAnimation`, this is done
     * automatically. However, if you are using LottieDrawable on its own, you should set this to false when
     * [com.airbnb.lottie.utils.Utils.getAnimationScale] is 0.
     */
    fun setSystemAnimationsAreEnabled(areEnabled: Boolean) {
        systemAnimationsEnabled = areEnabled
    }
    // </editor-fold>
    /**
     * Allows ignoring system animations settings, therefore allowing animations to run even if they are disabled.
     *
     *
     * Defaults to false.
     *
     * @param ignore if true animations will run even when they are disabled in the system settings.
     */
    fun setIgnoreDisabledSystemAnimations(ignore: Boolean) {
        ignoreSystemAnimationsDisabled = ignore
    }

    /**
     * Lottie files can specify a target frame rate. By default, Lottie ignores it and re-renders
     * on every frame. If that behavior is undesirable, you can set this to true to use the composition
     * frame rate instead.
     *
     *
     * Note: composition frame rates are usually lower than display frame rates
     * so this will likely make your animation feel janky. However, it may be desirable
     * for specific situations such as pixel art that are intended to have low frame rates.
     */
    fun setUseCompositionFrameRate(useCompositionFrameRate: Boolean) {
        animator.setUseCompositionFrameRate(useCompositionFrameRate)
    }

    /**
     * Use this if you can't bundle images with your app. This may be useful if you download the
     * animations from the network or have the images saved to an SD Card. In that case, Lottie
     * will defer the loading of the bitmap to this delegate.
     *
     *
     * Be wary if you are using many images, however. Lottie is designed to work with vector shapes
     * from After Effects. If your images look like they could be represented with vector shapes,
     * see if it is possible to convert them to shape layers and re-export your animation. Check
     * the documentation at [http://airbnb.io/lottie](http://airbnb.io/lottie) for more information about importing shapes from
     * Sketch or Illustrator to avoid this.
     */
    fun setImageAssetDelegate(assetDelegate: ImageAssetDelegate?) {
        imageAssetDelegate = assetDelegate
        if (imageAssetManager != null) {
            imageAssetManager.setDelegate(assetDelegate)
        }
    }

    /**
     * Use this to manually set fonts.
     */
    fun setFontAssetDelegate(assetDelegate: FontAssetDelegate?) {
        fontAssetDelegate = assetDelegate
        if (fontAssetManager != null) {
            fontAssetManager.setDelegate(assetDelegate)
        }
    }

    /**
     * Set a map from font name keys to Typefaces.
     * The keys can be in the form:
     * * fontFamily
     * * fontFamily-fontStyle
     * * fontName
     * All 3 are defined as fName, fFamily, and fStyle in the Lottie file.
     *
     *
     * If you change a value in fontMap, create a new map or call
     * [.invalidateSelf]. Setting the same map again will noop.
     */
    fun setFontMap(fontMap: Map<String, android.graphics.Typeface>?) {
        if (fontMap === this.fontMap) {
            return
        }
        this.fontMap = fontMap
        invalidateSelf()
    }

    fun setTextDelegate(textDelegate: com.airbnb.lottie.TextDelegate?) {
        this.textDelegate = textDelegate
    }

    fun getTextDelegate(): com.airbnb.lottie.TextDelegate? {
        return textDelegate
    }

    fun useTextGlyphs(): Boolean {
        return fontMap == null && textDelegate == null && composition.getCharacters().size() > 0
    }

    fun getComposition(): LottieComposition? {
        return composition
    }

    fun cancelAnimation() {
        lazyCompositionTasks.clear()
        animator.cancel()
        if (!isVisible()) {
            onVisibleAction = OnVisibleAction.NONE
        }
    }

    fun pauseAnimation() {
        lazyCompositionTasks.clear()
        animator.pauseAnimation()
        if (!isVisible()) {
            onVisibleAction = OnVisibleAction.NONE
        }
    }

    @get:androidx.annotation.FloatRange(from = 0f, to = 1f)
    var progress: Float
        get() = animator.getAnimatedValueAbsolute()
        set(progress) {
            if (composition == null) {
                lazyCompositionTasks.add(LazyCompositionTask { c: LottieComposition? ->
                    setProgress(
                        progress
                    )
                })
                return
            }
            com.airbnb.lottie.L.beginSection("Drawable#setProgress")
            animator.setFrame(composition.getFrameForProgress(progress))
            com.airbnb.lottie.L.endSection("Drawable#setProgress")
        }
    val intrinsicWidth: Int
        get() = if (composition == null) -1 else composition.getBounds().width()
    val intrinsicHeight: Int
        get() = if (composition == null) -1 else composition.getBounds().height()

    /**
     * Takes a [KeyPath], potentially with wildcards or globstars and resolve it to a list of
     * zero or more actual [Keypaths][KeyPath] that exist in the current animation.
     *
     *
     * If you want to set value callbacks for any of these values, it is recommend to use the
     * returned [KeyPath] objects because they will be internally resolved to their content
     * and won't trigger a tree walk of the animation contents when applied.
     */
    fun resolveKeyPath(keyPath: KeyPath?): List<KeyPath> {
        if (compositionLayer == null) {
            com.airbnb.lottie.utils.Logger.warning("Cannot resolve KeyPath. Composition is not set yet.")
            return emptyList<KeyPath>()
        }
        val keyPaths: List<KeyPath> = ArrayList<KeyPath>()
        compositionLayer.resolveKeyPath(keyPath, 0, keyPaths, KeyPath())
        return keyPaths
    }

    /**
     * Add an property callback for the specified [KeyPath]. This [KeyPath] can resolve
     * to multiple contents. In that case, the callback's value will apply to all of them.
     *
     *
     * Internally, this will check if the [KeyPath] has already been resolved with
     * [.resolveKeyPath] and will resolve it if it hasn't.
     *
     *
     * Set the callback to null to clear it.
     */
    fun <T> addValueCallback(
        keyPath: KeyPath, property: T, callback: LottieValueCallback<T>?
    ) {
        if (compositionLayer == null) {
            lazyCompositionTasks.add(LazyCompositionTask { c: LottieComposition? ->
                addValueCallback<T>(
                    keyPath,
                    property,
                    callback
                )
            })
            return
        }
        val invalidate: Boolean
        invalidate = if (keyPath === KeyPath.COMPOSITION) {
            compositionLayer.addValueCallback<T>(property, callback)
            true
        } else if (keyPath.getResolvedElement() != null) {
            keyPath.getResolvedElement().addValueCallback<T>(property, callback)
            true
        } else {
            val elements: List<KeyPath> = resolveKeyPath(keyPath)
            for (i in elements.indices) {
                elements[i].getResolvedElement().addValueCallback<T>(property, callback)
            }
            !elements.isEmpty()
        }
        if (invalidate) {
            invalidateSelf()
            if (property === LottieProperty.TIME_REMAP) {
                // Time remapping values are read in setProgress. In order for the new value
                // to apply, we have to re-set the progress with the current progress so that the
                // time remapping can be reapplied.
                progress = progress
            }
        }
    }

    /**
     * Overload of [.addValueCallback] that takes an interface. This allows you to use a single abstract
     * method code block in Kotlin such as:
     * drawable.addValueCallback(yourKeyPath, LottieProperty.COLOR) { yourColor }
     */
    fun <T> addValueCallback(
        keyPath: KeyPath?, property: T,
        callback: SimpleLottieValueCallback<T>
    ) {
        addValueCallback<T>(keyPath, property, object : LottieValueCallback<T>() {
            override fun getValue(frameInfo: LottieFrameInfo<T>): T? {
                return callback.getValue(frameInfo)
            }
        })
    }

    /**
     * Allows you to modify or clear a bitmap that was loaded for an image either automatically
     * through [.setImagesAssetsFolder] or with an [ImageAssetDelegate].
     *
     * @return the previous Bitmap or null.
     */
    fun updateBitmap(id: String?, bitmap: android.graphics.Bitmap?): android.graphics.Bitmap? {
        val bm: ImageAssetManager? = getImageAssetManager()
        if (bm == null) {
            com.airbnb.lottie.utils.Logger.warning(
                "Cannot update bitmap. Most likely the drawable is not added to a View " +
                        "which prevents Lottie from getting a Context."
            )
            return null
        }
        val ret: android.graphics.Bitmap = bm.updateBitmap(id, bitmap)
        invalidateSelf()
        return ret
    }

    @Deprecated("use {@link #getBitmapForId(String)}.")
    fun getImageAsset(id: String?): android.graphics.Bitmap? {
        val bm: ImageAssetManager? = getImageAssetManager()
        if (bm != null) {
            return bm.bitmapForId(id)
        }
        val imageAsset: LottieImageAsset? =
            if (composition == null) null else composition.getImages().get(id)
        return if (imageAsset != null) {
            imageAsset.getBitmap()
        } else null
    }

    /**
     * Returns the bitmap that will be rendered for the given id in the Lottie animation file.
     * The id is the asset reference id stored in the "id" property of each object in the "assets" array.
     *
     *
     * The returned bitmap could be from:
     * * Embedded in the animation file as a base64 string.
     * * In the same directory as the animation file.
     * * In the same zip file as the animation file.
     * * Returned from an [ImageAssetDelegate].
     * or null if the image doesn't exist from any of those places.
     */
    fun getBitmapForId(id: String?): android.graphics.Bitmap? {
        val assetManager: ImageAssetManager? = getImageAssetManager()
        return if (assetManager != null) {
            assetManager.bitmapForId(id)
        } else null
    }

    /**
     * Returns the [LottieImageAsset] that will be rendered for the given id in the Lottie animation file.
     * The id is the asset reference id stored in the "id" property of each object in the "assets" array.
     *
     *
     * The returned bitmap could be from:
     * * Embedded in the animation file as a base64 string.
     * * In the same directory as the animation file.
     * * In the same zip file as the animation file.
     * * Returned from an [ImageAssetDelegate].
     * or null if the image doesn't exist from any of those places.
     */
    fun getLottieImageAssetForId(id: String?): LottieImageAsset? {
        val composition: LottieComposition? = composition ?: return null
        return composition.getImages().get(id)
    }

    private fun getImageAssetManager(): ImageAssetManager? {
        if (imageAssetManager != null && !imageAssetManager.hasSameContext(context)) {
            imageAssetManager = null
        }
        if (imageAssetManager == null) {
            imageAssetManager = ImageAssetManager(
                getCallback(),
                imageAssetsFolder, imageAssetDelegate, composition.getImages()
            )
        }
        return imageAssetManager
    }

    @androidx.annotation.RestrictTo(androidx.annotation.RestrictTo.Scope.LIBRARY)
    fun getTypeface(font: com.airbnb.lottie.model.Font): android.graphics.Typeface? {
        val fontMap: Map<String, android.graphics.Typeface>? = fontMap
        if (fontMap != null) {
            var key: String = font.getFamily()
            if (fontMap.containsKey(key)) {
                return fontMap[key]
            }
            key = font.getName()
            if (fontMap.containsKey(key)) {
                return fontMap[key]
            }
            key = font.getFamily() + "-" + font.getStyle()
            if (fontMap.containsKey(key)) {
                return fontMap[key]
            }
        }
        val assetManager: FontAssetManager? = getFontAssetManager()
        return if (assetManager != null) {
            assetManager.getTypeface(font)
        } else null
    }

    private fun getFontAssetManager(): FontAssetManager? {
        if (getCallback() == null) {
            // We can't get a bitmap since we can't get a Context from the callback.
            return null
        }
        if (fontAssetManager == null) {
            fontAssetManager = FontAssetManager(getCallback(), fontAssetDelegate)
            val defaultExtension = defaultFontFileExtension
            if (defaultExtension != null) {
                fontAssetManager.setDefaultFontFileExtension(defaultFontFileExtension)
            }
        }
        return fontAssetManager
    }

    /**
     * By default, Lottie will look in src/assets/fonts/FONT_NAME.ttf
     * where FONT_NAME is the fFamily specified in your Lottie file.
     * If your fonts have a different extension, you can override the
     * default here.
     *
     *
     * Alternatively, you can use [.setFontAssetDelegate]
     * for more control.
     *
     * @see .setFontAssetDelegate
     */
    fun setDefaultFontFileExtension(extension: String?) {
        defaultFontFileExtension = extension
        val fam: FontAssetManager? = getFontAssetManager()
        if (fam != null) {
            fam.setDefaultFontFileExtension(extension)
        }
    }

    private val context: android.content.Context?
        private get() {
            val callback: android.graphics.drawable.Drawable.Callback = getCallback() ?: return null
            return if (callback is android.view.View) {
                (callback as android.view.View).getContext()
            } else null
        }

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        // Sometimes, setVisible(false) gets called twice in a row. If we don't check wasNotVisibleAlready, we could
        // wind up clearing the onVisibleAction value for the second call.
        val wasNotVisibleAlready: Boolean = !isVisible()
        val ret: Boolean = super.setVisible(visible, restart)
        if (visible) {
            if (onVisibleAction == OnVisibleAction.PLAY) {
                playAnimation()
            } else if (onVisibleAction == OnVisibleAction.RESUME) {
                resumeAnimation()
            }
        } else {
            if (animator.isRunning()) {
                pauseAnimation()
                onVisibleAction = OnVisibleAction.RESUME
            } else if (!wasNotVisibleAlready) {
                onVisibleAction = OnVisibleAction.NONE
            }
        }
        return ret
    }

    /**
     * These Drawable.Callback methods proxy the calls so that this is the drawable that is
     * actually invalidated, not a child one which will not pass the view's validateDrawable check.
     */
    override fun invalidateDrawable(who: android.graphics.drawable.Drawable) {
        val callback: android.graphics.drawable.Drawable.Callback = getCallback() ?: return
        callback.invalidateDrawable(this)
    }

    override fun scheduleDrawable(
        who: android.graphics.drawable.Drawable,
        what: java.lang.Runnable,
        `when`: Long
    ) {
        val callback: android.graphics.drawable.Drawable.Callback = getCallback() ?: return
        callback.scheduleDrawable(this, what, `when`)
    }

    override fun unscheduleDrawable(
        who: android.graphics.drawable.Drawable,
        what: java.lang.Runnable
    ) {
        val callback: android.graphics.drawable.Drawable.Callback = getCallback() ?: return
        callback.unscheduleDrawable(this, what)
    }

    /**
     * Hardware accelerated render path.
     */
    private fun drawDirectlyToCanvas(canvas: android.graphics.Canvas) {
        val compositionLayer: CompositionLayer? = compositionLayer
        val composition: LottieComposition? = composition
        if (compositionLayer == null || composition == null) {
            return
        }
        renderingMatrix.reset()
        val bounds: android.graphics.Rect = getBounds()
        if (!bounds.isEmpty()) {
            // In fitXY mode, the scale doesn't take effect.
            val scaleX: Float = bounds.width() / composition.getBounds().width().toFloat()
            val scaleY: Float = bounds.height() / composition.getBounds().height().toFloat()
            renderingMatrix.preScale(scaleX, scaleY)
            renderingMatrix.preTranslate(bounds.left.toFloat(), bounds.top.toFloat())
        }
        compositionLayer.draw(canvas, renderingMatrix, alpha)
    }

    /**
     * Software accelerated render path.
     *
     *
     * This draws the animation to an internally managed bitmap and then draws the bitmap to the original canvas.
     *
     * @see LottieAnimationView.setRenderMode
     */
    private fun renderAndDrawAsBitmap(
        originalCanvas: android.graphics.Canvas,
        compositionLayer: CompositionLayer?
    ) {
        if (composition == null || compositionLayer == null) {
            return
        }
        ensureSoftwareRenderingObjectsInitialized()
        originalCanvas.getMatrix(softwareRenderingOriginalCanvasMatrix)

        // Get the canvas clip bounds and map it to the coordinate space of canvas with it's current transform.
        originalCanvas.getClipBounds(canvasClipBounds)
        convertRect(canvasClipBounds, canvasClipBoundsRectF)
        softwareRenderingOriginalCanvasMatrix.mapRect(canvasClipBoundsRectF)
        convertRect(canvasClipBoundsRectF, canvasClipBounds)
        if (clipToCompositionBounds) {
            // Start with the intrinsic bounds. This will later be unioned with the clip bounds to find the
            // smallest possible render area.
            softwareRenderingTransformedBounds.set(
                0f,
                0f,
                intrinsicWidth.toFloat(),
                intrinsicHeight.toFloat()
            )
        } else {
            // Calculate the full bounds of the animation.
            compositionLayer.getBounds(softwareRenderingTransformedBounds, null, false)
        }
        // Transform the animation bounds to the bounds that they will render to on the canvas.
        softwareRenderingOriginalCanvasMatrix.mapRect(softwareRenderingTransformedBounds)

        // The bounds are usually intrinsicWidth x intrinsicHeight. If they are different, an external source is scaling this drawable.
        // This is how ImageView.ScaleType.FIT_XY works.
        val bounds: android.graphics.Rect = getBounds()
        val scaleX: Float = bounds.width() / intrinsicWidth.toFloat()
        val scaleY: Float = bounds.height() / intrinsicHeight.toFloat()
        scaleRect(softwareRenderingTransformedBounds, scaleX, scaleY)
        if (!ignoreCanvasClipBounds()) {
            softwareRenderingTransformedBounds.intersect(
                canvasClipBounds.left.toFloat(),
                canvasClipBounds.top.toFloat(),
                canvasClipBounds.right.toFloat(),
                canvasClipBounds.bottom.toFloat()
            )
        }
        val renderWidth =
            ceil(softwareRenderingTransformedBounds.width().toDouble()).toInt()
        val renderHeight =
            ceil(softwareRenderingTransformedBounds.height().toDouble()).toInt()
        if (renderWidth <= 0 || renderHeight <= 0) {
            return
        }
        ensureSoftwareRenderingBitmap(renderWidth, renderHeight)
        if (isDirty) {
            renderingMatrix.set(softwareRenderingOriginalCanvasMatrix)
            renderingMatrix.preScale(scaleX, scaleY)
            // We want to render the smallest bitmap possible. If the animation doesn't start at the top left, we translate the canvas and shrink the
            // bitmap to avoid allocating and copying the empty space on the left and top. renderWidth and renderHeight take this into account.
            renderingMatrix.postTranslate(
                -softwareRenderingTransformedBounds.left,
                -softwareRenderingTransformedBounds.top
            )
            softwareRenderingBitmap.eraseColor(0)
            compositionLayer.draw(softwareRenderingCanvas, renderingMatrix, alpha)

            // Calculate the dst bounds.
            // We need to map the rendered coordinates back to the canvas's coordinates. To do so, we need to invert the transform
            // of the original canvas.
            // Take the bounds of the rendered animation and map them to the canvas's coordinates.
            // This is similar to the src rect above but the src bound may have a left and top offset.
            softwareRenderingOriginalCanvasMatrix.invert(
                softwareRenderingOriginalCanvasMatrixInverse
            )
            softwareRenderingOriginalCanvasMatrixInverse.mapRect(
                softwareRenderingDstBoundsRectF,
                softwareRenderingTransformedBounds
            )
            convertRect(softwareRenderingDstBoundsRectF, softwareRenderingDstBoundsRect)
        }
        softwareRenderingSrcBoundsRect.set(0, 0, renderWidth, renderHeight)
        originalCanvas.drawBitmap(
            softwareRenderingBitmap,
            softwareRenderingSrcBoundsRect,
            softwareRenderingDstBoundsRect,
            softwareRenderingPaint
        )
    }

    private fun ensureSoftwareRenderingObjectsInitialized() {
        if (softwareRenderingCanvas != null) {
            return
        }
        softwareRenderingCanvas = android.graphics.Canvas()
        softwareRenderingTransformedBounds = RectF()
        softwareRenderingOriginalCanvasMatrix = android.graphics.Matrix()
        softwareRenderingOriginalCanvasMatrixInverse = android.graphics.Matrix()
        canvasClipBounds = android.graphics.Rect()
        canvasClipBoundsRectF = RectF()
        softwareRenderingPaint = LPaint()
        softwareRenderingSrcBoundsRect = android.graphics.Rect()
        softwareRenderingDstBoundsRect = android.graphics.Rect()
        softwareRenderingDstBoundsRectF = RectF()
    }

    private fun ensureSoftwareRenderingBitmap(renderWidth: Int, renderHeight: Int) {
        if (softwareRenderingBitmap == null || softwareRenderingBitmap.getWidth() < renderWidth || softwareRenderingBitmap.getHeight() < renderHeight) {
            // The bitmap is larger. We need to create a new one.
            softwareRenderingBitmap = android.graphics.Bitmap.createBitmap(
                renderWidth,
                renderHeight,
                android.graphics.Bitmap.Config.ARGB_8888
            )
            softwareRenderingCanvas.setBitmap(softwareRenderingBitmap)
            isDirty = true
        } else if (softwareRenderingBitmap.getWidth() > renderWidth || softwareRenderingBitmap.getHeight() > renderHeight) {
            // The bitmap is smaller. Take subset of the original.
            softwareRenderingBitmap = android.graphics.Bitmap.createBitmap(
                softwareRenderingBitmap,
                0,
                0,
                renderWidth,
                renderHeight
            )
            softwareRenderingCanvas.setBitmap(softwareRenderingBitmap)
            isDirty = true
        }
    }

    /**
     * Convert a RectF to a Rect
     */
    private fun convertRect(src: RectF, dst: android.graphics.Rect) {
        dst.set(
            floor(src.left.toDouble()).toInt(),
            floor(src.top.toDouble()).toInt(),
            ceil(src.right.toDouble()).toInt(),
            ceil(src.bottom.toDouble()).toInt()
        )
    }

    /**
     * Convert a Rect to a RectF
     */
    private fun convertRect(src: android.graphics.Rect, dst: RectF) {
        dst.set(
            src.left.toFloat(),
            src.top.toFloat(),
            src.right.toFloat(),
            src.bottom.toFloat()
        )
    }

    private fun scaleRect(rect: RectF?, scaleX: Float, scaleY: Float) {
        rect.set(
            rect.left * scaleX,
            rect.top * scaleY,
            rect.right * scaleX,
            rect.bottom * scaleY
        )
    }

    /**
     * When a View's parent has clipChildren set to false, it doesn't affect the clipBound
     * of its child canvases so we should explicitly check for it and draw the full animation
     * bounds instead.
     */
    private fun ignoreCanvasClipBounds(): Boolean {
        val callback: android.graphics.drawable.Drawable.Callback = getCallback()
        if (callback !is android.view.View) {
            // If the callback isn't a view then respect the canvas's clip bounds.
            return false
        }
        val parent: ViewParent = (callback as android.view.View).getParent()
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2 && parent is android.view.ViewGroup) {
            !(parent as android.view.ViewGroup).getClipChildren()
        } else false
        // Unlikely to ever happen. If the callback is a View, its parent should be a ViewGroup.
    }

    companion object {
        /**
         * Prior to Oreo, you could only call invalidateDrawable() from the main thread.
         * This means that when async updates are enabled, we must post the invalidate call to the main thread.
         * Newer devices can call invalidate directly from whatever thread asyncUpdates runs on.
         */
        private val invalidateSelfOnMainThread: Boolean =
            android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N_MR1

        /**
         * The executor that [AsyncUpdates] will be run on.
         *
         *
         * Defaults to a core size of 0 so that when no animations are playing, there will be no
         * idle cores consuming resources.
         *
         *
         * Allows up to two active threads so that if there are many animations, they can all work in parallel.
         * Two was arbitrarily chosen but should be sufficient for most uses cases. In the case of a single
         * animation, this should never exceed one.
         *
         *
         * Each thread will timeout after 35ms which gives it enough time to persist for one frame, one dropped frame
         * and a few extra ms just in case.
         */
        private val setProgressExecutor: java.util.concurrent.Executor =
            java.util.concurrent.ThreadPoolExecutor(
                0,
                2,
                35,
                java.util.concurrent.TimeUnit.MILLISECONDS,
                java.util.concurrent.LinkedBlockingQueue<java.lang.Runnable>(),
                LottieThreadFactory()
            )
        private const val MAX_DELTA_MS_ASYNC_SET_PROGRESS = 3 / 60f * 1000

        /**
         * When the animation reaches the end and `repeatCount` is INFINITE
         * or a positive value, the animation restarts from the beginning.
         */
        val RESTART: Int = ValueAnimator.RESTART

        /**
         * When the animation reaches the end and `repeatCount` is INFINITE
         * or a positive value, the animation reverses direction on every iteration.
         */
        val REVERSE: Int = ValueAnimator.REVERSE

        /**
         * This value used used with the [.setRepeatCount] property to repeat
         * the animation indefinitely.
         */
        val INFINITE: Int = ValueAnimator.INFINITE
    }
}

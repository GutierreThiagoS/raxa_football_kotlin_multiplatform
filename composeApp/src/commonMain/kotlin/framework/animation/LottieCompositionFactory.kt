package framework.animation

import kotlin.jvm.JvmOverloads

/**
 * Helpers to create or cache a LottieComposition.
 *
 *
 * All factory methods take a cache key. The animation will be stored in an LRU cache for future use.
 * In-progress tasks will also be held so they can be returned for subsequent requests for the same
 * animation prior to the cache being populated.
 */
object LottieCompositionFactory {
    /**
     * Keep a map of cache keys to in-progress tasks and return them for new requests.
     * Without this, simultaneous requests to parse a composition will trigger multiple parallel
     * parse tasks prior to the cache getting populated.
     */
    private val taskCache: Map<String, LottieTask<LottieComposition>> =
        HashMap<String, LottieTask<LottieComposition>>()
    private val taskIdleListeners: Set<LottieTaskIdleListener> =
        java.util.HashSet<LottieTaskIdleListener>()

    /**
     * reference magic bytes for zip compressed files.
     * useful to determine if an InputStream is a zip file or not
     */
    private val ZIP_MAGIC = byteArrayOf(0x50, 0x4b, 0x03, 0x04)
    private val GZIP_MAGIC = byteArrayOf(0x1f, 0x8b.toByte(), 0x08)

    /**
     * Set the maximum number of compositions to keep cached in memory.
     * This must be &gt; 0.
     */
    fun setMaxCacheSize(size: Int) {
        LottieCompositionCache.getInstance().resize(size)
    }

    fun clearCache(context: android.content.Context?) {
        taskCache.clear()
        LottieCompositionCache.getInstance().clear()
        val networkCache: com.airbnb.lottie.network.NetworkCache =
            com.airbnb.lottie.L.networkCache(context)
        if (networkCache != null) {
            networkCache.clear()
        }
    }

    /**
     * Use this to register a callback for when the composition factory is idle or not.
     * This can be used to provide data to an espresso idling resource.
     * Refer to FragmentVisibilityTests and its LottieIdlingResource in the Lottie repo for
     * an example.
     */
    fun registerLottieTaskIdleListener(listener: LottieTaskIdleListener) {
        taskIdleListeners.add(listener)
        listener.onIdleChanged(taskCache.size == 0)
    }

    fun unregisterLottieTaskIdleListener(listener: LottieTaskIdleListener?) {
        taskIdleListeners.remove(listener)
    }
    /**
     * Fetch an animation from an http url. Once it is downloaded once, Lottie will cache the file to disk for
     * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
     * might need an animation in the future.
     */
    /**
     * Fetch an animation from an http url. Once it is downloaded once, Lottie will cache the file to disk for
     * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
     * might need an animation in the future.
     *
     *
     * To skip the cache, add null as a third parameter.
     */
    @JvmOverloads
    fun fromUrl(
        context: android.content.Context?,
        url: String,
        cacheKey: String? = "url_$url"
    ): LottieTask<LottieComposition> {
        return cache(cacheKey,
            java.util.concurrent.Callable<LottieResult<LottieComposition>> {
                val result: LottieResult<LottieComposition> =
                    com.airbnb.lottie.L.networkFetcher(context).fetchSync(context, url, cacheKey)
                if (cacheKey != null && result.getValue() != null) {
                    LottieCompositionCache.getInstance().put(cacheKey, result.getValue())
                }
                result
            }, null
        )
    }

    /**
     * Fetch an animation from an http url. Once it is downloaded once, Lottie will cache the file to disk for
     * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
     * might need an animation in the future.
     */
    @androidx.annotation.WorkerThread
    fun fromUrlSync(
        context: android.content.Context?,
        url: String?
    ): LottieResult<LottieComposition> {
        return fromUrlSync(context, url, url)
    }

    /**
     * Fetch an animation from an http url. Once it is downloaded once, Lottie will cache the file to disk for
     * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
     * might need an animation in the future.
     */
    @androidx.annotation.WorkerThread
    fun fromUrlSync(
        context: android.content.Context?,
        url: String?,
        cacheKey: String?
    ): LottieResult<LottieComposition> {
        val cachedComposition: LottieComposition? =
            if (cacheKey == null) null else LottieCompositionCache.getInstance().get(cacheKey)
        if (cachedComposition != null) {
            return LottieResult<LottieComposition>(cachedComposition)
        }
        val result: LottieResult<LottieComposition> =
            com.airbnb.lottie.L.networkFetcher(context).fetchSync(context, url, cacheKey)
        if (cacheKey != null && result.getValue() != null) {
            LottieCompositionCache.getInstance().put(cacheKey, result.getValue())
        }
        return result
    }

    /**
     * Parse an animation from src/main/assets. It is recommended to use [.fromRawRes] instead.
     * The asset file name will be used as a cache key so future usages won't have to parse the json again.
     * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
     *
     *
     * To skip the cache, add null as a third parameter.
     *
     * @see .fromZipStream
     */
    fun fromAsset(
        context: android.content.Context?,
        fileName: String
    ): LottieTask<LottieComposition> {
        val cacheKey = "asset_$fileName"
        return fromAsset(context, fileName, cacheKey)
    }

    /**
     * Parse an animation from src/main/assets. It is recommended to use [.fromRawRes] instead.
     * The asset file name will be used as a cache key so future usages won't have to parse the json again.
     * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
     *
     *
     * Pass null as the cache key to skip the cache.
     *
     * @see .fromZipStream
     */
    fun fromAsset(
        context: android.content.Context,
        fileName: String?,
        cacheKey: String?
    ): LottieTask<LottieComposition> {
        // Prevent accidentally leaking an Activity.
        val appContext: android.content.Context = context.getApplicationContext()
        return cache(cacheKey,
            java.util.concurrent.Callable<LottieResult<LottieComposition>> {
                fromAssetSync(
                    appContext,
                    fileName,
                    cacheKey
                )
            }, null
        )
    }

    /**
     * Parse an animation from src/main/assets. It is recommended to use [.fromRawRes] instead.
     * The asset file name will be used as a cache key so future usages won't have to parse the json again.
     * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
     *
     *
     * To skip the cache, add null as a third parameter.
     *
     * @see .fromZipStreamSync
     */
    @androidx.annotation.WorkerThread
    fun fromAssetSync(
        context: android.content.Context?,
        fileName: String
    ): LottieResult<LottieComposition> {
        val cacheKey = "asset_$fileName"
        return fromAssetSync(context, fileName, cacheKey)
    }

    /**
     * Parse an animation from src/main/assets. It is recommended to use [.fromRawRes] instead.
     * The asset file name will be used as a cache key so future usages won't have to parse the json again.
     * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
     *
     *
     * Pass null as the cache key to skip the cache.
     *
     * @see .fromZipStreamSync
     */
    @androidx.annotation.WorkerThread
    fun fromAssetSync(
        context: android.content.Context,
        fileName: String?,
        cacheKey: String?
    ): LottieResult<LottieComposition> {
        val cachedComposition: LottieComposition? =
            if (cacheKey == null) null else LottieCompositionCache.getInstance().get(cacheKey)
        return if (cachedComposition != null) {
            LottieResult<LottieComposition>(cachedComposition)
        } else try {
            val source: BufferedSource = Okio.buffer(source(context.getAssets().open(fileName)))
            if (isZipCompressed(source)) {
                return fromZipStreamSync(
                    context,
                    java.util.zip.ZipInputStream(source.inputStream()),
                    cacheKey
                )
            } else if (isGzipCompressed(source)) {
                return fromJsonInputStreamSync(
                    java.util.zip.GZIPInputStream(
                        source.inputStream()
                    ), cacheKey
                )
            }
            fromJsonInputStreamSync(
                source.inputStream(),
                cacheKey
            )
        } catch (e: java.io.IOException) {
            LottieResult<LottieComposition>(e)
        }
    }
    /**
     * Parse an animation from raw/res. This is recommended over putting your animation in assets because
     * it uses a hard reference to R.
     * The resource id will be used as a cache key so future usages won't parse the json again.
     * Note: to correctly load dark mode (-night) resources, make sure you pass Activity as a context (instead of e.g. the application context).
     * The Activity won't be leaked.
     *
     *
     * Pass null as the cache key to skip caching.
     */
    /**
     * Parse an animation from raw/res. This is recommended over putting your animation in assets because
     * it uses a hard reference to R.
     * The resource id will be used as a cache key so future usages won't parse the json again.
     * Note: to correctly load dark mode (-night) resources, make sure you pass Activity as a context (instead of e.g. the application context).
     * The Activity won't be leaked.
     *
     *
     * To skip the cache, add null as a third parameter.
     */
    @JvmOverloads
    fun fromRawRes(
        context: android.content.Context,
        @androidx.annotation.RawRes rawRes: Int,
        cacheKey: String? = rawResCacheKey(
            context,
            rawRes
        )
    ): LottieTask<LottieComposition> {
        // Prevent accidentally leaking an Activity.
        val contextRef: java.lang.ref.WeakReference<android.content.Context> =
            java.lang.ref.WeakReference<android.content.Context>(context)
        val appContext: android.content.Context = context.getApplicationContext()
        return cache(cacheKey,
            java.util.concurrent.Callable<LottieResult<LottieComposition>> {
                val originalContext: android.content.Context = contextRef.get()
                val context1: android.content.Context =
                    if (originalContext != null) originalContext else appContext
                fromRawResSync(
                    context1,
                    rawRes,
                    cacheKey
                )
            }, null
        )
    }

    /**
     * Parse an animation from raw/res. This is recommended over putting your animation in assets because
     * it uses a hard reference to R.
     * The resource id will be used as a cache key so future usages won't parse the json again.
     * Note: to correctly load dark mode (-night) resources, make sure you pass Activity as a context (instead of e.g. the application context).
     * The Activity won't be leaked.
     *
     *
     * To skip the cache, add null as a third parameter.
     */
    @androidx.annotation.WorkerThread
    fun fromRawResSync(
        context: android.content.Context?,
        @androidx.annotation.RawRes rawRes: Int
    ): LottieResult<LottieComposition> {
        return fromRawResSync(
            context,
            rawRes,
            rawResCacheKey(context, rawRes)
        )
    }

    /**
     * Parse an animation from raw/res. This is recommended over putting your animation in assets because
     * it uses a hard reference to R.
     * The resource id will be used as a cache key so future usages won't parse the json again.
     * Note: to correctly load dark mode (-night) resources, make sure you pass Activity as a context (instead of e.g. the application context).
     * The Activity won't be leaked.
     *
     *
     * Pass null as the cache key to skip caching.
     */
    @androidx.annotation.WorkerThread
    fun fromRawResSync(
        context: android.content.Context,
        @androidx.annotation.RawRes rawRes: Int,
        cacheKey: String?
    ): LottieResult<LottieComposition> {
        val cachedComposition: LottieComposition? =
            if (cacheKey == null) null else LottieCompositionCache.getInstance().get(cacheKey)
        return if (cachedComposition != null) {
            LottieResult<LottieComposition>(cachedComposition)
        } else try {
            val source: BufferedSource =
                Okio.buffer(source(context.getResources().openRawResource(rawRes)))
            if (isZipCompressed(source)) {
                return fromZipStreamSync(
                    context,
                    java.util.zip.ZipInputStream(source.inputStream()),
                    cacheKey
                )
            } else if (isGzipCompressed(source)) {
                return try {
                    fromJsonInputStreamSync(
                        java.util.zip.GZIPInputStream(
                            source.inputStream()
                        ), cacheKey
                    )
                } catch (e: java.io.IOException) {
                    // This shouldn't happen because we check the header for magic bytes.
                    LottieResult<LottieComposition>(e)
                }
            }
            fromJsonInputStreamSync(
                source.inputStream(),
                cacheKey
            )
        } catch (e: android.content.res.Resources.NotFoundException) {
            LottieResult<LottieComposition>(e)
        }
    }

    private fun rawResCacheKey(
        context: android.content.Context,
        @androidx.annotation.RawRes resId: Int
    ): String {
        return "rawRes" + (if (isNightMode(context)) "_night_" else "_day_") + resId
    }

    /**
     * It is important to include day/night in the cache key so that if it changes, the cache won't return an animation from the wrong bucket.
     */
    private fun isNightMode(context: android.content.Context): Boolean {
        val nightModeMasked: Int = context.getResources()
            .getConfiguration().uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return nightModeMasked == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * Auto-closes the stream.
     *
     * @see .fromJsonInputStreamSync
     */
    fun fromJsonInputStream(
        stream: java.io.InputStream?,
        cacheKey: String?
    ): LottieTask<LottieComposition> {
        return cache(cacheKey,
            java.util.concurrent.Callable<LottieResult<LottieComposition>> {
                fromJsonInputStreamSync(
                    stream,
                    cacheKey
                )
            },
            java.lang.Runnable { com.airbnb.lottie.utils.Utils.closeQuietly(stream) })
    }

    /**
     * @see .fromJsonInputStreamSync
     */
    fun fromJsonInputStream(
        stream: java.io.InputStream?,
        cacheKey: String?,
        close: Boolean
    ): LottieTask<LottieComposition> {
        return cache(cacheKey,
            java.util.concurrent.Callable<LottieResult<LottieComposition>> {
                fromJsonInputStreamSync(
                    stream,
                    cacheKey,
                    close
                )
            },
            java.lang.Runnable {
                if (close) {
                    com.airbnb.lottie.utils.Utils.closeQuietly(stream)
                }
            })
    }

    /**
     * Return a LottieComposition for the given InputStream to json.
     */
    @androidx.annotation.WorkerThread
    fun fromJsonInputStreamSync(
        stream: java.io.InputStream?,
        cacheKey: String?
    ): LottieResult<LottieComposition> {
        return fromJsonInputStreamSync(
            stream,
            cacheKey,
            true
        )
    }

    /**
     * Return a LottieComposition for the given InputStream to json.
     */
    @androidx.annotation.WorkerThread
    fun fromJsonInputStreamSync(
        stream: java.io.InputStream?,
        cacheKey: String?,
        close: Boolean
    ): LottieResult<LottieComposition> {
        return fromJsonReaderSync(
            com.airbnb.lottie.parser.moshi.JsonReader.of(
                buffer(source(stream))
            ), cacheKey, close
        )
    }

    /**
     * @see .fromJsonSync
     */
    @Deprecated("")
    fun fromJson(
        json: org.json.JSONObject?,
        cacheKey: String?
    ): LottieTask<LottieComposition> {
        return cache(cacheKey,
            java.util.concurrent.Callable<LottieResult<LottieComposition>> {
                fromJsonSync(
                    json,
                    cacheKey
                )
            }, null
        )
    }

    /**
     * Prefer passing in the json string directly. This method just calls `toString()` on your JSONObject.
     * If you are loading this animation from the network, just use the response body string instead of
     * parsing it first for improved performance.
     */
    @Deprecated("")
    @androidx.annotation.WorkerThread
    fun fromJsonSync(
        json: org.json.JSONObject,
        cacheKey: String?
    ): LottieResult<LottieComposition> {
        return fromJsonStringSync(
            json.toString(),
            cacheKey
        )
    }

    /**
     * @see .fromJsonStringSync
     */
    fun fromJsonString(
        json: String?,
        cacheKey: String?
    ): LottieTask<LottieComposition> {
        return cache(cacheKey,
            java.util.concurrent.Callable<LottieResult<LottieComposition>> {
                fromJsonStringSync(
                    json,
                    cacheKey
                )
            }, null
        )
    }

    /**
     * Return a LottieComposition for the specified raw json string.
     * If loading from a file, it is preferable to use the InputStream or rawRes version.
     */
    @androidx.annotation.WorkerThread
    fun fromJsonStringSync(
        json: String,
        cacheKey: String?
    ): LottieResult<LottieComposition> {
        val stream: java.io.ByteArrayInputStream = java.io.ByteArrayInputStream(json.toByteArray())
        return fromJsonReaderSync(
            com.airbnb.lottie.parser.moshi.JsonReader.of(
                buffer(source(stream))
            ), cacheKey
        )
    }

    fun fromJsonReader(
        reader: com.airbnb.lottie.parser.moshi.JsonReader?,
        cacheKey: String?
    ): LottieTask<LottieComposition> {
        return cache(cacheKey,
            java.util.concurrent.Callable<LottieResult<LottieComposition>> {
                fromJsonReaderSync(
                    reader,
                    cacheKey
                )
            },
            java.lang.Runnable { com.airbnb.lottie.utils.Utils.closeQuietly(reader) })
    }

    @androidx.annotation.WorkerThread
    fun fromJsonReaderSync(
        reader: com.airbnb.lottie.parser.moshi.JsonReader?,
        cacheKey: String?
    ): LottieResult<LottieComposition> {
        return fromJsonReaderSync(reader, cacheKey, true)
    }

    @androidx.annotation.WorkerThread
    fun fromJsonReaderSync(
        reader: com.airbnb.lottie.parser.moshi.JsonReader?, cacheKey: String?,
        close: Boolean
    ): LottieResult<LottieComposition> {
        return fromJsonReaderSyncInternal(
            reader,
            cacheKey,
            close
        )
    }

    private fun fromJsonReaderSyncInternal(
        reader: com.airbnb.lottie.parser.moshi.JsonReader, cacheKey: String?, close: Boolean
    ): LottieResult<LottieComposition> {
        return try {
            val cachedComposition: LottieComposition? =
                if (cacheKey == null) null else LottieCompositionCache.getInstance().get(cacheKey)
            if (cachedComposition != null) {
                return LottieResult<LottieComposition>(cachedComposition)
            }
            val composition: LottieComposition =
                LottieCompositionMoshiParser.parse(reader)
            if (cacheKey != null) {
                LottieCompositionCache.getInstance().put(cacheKey, composition)
            }
            LottieResult<LottieComposition>(composition)
        } catch (e: java.lang.Exception) {
            LottieResult<LottieComposition>(e)
        } finally {
            if (close) {
                com.airbnb.lottie.utils.Utils.closeQuietly(reader)
            }
        }
    }

    /**
     * In this overload, embedded fonts will NOT be parsed. If your zip file has custom fonts, use the overload
     * that takes Context as the first parameter.
     */
    fun fromZipStream(
        inputStream: java.util.zip.ZipInputStream?,
        cacheKey: String?
    ): LottieTask<LottieComposition> {
        return fromZipStream(null, inputStream, cacheKey)
    }

    /**
     * In this overload, embedded fonts will NOT be parsed. If your zip file has custom fonts, use the overload
     * that takes Context as the first parameter.
     */
    fun fromZipStream(
        inputStream: java.util.zip.ZipInputStream?,
        cacheKey: String?,
        close: Boolean
    ): LottieTask<LottieComposition> {
        return fromZipStream(
            null,
            inputStream,
            cacheKey,
            close
        )
    }

    /**
     * @see .fromZipStreamSync
     */
    fun fromZipStream(
        context: android.content.Context?,
        inputStream: java.util.zip.ZipInputStream?,
        cacheKey: String?
    ): LottieTask<LottieComposition> {
        return cache(cacheKey,
            java.util.concurrent.Callable<LottieResult<LottieComposition>> {
                fromZipStreamSync(
                    context,
                    inputStream,
                    cacheKey
                )
            },
            java.lang.Runnable { com.airbnb.lottie.utils.Utils.closeQuietly(inputStream) })
    }

    /**
     * @see .fromZipStreamSync
     */
    fun fromZipStream(
        context: android.content.Context?, inputStream: java.util.zip.ZipInputStream?,
        cacheKey: String?, close: Boolean
    ): LottieTask<LottieComposition> {
        return cache(cacheKey,
            java.util.concurrent.Callable<LottieResult<LottieComposition>> {
                fromZipStreamSync(
                    context,
                    inputStream,
                    cacheKey
                )
            }, if (close) java.lang.Runnable {
                com.airbnb.lottie.utils.Utils.closeQuietly(
                    inputStream
                )
            } else null)
    }
    /**
     * Parses a zip input stream into a Lottie composition.
     * Your zip file should just be a folder with your json file and images zipped together.
     * It will automatically store and configure any images inside the animation if they exist.
     *
     *
     * In this overload, embedded fonts will NOT be parsed. If your zip file has custom fonts, use the overload
     * that takes Context as the first parameter.
     */
    /**
     * Parses a zip input stream into a Lottie composition.
     * Your zip file should just be a folder with your json file and images zipped together.
     * It will automatically store and configure any images inside the animation if they exist.
     *
     *
     * In this overload, embedded fonts will NOT be parsed. If your zip file has custom fonts, use the overload
     * that takes Context as the first parameter.
     *
     *
     * The ZipInputStream will be automatically closed at the end. If you would like to keep it open, use the overload
     * with a close parameter and pass in false.
     */
    @JvmOverloads
    fun fromZipStreamSync(
        inputStream: java.util.zip.ZipInputStream?,
        cacheKey: String?,
        close: Boolean = true
    ): LottieResult<LottieComposition> {
        return fromZipStreamSync(
            null,
            inputStream,
            cacheKey,
            close
        )
    }

    /**
     * Parses a zip input stream into a Lottie composition.
     * Your zip file should just be a folder with your json file and images zipped together.
     * It will automatically store and configure any images inside the animation if they exist.
     *
     *
     * The ZipInputStream will be automatically closed at the end. If you would like to keep it open, use the overload
     * with a close parameter and pass in false.
     *
     * @param context is optional and only needed if your zip file contains ttf or otf fonts. If yours doesn't, you may pass null.
     * Embedded fonts may be .ttf or .otf files, can be in subdirectories, but must have the same name as the
     * font family (fFamily) in your animation file.
     */
    @androidx.annotation.WorkerThread
    fun fromZipStreamSync(
        context: android.content.Context?,
        inputStream: java.util.zip.ZipInputStream?,
        cacheKey: String?
    ): LottieResult<LottieComposition> {
        return fromZipStreamSync(
            context,
            inputStream,
            cacheKey,
            true
        )
    }

    /**
     * Parses a zip input stream into a Lottie composition.
     * Your zip file should just be a folder with your json file and images zipped together.
     * It will automatically store and configure any images inside the animation if they exist.
     *
     * @param context is optional and only needed if your zip file contains ttf or otf fonts. If yours doesn't, you may pass null.
     * Embedded fonts may be .ttf or .otf files, can be in subdirectories, but must have the same name as the
     * font family (fFamily) in your animation file.
     */
    @androidx.annotation.WorkerThread
    fun fromZipStreamSync(
        context: android.content.Context?, inputStream: java.util.zip.ZipInputStream?,
        cacheKey: String?, close: Boolean
    ): LottieResult<LottieComposition> {
        return try {
            fromZipStreamSyncInternal(
                context,
                inputStream,
                cacheKey
            )
        } finally {
            if (close) {
                com.airbnb.lottie.utils.Utils.closeQuietly(inputStream)
            }
        }
    }

    @androidx.annotation.WorkerThread
    private fun fromZipStreamSyncInternal(
        context: android.content.Context,
        inputStream: java.util.zip.ZipInputStream,
        cacheKey: String?
    ): LottieResult<LottieComposition>? {
        var composition: LottieComposition? = null
        val images: MutableMap<String, android.graphics.Bitmap> =
            HashMap<String, android.graphics.Bitmap>()
        val fonts: MutableMap<String, android.graphics.Typeface> =
            HashMap<String, android.graphics.Typeface>()
        try {
            val cachedComposition: LottieComposition? =
                if (cacheKey == null) null else LottieCompositionCache.getInstance().get(cacheKey)
            if (cachedComposition != null) {
                return LottieResult<LottieComposition>(cachedComposition)
            }
            var entry: java.util.zip.ZipEntry = inputStream.getNextEntry()
            while (entry != null) {
                val entryName: String = entry.getName()
                if (entryName.contains("__MACOSX")) {
                    inputStream.closeEntry()
                } else if (entry.getName()
                        .equals("manifest.json", ignoreCase = true)
                ) { //ignore .lottie manifest
                    inputStream.closeEntry()
                } else if (entry.getName().contains(".json")) {
                    val reader: com.airbnb.lottie.parser.moshi.JsonReader =
                        com.airbnb.lottie.parser.moshi.JsonReader.of(buffer(source(inputStream)))
                    composition =
                        fromJsonReaderSyncInternal(
                            reader,
                            null,
                            false
                        ).getValue()
                } else if (entryName.contains(".png") || entryName.contains(".webp") || entryName.contains(
                        ".jpg"
                    ) || entryName.contains(".jpeg")
                ) {
                    val splitName = entryName.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val name = splitName[splitName.size - 1]
                    images[name] = BitmapFactory.decodeStream(inputStream)
                } else if (entryName.contains(".ttf") || entryName.contains(".otf")) {
                    val splitName = entryName.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val fileName = splitName[splitName.size - 1]
                    val fontFamily = fileName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[0]
                    val tempFile: java.io.File = java.io.File(context.getCacheDir(), fileName)
                    val fos: java.io.FileOutputStream = java.io.FileOutputStream(tempFile)
                    try {
                        java.io.FileOutputStream(tempFile).use { output ->
                            val buffer = ByteArray(4 * 1024)
                            var read: Int
                            while (inputStream.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                            }
                            output.flush()
                        }
                    } catch (e: Throwable) {
                        com.airbnb.lottie.utils.Logger.warning(
                            "Unable to save font $fontFamily to the temporary file: $fileName. ",
                            e
                        )
                    }
                    val typeface: android.graphics.Typeface =
                        android.graphics.Typeface.createFromFile(tempFile)
                    if (!tempFile.delete()) {
                        com.airbnb.lottie.utils.Logger.warning("Failed to delete temp font file " + tempFile.getAbsolutePath() + ".")
                    }
                    fonts[fontFamily] = typeface
                } else {
                    inputStream.closeEntry()
                }
                entry = inputStream.getNextEntry()
            }
        } catch (e: java.io.IOException) {
            return LottieResult<LottieComposition>(e)
        }
        if (composition == null) {
            return LottieResult<LottieComposition>(
                java.lang.IllegalArgumentException(
                    "Unable to parse composition"
                )
            )
        }
        for ((key, value) in images) {
            val imageAsset: com.airbnb.lottie.LottieImageAsset =
                findImageAssetForFileName(
                    composition,
                    key
                )
            if (imageAsset != null) {
                imageAsset.setBitmap(
                    com.airbnb.lottie.utils.Utils.resizeBitmapIfNeeded(
                        value,
                        imageAsset.getWidth(),
                        imageAsset.getHeight()
                    )
                )
            }
        }
        for ((key, value) in fonts) {
            var found = false
            for (font in composition.getFonts().values) {
                if (font.getFamily() == key) {
                    found = true
                    font.setTypeface(value)
                }
            }
            if (!found) {
                com.airbnb.lottie.utils.Logger.warning("Parsed font for $key however it was not found in the animation.")
            }
        }
        if (images.isEmpty()) {
            for ((_, value) in composition.getImages().entries) {
                val asset: com.airbnb.lottie.LottieImageAsset = value ?: return null
                val filename: String = asset.getFileName()
                val opts: android.graphics.BitmapFactory.Options =
                    android.graphics.BitmapFactory.Options()
                opts.inScaled = true
                opts.inDensity = 160
                if (filename.startsWith("data:") && filename.indexOf("base64,") > 0) {
                    // Contents look like a base64 data URI, with the format data:image/png;base64,<data>.
                    val data: ByteArray
                    data = try {
                        android.util.Base64.decode(
                            filename.substring(filename.indexOf(',') + 1),
                            android.util.Base64.DEFAULT
                        )
                    } catch (e: java.lang.IllegalArgumentException) {
                        com.airbnb.lottie.utils.Logger.warning(
                            "data URL did not have correct base64 format.",
                            e
                        )
                        return null
                    }
                    asset.setBitmap(BitmapFactory.decodeByteArray(data, 0, data.size, opts))
                }
            }
        }
        if (cacheKey != null) {
            LottieCompositionCache.getInstance().put(cacheKey, composition)
        }
        return LottieResult<LottieComposition>(composition)
    }

    /**
     * Check if a given InputStream points to a .zip compressed file
     */
    private fun isZipCompressed(inputSource: BufferedSource): Boolean {
        return matchesMagicBytes(
            inputSource,
            ZIP_MAGIC
        )
    }

    /**
     * Check if a given InputStream points to a .gzip compressed file
     */
    private fun isGzipCompressed(inputSource: BufferedSource): Boolean {
        return matchesMagicBytes(
            inputSource,
            GZIP_MAGIC
        )
    }

    private fun matchesMagicBytes(inputSource: BufferedSource, magic: ByteArray): Boolean {
        return try {
            val peek: BufferedSource = inputSource.peek()
            for (b in magic) {
                if (peek.readByte() !== b) {
                    return false
                }
            }
            peek.close()
            true
        } catch (e: Exception) {
            com.airbnb.lottie.utils.Logger.error("Failed to check zip file header", e)
            false
        }
    }

    private fun findImageAssetForFileName(
        composition: LottieComposition,
        fileName: String
    ): com.airbnb.lottie.LottieImageAsset? {
        for (asset in composition.getImages().values) {
            if (asset.getFileName() == fileName) {
                return asset
            }
        }
        return null
    }

    /**
     * First, check to see if there are any in-progress tasks associated with the cache key and return it if there is.
     * If not, create a new task for the callable.
     * Then, add the new task to the task cache and set up listeners so it gets cleared when done.
     */
    private fun cache(
        cacheKey: String?,
        callable: java.util.concurrent.Callable<LottieResult<LottieComposition>>,
        onCached: java.lang.Runnable?
    ): LottieTask<LottieComposition>? {
        var task: LottieTask<LottieComposition>? = null
        val cachedComposition: LottieComposition? =
            if (cacheKey == null) null else LottieCompositionCache.getInstance().get(cacheKey)
        if (cachedComposition != null) {
            task = LottieTask<LottieComposition>(cachedComposition)
        }
        if (cacheKey != null && taskCache.containsKey(
                cacheKey
            )
        ) {
            task = taskCache.get(cacheKey)
        }
        if (task != null) {
            if (onCached != null) {
                onCached.run()
            }
            return task
        }
        task = LottieTask<LottieComposition>(callable)
        if (cacheKey != null) {
            val resultAlreadyCalled: java.util.concurrent.atomic.AtomicBoolean =
                java.util.concurrent.atomic.AtomicBoolean(false)
            task.addListener(LottieListener<LottieComposition> { result: LottieComposition? ->
                taskCache.remove(cacheKey)
                resultAlreadyCalled.set(true)
                if (taskCache.size == 0) {
                    notifyTaskCacheIdleListeners(true)
                }
            })
            task.addFailureListener(LottieListener<Throwable> { result: Throwable? ->
                taskCache.remove(cacheKey)
                resultAlreadyCalled.set(true)
                if (taskCache.size == 0) {
                    notifyTaskCacheIdleListeners(true)
                }
            })
            // It is technically possible for the task to finish and for the listeners to get called
            // before this code runs. If this happens, the task will be put in taskCache but never removed.
            // This would require this thread to be sleeping at exactly this point in the code
            // for long enough for the task to finish and call the listeners. Unlikely but not impossible.
            if (!resultAlreadyCalled.get()) {
                taskCache.put(cacheKey, task)
                if (taskCache.size == 1) {
                    notifyTaskCacheIdleListeners(false)
                }
            }
        }
        return task
    }

    private fun notifyTaskCacheIdleListeners(idle: Boolean) {
        val listeners =
            ArrayList<LottieTaskIdleListener>(taskIdleListeners)
        for (i in listeners.indices) {
            listeners[i].onIdleChanged(idle)
        }
    }
}

package framework.animation

import kotlin.concurrent.Volatile

@RestrictTo(RestrictTo.Scope.LIBRARY)
object L {
    var DBG = false
    const val TAG = "LOTTIE"
    private const val traceEnabled = false
    private const val networkCacheEnabled = true
    var disablePathInterpolatorCache: Boolean
        get() = disablePathInterpolatorCache
        set(disablePathInterpolatorCache) {
            disablePathInterpolatorCache = disablePathInterpolatorCache
        }
    var defaultAsyncUpdates: AsyncUpdates
        get() = defaultAsyncUpdates
        set(asyncUpdates) {
            defaultAsyncUpdates = asyncUpdates
        }
    private val fetcher: LottieNetworkFetcher? = null
    private val cacheProvider: LottieNetworkCacheProvider? = null

    @Volatile
    private val networkFetcher: NetworkFetcher? = null

    @Volatile
    private val networkCache: com.airbnb.lottie.network.NetworkCache? = null
    private val lottieTrace: java.lang.ThreadLocal<LottieTrace>? = null
    fun setTraceEnabled(enabled: Boolean) {
        if (traceEnabled == enabled) {
            return
        }
        traceEnabled = enabled
        if (traceEnabled && lottieTrace == null) {
            lottieTrace = java.lang.ThreadLocal<LottieTrace>()
        }
    }

    fun setNetworkCacheEnabled(enabled: Boolean) {
        networkCacheEnabled = enabled
    }

    fun beginSection(section: String?) {
        if (!traceEnabled) {
            return
        }
        getTrace().beginSection(section)
    }

    fun endSection(section: String?): Float {
        return if (!traceEnabled) {
            0
        } else getTrace().endSection(section)
    }

    private val trace: LottieTrace?
        private get() {
            var trace: LottieTrace? = lottieTrace.get()
            if (trace == null) {
                trace = LottieTrace()
                lottieTrace.set(trace)
            }
            return trace
        }

    fun setFetcher(customFetcher: LottieNetworkFetcher?) {
        if (fetcher == null && customFetcher == null || fetcher != null && fetcher == customFetcher) {
            return
        }
        fetcher = customFetcher
        networkFetcher = null
    }

    fun setCacheProvider(customProvider: LottieNetworkCacheProvider?) {
        if (cacheProvider == null && customProvider == null || cacheProvider != null && cacheProvider == customProvider) {
            return
        }
        cacheProvider = customProvider
        networkCache = null
    }

    fun networkFetcher(context: android.content.Context): NetworkFetcher {
        var local: NetworkFetcher = networkFetcher
        if (local == null) {
            synchronized(NetworkFetcher::class.java) {
                local = networkFetcher
                if (local == null) {
                    local = NetworkFetcher(
                        networkCache(context),
                        if (fetcher != null) fetcher else DefaultLottieNetworkFetcher()
                    )
                    networkFetcher = local
                }
            }
        }
        return local
    }

    fun networkCache(context: android.content.Context): com.airbnb.lottie.network.NetworkCache? {
        if (!networkCacheEnabled) {
            return null
        }
        val appContext: android.content.Context = context.getApplicationContext()
        var local: com.airbnb.lottie.network.NetworkCache = networkCache
        if (local == null) {
            synchronized(com.airbnb.lottie.network.NetworkCache::class.java) {
                local = networkCache
                if (local == null) {
                    local =
                        com.airbnb.lottie.network.NetworkCache(if (cacheProvider != null) cacheProvider else LottieNetworkCacheProvider {
                            java.io.File(
                                appContext.getCacheDir(),
                                "lottie_network_cache"
                            )
                        })
                    networkCache = local
                }
            }
        }
        return local
    }
}

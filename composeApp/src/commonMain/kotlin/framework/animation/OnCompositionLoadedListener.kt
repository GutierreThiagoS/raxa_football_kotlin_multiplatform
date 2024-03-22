package framework.animation

/**
 * @see LottieCompositionFactory
 * @see LottieResult
 */
interface OnCompositionLoadedListener {
    /**
     * Composition will be null if there was an error loading it. Check logcat for more details.
     */
    fun onCompositionLoaded(composition: LottieComposition?)
}
package framework.animation

/**
 * Receive a result with either the value or exception for a {@link LottieTask}
 */

interface LottieListener<T> {
    fun onResult(result: T)
}

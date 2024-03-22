package framework.animation

import framework.animation.annotation.RestrictTo
import framework.animation.utils.Callable
import kotlin.concurrent.Volatile
import kotlin.jvm.Synchronized

/**
 * Helper to run asynchronous tasks with a result.
 * Results can be obtained with [.addListener].
 * Failures can be obtained with [.addFailureListener].
 *
 *
 * A task will produce a single result or a single failure.
 */
class LottieTask<T> {
    /* Preserve add order. */
    private val successListeners: MutableSet<LottieListener<T>> =
        LinkedHashSet(1)
    private val failureListeners: MutableSet<LottieListener<Throwable>> =
        LinkedHashSet(1)
    private val handler: android.os.Handler = android.os.Handler(Looper.getMainLooper())

    @Volatile
    private var result: LottieResult<T>? = null

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    constructor(runnable: Callable<LottieResult<T>?>) : this(runnable, false)
    constructor(result: T) {
        setResult(LottieResult<T>(result))
    }

    /**
     * runNow is only used for testing.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    internal constructor(
        runnable: Callable<LottieResult<T>?>,
        runNow: Boolean
    ) {
        if (runNow) {
            try {
                setResult(runnable.call())
            } catch (e: Throwable) {
                setResult(LottieResult<T>(e))
            }
        } else {
            EXECUTOR.execute(
                LottieTask.LottieFutureTask(
                    runnable
                )
            )
        }
    }

    private fun setResult(result: LottieResult<T>?) {
        if (this.result != null) {
            throw IllegalStateException("A task may only be set once.")
        }
        this.result = result
        notifyListeners()
    }

    /**
     * Add a task listener. If the task has completed, the listener will be called synchronously.
     *
     * @return the task for call chaining.
     */
    @Synchronized
    fun addListener(listener: LottieListener<T>): LottieTask<T> {
        val result: LottieResult<T>? = result
        if (result?.value != null) {
            listener.onResult(result.value)
        }
        successListeners.add(listener)
        return this
    }

    /**
     * Remove a given task listener. The task will continue to execute so you can re-add
     * a listener if necessary.
     *
     * @return the task for call chaining.
     */
    @Synchronized
    fun removeListener(listener: LottieListener<T>): LottieTask<T> {
        successListeners.remove(listener)
        return this
    }

    /**
     * Add a task failure listener. This will only be called in the even that an exception
     * occurs. If an exception has already occurred, the listener will be called immediately.
     *
     * @return the task for call chaining.
     */
    @Synchronized
    fun addFailureListener(listener: LottieListener<Throwable>): LottieTask<T> {
        val result: LottieResult<T>? = result
        if (result?.exception != null) {
            listener.onResult(result.exception)
        }
        failureListeners.add(listener)
        return this
    }

    /**
     * Remove a given task failure listener. The task will continue to execute so you can re-add
     * a listener if necessary.
     *
     * @return the task for call chaining.
     */
    @Synchronized
    fun removeFailureListener(listener: LottieListener<Throwable>): LottieTask<T> {
        failureListeners.remove(listener)
        return this
    }

    fun getResult(): LottieResult<T>? {
        return result
    }

    private fun notifyListeners() {
        // Listeners should be called on the main thread.
        handler.post {

            // Local reference in case it gets set on a background thread.
            val result: LottieResult<T> = result ?: return@post
            if (result.value != null) {
                notifySuccessListeners(result.value)
            } else {
                notifyFailureListeners(result.exception ?: Exception())
            }
        }
    }

    @Synchronized
    private fun notifySuccessListeners(value: T) {
        // Allows listeners to remove themselves in onResult.
        // Otherwise we risk ConcurrentModificationException.
        val listenersCopy: List<LottieListener<T>> =
            ArrayList(successListeners)
        for (l in listenersCopy) {
            l.onResult(value)
        }
    }

    @Synchronized
    private fun notifyFailureListeners(e: Throwable) {
        // Allows listeners to remove themselves in onResult.
        // Otherwise we risk ConcurrentModificationException.
        val listenersCopy: List<LottieListener<Throwable>> =
            ArrayList(failureListeners)
        if (listenersCopy.isEmpty()) {
            com.airbnb.lottie.utils.Logger.warning(
                "Lottie encountered an error but no failure listener was added:",
                e
            )
            return
        }
        for (l in listenersCopy) {
            l.onResult(e)
        }
    }

    private inner class LottieFutureTask constructor(callable: Callable<LottieResult<T>?>?) :
        java.util.concurrent.FutureTask<LottieResult<T>?>(callable) {
        protected override fun done() {
            if (isCancelled) {
                // We don't need to notify and listeners if the task is cancelled.
                return
            }
            try {
                setResult(get())
            } catch (e: Exception) {
                setResult(LottieResult<T>(e))
            }
        }
    }

    companion object {
        /**
         * Set this to change the executor that LottieTasks are run on. This will be the executor that composition parsing and url
         * fetching happens on.
         *
         *
         * You may change this to run deserialization synchronously for testing.
         */
        var EXECUTOR: java.util.concurrent.Executor =
            java.util.concurrent.Executors.newCachedThreadPool()
    }
}

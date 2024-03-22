package framework.animation.utils


/**
 * A [Future] that is [Runnable]. Successful execution of
 * the `run` method causes completion of the `Future`
 * and allows access to its results.
 * @see FutureTask
 *
 * @see Executor
 *
 * @since 1.6
 * @author Doug Lea
 * @param <V> The result type returned by this Future's `get` method
</V> */
interface RunnableFuture<V> : Runnable, Future<V> {
    /**
     * Sets this Future to the result of its computation
     * unless it has been cancelled.
     */
    override fun run()
}

package framework.animation.utils

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
/**
 * A `Future` represents the result of an asynchronous
 * computation.  Methods are provided to check if the computation is
 * complete, to wait for its completion, and to retrieve the result of
 * the computation.  The result can only be retrieved using method
 * `get` when the computation has completed, blocking if
 * necessary until it is ready.  Cancellation is performed by the
 * `cancel` method.  Additional methods are provided to
 * determine if the task completed normally or was cancelled. Once a
 * computation has completed, the computation cannot be cancelled.
 * If you would like to use a `Future` for the sake
 * of cancellability but not provide a usable result, you can
 * declare types of the form `Future<?>` and
 * return `null` as a result of the underlying task.
 *
 *
 * **Sample Usage** (Note that the following classes are all
 * made-up.)
 *
 * <pre> `interface ArchiveSearcher { String search(String target); }
 * class App {
 * ExecutorService executor = ...;
 * ArchiveSearcher searcher = ...;
 * void showSearch(String target) throws InterruptedException {
 * Callable<String> task = () -> searcher.search(target);
 * Future<String> future = executor.submit(task);
 * displayOtherThings(); // do other things while searching
 * try {
 * displayText(future.get()); // use future
 * } catch (ExecutionException ex) { cleanup(); return; }
 * }
 * }`</pre>
 *
 * The [FutureTask] class is an implementation of `Future` that
 * implements `Runnable`, and so may be executed by an `Executor`.
 * For example, the above construction with `submit` could be replaced by:
 * <pre> `FutureTask<String> future = new FutureTask<>(task);
 * executor.execute(future);`</pre>
 *
 *
 * Memory consistency effects: Actions taken by the asynchronous computation
 * [ *happen-before*](package-summary.html#MemoryVisibility)
 * actions following the corresponding `Future.get()` in another thread.
 *
 * @see FutureTask
 *
 * @see Executor
 *
 * @since 1.5
 * @author Doug Lea
 * @param <V> The result type returned by this Future's `get` method
</V> */
interface Future<V> {
    /**
     * Attempts to cancel execution of this task.  This method has no
     * effect if the task is already completed or cancelled, or could
     * not be cancelled for some other reason.  Otherwise, if this
     * task has not started when `cancel` is called, this task
     * should never run.  If the task has already started, then the
     * `mayInterruptIfRunning` parameter determines whether the
     * thread executing this task (when known by the implementation)
     * is interrupted in an attempt to stop the task.
     *
     *
     * The return value from this method does not necessarily
     * indicate whether the task is now cancelled; use [ ][.isCancelled].
     *
     * @param mayInterruptIfRunning `true` if the thread
     * executing this task should be interrupted (if the thread is
     * known to the implementation); otherwise, in-progress tasks are
     * allowed to complete
     * @return `false` if the task could not be cancelled,
     * typically because it has already completed; `true`
     * otherwise. If two or more threads cause a task to be cancelled,
     * then at least one of them returns `true`. Implementations
     * may provide stronger guarantees.
     */
    fun cancel(mayInterruptIfRunning: Boolean): Boolean

    /**
     * Returns `true` if this task was cancelled before it completed
     * normally.
     *
     * @return `true` if this task was cancelled before it completed
     */
    val isCancelled: Boolean

    /**
     * Returns `true` if this task completed.
     *
     * Completion may be due to normal termination, an exception, or
     * cancellation -- in all of these cases, this method will return
     * `true`.
     *
     * @return `true` if this task completed
     */
    val isDone: Boolean

    /**
     * Waits if necessary for the computation to complete, and then
     * retrieves its result.
     *
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if the computation threw an
     * exception
     * @throws InterruptedException if the current thread was interrupted
     * while waiting
     */
//    @Throws(InterruptedException::class, ExecutionException::class)
    fun get(): V

    /**
     * Waits if necessary for at most the given time for the computation
     * to complete, and then retrieves its result, if available.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if the computation threw an
     * exception
     * @throws InterruptedException if the current thread was interrupted
     * while waiting
     * @throws TimeoutException if the wait timed out
     */
    /*@Throws(
        java.lang.InterruptedException::class,
        java.util.concurrent.ExecutionException::class,
        java.util.concurrent.TimeoutException::class
    )*/
    operator fun get(timeout: Long, unit: TimeUnit?): V
}

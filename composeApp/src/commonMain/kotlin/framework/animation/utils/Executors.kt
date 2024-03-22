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
package java.util.concurrent
import dalvik.annotation.optimization.ReachabilitySensitive

// BEGIN android-note
// removed security manager docs
// END android-note
/**
 * Factory and utility methods for [Executor], [ ], [ScheduledExecutorService], [ ], and [Callable] classes defined in this
 * package. This class supports the following kinds of methods:
 *
 *
 *  * Methods that create and return an [ExecutorService]
 * set up with commonly useful configuration settings.
 *  * Methods that create and return a [ScheduledExecutorService]
 * set up with commonly useful configuration settings.
 *  * Methods that create and return a "wrapped" ExecutorService, that
 * disables reconfiguration by making implementation-specific methods
 * inaccessible.
 *  * Methods that create and return a [ThreadFactory]
 * that sets newly created threads to a known state.
 *  * Methods that create and return a [Callable]
 * out of other closure-like forms, so they can be used
 * in execution methods requiring `Callable`.
 *
 *
 * @since 1.5
 * @author Doug Lea
 */
object Executors {
    /**
     * Creates a thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue.  At any point, at most
     * `nThreads` threads will be active processing tasks.
     * If additional tasks are submitted when all threads are active,
     * they will wait in the queue until a thread is available.
     * If any thread terminates due to a failure during execution
     * prior to shutdown, a new one will take its place if needed to
     * execute subsequent tasks.  The threads in the pool will exist
     * until it is explicitly [shutdown][ExecutorService.shutdown].
     *
     * @param nThreads the number of threads in the pool
     * @return the newly created thread pool
     * @throws IllegalArgumentException if `nThreads <= 0`
     */
    fun newFixedThreadPool(nThreads: Int): java.util.concurrent.ExecutorService {
        return java.util.concurrent.ThreadPoolExecutor(
            nThreads, nThreads,
            0L, TimeUnit.MILLISECONDS,
            java.util.concurrent.LinkedBlockingQueue<Runnable>()
        )
    }

    /**
     * Creates a thread pool that maintains enough threads to support
     * the given parallelism level, and may use multiple queues to
     * reduce contention. The parallelism level corresponds to the
     * maximum number of threads actively engaged in, or available to
     * engage in, task processing. The actual number of threads may
     * grow and shrink dynamically. A work-stealing pool makes no
     * guarantees about the order in which submitted tasks are
     * executed.
     *
     * @param parallelism the targeted parallelism level
     * @return the newly created thread pool
     * @throws IllegalArgumentException if `parallelism <= 0`
     * @since 1.8
     */
    fun newWorkStealingPool(parallelism: Int): java.util.concurrent.ExecutorService {
        return java.util.concurrent.ForkJoinPool(
            parallelism,
            java.util.concurrent.ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            null, true
        )
    }

    /**
     * Creates a work-stealing thread pool using the number of
     * [available processors][Runtime.availableProcessors]
     * as its target parallelism level.
     *
     * @return the newly created thread pool
     * @see .newWorkStealingPool
     * @since 1.8
     */
    fun newWorkStealingPool(): java.util.concurrent.ExecutorService {
        return java.util.concurrent.ForkJoinPool(
            java.lang.Runtime.getRuntime().availableProcessors(),
            java.util.concurrent.ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            null, true
        )
    }

    /**
     * Creates a thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue, using the provided
     * ThreadFactory to create new threads when needed.  At any point,
     * at most `nThreads` threads will be active processing
     * tasks.  If additional tasks are submitted when all threads are
     * active, they will wait in the queue until a thread is
     * available.  If any thread terminates due to a failure during
     * execution prior to shutdown, a new one will take its place if
     * needed to execute subsequent tasks.  The threads in the pool will
     * exist until it is explicitly [ shutdown][ExecutorService.shutdown].
     *
     * @param nThreads the number of threads in the pool
     * @param threadFactory the factory to use when creating new threads
     * @return the newly created thread pool
     * @throws NullPointerException if threadFactory is null
     * @throws IllegalArgumentException if `nThreads <= 0`
     */
    fun newFixedThreadPool(
        nThreads: Int,
        threadFactory: java.util.concurrent.ThreadFactory?
    ): java.util.concurrent.ExecutorService {
        return java.util.concurrent.ThreadPoolExecutor(
            nThreads, nThreads,
            0L, java.util.concurrent.TimeUnit.MILLISECONDS,
            java.util.concurrent.LinkedBlockingQueue<java.lang.Runnable>(),
            threadFactory
        )
    }

    /**
     * Creates an Executor that uses a single worker thread operating
     * off an unbounded queue. (Note however that if this single
     * thread terminates due to a failure during execution prior to
     * shutdown, a new one will take its place if needed to execute
     * subsequent tasks.)  Tasks are guaranteed to execute
     * sequentially, and no more than one task will be active at any
     * given time. Unlike the otherwise equivalent
     * `newFixedThreadPool(1)` the returned executor is
     * guaranteed not to be reconfigurable to use additional threads.
     *
     * @return the newly created single-threaded Executor
     */
    fun newSingleThreadExecutor(): java.util.concurrent.ExecutorService {
        return java.util.concurrent.Executors.FinalizableDelegatedExecutorService(
            java.util.concurrent.ThreadPoolExecutor(
                1, 1,
                0L, java.util.concurrent.TimeUnit.MILLISECONDS,
                java.util.concurrent.LinkedBlockingQueue<java.lang.Runnable>()
            )
        )
    }

    /**
     * Creates an Executor that uses a single worker thread operating
     * off an unbounded queue, and uses the provided ThreadFactory to
     * create a new thread when needed. Unlike the otherwise
     * equivalent `newFixedThreadPool(1, threadFactory)` the
     * returned executor is guaranteed not to be reconfigurable to use
     * additional threads.
     *
     * @param threadFactory the factory to use when creating new threads
     * @return the newly created single-threaded Executor
     * @throws NullPointerException if threadFactory is null
     */
    fun newSingleThreadExecutor(threadFactory: java.util.concurrent.ThreadFactory?): java.util.concurrent.ExecutorService {
        return java.util.concurrent.Executors.FinalizableDelegatedExecutorService(
            java.util.concurrent.ThreadPoolExecutor(
                1, 1,
                0L, java.util.concurrent.TimeUnit.MILLISECONDS,
                java.util.concurrent.LinkedBlockingQueue<java.lang.Runnable>(),
                threadFactory
            )
        )
    }

    /**
     * Creates a thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available.  These pools will typically improve the performance
     * of programs that execute many short-lived asynchronous tasks.
     * Calls to `execute` will reuse previously constructed
     * threads if available. If no existing thread is available, a new
     * thread will be created and added to the pool. Threads that have
     * not been used for sixty seconds are terminated and removed from
     * the cache. Thus, a pool that remains idle for long enough will
     * not consume any resources. Note that pools with similar
     * properties but different details (for example, timeout parameters)
     * may be created using [ThreadPoolExecutor] constructors.
     *
     * @return the newly created thread pool
     */
    fun newCachedThreadPool(): java.util.concurrent.ExecutorService {
        return java.util.concurrent.ThreadPoolExecutor(
            0, Int.MAX_VALUE,
            60L, java.util.concurrent.TimeUnit.SECONDS,
            java.util.concurrent.SynchronousQueue<java.lang.Runnable>()
        )
    }

    /**
     * Creates a thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available, and uses the provided
     * ThreadFactory to create new threads when needed.
     *
     * @param threadFactory the factory to use when creating new threads
     * @return the newly created thread pool
     * @throws NullPointerException if threadFactory is null
     */
    fun newCachedThreadPool(threadFactory: java.util.concurrent.ThreadFactory?): java.util.concurrent.ExecutorService {
        return java.util.concurrent.ThreadPoolExecutor(
            0, Int.MAX_VALUE,
            60L, java.util.concurrent.TimeUnit.SECONDS,
            java.util.concurrent.SynchronousQueue<java.lang.Runnable>(),
            threadFactory
        )
    }

    /**
     * Creates a single-threaded executor that can schedule commands
     * to run after a given delay, or to execute periodically.
     * (Note however that if this single
     * thread terminates due to a failure during execution prior to
     * shutdown, a new one will take its place if needed to execute
     * subsequent tasks.)  Tasks are guaranteed to execute
     * sequentially, and no more than one task will be active at any
     * given time. Unlike the otherwise equivalent
     * `newScheduledThreadPool(1)` the returned executor is
     * guaranteed not to be reconfigurable to use additional threads.
     *
     * @return the newly created scheduled executor
     */
    fun newSingleThreadScheduledExecutor(): java.util.concurrent.ScheduledExecutorService {
        return java.util.concurrent.Executors.DelegatedScheduledExecutorService(
            java.util.concurrent.ScheduledThreadPoolExecutor(
                1
            )
        )
    }

    /**
     * Creates a single-threaded executor that can schedule commands
     * to run after a given delay, or to execute periodically.  (Note
     * however that if this single thread terminates due to a failure
     * during execution prior to shutdown, a new one will take its
     * place if needed to execute subsequent tasks.)  Tasks are
     * guaranteed to execute sequentially, and no more than one task
     * will be active at any given time. Unlike the otherwise
     * equivalent `newScheduledThreadPool(1, threadFactory)`
     * the returned executor is guaranteed not to be reconfigurable to
     * use additional threads.
     *
     * @param threadFactory the factory to use when creating new threads
     * @return the newly created scheduled executor
     * @throws NullPointerException if threadFactory is null
     */
    fun newSingleThreadScheduledExecutor(threadFactory: java.util.concurrent.ThreadFactory?): java.util.concurrent.ScheduledExecutorService {
        return java.util.concurrent.Executors.DelegatedScheduledExecutorService(
            java.util.concurrent.ScheduledThreadPoolExecutor(
                1,
                threadFactory
            )
        )
    }

    /**
     * Creates a thread pool that can schedule commands to run after a
     * given delay, or to execute periodically.
     * @param corePoolSize the number of threads to keep in the pool,
     * even if they are idle
     * @return the newly created scheduled thread pool
     * @throws IllegalArgumentException if `corePoolSize < 0`
     */
    fun newScheduledThreadPool(corePoolSize: Int): java.util.concurrent.ScheduledExecutorService {
        return java.util.concurrent.ScheduledThreadPoolExecutor(corePoolSize)
    }

    /**
     * Creates a thread pool that can schedule commands to run after a
     * given delay, or to execute periodically.
     * @param corePoolSize the number of threads to keep in the pool,
     * even if they are idle
     * @param threadFactory the factory to use when the executor
     * creates a new thread
     * @return the newly created scheduled thread pool
     * @throws IllegalArgumentException if `corePoolSize < 0`
     * @throws NullPointerException if threadFactory is null
     */
    fun newScheduledThreadPool(
        corePoolSize: Int, threadFactory: java.util.concurrent.ThreadFactory?
    ): java.util.concurrent.ScheduledExecutorService {
        return java.util.concurrent.ScheduledThreadPoolExecutor(corePoolSize, threadFactory)
    }

    /**
     * Returns an object that delegates all defined [ ] methods to the given executor, but not any
     * other methods that might otherwise be accessible using
     * casts. This provides a way to safely "freeze" configuration and
     * disallow tuning of a given concrete implementation.
     * @param executor the underlying implementation
     * @return an `ExecutorService` instance
     * @throws NullPointerException if executor null
     */
    fun unconfigurableExecutorService(executor: java.util.concurrent.ExecutorService?): java.util.concurrent.ExecutorService {
        if (executor == null) throw java.lang.NullPointerException()
        return java.util.concurrent.Executors.DelegatedExecutorService(executor)
    }

    /**
     * Returns an object that delegates all defined [ ] methods to the given executor, but
     * not any other methods that might otherwise be accessible using
     * casts. This provides a way to safely "freeze" configuration and
     * disallow tuning of a given concrete implementation.
     * @param executor the underlying implementation
     * @return a `ScheduledExecutorService` instance
     * @throws NullPointerException if executor null
     */
    fun unconfigurableScheduledExecutorService(executor: java.util.concurrent.ScheduledExecutorService?): java.util.concurrent.ScheduledExecutorService {
        if (executor == null) throw java.lang.NullPointerException()
        return java.util.concurrent.Executors.DelegatedScheduledExecutorService(executor)
    }
    // Android-changed: Removed references to SecurityManager from javadoc.
    /**
     * Returns a default thread factory used to create new threads.
     * This factory creates all new threads used by an Executor in the
     * same [ThreadGroup]. Each new
     * thread is created as a non-daemon thread with priority set to
     * the smaller of `Thread.NORM_PRIORITY` and the maximum
     * priority permitted in the thread group.  New threads have names
     * accessible via [Thread.getName] of
     * *pool-N-thread-M*, where *N* is the sequence
     * number of this factory, and *M* is the sequence number
     * of the thread created by this factory.
     * @return a thread factory
     */
    fun defaultThreadFactory(): java.util.concurrent.ThreadFactory {
        return java.util.concurrent.Executors.DefaultThreadFactory()
    }
    // Android-changed: Dropped documentation for legacy security code.
    /**
     * Legacy security code; do not use.
     *
     */
    @Deprecated(
        """This method is only useful in conjunction with
            {@linkplain SecurityManager the Security Manager}, which is
            deprecated and subject to removal in a future release.
            Consequently, this method is also deprecated and subject to
            removal. There is no replacement for the Security Manager or this
            method."""
    )
    fun privilegedThreadFactory(): java.util.concurrent.ThreadFactory {
        return java.util.concurrent.Executors.PrivilegedThreadFactory()
    }

    /**
     * Returns a [Callable] object that, when
     * called, runs the given task and returns the given result.  This
     * can be useful when applying methods requiring a
     * `Callable` to an otherwise resultless action.
     * @param task the task to run
     * @param result the result to return
     * @param <T> the type of the result
     * @return a callable object
     * @throws NullPointerException if task null
    </T> */
    fun <T> callable(task: java.lang.Runnable?, result: T): java.util.concurrent.Callable<T> {
        if (task == null) throw java.lang.NullPointerException()
        return java.util.concurrent.Executors.RunnableAdapter<T>(task, result)
    }

    /**
     * Returns a [Callable] object that, when
     * called, runs the given task and returns `null`.
     * @param task the task to run
     * @return a callable object
     * @throws NullPointerException if task null
     */
    fun callable(task: java.lang.Runnable?): java.util.concurrent.Callable<Any> {
        if (task == null) throw java.lang.NullPointerException()
        return java.util.concurrent.Executors.RunnableAdapter<Any>(task, null)
    }

    /**
     * Returns a [Callable] object that, when
     * called, runs the given privileged action and returns its result.
     * @param action the privileged action to run
     * @return a callable object
     * @throws NullPointerException if action null
     */
    fun callable(action: java.security.PrivilegedAction<*>?): java.util.concurrent.Callable<Any> {
        if (action == null) throw java.lang.NullPointerException()
        return object : java.util.concurrent.Callable<Any?>() {
            override fun call(): Any {
                return action.run()
            }
        }
    }

    /**
     * Returns a [Callable] object that, when
     * called, runs the given privileged exception action and returns
     * its result.
     * @param action the privileged exception action to run
     * @return a callable object
     * @throws NullPointerException if action null
     */
    fun callable(action: java.security.PrivilegedExceptionAction<*>?): java.util.concurrent.Callable<Any> {
        if (action == null) throw java.lang.NullPointerException()
        return object : java.util.concurrent.Callable<Any?>() {
            @Throws(java.lang.Exception::class)
            override fun call(): Any {
                return action.run()
            }
        }
    }
    // Android-changed: Dropped documentation for legacy security code.
    /**
     * Legacy security code; do not use.
     *
     */
    @Deprecated(
        """This method is only useful in conjunction with
            {@linkplain SecurityManager the Security Manager}, which is
            deprecated and subject to removal in a future release.
            Consequently, this method is also deprecated and subject to
            removal. There is no replacement for the Security Manager or this
            method."""
    )
    fun <T> privilegedCallable(callable: java.util.concurrent.Callable<T>?): java.util.concurrent.Callable<T> {
        if (callable == null) throw java.lang.NullPointerException()
        return java.util.concurrent.Executors.PrivilegedCallable<T>(callable)
    }
    // Android-changed: Dropped documentation for legacy security code.
    /**
     * Legacy security code; do not use.
     *
     */
    @Deprecated(
        """This method is only useful in conjunction with
            {@linkplain SecurityManager the Security Manager}, which is
            deprecated and subject to removal in a future release.
            Consequently, this method is also deprecated and subject to
            removal. There is no replacement for the Security Manager or this
            method."""
    )
    fun <T> privilegedCallableUsingCurrentClassLoader(callable: java.util.concurrent.Callable<T>?): java.util.concurrent.Callable<T> {
        if (callable == null) throw java.lang.NullPointerException()
        return java.util.concurrent.Executors.PrivilegedCallableUsingCurrentClassLoader<T>(callable)
    }
    // Non-public classes supporting the public methods
    /**
     * A callable that runs given task and returns given result.
     */
    private class RunnableAdapter<T> internal constructor(task: java.lang.Runnable, result: T) :
        java.util.concurrent.Callable<T> {
        private val task: java.lang.Runnable
        private val result: T

        init {
            this.task = task
            this.result = result
        }

        override fun call(): T {
            task.run()
            return result
        }

        override fun toString(): String {
            return super.toString() + "[Wrapped task = " + task + "]"
        }
    }

    /**
     * A callable that runs under established access control settings.
     */
    private class PrivilegedCallable<T> internal constructor(task: java.util.concurrent.Callable<T>) :
        java.util.concurrent.Callable<T> {
        val task: java.util.concurrent.Callable<T>
        val acc: java.security.AccessControlContext

        init {
            this.task = task
            acc = java.security.AccessController.getContext()
        }

        @Throws(java.lang.Exception::class)
        override fun call(): T {
            return try {
                java.security.AccessController.doPrivileged<T>(
                    object : java.security.PrivilegedExceptionAction<T>() {
                        @Throws(java.lang.Exception::class)
                        override fun run(): T {
                            return task.call()
                        }
                    }, acc
                )
            } catch (e: java.security.PrivilegedActionException) {
                throw e.getException()
            }
        }

        override fun toString(): String {
            return super.toString() + "[Wrapped task = " + task + "]"
        }
    }

    /**
     * A callable that runs under established access control settings and
     * current ClassLoader.
     */
    private class PrivilegedCallableUsingCurrentClassLoader<T> internal constructor(task: java.util.concurrent.Callable<T>) :
        java.util.concurrent.Callable<T> {
        val task: java.util.concurrent.Callable<T>
        val acc: java.security.AccessControlContext
        val ccl: java.lang.ClassLoader

        init {
            // Android-removed: System.getSecurityManager always returns null.
            /*
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                // Calls to getContextClassLoader from this class
                // never trigger a security check, but we check
                // whether our callers have this permission anyways.
                sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);

                // Whether setContextClassLoader turns out to be necessary
                // or not, we fail fast if permission is not available.
                sm.checkPermission(new RuntimePermission("setContextClassLoader"));
            }
            */
            this.task = task
            acc = java.security.AccessController.getContext()
            ccl = java.lang.Thread.currentThread().getContextClassLoader()
        }

        @Throws(java.lang.Exception::class)
        override fun call(): T {
            return try {
                java.security.AccessController.doPrivileged<T>(
                    object : java.security.PrivilegedExceptionAction<T>() {
                        @Throws(java.lang.Exception::class)
                        override fun run(): T {
                            val t: java.lang.Thread = java.lang.Thread.currentThread()
                            val cl: java.lang.ClassLoader = t.getContextClassLoader()
                            return if (ccl === cl) {
                                task.call()
                            } else {
                                t.setContextClassLoader(ccl)
                                try {
                                    task.call()
                                } finally {
                                    t.setContextClassLoader(cl)
                                }
                            }
                        }
                    }, acc
                )
            } catch (e: java.security.PrivilegedActionException) {
                throw e.getException()
            }
        }

        override fun toString(): String {
            return super.toString() + "[Wrapped task = " + task + "]"
        }
    }

    /**
     * The default thread factory.
     */
    private class DefaultThreadFactory internal constructor() : java.util.concurrent.ThreadFactory {
        private val group: java.lang.ThreadGroup
        private val threadNumber: java.util.concurrent.atomic.AtomicInteger =
            java.util.concurrent.atomic.AtomicInteger(1)
        private val namePrefix: String

        init {
            val s: java.lang.SecurityManager = java.lang.System.getSecurityManager()
            group = if (s != null) s.getThreadGroup() else java.lang.Thread.currentThread()
                .getThreadGroup()
            namePrefix = "pool-" +
                    java.util.concurrent.Executors.DefaultThreadFactory.Companion.poolNumber.getAndIncrement() + "-thread-"
        }

        override fun newThread(r: java.lang.Runnable): java.lang.Thread {
            val t: java.lang.Thread = java.lang.Thread(
                group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0
            )
            if (t.isDaemon()) t.setDaemon(false)
            if (t.getPriority() != java.lang.Thread.NORM_PRIORITY) t.setPriority(java.lang.Thread.NORM_PRIORITY)
            return t
        }

        companion object {
            private val poolNumber: java.util.concurrent.atomic.AtomicInteger =
                java.util.concurrent.atomic.AtomicInteger(1)
        }
    }

    /**
     * Thread factory capturing access control context and class loader.
     */
    private class PrivilegedThreadFactory internal constructor() :
        java.util.concurrent.Executors.DefaultThreadFactory() {
        val acc: java.security.AccessControlContext
        val ccl: java.lang.ClassLoader

        init {
            // Android-removed: System.getSecurityManager always returns null.
            /*
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                // Calls to getContextClassLoader from this class
                // never trigger a security check, but we check
                // whether our callers have this permission anyways.
                sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);

                // Fail fast
                sm.checkPermission(new RuntimePermission("setContextClassLoader"));
            }
            */acc = java.security.AccessController.getContext()
            ccl = java.lang.Thread.currentThread().getContextClassLoader()
        }

        override fun newThread(r: java.lang.Runnable): java.lang.Thread {
            return super.newThread(object : java.lang.Runnable() {
                override fun run() {
                    java.security.AccessController.doPrivileged<Any>(object :
                        java.security.PrivilegedAction<Any?>() {
                        override fun run(): java.lang.Void {
                            java.lang.Thread.currentThread().setContextClassLoader(ccl)
                            r.run()
                            return null
                        }
                    }, acc)
                }
            })
        }
    }

    /**
     * A wrapper class that exposes only the ExecutorService methods
     * of an ExecutorService implementation.
     */
    private class DelegatedExecutorService internal constructor(executor: java.util.concurrent.ExecutorService) :
        java.util.concurrent.ExecutorService {
        // Android-added: @ReachabilitySensitive
        // Needed for FinalizableDelegatedExecutorService below.
        @ReachabilitySensitive
        private val e: java.util.concurrent.ExecutorService

        init {
            e = executor
        }

        override fun execute(command: java.lang.Runnable) {
            try {
                e.execute(command)
            } finally {
                java.lang.ref.Reference.reachabilityFence(this)
            }
        }

        override fun shutdown() {
            e.shutdown()
        }

        override fun shutdownNow(): List<java.lang.Runnable> {
            return try {
                e.shutdownNow()
            } finally {
                java.lang.ref.Reference.reachabilityFence(this)
            }
        }

        val isShutdown: Boolean
            get() = try {
                e.isShutdown()
            } finally {
                java.lang.ref.Reference.reachabilityFence(this)
            }
        val isTerminated: Boolean
            get() {
                return try {
                    e.isTerminated()
                } finally {
                    java.lang.ref.Reference.reachabilityFence(this)
                }
            }

        @Throws(java.lang.InterruptedException::class)
        override fun awaitTermination(timeout: Long, unit: java.util.concurrent.TimeUnit): Boolean {
            return try {
                e.awaitTermination(timeout, unit)
            } finally {
                java.lang.ref.Reference.reachabilityFence(this)
            }
        }

        override fun submit(task: java.lang.Runnable): java.util.concurrent.Future<*> {
            return try {
                e.submit(task)
            } finally {
                java.lang.ref.Reference.reachabilityFence(this)
            }
        }

        override fun <T> submit(task: java.util.concurrent.Callable<T>): java.util.concurrent.Future<T> {
            return try {
                e.submit<T>(task)
            } finally {
                java.lang.ref.Reference.reachabilityFence(this)
            }
        }

        override fun <T> submit(
            task: java.lang.Runnable,
            result: T
        ): java.util.concurrent.Future<T> {
            return try {
                e.submit<T>(task, result)
            } finally {
                java.lang.ref.Reference.reachabilityFence(this)
            }
        }

        @Throws(java.lang.InterruptedException::class)
        override fun <T> invokeAll(tasks: Collection<java.util.concurrent.Callable<T>?>): List<java.util.concurrent.Future<T>> {
            return try {
                e.invokeAll<T>(tasks)
            } finally {
                java.lang.ref.Reference.reachabilityFence(this)
            }
        }

        @Throws(java.lang.InterruptedException::class)
        override fun <T> invokeAll(
            tasks: Collection<java.util.concurrent.Callable<T>?>,
            timeout: Long, unit: java.util.concurrent.TimeUnit
        ): List<java.util.concurrent.Future<T>> {
            return try {
                e.invokeAll<T>(tasks, timeout, unit)
            } finally {
                java.lang.ref.Reference.reachabilityFence(this)
            }
        }

        @Throws(
            java.lang.InterruptedException::class,
            java.util.concurrent.ExecutionException::class
        )
        override fun <T> invokeAny(tasks: Collection<java.util.concurrent.Callable<T>?>): T {
            return try {
                e.invokeAny<T>(tasks)
            } finally {
                java.lang.ref.Reference.reachabilityFence(this)
            }
        }

        @Throws(
            java.lang.InterruptedException::class,
            java.util.concurrent.ExecutionException::class,
            java.util.concurrent.TimeoutException::class
        )
        override fun <T> invokeAny(
            tasks: Collection<java.util.concurrent.Callable<T>?>,
            timeout: Long, unit: java.util.concurrent.TimeUnit
        ): T {
            return try {
                e.invokeAny<T>(tasks, timeout, unit)
            } finally {
                java.lang.ref.Reference.reachabilityFence(this)
            }
        }
    }

    private class FinalizableDelegatedExecutorService internal constructor(executor: java.util.concurrent.ExecutorService?) :
        Executors.DelegatedExecutorService(executor) {
        @Suppress("deprecation")
        protected fun finalize() {
            super.shutdown()
        }
    }

    /**
     * A wrapper class that exposes only the ScheduledExecutorService
     * methods of a ScheduledExecutorService implementation.
     */
    private class DelegatedScheduledExecutorService internal constructor(executor: java.util.concurrent.ScheduledExecutorService) :
        Executors.DelegatedExecutorService(executor),
        java.util.concurrent.ScheduledExecutorService {
        private val e: java.util.concurrent.ScheduledExecutorService

        init {
            e = executor
        }

        override fun schedule(
            command: java.lang.Runnable,
            delay: Long,
            unit: java.util.concurrent.TimeUnit
        ): java.util.concurrent.ScheduledFuture<*> {
            return e.schedule(command, delay, unit)
        }

        override fun <V> schedule(
            callable: java.util.concurrent.Callable<V>,
            delay: Long,
            unit: java.util.concurrent.TimeUnit
        ): java.util.concurrent.ScheduledFuture<V> {
            return e.schedule<V>(callable, delay, unit)
        }

        override fun scheduleAtFixedRate(
            command: Runnable,
            initialDelay: Long,
            period: Long,
            unit: TimeUnit
        ): java.util.concurrent.ScheduledFuture<*> {
            return e.scheduleAtFixedRate(command, initialDelay, period, unit)
        }

        override fun scheduleWithFixedDelay(
            command: Runnable,
            initialDelay: Long,
            delay: Long,
            unit: TimeUnit
        ): java.util.concurrent.ScheduledFuture<*> {
            return e.scheduleWithFixedDelay(command, initialDelay, delay, unit)
        }
    }
}

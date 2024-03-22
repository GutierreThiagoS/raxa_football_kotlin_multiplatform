package framework.animation.utils

import kotlin.concurrent.Volatile
import kotlin.coroutines.cancellation.CancellationException

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
 * A cancellable asynchronous computation.  This class provides a base
 * implementation of [Future], with methods to start and cancel
 * a computation, query to see if the computation is complete, and
 * retrieve the result of the computation.  The result can only be
 * retrieved when the computation has completed; the `get`
 * methods will block if the computation has not yet completed.  Once
 * the computation has completed, the computation cannot be restarted
 * or cancelled (unless the computation is invoked using
 * [.runAndReset]).
 *
 *
 * A `FutureTask` can be used to wrap a [Callable] or
 * [Runnable] object.  Because `FutureTask` implements
 * `Runnable`, a `FutureTask` can be submitted to an
 * [Executor] for execution.
 *
 *
 * In addition to serving as a standalone class, this class provides
 * `protected` functionality that may be useful when creating
 * customized task classes.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <V> The result type returned by this FutureTask's `get` methods
</V> */
open class FutureTask<V> : RunnableFuture<V> {
    /*
     * Revision notes: This differs from previous versions of this
     * class that relied on AbstractQueuedSynchronizer, mainly to
     * avoid surprising users about retaining interrupt status during
     * cancellation races. Sync control in the current design relies
     * on a "state" field updated via CAS to track completion, along
     * with a simple Treiber stack to hold waiting threads.
     */
    /**
     * The run state of this task, initially NEW.  The run state
     * transitions to a terminal state only in methods set,
     * setException, and cancel.  During completion, state may take on
     * transient values of COMPLETING (while outcome is being set) or
     * INTERRUPTING (only while interrupting the runner to satisfy a
     * cancel(true)). Transitions from these intermediate to final
     * states use cheaper ordered/lazy writes because values are unique
     * and cannot be further modified.
     *
     * Possible state transitions:
     * NEW -> COMPLETING -> NORMAL
     * NEW -> COMPLETING -> EXCEPTIONAL
     * NEW -> CANCELLED
     * NEW -> INTERRUPTING -> INTERRUPTED
     */
    @Volatile
    private var state: Int

    /** The underlying callable; nulled out after running  */
    private var callable: Callable<V>?

    /** The result to return or exception to throw from get()  */
    private var outcome: Any? = null // non-volatile, protected by state reads/writes

    /** The thread running the callable; CASed during run()  */
    @Volatile
    private var runner: java.lang.Thread? = null

    /** Treiber stack of waiting threads  */
    @Volatile
    private val waiters: WaitNode? = null

    /**
     * Returns result or throws exception for completed task.
     *
     * @param s completed state value
     */
    @Throws(Exception::class)
    private fun report(s: Int): V {
        val x = outcome
        if (s == NORMAL) return x as V
        if (s >= CANCELLED) throw CancellationException()
        throw Exception(x as Throwable?)
    }

    /**
     * Creates a `FutureTask` that will, upon running, execute the
     * given `Callable`.
     *
     * @param  callable the callable task
     * @throws NullPointerException if the callable is null
     */
    constructor(callable: Callable<V>?) {
        if (callable == null) throw NullPointerException()
        this.callable = callable
        state = NEW // ensure visibility of callable
    }

    /**
     * Creates a `FutureTask` that will, upon running, execute the
     * given `Runnable`, and arrange that `get` will return the
     * given result on successful completion.
     *
     * @param runnable the runnable task
     * @param result the result to return on successful completion. If
     * you don't need a particular result, consider using
     * constructions of the form:
     * `Future<?> f = new FutureTask<Void>(runnable, null)`
     * @throws NullPointerException if the runnable is null
     */
    constructor(runnable: Runnable?, result: V) {
        callable = Executors.callable(runnable, result)
        state = NEW // ensure visibility of callable
    }

    val isCancelled: Boolean
        get() = state >= CANCELLED
    val isDone: Boolean
        get() = state != NEW

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        if (!(state == NEW && STATE.compareAndSet(
                this,
                NEW,
                if (mayInterruptIfRunning) INTERRUPTING else CANCELLED
            ))
        ) return false
        try {    // in case call to interrupt throws exception
            if (mayInterruptIfRunning) {
                try {
                    val t: java.lang.Thread? = runner
                    if (t != null) t.interrupt()
                } finally { // final state
                    STATE.setRelease(
                        this,
                        INTERRUPTED
                    )
                }
            }
        } finally {
            finishCompletion()
        }
        return true
    }

    /**
     * @throws CancellationException {@inheritDoc}
     */
//    @Throws(java.lang.InterruptedException::class, java.util.concurrent.ExecutionException::class)
    override fun get(): V {
        var s = state
        if (s <= COMPLETING) s = awaitDone(false, 0L)
        return report(s)
    }

    /**
     * @throws CancellationException {@inheritDoc}
     */
    /*@Throws(
        java.lang.InterruptedException::class,
        java.util.concurrent.ExecutionException::class,
        java.util.concurrent.TimeoutException::class
    )*/
    override operator fun get(timeout: Long, unit: TimeUnit?): V {
        if (unit == null) throw NullPointerException()
        var s = state
        if (s <= COMPLETING &&
            awaitDone(true, unit.toNanos(timeout)).also {
                s = it
            } <= COMPLETING
        ) throw Exception()
//        ) throw TimeoutException()
        return report(s)
    }

    /**
     * Protected method invoked when this task transitions to state
     * `isDone` (whether normally or via cancellation). The
     * default implementation does nothing.  Subclasses may override
     * this method to invoke completion callbacks or perform
     * bookkeeping. Note that you can query status inside the
     * implementation of this method to determine whether this task
     * has been cancelled.
     */
    protected open fun done() {}

    /**
     * Sets the result of this future to the given value unless
     * this future has already been set or has been cancelled.
     *
     *
     * This method is invoked internally by the [.run] method
     * upon successful completion of the computation.
     *
     * @param v the value
     */
    protected fun set(v: V?) {
        if (STATE.compareAndSet(
                this,
                NEW,
                COMPLETING
            )
        ) {
            outcome = v
            STATE.setRelease(
                this,
                NORMAL
            ) // final state
            finishCompletion()
        }
    }

    /**
     * Causes this future to report an [ExecutionException]
     * with the given throwable as its cause, unless this future has
     * already been set or has been cancelled.
     *
     *
     * This method is invoked internally by the [.run] method
     * upon failure of the computation.
     *
     * @param t the cause of failure
     */
    protected fun setException(t: Throwable?) {
        if (STATE.compareAndSet(
                this,
                NEW,
                COMPLETING
            )
        ) {
            outcome = t
            STATE.setRelease(
                this,
                EXCEPTIONAL
            ) // final state
            finishCompletion()
        }
    }

    override fun run() {
        if (state != NEW ||
            !RUNNER.compareAndSet(
                this,
                null,
                java.lang.Thread.currentThread()
            )
        ) return
        try {
            val c: Callable<V>? = callable
            if (c != null && state == NEW) {
                var result: V?
                var ran: Boolean
                try {
                    result = c.call()
                    ran = true
                } catch (ex: Throwable) {
                    result = null
                    ran = false
                    setException(ex)
                }
                if (ran) set(result)
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            val s = state
            if (s >= INTERRUPTING) handlePossibleCancellationInterrupt(
                s
            )
        }
    }

    /**
     * Executes the computation without setting its result, and then
     * resets this future to initial state, failing to do so if the
     * computation encounters an exception or is cancelled.  This is
     * designed for use with tasks that intrinsically execute more
     * than once.
     *
     * @return `true` if successfully run and reset
     */
    protected fun runAndReset(): Boolean {
        if (state != NEW ||
            !RUNNER.compareAndSet(
                this,
                null,
                java.lang.Thread.currentThread()
            )
        ) return false
        var ran = false
        var s = state
        try {
            val c: Callable<V>? = callable
            if (c != null && s == NEW) {
                try {
                    c.call() // don't set result
                    ran = true
                } catch (ex: Throwable) {
                    setException(ex)
                }
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            s = state
            if (s >= INTERRUPTING) handlePossibleCancellationInterrupt(
                s
            )
        }
        return ran && s == NEW
    }

    /**
     * Ensures that any interrupt from a possible cancel(true) is only
     * delivered to a task while in run or runAndReset.
     */
    private fun handlePossibleCancellationInterrupt(s: Int) {
        // It is possible for our interrupter to stall before getting a
        // chance to interrupt us.  Let's spin-wait patiently.
        if (s == INTERRUPTING) while (state == INTERRUPTING) java.lang.Thread.yield() // wait out pending interrupt

        // assert state == INTERRUPTED;

        // We want to clear any interrupt we may have received from
        // cancel(true).  However, it is permissible to use interrupts
        // as an independent mechanism for a task to communicate with
        // its caller, and there is no way to clear only the
        // cancellation interrupt.
        //
        // Thread.interrupted();
    }

    /**
     * Simple linked list nodes to record waiting threads in a Treiber
     * stack.  See other classes such as Phaser and SynchronousQueue
     * for more detailed explanation.
     */
    internal class WaitNode {
        @Volatile
        var thread: java.lang.Thread?

        @Volatile
        var next: WaitNode? = null

        init {
            thread = java.lang.Thread.currentThread()
        }
    }

    /**
     * Removes and signals all waiting threads, invokes done(), and
     * nulls out callable.
     */
    private fun finishCompletion() {
        // assert state > COMPLETING;
        var q: WaitNode
        while (waiters.also { q = it!! } != null) {
            if (WAITERS.weakCompareAndSet(
                    this,
                    q,
                    null
                )
            ) {
                while (true) {
                    val t: java.lang.Thread? = q.thread
                    if (t != null) {
                        q.thread = null
                        java.util.concurrent.locks.LockSupport.unpark(t)
                    }
                    val next = q.next ?: break
                    q.next = null // unlink to help gc
                    q = next
                }
                break
            }
        }
        done()
        callable = null // to reduce footprint
    }

    /**
     * Awaits completion or aborts on interrupt or timeout.
     *
     * @param timed true if use timed waits
     * @param nanos time to wait, if timed
     * @return state upon completion or at timeout
     */
//    @Throws(java.lang.InterruptedException::class)
    private fun awaitDone(timed: Boolean, nanos: Long): Int {
        // The code below is very delicate, to achieve these goals:
        // - call nanoTime exactly once for each call to park
        // - if nanos <= 0L, return promptly without allocation or nanoTime
        // - if nanos == Long.MIN_VALUE, don't underflow
        // - if nanos == Long.MAX_VALUE, and nanoTime is non-monotonic
        //   and we suffer a spurious wakeup, we will do no worse than
        //   to park-spin for a while
        var startTime = 0L // Special value 0L means not yet parked
        var q: WaitNode? = null
        var queued = false
        while (true) {
            val s = state
            if (s > COMPLETING) {
                if (q != null) q.thread = null
                return s
            } else if (s == COMPLETING) // We may have already promised (via isDone) that we are done
            // so never return empty-handed or throw InterruptedException
                java.lang.Thread.yield() else if (java.lang.Thread.interrupted()) {
                removeWaiter(q)
                throw java.lang.InterruptedException()
            } else if (q == null) {
                if (timed && nanos <= 0L) return s
                q = WaitNode()
            } else if (!queued) queued =
                WAITERS.weakCompareAndSet(
                    this,
                    waiters.also {
                        q.next = it
                    },
                    q
                ) else if (timed) {
                val parkNanos: Long
                if (startTime == 0L) { // first time
                    startTime = java.lang.System.nanoTime()
                    if (startTime == 0L) startTime = 1L
                    parkNanos = nanos
                } else {
                    val elapsed: Long = java.lang.System.nanoTime() - startTime
                    if (elapsed >= nanos) {
                        removeWaiter(q)
                        return state
                    }
                    parkNanos = nanos - elapsed
                }
                // nanoTime may be slow; recheck before parking
                if (state < COMPLETING) java.util.concurrent.locks.LockSupport.parkNanos(
                    this,
                    parkNanos
                )
            } else java.util.concurrent.locks.LockSupport.park(this)
        }
    }

    /**
     * Tries to unlink a timed-out or interrupted wait node to avoid
     * accumulating garbage.  Internal nodes are simply unspliced
     * without CAS since it is harmless if they are traversed anyway
     * by releasers.  To avoid effects of unsplicing from already
     * removed nodes, the list is retraversed in case of an apparent
     * race.  This is slow when there are a lot of nodes, but we don't
     * expect lists to be long enough to outweigh higher-overhead
     * schemes.
     */
    private fun removeWaiter(node: WaitNode?) {
        if (node != null) {
            node.thread = null
            retry@ while (true) {
                // restart on removeWaiter race
                var pred: WaitNode? = null
                var q = waiters
                var s: WaitNode?
                while (q != null) {
                    s = q.next
                    if (q.thread != null) pred = q else if (pred != null) {
                        pred.next = s
                        if (pred.thread == null) {
                            // check for race
                            continue@retry
                        }
                    } else if (!WAITERS.compareAndSet(
                            this,
                            q,
                            s
                        )
                    ) {
                        continue@retry
                    }
                    q = s
                }
                break
            }
        }
    }

    /**
     * Returns a string representation of this FutureTask.
     *
     * @implSpec
     * The default implementation returns a string identifying this
     * FutureTask, as well as its completion state.  The state, in
     * brackets, contains one of the strings `"Completed Normally"`,
     * `"Completed Exceptionally"`, `"Cancelled"`, or `"Not completed"`.
     *
     * @return a string representation of this FutureTask
     */
    override fun toString(): String {
        val status: String
        status = when (state) {
            NORMAL -> "[Completed normally]"
            EXCEPTIONAL -> "[Completed exceptionally: $outcome]"
            CANCELLED, INTERRUPTING, INTERRUPTED -> "[Cancelled]"
            else ->             // BEGIN Android-changed: recursion risk building string (b/241297967)
                /*
                 final Callable<?> callable = this.callable;
                 status = (callable == null)
                     ? "[Not completed]"
                     : "[Not completed, task = " + callable + "]";
                 */"[Not completed]"
        }
        return super.toString() + status
    }

    companion object {
        private const val NEW = 0
        private const val COMPLETING = 1
        private const val NORMAL = 2
        private const val EXCEPTIONAL = 3
        private const val CANCELLED = 4
        private const val INTERRUPTING = 5
        private const val INTERRUPTED = 6

        // VarHandle mechanics
        private val STATE: java.lang.invoke.VarHandle
        private val RUNNER: java.lang.invoke.VarHandle
        private val WAITERS: java.lang.invoke.VarHandle

        init {
            try {
                val l: java.lang.invoke.MethodHandles.Lookup =
                    java.lang.invoke.MethodHandles.lookup()
                STATE = l.findVarHandle(
                    FutureTask::class, "state",
                    Int::class
                )
                RUNNER = l.findVarHandle(
                    FutureTask::class, "runner",
                    java.lang.Thread::class
                )
                WAITERS = l.findVarHandle(
                    FutureTask::class, "waiters",
                    WaitNode::class
                )
            } catch (e: java.lang.ReflectiveOperationException) {
                throw java.lang.ExceptionInInitializerError(e)
            }

            // Reduce the risk of rare disastrous classloading in first call to
            // LockSupport.park: https://bugs.openjdk.java.net/browse/JDK-8074773
            val ensureLoaded: java.lang.Class<*> =
                java.util.concurrent.locks.LockSupport::class.java
        }
    }
}

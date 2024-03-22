package framework.animation.utils

import kotlin.concurrent.Volatile
import kotlin.jvm.JvmOverloads
import kotlin.jvm.Synchronized



/**
 * A *thread* is a thread of execution in a program. The Java
 * Virtual Machine allows an application to have multiple threads of
 * execution running concurrently.
 *
 *
 * Every thread has a priority. Threads with higher priority are
 * executed in preference to threads with lower priority. Each thread
 * may or may not also be marked as a daemon. When code running in
 * some thread creates a new `Thread` object, the new
 * thread has its priority initially set equal to the priority of the
 * creating thread, and is a daemon thread if and only if the
 * creating thread is a daemon.
 *
 *
 * When a Java Virtual Machine starts up, there is usually a single
 * non-daemon thread (which typically calls the method named
 * `main` of some designated class). The Java Virtual
 * Machine continues to execute threads until either of the following
 * occurs:
 *
 *  * The `exit` method of class `Runtime` has been
 * called and the security manager has permitted the exit operation
 * to take place.
 *  * All threads that are not daemon threads have died, either by
 * returning from the call to the `run` method or by
 * throwing an exception that propagates beyond the `run`
 * method.
 *
 *
 *
 * There are two ways to create a new thread of execution. One is to
 * declare a class to be a subclass of `Thread`. This
 * subclass should override the `run` method of class
 * `Thread`. An instance of the subclass can then be
 * allocated and started. For example, a thread that computes primes
 * larger than a stated value could be written as follows:
 * <hr></hr><blockquote><pre>
 * class PrimeThread extends Thread {
 * long minPrime;
 * PrimeThread(long minPrime) {
 * this.minPrime = minPrime;
 * }
 *
 * public void run() {
 * // compute primes larger than minPrime
 * &nbsp;.&nbsp;.&nbsp;.
 * }
 * }
</pre></blockquote> * <hr></hr>
 *
 *
 * The following code would then create a thread and start it running:
 * <blockquote><pre>
 * PrimeThread p = new PrimeThread(143);
 * p.start();
</pre></blockquote> *
 *
 *
 * The other way to create a thread is to declare a class that
 * implements the `Runnable` interface. That class then
 * implements the `run` method. An instance of the class can
 * then be allocated, passed as an argument when creating
 * `Thread`, and started. The same example in this other
 * style looks like the following:
 * <hr></hr><blockquote><pre>
 * class PrimeRun implements Runnable {
 * long minPrime;
 * PrimeRun(long minPrime) {
 * this.minPrime = minPrime;
 * }
 *
 * public void run() {
 * // compute primes larger than minPrime
 * &nbsp;.&nbsp;.&nbsp;.
 * }
 * }
</pre></blockquote> * <hr></hr>
 *
 *
 * The following code would then create a thread and start it running:
 * <blockquote><pre>
 * PrimeRun p = new PrimeRun(143);
 * new Thread(p).start();
</pre></blockquote> *
 *
 *
 * Every thread has a name for identification purposes. More than
 * one thread may have the same name. If a name is not specified when
 * a thread is created, a new name is generated for it.
 *
 *
 * Unless otherwise noted, passing a `null` argument to a constructor
 * or method in this class will cause a [NullPointerException] to be
 * thrown.
 *
 * @author  unascribed
 * @see Runnable
 *
 * @see Runtime.exit
 * @see .run
 * @see .stop
 * @since   1.0
 */
open class Thread : Runnable {
    // Android-removed: registerNatives() not used on Android.
    /*
    / * Make sure registerNatives is the first thing <clinit> does. *
    private static native void registerNatives();
    static {
        registerNatives();
    }
    */
    // BEGIN Android-added: Android specific fields lock, nativePeer.
    /**
     * The synchronization object responsible for this thread's join/sleep/park operations.
     */
    private val lock = Any()

    /**
     * Reference to the native thread object.
     *
     *
     * Is 0 if the native thread has not yet been created/started, or has been destroyed.
     */
    @Volatile
    private val nativePeer: Long = 0

    // END Android-added: Android specific fields lock, nativePeer.
    @Volatile
    private var name: String
    private var priority: Int

    /* Whether or not to single_step this thread. */
    private val single_step = false

    /* Whether or not the thread is a daemon thread. */
    private var daemon = false

    /* Fields reserved for exclusive use by the JVM */
    private val stillborn = false
    private val eetop: Long = 0

    /* What will be run. */
    private var target: Runnable? = null

    /* The group of this thread */
    private var group: ThreadGroup?

    /* The context ClassLoader for this thread */
    private var contextClassLoader: java.lang.ClassLoader? = null

    /* The inherited AccessControlContext of this thread */
    private var inheritedAccessControlContext: java.security.AccessControlContext? = null

    /* ThreadLocal values pertaining to this thread. This map is maintained
     * by the ThreadLocal class. */
    var threadLocals: ThreadLocalMap? = null

    /*
     * InheritableThreadLocal values pertaining to this thread. This map is
     * maintained by the InheritableThreadLocal class.
     */
    var inheritableThreadLocals: ThreadLocalMap? = null

    /*
     * The requested stack size for this thread, or 0 if the creator did
     * not specify a stack size.  It is up to the VM to do whatever it
     * likes with this number; some VMs will ignore it.
     */
    private val stackSize: Long
    // BEGIN Android-changed: Keep track of whether this thread was unparked while not alive.
    /*
    / *
     * JVM-private state that persists after native thread termination.
     *
    private long nativeParkEventPointer;
    */
    /**
     * Indicates whether this thread was unpark()ed while not alive, in which case start()ing
     * it should leave it in unparked state. This field is read and written by native code in
     * the runtime, guarded by thread_list_lock. See http://b/28845097#comment49
     */
    private val unparkedBeforeStart = false

    /**
     * Returns the identifier of this   The thread ID is a positive
     * `long` number generated when this thread was created.
     * The thread ID is unique and remains unchanged during its lifetime.
     * When a thread is terminated, this thread ID may be reused.
     *
     * @return this thread's ID.
     * @since 1.5
     */
    // END Android-changed: Keep track of whether this thread was unparked while not alive.
    /*
     * Thread ID
     */
    val id: Long
    // Android-added: The concept of "system-daemon" threads. See java.lang.Daemons.
    /** True if this thread is managed by [Daemons].  */
    private var systemDaemon = false
    /* Java thread status for tools,
     * initialized to indicate thread 'not yet started'
     */
    // BEGIN Android-changed: Replace unused threadStatus field with started field.
    // Upstream this is modified by the native code and read in the start() and getState() methods
    // but in Android it is unused. The threadStatus is essentially an internal representation of
    // the State enum. Android uses two sources for that information, the native thread
    // state and the started field. The reason two sources are needed is because the native thread
    // is created when the thread is started and destroyed when the thread is stopped. That means
    // that the native thread state does not exist before the Thread has started (in State.NEW) or
    // after it has been stopped (in State.TERMINATED). In that case (i.e. when the nativePeer = 0)
    // the started field differentiates between the two states, i.e. if started = false then the
    // thread is in State.NEW and if started = true then the thread is in State.TERMINATED.
    // private volatile int threadStatus = 0;
    /**
     * True if the the Thread has been started, even it has since been stopped.
     */
    var started = false
    // END Android-changed: Replace unused threadStatus field with started field.
    /**
     * The argument supplied to the current call to
     * java.util.concurrent.locks.LockSupport.park.
     * Set by (private) java.util.concurrent.locks.LockSupport.setBlocker
     * Accessed using java.util.concurrent.locks.LockSupport.getBlocker
     */
    @Volatile
    var parkBlocker: Any? = null

    /* The object in which this thread is blocked in an interruptible I/O
     * operation, if any.  The blocker's interrupt method should be invoked
     * after setting this thread's interrupt status.
     */
    @Volatile
    private var blocker: Interruptible? = null
    private val blockerLock = Any()
    // Android-changed: Make blockedOn() @hide public, for internal use.
    // Changed comment to reflect usage on Android
    /* Set the blocker field; used by java.nio.channels.spi.AbstractInterruptibleChannel
     */
    /** @hide
     */
    fun blockedOn(b: Interruptible?) {
        synchronized(blockerLock) { blocker = b }
    }

    /**
     * Initializes a 
     *
     * @param g the Thread group
     * @param target the object whose run() method gets called
     * @param name the name of the new Thread
     * @param stackSize the desired stack size for the new thread, or
     * zero to indicate that this parameter is to be ignored.
     * @param acc the AccessControlContext to inherit, or
     * AccessController.getContext() if null
     * @param inheritThreadLocals if `true`, inherit initial values for
     * inheritable thread-locals from the constructing thread
     */
    private constructor(
        g: ThreadGroup?, target: java.lang.Runnable?, name: String?,
        stackSize: Long, acc: java.security.AccessControlContext?,
        inheritThreadLocals: Boolean
    ) {
        var g: ThreadGroup? = g
        if (name == null) {
            throw java.lang.NullPointerException("name cannot be null")
        }
        this.name = name
        val parent: Thread = currentThread()
        // Android-removed: SecurityManager stubbed out on Android.
        // SecurityManager security = System.getSecurityManager();
        if (g == null) {
            // Android-changed: SecurityManager stubbed out on Android.
            /*
            / * Determine if it's an applet or not *

            / * If there is a security manager, ask the security manager
               what to do. *
            if (security != null) {
                g = security.getThreadGroup();
            }

            / * If the security manager doesn't have a strong opinion
               on the matter, use the parent thread group. *
            if (g == null) {
            */
            g = parent.getThreadGroup()
            // }
        }

        // Android-removed: SecurityManager stubbed out on Android.
        /*
        / * checkAccess regardless of whether or not threadgroup is
           explicitly passed in. *
        g.checkAccess();

        / *
         * Do we have the required permissions?
         *
        if (security != null) {
            if (isCCLOverridden(getClass())) {
                security.checkPermission(
                        SecurityConstants.SUBCLASS_IMPLEMENTATION_PERMISSION);
            }
        }
        */g.addUnstarted()
        group = g
        daemon = parent.isDaemon()
        priority = parent.getPriority()
        // Android-changed: Moved into init2(Thread, boolean) helper method.
        /*
        if (security == null || isCCLOverridden(parent.getClass()))
            this.contextClassLoader = parent.getContextClassLoader();
        else
            this.contextClassLoader = parent.contextClassLoader;
        this.inheritedAccessControlContext =
                acc != null ? acc : AccessController.getContext();
        */this.target = target
        // Android-removed: The priority parameter is unchecked on Android.
        // It is unclear why this is not being done (b/80180276).
        // setPriority(priority);
        // Android-changed: Moved into init2(Thread, boolean) helper method.
        // if (inheritThreadLocals && parent.inheritableThreadLocals != null)
        //     this.inheritableThreadLocals =
        //         ThreadLocal.createInheritedMap(parent.inheritableThreadLocals);
        init2(parent, inheritThreadLocals)

        /* Stash the specified stack size in case the VM cares */this.stackSize = stackSize

        /* Set thread ID */id = nextThreadID()
    }

    /**
     * Throws CloneNotSupportedException as a Thread can not be meaningfully
     * cloned. Construct a new Thread instead.
     *
     * @throws  CloneNotSupportedException
     * always
     */
    @Throws(java.lang.CloneNotSupportedException::class)
    protected override fun clone(): Any {
        throw java.lang.CloneNotSupportedException()
    }

    /**
     * Allocates a new `Thread` object. This constructor has the same
     * effect as [Thread][.Thread]
     * `(null, target, gname)`, where `gname` is a newly generated
     * name. Automatically generated names are of the form
     * `"Thread-"+`*n*, where *n* is an integer.
     *
     * @param  target
     * the object whose `run` method is invoked when this thread
     * is started. If `null`, this classes `run` method does
     * nothing.
     */
    constructor(target: java.lang.Runnable?) : this(
        null,
        target,
        "Thread-" + nextThreadNum(),
        0
    )

    /**
     * Creates a new Thread that inherits the given AccessControlContext
     * but thread-local variables are not inherited.
     * This is not a public constructor.
     */
    internal constructor(
        target: java.lang.Runnable?,
        acc: java.security.AccessControlContext?
    ) : this(null, target, "Thread-" + nextThreadNum(), 0, acc, false)

    /**
     * Allocates a new `Thread` object. This constructor has the same
     * effect as [Thread][.Thread]
     * `(null, null, name)`.
     *
     * @param   name
     * the name of the new thread
     */
    constructor(name: String?) : this(null, null, name, 0)

    /**
     * Allocates a new `Thread` object. This constructor has the same
     * effect as [Thread][.Thread]
     * `(group, null, name)`.
     *
     * @param  group
     * the thread group. If `null` and there is a security
     * manager, the group is determined by [         ][SecurityManager.getThreadGroup].
     * If there is not a security manager or `SecurityManager.getThreadGroup()` returns `null`, the group
     * is set to the current thread's thread group.
     *
     * @param  name
     * the name of the new thread
     *
     * @throws  SecurityException
     * if the current thread cannot create a thread in the specified
     * thread group
     */
    constructor(group: ThreadGroup?, name: String?) : this(group, null, name, 0)
    // BEGIN Android-added: Private constructor - used by the runtime.
    /** @hide
     */
    internal constructor(
        group: ThreadGroup?,
        name: String?,
        priority: Int,
        daemon: Boolean
    ) {
        var name = name
        this.group = group
        this.group.addUnstarted()
        // Must be tolerant of threads without a name.
        if (name == null) {
            name = "Thread-" + nextThreadNum()
        }

        // NOTE: Resist the temptation to call setName() here. This constructor is only called
        // by the runtime to construct peers for threads that have attached via JNI and it's
        // undesirable to clobber their natively set name.
        this.name = name
        this.priority = priority
        this.daemon = daemon
        init2(currentThread(), true)
        stackSize = 0
        id = nextThreadID()
    }

    // Android-added: Helper method for previous constructor and init(...) method.
    private fun init2(parent: Thread, inheritThreadLocals: Boolean) {
        contextClassLoader = parent.getContextClassLoader()
        inheritedAccessControlContext = java.security.AccessController.getContext()
        if (inheritThreadLocals && parent.inheritableThreadLocals != null) {
            inheritableThreadLocals =
                ThreadLocal.createInheritedMap(parent.inheritableThreadLocals)
        }
    }
    // END Android-added: Private constructor - used by the runtime.
    /**
     * Allocates a new `Thread` object. This constructor has the same
     * effect as [Thread][.Thread]
     * `(null, target, name)`.
     *
     * @param  target
     * the object whose `run` method is invoked when this thread
     * is started. If `null`, this thread's run method is invoked.
     *
     * @param  name
     * the name of the new thread
     */
    constructor(target: Runnable?, name: String?) : this(null, target, name, 0)
    /**
     * Allocates a new `Thread` object so that it has `target`
     * as its run object, has the specified `name` as its name,
     * and belongs to the thread group referred to by `group`, and has
     * the specified *stack size*.
     *
     *
     * This constructor is identical to [ ][.Thread] with the exception of the fact
     * that it allows the thread stack size to be specified.  The stack size
     * is the approximate number of bytes of address space that the virtual
     * machine is to allocate for this thread's stack.  **The effect of the
     * `stackSize` parameter, if any, is highly platform dependent.**
     *
     *
     * On some platforms, specifying a higher value for the
     * `stackSize` parameter may allow a thread to achieve greater
     * recursion depth before throwing a [StackOverflowError].
     * Similarly, specifying a lower value may allow a greater number of
     * threads to exist concurrently without throwing an [ ] (or other internal error).  The details of
     * the relationship between the value of the `stackSize` parameter
     * and the maximum recursion depth and concurrency level are
     * platform-dependent.  **On some platforms, the value of the
     * `stackSize` parameter may have no effect whatsoever.**
     *
     *
     * The virtual machine is free to treat the `stackSize`
     * parameter as a suggestion.  If the specified value is unreasonably low
     * for the platform, the virtual machine may instead use some
     * platform-specific minimum value; if the specified value is unreasonably
     * high, the virtual machine may instead use some platform-specific
     * maximum.  Likewise, the virtual machine is free to round the specified
     * value up or down as it sees fit (or to ignore it completely).
     *
     *
     * Specifying a value of zero for the `stackSize` parameter will
     * cause this constructor to behave exactly like the
     * `Thread(ThreadGroup, Runnable, String)` constructor.
     *
     *
     * *Due to the platform-dependent nature of the behavior of this
     * constructor, extreme care should be exercised in its use.
     * The thread stack size necessary to perform a given computation will
     * likely vary from one JRE implementation to another.  In light of this
     * variation, careful tuning of the stack size parameter may be required,
     * and the tuning may need to be repeated for each JRE implementation on
     * which an application is to run.*
     *
     *
     * Implementation note: Java platform implementers are encouraged to
     * document their implementation's behavior with respect to the
     * `stackSize` parameter.
     *
     *
     * @param  group
     * the thread group. If `null` and there is a security
     * manager, the group is determined by [         ][SecurityManager.getThreadGroup].
     * If there is not a security manager or `SecurityManager.getThreadGroup()` returns `null`, the group
     * is set to the current thread's thread group.
     *
     * @param  target
     * the object whose `run` method is invoked when this thread
     * is started. If `null`, this thread's run method is invoked.
     *
     * @param  name
     * the name of the new thread
     *
     * @param  stackSize
     * the desired stack size for the new thread, or zero to indicate
     * that this parameter is to be ignored.
     *
     * @throws  SecurityException
     * if the current thread cannot create a thread in the specified
     * thread group
     *
     * @since 1.4
     */
    /**
     * Allocates a new `Thread` object. This constructor has the same
     * effect as [Thread][.Thread]
     * `(null, null, gname)`, where `gname` is a newly generated
     * name. Automatically generated names are of the form
     * `"Thread-"+`*n*, where *n* is an integer.
     */
    /**
     * Allocates a new `Thread` object. This constructor has the same
     * effect as [Thread][.Thread]
     * `(group, target, gname)` ,where `gname` is a newly generated
     * name. Automatically generated names are of the form
     * `"Thread-"+`*n*, where *n* is an integer.
     *
     * @param  group
     * the thread group. If `null` and there is a security
     * manager, the group is determined by [         ][SecurityManager.getThreadGroup].
     * If there is not a security manager or `SecurityManager.getThreadGroup()` returns `null`, the group
     * is set to the current thread's thread group.
     *
     * @param  target
     * the object whose `run` method is invoked when this thread
     * is started. If `null`, this thread's run method is invoked.
     *
     * @throws  SecurityException
     * if the current thread cannot create a thread in the specified
     * thread group
     */
    /**
     * Allocates a new `Thread` object so that it has `target`
     * as its run object, has the specified `name` as its name,
     * and belongs to the thread group referred to by `group`.
     *
     *
     * If there is a security manager, its
     * [checkAccess][SecurityManager.checkAccess]
     * method is invoked with the ThreadGroup as its argument.
     *
     *
     * In addition, its `checkPermission` method is invoked with
     * the `RuntimePermission("enableContextClassLoaderOverride")`
     * permission when invoked directly or indirectly by the constructor
     * of a subclass which overrides the `getContextClassLoader`
     * or `setContextClassLoader` methods.
     *
     *
     * The priority of the newly created thread is set equal to the
     * priority of the thread creating it, that is, the currently running
     * thread. The method [setPriority][.setPriority] may be
     * used to change the priority to a new value.
     *
     *
     * The newly created thread is initially marked as being a daemon
     * thread if and only if the thread creating it is currently marked
     * as a daemon thread. The method [setDaemon][.setDaemon]
     * may be used to change whether or not a thread is a daemon.
     *
     * @param  group
     * the thread group. If `null` and there is a security
     * manager, the group is determined by [         ][SecurityManager.getThreadGroup].
     * If there is not a security manager or `SecurityManager.getThreadGroup()` returns `null`, the group
     * is set to the current thread's thread group.
     *
     * @param  target
     * the object whose `run` method is invoked when this thread
     * is started. If `null`, this thread's run method is invoked.
     *
     * @param  name
     * the name of the new thread
     *
     * @throws  SecurityException
     * if the current thread cannot create a thread in the specified
     * thread group or cannot override the context class loader methods.
     */
    @JvmOverloads
    constructor(
        group: ThreadGroup? = null,
        target: java.lang.Runnable? = null,
        name: String? = "Thread-" + nextThreadNum(),
        stackSize: Long = 0
    ) : this(group, target, name, stackSize, null, true)

    /**
     * Allocates a new `Thread` object so that it has `target`
     * as its run object, has the specified `name` as its name,
     * belongs to the thread group referred to by `group`, has
     * the specified `stackSize`, and inherits initial values for
     * [inheritable thread-local][InheritableThreadLocal] variables
     * if `inheritThreadLocals` is `true`.
     *
     *
     *  This constructor is identical to [ ][.Thread] with the added ability to
     * suppress, or not, the inheriting of initial values for inheritable
     * thread-local variables from the constructing thread. This allows for
     * finer grain control over inheritable thread-locals. Care must be taken
     * when passing a value of `false` for `inheritThreadLocals`,
     * as it may lead to unexpected behavior if the new thread executes code
     * that expects a specific thread-local value to be inherited.
     *
     *
     *  Specifying a value of `true` for the `inheritThreadLocals`
     * parameter will cause this constructor to behave exactly like the
     * `Thread(ThreadGroup, Runnable, String, long)` constructor.
     *
     * @param  group
     * the thread group. If `null` and there is a security
     * manager, the group is determined by [         ][SecurityManager.getThreadGroup].
     * If there is not a security manager or `SecurityManager.getThreadGroup()` returns `null`, the group
     * is set to the current thread's thread group.
     *
     * @param  target
     * the object whose `run` method is invoked when this thread
     * is started. If `null`, this thread's run method is invoked.
     *
     * @param  name
     * the name of the new thread
     *
     * @param  stackSize
     * the desired stack size for the new thread, or zero to indicate
     * that this parameter is to be ignored
     *
     * @param  inheritThreadLocals
     * if `true`, inherit initial values for inheritable
     * thread-locals from the constructing thread, otherwise no initial
     * values are inherited
     *
     * @throws  SecurityException
     * if the current thread cannot create a thread in the specified
     * thread group
     *
     * @since 9
     */
    constructor(
        group: ThreadGroup?, target: java.lang.Runnable?, name: String?,
        stackSize: Long, inheritThreadLocals: Boolean
    ) : this(group, target, name, stackSize, null, inheritThreadLocals)

    /**
     * Causes this thread to begin execution; the Java Virtual Machine
     * calls the `run` method of this thread.
     *
     *
     * The result is that two threads are running concurrently: the
     * current thread (which returns from the call to the
     * `start` method) and the other thread (which executes its
     * `run` method).
     *
     *
     * It is never legal to start a thread more than once.
     * In particular, a thread may not be restarted once it has completed
     * execution.
     *
     * @throws     IllegalThreadStateException  if the thread was already started.
     * @see .run
     * @see .stop
     */
    @Synchronized
    fun start() {
        /**
         * This method is not invoked for the main method thread or "system"
         * group threads created/set up by the VM. Any new functionality added
         * to this method in the future may have to also be added to the VM.
         *
         * A zero status value corresponds to state "NEW".
         */
        // Android-changed: Replace unused threadStatus field with started field.
        // The threadStatus field is unused on Android.
        // if (threadStatus != 0)
        if (started) throw java.lang.IllegalThreadStateException()

        /* Notify the group that this thread is about to be started
         * so that it can be added to the group's list of threads
         * and the group's unstarted count can be decremented. */group.add(this)

        // Android-changed: Use field instead of local variable.
        // It is necessary to remember the state of this across calls to this method so that it
        // can throw an IllegalThreadStateException if this method is called on an already
        // started thread.
        // boolean started = false;
        started = false
        started = try {
            // Android-changed: Use Android specific nativeCreate() method to create/start thread.
            // start0();
            nativeCreate(this, stackSize, daemon)
            true
        } finally {
            try {
                if (!started) {
                    group.threadStartFailed(this)
                }
            } catch (ignore: Throwable) {
                /* do nothing. If start0 threw a Throwable then
                       it will be passed up the call stack */
            }
        }
    }

    /**
     * If this thread was constructed using a separate
     * `Runnable` run object, then that
     * `Runnable` object's `run` method is called;
     * otherwise, this method does nothing and returns.
     *
     *
     * Subclasses of `Thread` should override this method.
     *
     * @see .start
     * @see .stop
     * @see .Thread
     */
    override fun run() {
        if (target != null) {
            target.run()
        }
    }

    /**
     * This method is called by the system to give a Thread
     * a chance to clean up before it actually exits.
     */
    private fun exit() {
        if (group != null) {
            group.threadTerminated(this)
            group = null
        }
        /* Aggressively null out all reference fields: see bug 4006245 */target = null
        /* Speed the release of some of these resources */threadLocals = null
        inheritableThreadLocals = null
        inheritedAccessControlContext = null
        blocker = null
        uncaughtExceptionHandler = null
    }
    // Android-changed: Throws UnsupportedOperationException.
    /**
     * Throws `UnsupportedOperationException`.
     *
     */
    @Deprecated("""This method was originally designed to force a thread to stop
            and throw a {@code ThreadDeath} as an exception. It was inherently unsafe.
            Stopping a thread with
            stop causes it to unlock all of the monitors that it
            has locked (as a natural consequence of the unchecked
            {@code ThreadDeath} exception propagating up the stack).  If
            any of the objects previously protected by these monitors were in
            an inconsistent state, the damaged objects become visible to
            other threads, potentially resulting in arbitrary behavior.  Many
            uses of {@code stop} should be replaced by code that simply
            modifies some variable to indicate that the target thread should
            stop running.  The target thread should check this variable
            regularly, and return from its run method in an orderly fashion
            if the variable indicates that it is to stop running.  If the
            target thread waits for long periods (on a condition variable,
            for example), the {@code interrupt} method should be used to
            interrupt the wait.
            For more information, see
${"            <a href=" { @docRoot } / .. / technotes / guides / concurrency / threadPrimitiveDeprecation.html}"""
        """>Why
            are stop, suspend and resume Deprecated?</a>.""")
    fun stop() {
        /*
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            checkAccess();
            if (this != currentThread()) {
                security.checkPermission(SecurityConstants.STOP_THREAD_PERMISSION);
            }
        }
        // A zero status value corresponds to "NEW", it can't change to
        // not-NEW because we hold the lock.
        if (threadStatus != 0) {
            resume(); // Wake up thread if it was suspended; no-op otherwise
        }

        // The VM can handle all thread states
        stop0(new ThreadDeath());
        */
        throw java.lang.UnsupportedOperationException()
    }

    /**
     * Throws `UnsupportedOperationException`.
     *
     * @param obj ignored
     *
     */
    @Deprecated("""This method was originally designed to force a thread to stop
             and throw a given {@code Throwable} as an exception. It was
             inherently unsafe (see {@link #stop()} for details), and furthermore
             could be used to generate exceptions that the target thread was
             not prepared to handle.
             For more information, see
${"             <a href=" { @docRoot } / .. / technotes / guides / concurrency / threadPrimitiveDeprecation.html}"""
        """>Why
             are stop, suspend and resume Deprecated?</a>.""")
    @Synchronized
    fun stop(obj: Throwable?) {
        throw java.lang.UnsupportedOperationException()
    }

    /**
     * Interrupts this thread.
     *
     *
     *  Unless the current thread is interrupting itself, which is
     * always permitted, the [checkAccess][.checkAccess] method
     * of this thread is invoked, which may cause a [ ] to be thrown.
     *
     *
     *  If this thread is blocked in an invocation of the [ ][Object.wait], [wait(long)][Object.wait], or [ ][Object.wait] methods of the [Object]
     * class, or of the [.join], [.join], [ ][.join], [.sleep], or [.sleep],
     * methods of this class, then its interrupt status will be cleared and it
     * will receive an [InterruptedException].
     *
     *
     *  If this thread is blocked in an I/O operation upon an [ ]
     * then the channel will be closed, the thread's interrupt
     * status will be set, and the thread will receive a [ ].
     *
     *
     *  If this thread is blocked in a [java.nio.channels.Selector]
     * then the thread's interrupt status will be set and it will return
     * immediately from the selection operation, possibly with a non-zero
     * value, just as if the selector's [ ][java.nio.channels.Selector.wakeup] method were invoked.
     *
     *
     *  If none of the previous conditions hold then this thread's interrupt
     * status will be set.
     *
     *
     *  Interrupting a thread that is not alive need not have any effect.
     *
     * @throws  SecurityException
     * if the current thread cannot modify this thread
     *
     * @revised 6.0
     * @spec JSR-51
     */
    fun interrupt() {
        if (this !== currentThread()) {
            checkAccess()

            // thread may be blocked in an I/O operation
            synchronized(blockerLock) {
                val b: Interruptible? = blocker
                if (b != null) {
                    interrupt0() // set interrupt status
                    b.interrupt(this)
                    return
                }
            }
        }

        // set interrupt status
        interrupt0()
    }

    @get:FastNative
    val isInterrupted: Boolean
        /**
         * Tests whether this thread has been interrupted.  The *interrupted
         * status* of the thread is unaffected by this method.
         *
         *
         * A thread interruption ignored because a thread was not alive
         * at the time of the interrupt will be reflected by this method
         * returning false.
         *
         * @return  `true` if this thread has been interrupted;
         * `false` otherwise.
         * @see .interrupted
         * @revised 6.0
         */
        external get
    // Android-removed: Use native interrupted()/isInterrupted() methods.
    /*
    / **
     * Tests if some Thread has been interrupted.  The interrupted state
     * is reset or not based on the value of ClearInterrupted that is
     * passed.
     *
    @IntrinsicCandidate
    private native boolean isInterrupted(boolean ClearInterrupted);
    */
    // BEGIN Android-changed: Throw UnsupportedOperationException instead of NoSuchMethodError.
    /**
     * Throws [UnsupportedOperationException].
     *
     * @throws UnsupportedOperationException always
     */
    @Deprecated(
        """This method was originally designed to destroy this
          thread without any cleanup. Any monitors it held would have
          remained locked. However, the method was never implemented.
          If if were to be implemented, it would be deadlock-prone in
          much the manner of {@link #suspend}. If the target thread held
          a lock protecting a critical system resource when it was
          destroyed, no thread could ever access this resource again.
          If another thread ever attempted to lock this resource, deadlock
          would result. Such deadlocks typically manifest themselves as
          """ frozen """ processes. For more information, see
${"          <a href=" { @docRoot } / .. / technotes / guides / concurrency / threadPrimitiveDeprecation.html}"""
        """>
          Why are stop, suspend and resume Deprecated?</a>.
      """)
    fun destroy() {
        throw java.lang.UnsupportedOperationException()
    }

    // END Android-changed: Throw UnsupportedOperationException instead of NoSuchMethodError.
    val isAlive: Boolean
        /**
         * Tests if this thread is alive. A thread is alive if it has
         * been started and has not yet died.
         *
         * @return  `true` if this thread is alive;
         * `false` otherwise.
         */
        get() = nativePeer != 0L
    // Android-changed: Updated JavaDoc as it always throws an UnsupportedOperationException.
    /**
     * Throws [UnsupportedOperationException].
     *
     * @throws UnsupportedOperationException always
     */
    @Deprecated(
        """This method has been deprecated, as it is
        inherently deadlock-prone.  If the target thread holds a lock on the
        monitor protecting a critical system resource when it is suspended, no
        thread can access this resource until the target thread is resumed. If
        the thread that would resume the target thread attempts to lock this
        monitor prior to calling {@code resume}, deadlock results.  Such
        deadlocks typically manifest themselves as """ frozen """ processes.
        For more information, see
${"        <a href=" { @docRoot } / .. / technotes / guides / concurrency / threadPrimitiveDeprecation.html}"""
        """>Why
        are stop, suspend and resume Deprecated?</a>.
      """)
    fun suspend() {
        // Android-changed: Unsupported on Android.
        // checkAccess();
        // suspend0();
        throw java.lang.UnsupportedOperationException()
    }
    // Android-changed: Updated JavaDoc as it always throws an UnsupportedOperationException.
    /**
     * Throws [UnsupportedOperationException].
     *
     * @throws UnsupportedOperationException always
     */
    @Deprecated("""This method exists solely for use with {@link #suspend},
          which has been deprecated because it is deadlock-prone.
          For more information, see
${"          <a href=" { @docRoot } / .. / technotes / guides / concurrency / threadPrimitiveDeprecation.html}"""
        """>Why
          are stop, suspend and resume Deprecated?</a>.
      """)
    fun resume() {
        // Android-changed: Unsupported on Android.
        // checkAccess();
        // resume0();
        throw java.lang.UnsupportedOperationException()
    }

    /**
     * Changes the priority of this thread.
     *
     *
     * First the `checkAccess` method of this thread is called
     * with no arguments. This may result in throwing a `SecurityException`.
     *
     *
     * Otherwise, the priority of this thread is set to the smaller of
     * the specified `newPriority` and the maximum permitted
     * priority of the thread's thread group.
     *
     * @param newPriority priority to set this thread to
     * @throws     IllegalArgumentException  If the priority is not in the
     * range `MIN_PRIORITY` to
     * `MAX_PRIORITY`.
     * @throws     SecurityException  if the current thread cannot modify
     * this thread.
     * @see .getPriority
     *
     * @see .checkAccess
     * @see .getThreadGroup
     * @see .MAX_PRIORITY
     *
     * @see .MIN_PRIORITY
     *
     * @see ThreadGroup.getMaxPriority
     */
    fun setPriority(newPriority: Int) {
        var newPriority = newPriority
        var g: ThreadGroup
        checkAccess()
        if (newPriority > MAX_PRIORITY || newPriority < MIN_PRIORITY) {
            // Android-changed: Improve exception message when the new priority is out of bounds.
            throw java.lang.IllegalArgumentException("Priority out of range: $newPriority")
        }
        if (threadGroup.also { g = it } != null) {
            if (newPriority > g.getMaxPriority()) {
                newPriority = g.getMaxPriority()
            }
            // Android-changed: Avoid native call if Thread is not yet started.
            // setPriority0(priority = newPriority);
            synchronized(this) {
                priority = newPriority
                if (isAlive) {
                    setPriority0(newPriority)
                }
            }
        }
    }

    /**
     * Returns this thread's priority.
     *
     * @return  this thread's priority.
     * @see .setPriority
     */
    fun getPriority(): Int {
        return priority
    }

    /**
     * Changes the name of this thread to be equal to the argument `name`.
     *
     *
     * First the `checkAccess` method of this thread is called
     * with no arguments. This may result in throwing a
     * `SecurityException`.
     *
     * @param      name   the new name for this thread.
     * @throws     SecurityException  if the current thread cannot modify this
     * thread.
     * @see .getName
     *
     * @see .checkAccess
     */
    @Synchronized
    fun setName(name: String?) {
        checkAccess()
        if (name == null) {
            throw java.lang.NullPointerException("name cannot be null")
        }
        this.name = name
        // Android-changed: Use isAlive() not threadStatus to check whether Thread has started.
        // The threadStatus field is not used in Android.
        // if (threadStatus != 0) {
        if (isAlive) {
            setNativeName(name)
        }
    }

    /**
     * Returns this thread's name.
     *
     * @return  this thread's name.
     * @see .setName
     */
    fun getName(): String {
        return name
    }

    val threadGroup: ThreadGroup?
        /**
         * Returns the thread group to which this thread belongs.
         * This method returns null if this thread has died
         * (been stopped).
         *
         * @return  this thread's thread group.
         */
        get() =// BEGIN Android-added: Work around exit() not being called.
        // Android runtime does not call exit() when a Thread exits so the group field is not
        // set to null so it needs to pretend as if it did. If we are not going to call exit()
        // then this should probably just check isAlive() here rather than getState() as the
            // latter requires a native call.
            if (state == State.TERMINATED) {
                null
            } else group
    // END Android-added: Work around exit() not being called.

    /**
     * Counts the number of stack frames in this thread. The thread must
     * be suspended.
     *
     * @return     the number of stack frames in this thread.
     * @throws     IllegalThreadStateException  if this thread is not
     * suspended.
     */
    @Deprecated(
        """The definition of this call depends on {@link #suspend},
                  which is deprecated.  Further, the results of this call
                  were never well-defined.
                  This method is subject to removal in a future version of Java SE."""
    )  // Android-changed: Provide non-native implementation of countStackFrames().
    // public native int countStackFrames();
    fun countStackFrames(): Int {
        return stackTrace.size
    }
    /**
     * Waits at most `millis` milliseconds for this thread to
     * die. A timeout of `0` means to wait forever.
     *
     *
     *  This implementation uses a loop of `this.wait` calls
     * conditioned on `this.isAlive`. As a thread terminates the
     * `this.notifyAll` method is invoked. It is recommended that
     * applications not use `wait`, `notify`, or
     * `notifyAll` on `Thread` instances.
     *
     * @param  millis
     * the time to wait in milliseconds
     *
     * @throws  IllegalArgumentException
     * if the value of `millis` is negative
     *
     * @throws  InterruptedException
     * if any thread has interrupted the current thread. The
     * *interrupted status* of the current thread is
     * cleared when this exception is thrown.
     */
    // BEGIN Android-changed: Synchronize on separate lock object not this 
    // nativePeer and hence isAlive() can change asynchronously, but Thread::Destroy
    // will always acquire and notify lock after isAlive() changes to false.
    // public final synchronized void join(long millis)
    // END Android-changed: Synchronize on separate lock object not this 
    /**
     * Waits for this thread to die.
     *
     *
     *  An invocation of this method behaves in exactly the same
     * way as the invocation
     *
     * <blockquote>
     * [join][.join]`(0)`
    </blockquote> *
     *
     * @throws  InterruptedException
     * if any thread has interrupted the current thread. The
     * *interrupted status* of the current thread is
     * cleared when this exception is thrown.
     */
    @JvmOverloads
    @Throws(java.lang.InterruptedException::class)
    fun join(millis: Long = 0) {
        synchronized(lock) {
            val base: Long = java.lang.System.currentTimeMillis()
            var now: Long = 0
            if (millis < 0) {
                throw java.lang.IllegalArgumentException("timeout value is negative")
            }
            if (millis == 0L) {
                while (isAlive) {
                    (lock as java.lang.Object).wait(0)
                }
            } else {
                while (isAlive) {
                    val delay = millis - now
                    if (delay <= 0) {
                        break
                    }
                    (lock as java.lang.Object).wait(delay)
                    now = java.lang.System.currentTimeMillis() - base
                }
            }
        }
    }
    // END Android-changed: Synchronize on separate lock object not this 
    /**
     * Waits at most `millis` milliseconds plus
     * `nanos` nanoseconds for this thread to die.
     * If both arguments are `0`, it means to wait forever.
     *
     *
     *  This implementation uses a loop of `this.wait` calls
     * conditioned on `this.isAlive`. As a thread terminates the
     * `this.notifyAll` method is invoked. It is recommended that
     * applications not use `wait`, `notify`, or
     * `notifyAll` on `Thread` instances.
     *
     * @param  millis
     * the time to wait in milliseconds
     *
     * @param  nanos
     * `0-999999` additional nanoseconds to wait
     *
     * @throws  IllegalArgumentException
     * if the value of `millis` is negative, or the value
     * of `nanos` is not in the range `0-999999`
     *
     * @throws  InterruptedException
     * if any thread has interrupted the current thread. The
     * *interrupted status* of the current thread is
     * cleared when this exception is thrown.
     */
    // BEGIN Android-changed: Synchronize on separate lock object not this 
    // public final synchronized void join(long millis, int nanos)
    @Throws(java.lang.InterruptedException::class)
    fun join(millis: Long, nanos: Int) {
        var millis = millis
        synchronized(lock) {
            if (millis < 0) {
                throw java.lang.IllegalArgumentException("timeout value is negative")
            }
            if (nanos < 0 || nanos > 999999) {
                throw java.lang.IllegalArgumentException(
                    "nanosecond timeout value out of range"
                )
            }
            if (nanos >= 500000 || nanos != 0 && millis == 0L) {
                millis++
            }
            join(millis)
        }
    }

    /**
     * Marks this thread as either a [daemon][.isDaemon] thread
     * or a user thread. The Java Virtual Machine exits when the only
     * threads running are all daemon threads.
     *
     *
     *  This method must be invoked before the thread is started.
     *
     * @param  on
     * if `true`, marks this thread as a daemon thread
     *
     * @throws  IllegalThreadStateException
     * if this thread is [alive][.isAlive]
     *
     * @throws  SecurityException
     * if [.checkAccess] determines that the current
     * thread cannot modify this thread
     */
    fun setDaemon(on: Boolean) {
        checkAccess()
        if (isAlive) {
            throw java.lang.IllegalThreadStateException()
        }
        daemon = on
    }

    /**
     * Tests if this thread is a daemon thread.
     *
     * @return  `true` if this thread is a daemon thread;
     * `false` otherwise.
     * @see .setDaemon
     */
    fun isDaemon(): Boolean {
        return daemon
    }

    /**
     * Determines if the currently running thread has permission to
     * modify this thread.
     *
     *
     * If there is a security manager, its `checkAccess` method
     * is called with this thread as its argument. This may result in
     * throwing a `SecurityException`.
     *
     * @throws  SecurityException  if the current thread is not allowed to
     * access this thread.
     * @see SecurityManager.checkAccess
     */
    fun checkAccess() {
        // Android-removed: SecurityManager stubbed out on Android.
        // SecurityManager security = System.getSecurityManager();
        // if (security != null) {
        //     security.checkAccess(this);
        // }
    }

    /**
     * Returns a string representation of this thread, including the
     * thread's name, priority, and thread group.
     *
     * @return  a string representation of this thread.
     */
    override fun toString(): String {
        val group: ThreadGroup? = threadGroup
        return if (group != null) {
            "Thread[" + getName() + "," + getPriority() + "," +
                    group.getName() + "]"
        } else {
            "Thread[" + getName() + "," + getPriority() + "," +
                    "" + "]"
        }
    }

    /**
     * Returns the context `ClassLoader` for this thread. The context
     * `ClassLoader` is provided by the creator of the thread for use
     * by code running in this thread when loading classes and resources.
     * If not [set][.setContextClassLoader], the default is the
     * `ClassLoader` context of the parent thread. The context
     * `ClassLoader` of the
     * primordial thread is typically set to the class loader used to load the
     * application.
     *
     *
     * @return  the context `ClassLoader` for this thread, or `null`
     * indicating the system class loader (or, failing that, the
     * bootstrap class loader)
     *
     * @throws  SecurityException
     * if the current thread cannot get the context ClassLoader
     *
     * @since 1.2
     */
    @CallerSensitive
    fun getContextClassLoader(): java.lang.ClassLoader? {
        // Android-removed: SecurityManager stubbed out on Android.
        /*
        if (contextClassLoader == null)
            return null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            ClassLoader.checkClassLoaderPermission(contextClassLoader,
                                                   Reflection.getCallerClass());
        }
        */
        return contextClassLoader
    }

    /**
     * Sets the context ClassLoader for this  The context
     * ClassLoader can be set when a thread is created, and allows
     * the creator of the thread to provide the appropriate class loader,
     * through `getContextClassLoader`, to code running in the thread
     * when loading classes and resources.
     *
     *
     * If a security manager is present, its [ ][SecurityManager.checkPermission]
     * method is invoked with a [RuntimePermission]`("setContextClassLoader")` permission to see if setting the context
     * ClassLoader is permitted.
     *
     * @param  cl
     * the context ClassLoader for this Thread, or null  indicating the
     * system class loader (or, failing that, the bootstrap class loader)
     *
     * @throws  SecurityException
     * if the current thread cannot set the context ClassLoader
     *
     * @since 1.2
     */
    open fun setContextClassLoader(cl: java.lang.ClassLoader?) {
        // Android-removed: SecurityManager stubbed out on Android.
        // SecurityManager sm = System.getSecurityManager();
        // if (sm != null) {
        //     sm.checkPermission(new RuntimePermission("setContextClassLoader"));
        // }
        contextClassLoader = cl
    }

    val stackTrace: Array<Any>
        /**
         * Returns an array of stack trace elements representing the stack dump
         * of this thread.  This method will return a zero-length array if
         * this thread has not started, has started but has not yet been
         * scheduled to run by the system, or has terminated.
         * If the returned array is of non-zero length then the first element of
         * the array represents the top of the stack, which is the most recent
         * method invocation in the sequence.  The last element of the array
         * represents the bottom of the stack, which is the least recent method
         * invocation in the sequence.
         *
         *
         * If there is a security manager, and this thread is not
         * the current thread, then the security manager's
         * `checkPermission` method is called with a
         * `RuntimePermission("getStackTrace")` permission
         * to see if it's ok to get the stack trace.
         *
         *
         * Some virtual machines may, under some circumstances, omit one
         * or more stack frames from the stack trace.  In the extreme case,
         * a virtual machine that has no stack trace information concerning
         * this thread is permitted to return a zero-length array from this
         * method.
         *
         * @return an array of `StackTraceElement`,
         * each represents one stack frame.
         *
         * @throws SecurityException
         * if a security manager exists and its
         * `checkPermission` method doesn't allow
         * getting the stack trace of thread.
         * @see SecurityManager.checkPermission
         *
         * @see RuntimePermission
         *
         * @see Throwable.getStackTrace
         *
         *
         * @since 1.5
         */
        get() {
            // BEGIN Android-changed: Use native VMStack to get stack trace.
            /*
            if (this != currentThread()) {
                // check for getStackTrace permission
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    security.checkPermission(
                        SecurityConstants.GET_STACK_TRACE_PERMISSION);
                }
                // optimization so we do not call into the vm for threads that
                // have not yet started or have terminated
                if (!isAlive()) {
                    return EMPTY_STACK_TRACE;
                }
                StackTraceElement[][] stackTraceArray = dumpThreads(new Thread[] {this});
                StackTraceElement[] stackTrace = stackTraceArray[0];
                // a thread that was alive during the previous isAlive call may have
                // since terminated, therefore not having a stacktrace.
                if (stackTrace == null) {
                    stackTrace = EMPTY_STACK_TRACE;
                }
                return stackTrace;
            } else {
                return (new Exception()).getStackTrace();
            }
            */
            val ste: Array<java.lang.StackTraceElement> = VMStack.getThreadStackTrace(this)
            return ste ?: EmptyArray.STACK_TRACE_ELEMENT
            // END Android-changed: Use native VMStack to get stack trace.
        }

    /** cache of subclass security audit results  */ /* Replace with ConcurrentReferenceHashMap when/if it appears in a future
     * release */
    private object Caches {
        /** cache of subclass security audit results  */
        val subclassAudits: java.util.concurrent.ConcurrentMap<WeakClassKey, Boolean> =
            java.util.concurrent.ConcurrentHashMap<WeakClassKey, Boolean>()

        /** queue for WeakReferences to audited subclasses  */
        val subclassAuditsQueue: java.lang.ref.ReferenceQueue<java.lang.Class<*>> =
            java.lang.ref.ReferenceQueue<java.lang.Class<*>>()
    }
    // Android-removed: Native methods that are unused on Android.
    // private static native StackTraceElement[][] dumpThreads(Thread[] threads);
    // private static native Thread[] getThreads();
    /**
     * A thread state.  A thread can be in one of the following states:
     *
     *  * [.NEW]<br></br>
     * A thread that has not yet started is in this state.
     *
     *  * [.RUNNABLE]<br></br>
     * A thread executing in the Java virtual machine is in this state.
     *
     *  * [.BLOCKED]<br></br>
     * A thread that is blocked waiting for a monitor lock
     * is in this state.
     *
     *  * [.WAITING]<br></br>
     * A thread that is waiting indefinitely for another thread to
     * perform a particular action is in this state.
     *
     *  * [.TIMED_WAITING]<br></br>
     * A thread that is waiting for another thread to perform an action
     * for up to a specified waiting time is in this state.
     *
     *  * [.TERMINATED]<br></br>
     * A thread that has exited is in this state.
     *
     *
     *
     *
     *
     * A thread can be in only one state at a given point in time.
     * These states are virtual machine states which do not reflect
     * any operating system thread states.
     *
     * @since   1.5
     * @see .getState
     */
    enum class State {
        /**
         * Thread state for a thread which has not yet started.
         */
        NEW,

        /**
         * Thread state for a runnable thread.  A thread in the runnable
         * state is executing in the Java virtual machine but it may
         * be waiting for other resources from the operating system
         * such as processor.
         */
        RUNNABLE,

        /**
         * Thread state for a thread blocked waiting for a monitor lock.
         * A thread in the blocked state is waiting for a monitor lock
         * to enter a synchronized block/method or
         * reenter a synchronized block/method after calling
         * [Object.wait].
         */
        BLOCKED,

        /**
         * Thread state for a waiting thread.
         * A thread is in the waiting state due to calling one of the
         * following methods:
         *
         *  * [Object.wait] with no timeout
         *  * [join][.join] with no timeout
         *  * [LockSupport.park]
         *
         *
         *
         * A thread in the waiting state is waiting for another thread to
         * perform a particular action.
         *
         * For example, a thread that has called `Object.wait()`
         * on an object is waiting for another thread to call
         * `Object.notify()` or `Object.notifyAll()` on
         * that object. A thread that has called `join()`
         * is waiting for a specified thread to terminate.
         */
        WAITING,

        /**
         * Thread state for a waiting thread with a specified waiting time.
         * A thread is in the timed waiting state due to calling one of
         * the following methods with a specified positive waiting time:
         *
         *  * [sleep][.sleep]
         *  * [Object.wait] with timeout
         *  * [join][.join] with timeout
         *  * [LockSupport.parkNanos]
         *  * [LockSupport.parkUntil]
         *
         */
        TIMED_WAITING,

        /**
         * Thread state for a terminated thread.
         * The thread has completed execution.
         */
        TERMINATED
    }

    val state: State
        /**
         * Returns the state of this thread.
         * This method is designed for use in monitoring of the system state,
         * not for synchronization control.
         *
         * @return this thread's state.
         * @since 1.5
         */
        get() =// get current thread state
        // Android-changed: Replace unused threadStatus field with started field.
        // Use Android specific nativeGetStatus() method. See comment on started field for more
        // information.
            // return sun.misc.VM.toThreadState(threadStatus);
            State.entries.get(nativeGetStatus(started))
    // Added in JSR-166
    /**
     * Interface for handlers invoked when a `Thread` abruptly
     * terminates due to an uncaught exception.
     *
     * When a thread is about to terminate due to an uncaught exception
     * the Java Virtual Machine will query the thread for its
     * `UncaughtExceptionHandler` using
     * [.getUncaughtExceptionHandler] and will invoke the handler's
     * `uncaughtException` method, passing the thread and the
     * exception as arguments.
     * If a thread has not had its `UncaughtExceptionHandler`
     * explicitly set, then its `ThreadGroup` object acts as its
     * `UncaughtExceptionHandler`. If the `ThreadGroup` object
     * has no
     * special requirements for dealing with the exception, it can forward
     * the invocation to the [ default uncaught exception handler][.getDefaultUncaughtExceptionHandler].
     *
     * @see .setDefaultUncaughtExceptionHandler
     *
     * @see .setUncaughtExceptionHandler
     *
     * @see ThreadGroup.uncaughtException
     *
     * @since 1.5
     */
    fun interface UncaughtExceptionHandler {
        /**
         * Method invoked when the given thread terminates due to the
         * given uncaught exception.
         *
         * Any exception thrown by this method will be ignored by the
         * Java Virtual Machine.
         * @param t the thread
         * @param e the exception
         */
        fun uncaughtException(t: Thread?, e: Throwable?)
    }

    // null unless explicitly set
    @Volatile
    private var uncaughtExceptionHandler: UncaughtExceptionHandler? = null
    // END Android-added: The concept of an uncaughtExceptionPreHandler for use by platform.
    /**
     * Returns the handler invoked when this thread abruptly terminates
     * due to an uncaught exception. If this thread has not had an
     * uncaught exception handler explicitly set then this thread's
     * `ThreadGroup` object is returned, unless this thread
     * has terminated, in which case `null` is returned.
     * @since 1.5
     * @return the uncaught exception handler for this thread
     */
    fun getUncaughtExceptionHandler(): UncaughtExceptionHandler {
        return if (uncaughtExceptionHandler != null) uncaughtExceptionHandler else group
    }

    /**
     * Set the handler invoked when this thread abruptly terminates
     * due to an uncaught exception.
     *
     * A thread can take full control of how it responds to uncaught
     * exceptions by having its uncaught exception handler explicitly set.
     * If no such handler is set then the thread's `ThreadGroup`
     * object acts as its handler.
     * @param eh the object to use as this thread's uncaught exception
     * handler. If `null` then this thread has no explicit handler.
     * @throws  SecurityException  if the current thread is not allowed to
     * modify this thread.
     * @see .setDefaultUncaughtExceptionHandler
     *
     * @see ThreadGroup.uncaughtException
     *
     * @since 1.5
     */
    fun setUncaughtExceptionHandler(eh: UncaughtExceptionHandler?) {
        checkAccess()
        uncaughtExceptionHandler = eh
    }

    /**
     * Dispatch an uncaught exception to the handler. This method is
     * intended to be called only by the runtime and by tests.
     *
     * @hide
     */
    // Android-changed: Make dispatchUncaughtException() public, for use by tests.
    fun dispatchUncaughtException(e: Throwable?) {
        // BEGIN Android-added: uncaughtExceptionPreHandler for use by platform.
        val initialUeh: UncaughtExceptionHandler =
            getUncaughtExceptionPreHandler()
        if (initialUeh != null) {
            try {
                initialUeh.uncaughtException(this, e)
            } catch (ignored: java.lang.RuntimeException) {
                // Throwables thrown by the initial handler are ignored
            } catch (ignored: java.lang.Error) {
            }
        }
        // END Android-added: uncaughtExceptionPreHandler for use by platform.
        getUncaughtExceptionHandler().uncaughtException(this, e)
    }
    // BEGIN Android-added: The concept of "system-daemon" threads. See java.lang.Daemons.
    /**
     * Marks this thread as either a special runtime-managed ("system daemon")
     * thread or a normal (i.e. app code created) daemon thread.)
     *
     *
     * System daemon threads get special handling when starting up in some
     * cases.
     *
     *
     * This method must be invoked before the thread is started.
     *
     *
     * This method must only be invoked on Thread instances that have already
     * had `setDaemon(true)` called on them.
     *
     *
     * Package-private since only [java.lang.Daemons] needs to call
     * this.
     *
     * @param  on if `true`, marks this thread as a system daemon thread
     *
     * @throws  IllegalThreadStateException
     * if this thread is [alive][.isAlive] or not a
     * [daemon][.isDaemon]
     *
     * @throws  SecurityException
     * if [.checkAccess] determines that the current
     * thread cannot modify this thread
     *
     * @hide For use by Daemons.java only.
     */
    fun setSystemDaemon(on: Boolean) {
        checkAccess()
        if (isAlive || !isDaemon()) {
            throw Exception()
//            throw java.lang.IllegalThreadStateException()
        }
        systemDaemon = on
    }

    /**
     * Weak key for Class objects.
     */
    internal class WeakClassKey(
        cl: java.lang.Class<*>?,
        refQueue: java.lang.ref.ReferenceQueue<java.lang.Class<*>?>?
    ) :
        java.lang.ref.WeakReference<java.lang.Class<*>?>(cl, refQueue) {
        /**
         * saved value of the referent's identity hash code, to maintain
         * a consistent hash code after the referent has been cleared
         */
        private val hash: Int

        /**
         * Create a new WeakClassKey to the given object, registered
         * with a queue.
         */
        init {
            hash = java.lang.System.identityHashCode(cl)
        }

        /**
         * Returns the identity hash code of the original referent.
         */
        override fun hashCode(): Int {
            return hash
        }

        /**
         * Returns true if the given object is this identical
         * WeakClassKey instance, or, if this object's referent has not
         * been cleared, if the given object is another WeakClassKey
         * instance with the identical non-null referent as this one.
         */
        override fun equals(obj: Any?): Boolean {
            if (obj === this) return true
            return if (obj is WeakClassKey) {
                val referent: Any = get()
                referent != null && referent === (obj as WeakClassKey?).get()
            } else {
                false
            }
        }
    }
    // The following three initially uninitialized fields are exclusively
    // managed by class java.util.concurrent.ThreadLocalRandom. These
    // fields are used to build the high-performance PRNGs in the
    // concurrent code, and we can not risk accidental false sharing.
    // Hence, the fields are isolated with @Contended.
    /** The current seed for a ThreadLocalRandom  */
    @Contended("tlr")
    var threadLocalRandomSeed: Long = 0

    /** Probe hash value; nonzero if threadLocalRandomSeed initialized  */
    @Contended("tlr")
    var threadLocalRandomProbe = 0

    /** Secondary seed isolated from public ThreadLocalRandom sequence  */
    @Contended("tlr")
    var threadLocalRandomSecondarySeed = 0

    /* Some private helper methods */
    private external fun setPriority0(newPriority: Int)

    // BEGIN Android-removed: Native methods that are unused on Android.
    /*
    private native void stop0(Object o);
    private native void suspend0();
    private native void resume0();
    */
    // END Android-removed: Native methods that are unused on Android.
    @FastNative
    private external fun interrupt0()
    private external fun setNativeName(name: String)

    // Android-added: Android specific nativeGetStatus() method.
    private external fun nativeGetStatus(hasBeenStarted: Boolean): Int

    companion object {
        /* For autonumbering anonymous threads. */
        private const val threadInitNumber = 0
        @Synchronized
        private fun nextThreadNum(): Int {
            return threadInitNumber++
        }

        /* For generating thread ID */
        private const val threadSeqNumber: Long = 0
        @Synchronized
        private fun nextThreadID(): Long {
            return ++threadSeqNumber
        }

        /**
         * The minimum priority that a thread can have.
         */
        const val MIN_PRIORITY = 1

        /**
         * The default priority that is assigned to a thread.
         */
        const val NORM_PRIORITY = 5

        /**
         * The maximum priority that a thread can have.
         */
        const val MAX_PRIORITY = 10

        /**
         * Returns a reference to the currently executing thread object.
         *
         * @return  the currently executing thread.
         */
//        @IntrinsicCandidate
//        @FastNative
        external fun currentThread(): Thread

        /**
         * A hint to the scheduler that the current thread is willing to yield
         * its current use of a processor. The scheduler is free to ignore this
         * hint.
         *
         *
         *  Yield is a heuristic attempt to improve relative progression
         * between threads that would otherwise over-utilise a CPU. Its use
         * should be combined with detailed profiling and benchmarking to
         * ensure that it actually has the desired effect.
         *
         *
         *  It is rarely appropriate to use this method. It may be useful
         * for debugging or testing purposes, where it may help to reproduce
         * bugs due to race conditions. It may also be useful when designing
         * concurrency control constructs such as the ones in the
         * [java.util.concurrent.locks] package.
         */
        external fun yield()
//        @FastNative
//        @Throws(java.lang.InterruptedException::class)
        private external fun sleep(lock: Any, millis: Long, nanos: Int)
        // END Android-changed: Implement sleep() methods using a shared native implementation.
        /**
         * Causes the currently executing thread to sleep (temporarily cease
         * execution) for the specified number of milliseconds plus the specified
         * number of nanoseconds, subject to the precision and accuracy of system
         * timers and schedulers. The thread does not lose ownership of any
         * monitors.
         *
         * @param  millis
         * the length of time to sleep in milliseconds
         *
         * @param  nanos
         * `0-999999` additional nanoseconds to sleep
         *
         * @throws  IllegalArgumentException
         * if the value of `millis` is negative, or the value of
         * `nanos` is not in the range `0-999999`
         *
         * @throws  InterruptedException
         * if any thread has interrupted the current thread. The
         * *interrupted status* of the current thread is
         * cleared when this exception is thrown.
         */
        /**
         * Causes the currently executing thread to sleep (temporarily cease
         * execution) for the specified number of milliseconds, subject to
         * the precision and accuracy of system timers and schedulers. The thread
         * does not lose ownership of any monitors.
         *
         * @param  millis
         * the length of time to sleep in milliseconds
         *
         * @throws  IllegalArgumentException
         * if the value of `millis` is negative
         *
         * @throws  InterruptedException
         * if any thread has interrupted the current thread. The
         * *interrupted status* of the current thread is
         * cleared when this exception is thrown.
         */
        // BEGIN Android-changed: Implement sleep() methods using a shared native implementation.
        @JvmOverloads
        @Throws(java.lang.InterruptedException::class)
        fun sleep(millis: Long, nanos: Int = 0) {
            // BEGIN Android-changed: Improve exception messages.
            /*
        if (millis < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }

        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException(
                                "nanosecond timeout value out of range");
        }
        */
            var millis = millis
            var nanos = nanos
            if (millis < 0) {
                throw java.lang.IllegalArgumentException("millis < 0: $millis")
            }
            if (nanos < 0) {
                throw java.lang.IllegalArgumentException("nanos < 0: $nanos")
            }
            if (nanos > 999999) {
                throw java.lang.IllegalArgumentException("nanos > 999999: $nanos")
            }
            // END Android-changed: Improve exception messages.

            // BEGIN Android-changed: Implement sleep() methods using a shared native implementation.
            // Attempt nanosecond rather than millisecond accuracy for sleep();
            // RI code rounds to the nearest millisecond.
            /*
        if (nanos >= 500000 || (nanos != 0 && millis == 0)) {
            millis++;
        }

        sleep(millis);
        */
            // The JLS 3rd edition, section 17.9 says: "...sleep for zero
            // time...need not have observable effects."
            if (millis == 0L && nanos == 0) {
                // ...but we still have to handle being interrupted.
                if (interrupted()) {
                    throw java.lang.InterruptedException()
                }
                return
            }
            val nanosPerMilli = 1000000
            val durationNanos: Long
            durationNanos = if (millis >= Long.MAX_VALUE / nanosPerMilli - 1L) {
                // > 292 years. Avoid overflow by capping it at roughly 292 years.
                Long.MAX_VALUE
            } else {
                millis * nanosPerMilli + nanos
            }
            val startNanos: Long = java.lang.System.nanoTime()
            val lock: Any = currentThread().lock

            // The native sleep(...) method actually does a monitor wait, which may return
            // early, so loop until sleep duration passes. The monitor is only notified when
            // we exit, which can't happen while we're sleeping.
            synchronized(lock) {
                var elapsed = 0L
                while (elapsed < durationNanos) {
                    val remaining = durationNanos - elapsed
                    millis = remaining / nanosPerMilli
                    nanos = (remaining % nanosPerMilli).toInt()
                    sleep(lock, millis, nanos)
                    elapsed = java.lang.System.nanoTime() - startNanos
                }
            }
            // END Android-changed: Implement sleep() methods using a shared native implementation.
        }

        /**
         * Indicates that the caller is momentarily unable to progress, until the
         * occurrence of one or more actions on the part of other activities. By
         * invoking this method within each iteration of a spin-wait loop construct,
         * the calling thread indicates to the runtime that it is busy-waiting.
         * The runtime may take action to improve the performance of invoking
         * spin-wait loop constructions.
         *
         * @apiNote
         * As an example consider a method in a class that spins in a loop until
         * some flag is set outside of that method. A call to the `onSpinWait`
         * method should be placed inside the spin loop.
         * <pre>`class EventHandler {
         * volatile boolean eventNotificationNotReceived;
         * void waitForEventAndHandleIt() {
         * while ( eventNotificationNotReceived ) {
         * onSpinWait();
         * }
         * readAndProcessEvent();
         * }
         *
         * void readAndProcessEvent() {
         * // Read event from some source and process it
         * . . .
         * }
         * }
        `</pre> *
         *
         *
         * The code above would remain correct even if the `onSpinWait`
         * method was not called at all. However on some architectures the Java
         * Virtual Machine may issue the processor instructions to address such
         * code patterns in a more beneficial way.
         *
         * @since 9
         */
        @IntrinsicCandidate
        fun onSpinWait() {
        }

        // Android-changed: Use Android specific nativeCreate() method to create/start thread.
        // The upstream native method start0() only takes a reference to this object and so must obtain
        // the stack size and daemon status directly from the field whereas Android supplies the values
        // explicitly on the method call.
        // private native void start0();
        private external fun nativeCreate(t: Thread, stackSize: Long, daemon: Boolean)

        /**
         * Tests whether the current thread has been interrupted.  The
         * *interrupted status* of the thread is cleared by this method.  In
         * other words, if this method were to be called twice in succession, the
         * second call would return false (unless the current thread were
         * interrupted again, after the first call had cleared its interrupted
         * status and before the second call had examined it).
         *
         *
         * A thread interruption ignored because a thread was not alive
         * at the time of the interrupt will be reflected by this method
         * returning false.
         *
         * @return  `true` if the current thread has been interrupted;
         * `false` otherwise.
         * @see .isInterrupted
         * @revised 6.0
         */
        // Android-changed: Use native interrupted()/isInterrupted() methods.
        // Upstream has one native method for both these methods that takes a boolean parameter that
        // determines whether the interrupted status of the thread should be cleared after reading
        // it. While that approach does allow code reuse it is less efficient/more complex than having
        // a native implementation of each method because:
        // * The pure Java interrupted() method requires two native calls, one to get the current
        //   thread and one to get its interrupted status.
        // * Updating the interrupted flag is more complex than simply reading it. Knowing that only
        //   the current thread can clear the interrupted status makes the code simpler as it does not
        //   need to be concerned about multiple threads trying to clear the status simultaneously.
        // public static boolean interrupted() {
        //     return currentThread().isInterrupted(true);
        // }
        @FastNative
        external fun interrupted(): Boolean

        /**
         * Returns an estimate of the number of active threads in the current
         * thread's [thread group][ThreadGroup] and its
         * subgroups. Recursively iterates over all subgroups in the current
         * thread's thread group.
         *
         *
         *  The value returned is only an estimate because the number of
         * threads may change dynamically while this method traverses internal
         * data structures, and might be affected by the presence of certain
         * system threads. This method is intended primarily for debugging
         * and monitoring purposes.
         *
         * @return  an estimate of the number of active threads in the current
         * thread's thread group and in any other thread group that
         * has the current thread's thread group as an ancestor
         */
        fun activeCount(): Int {
            return currentThread().getThreadGroup().activeCount()
        }

        /**
         * Copies into the specified array every active thread in the current
         * thread's thread group and its subgroups. This method simply
         * invokes the [ThreadGroup.enumerate]
         * method of the current thread's thread group.
         *
         *
         *  An application might use the [activeCount][.activeCount]
         * method to get an estimate of how big the array should be, however
         * *if the array is too short to hold all the threads, the extra threads
         * are silently ignored.*  If it is critical to obtain every active
         * thread in the current thread's thread group and its subgroups, the
         * invoker should verify that the returned int value is strictly less
         * than the length of `tarray`.
         *
         *
         *  Due to the inherent race condition in this method, it is recommended
         * that the method only be used for debugging and monitoring purposes.
         *
         * @param  tarray
         * an array into which to put the list of threads
         *
         * @return  the number of threads put into the array
         *
         * @throws  SecurityException
         * if [ThreadGroup.checkAccess] determines that
         * the current thread cannot access its thread group
         */
        fun enumerate(tarray: Array<Thread?>?): Int {
            return currentThread().getThreadGroup().enumerate(tarray)
        }

        /**
         * Prints a stack trace of the current thread to the standard error stream.
         * This method is used only for debugging.
         *
         * @see Throwable.printStackTrace
         */
        fun dumpStack() {
            java.lang.Exception("Stack trace").printStackTrace()
        }

        /**
         * Returns `true` if and only if the current thread holds the
         * monitor lock on the specified object.
         *
         *
         * This method is designed to allow a program to assert that
         * the current thread already holds a specified lock:
         * <pre>
         * assert holdsLock(obj);
        </pre> *
         *
         * @param  obj the object on which to test lock ownership
         * @throws NullPointerException if obj is `null`
         * @return `true` if the current thread holds the monitor lock on
         * the specified object.
         * @since 1.4
         */
        external fun holdsLock(obj: Any?): Boolean
        private val EMPTY_STACK_TRACE: Array<java.lang.StackTraceElement?> =
            arrayOfNulls<java.lang.StackTraceElement>(0)

        // Android-removed: SecurityManager paragraph.
        val allStackTraces: Map<Any?, Array<Any>>
            /**
             * Returns a map of stack traces for all live threads.
             * The map keys are threads and each map value is an array of
             * `StackTraceElement` that represents the stack dump
             * of the corresponding `Thread`.
             * The returned stack traces are in the format specified for
             * the [getStackTrace][.getStackTrace] method.
             *
             *
             * The threads may be executing while this method is called.
             * The stack trace of each thread only represents a snapshot and
             * each stack trace may be obtained at different time.  A zero-length
             * array will be returned in the map value if the virtual machine has
             * no stack trace information about a thread.
             *
             * @return a `Map` from `Thread` to an array of
             * `StackTraceElement` that represents the stack trace of
             * the corresponding thread.
             *
             * @see .getStackTrace
             *
             * @see SecurityManager.checkPermission
             *
             * @see RuntimePermission
             *
             * @see Throwable.getStackTrace
             *
             *
             * @since 1.5
             */
            get() {
                // Android-removed: SecurityManager stubbed out on Android.
                /*
            // check for getStackTrace permission
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(
                    SecurityConstants.GET_STACK_TRACE_PERMISSION);
                security.checkPermission(
                    SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
            }
            */

                // Get a snapshot of the list of all threads
                // BEGIN Android-changed: Use ThreadGroup and getStackTrace() instead of native methods.
                // Allocate a bit more space than needed, in case new ones are just being created.
                /*
            Thread[] threads = getThreads();
            StackTraceElement[][] traces = dumpThreads(threads);
            Map<Thread, StackTraceElement[]> m = new HashMap<>(threads.length);
            for (int i = 0; i < threads.length; i++) {
                StackTraceElement[] stackTrace = traces[i];
                if (stackTrace != null) {
                    m.put(threads[i], stackTrace);
                }
                // else terminated so we don't put it in the map
            }
            */
                var count: Int = ThreadGroup.systemThreadGroup.activeCount()
                val threads: Array<Thread?> =
                    arrayOfNulls<Thread>(count + count / 2)

                // Enumerate the threads.
                count = ThreadGroup.systemThreadGroup.enumerate(threads)

                // Collect the stacktraces
                val m: MutableMap<Thread?, Array<java.lang.StackTraceElement>> =
                    java.util.HashMap<Thread, Array<java.lang.StackTraceElement>>()
                for (i in 0 until count) {
                    val stackTrace: Array<java.lang.StackTraceElement> = threads[i].getStackTrace()
                    m[threads[i]] = stackTrace
                }
                // END Android-changed: Use ThreadGroup and getStackTrace() instead of native methods.
                return m
            }
        private val SUBCLASS_IMPLEMENTATION_PERMISSION: java.lang.RuntimePermission =
            java.lang.RuntimePermission("enableContextClassLoaderOverride")

        /**
         * Verifies that this (possibly subclass) instance can be constructed
         * without violating security constraints: the subclass must not override
         * security-sensitive non-final methods, or else the
         * "enableContextClassLoaderOverride" RuntimePermission is checked.
         */
        private fun isCCLOverridden(cl: java.lang.Class<*>): Boolean {
            if (cl == Thread::class.java) return false
            processQueue(
                Caches.subclassAuditsQueue,
                Caches.subclassAudits
            )
            val key: WeakClassKey =
                WeakClassKey(cl, Caches.subclassAuditsQueue)
            var result: Boolean = Caches.subclassAudits.get(key)
            if (result == null) {
                result = auditSubclass(cl)
                Caches.subclassAudits.putIfAbsent(key, result)
            }
            return result
        }

        /**
         * Performs reflective checks on given subclass to verify that it doesn't
         * override security-sensitive non-final methods.  Returns true if the
         * subclass overrides any of the methods, false otherwise.
         */
        private fun auditSubclass(subcl: java.lang.Class<*>): Boolean {
            return java.security.AccessController.doPrivileged<Boolean>(
                object : java.security.PrivilegedAction<Boolean?>() {
                    override fun run(): Boolean {
                        var cl: java.lang.Class<*> = subcl
                        while (cl != Thread::class.java) {
                            try {
                                cl.getDeclaredMethod(
                                    "getContextClassLoader",
                                    *arrayOfNulls<java.lang.Class<*>>(0)
                                )
                                return java.lang.Boolean.TRUE
                            } catch (ex: java.lang.NoSuchMethodException) {
                            }
                            try {
                                val params: Array<java.lang.Class<*>> =
                                    arrayOf<java.lang.Class<*>>(
                                        java.lang.ClassLoader::class.java
                                    )
                                cl.getDeclaredMethod("setContextClassLoader", *params)
                                return java.lang.Boolean.TRUE
                            } catch (ex: java.lang.NoSuchMethodException) {
                            }
                            cl = cl.getSuperclass()
                        }
                        return java.lang.Boolean.FALSE
                    }
                }
            )
        }

        // null unless explicitly set
        @Volatile
        var defaultUncaughtExceptionHandler: UncaughtExceptionHandler?
            /**
             * Returns the default handler invoked when a thread abruptly terminates
             * due to an uncaught exception. If the returned value is `null`,
             * there is no default.
             * @since 1.5
             * @see .setDefaultUncaughtExceptionHandler
             *
             * @return the default uncaught exception handler for all threads
             */
            get() = defaultUncaughtExceptionHandler
            /**
             * Set the default handler invoked when a thread abruptly terminates
             * due to an uncaught exception, and no other handler has been defined
             * for that thread.
             *
             *
             * Uncaught exception handling is controlled first by the thread, then
             * by the thread's [ThreadGroup] object and finally by the default
             * uncaught exception handler. If the thread does not have an explicit
             * uncaught exception handler set, and the thread's thread group
             * (including parent thread groups)  does not specialize its
             * `uncaughtException` method, then the default handler's
             * `uncaughtException` method will be invoked.
             *
             * By setting the default uncaught exception handler, an application
             * can change the way in which uncaught exceptions are handled (such as
             * logging to a specific device, or file) for those threads that would
             * already accept whatever &quot;default&quot; behavior the system
             * provided.
             *
             *
             * Note that the default uncaught exception handler should not usually
             * defer to the thread's `ThreadGroup` object, as that could cause
             * infinite recursion.
             *
             * @param eh the object to use as the default uncaught exception handler.
             * If `null` then there is no default handler.
             *
             * @see .setUncaughtExceptionHandler
             *
             * @see .getUncaughtExceptionHandler
             *
             * @see ThreadGroup.uncaughtException
             *
             * @since 1.5
             */
            set(eh) {
                // Android-removed: SecurityManager stubbed out on Android.
                /*
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(
                    new RuntimePermission("setDefaultUncaughtExceptionHandler")
                        );
            }
            */
                defaultUncaughtExceptionHandler = eh
            }

        // Android-removed: SecurityManager throws clause.
        // BEGIN Android-added: The concept of an uncaughtExceptionPreHandler for use by platform.
        // See http://b/29624607 for background information.
        // null unless explicitly set
        @Volatile
        var uncaughtExceptionPreHandler: UncaughtExceptionHandler?
            /**
             * Gets an [UncaughtExceptionHandler] that will be called before any
             * returned by [.getUncaughtExceptionHandler]. Can be `null` if
             * was not explicitly set with
             * [.setUncaughtExceptionPreHandler].
             *
             * @return the uncaught exception prehandler for this thread
             *
             * @hide
             */
            get() = uncaughtExceptionPreHandler
            /**
             * Sets an [UncaughtExceptionHandler] that will be called before any
             * returned by [.getUncaughtExceptionHandler]. To allow the standard
             * handlers to run, this handler should never terminate this process. Any
             * throwables thrown by the handler will be ignored by
             * [.dispatchUncaughtException].
             *
             * @hide used when configuring the runtime for exception logging; see
             * [dalvik.system.RuntimeHooks] b/29624607
             */
            set(eh) {
                uncaughtExceptionPreHandler = eh
            }
        // END Android-added: The concept of "system-daemon" threads. See java.lang.Daemons.
        /**
         * Removes from the specified map any keys that have been enqueued
         * on the specified reference queue.
         */
        fun processQueue(
            queue: java.lang.ref.ReferenceQueue<java.lang.Class<*>?>,
            map: java.util.concurrent.ConcurrentMap<out java.lang.ref.WeakReference<java.lang.Class<*>?>?, *>
        ) {
            var ref: java.lang.ref.Reference<out java.lang.Class<*>?>?
            while (queue.poll().also { ref = it } != null) {
                map.remove(ref)
            }
        }
    }
}

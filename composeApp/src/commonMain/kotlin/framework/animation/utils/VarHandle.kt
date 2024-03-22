package framework.animation.utils

/*
 * Copyright (c) 2014, 2018, Oracle and/or its affiliates. All rights reserved.
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

/**
 * A VarHandle is a dynamically strongly typed reference to a variable, or to a
 * parametrically-defined family of variables, including static fields,
 * non-static fields, array elements, or components of an off-heap data
 * structure.  Access to such variables is supported under various
 * *access modes*, including plain read/write access, volatile
 * read/write access, and compare-and-set.
 *
 *
 * VarHandles are immutable and have no visible state.  VarHandles cannot be
 * subclassed by the user.
 *
 *
 * A VarHandle has:
 *
 *  * a [variable type][.varType] T, the type of every variable referenced
 * by this VarHandle; and
 *  * a list of [coordinate types][.coordinateTypes]
 * `CT1, CT2, ..., CTn`, the types of *coordinate expressions* that
 * jointly locate a variable referenced by this VarHandle.
 *
 * Variable and coordinate types may be primitive or reference, and are
 * represented by `Class` objects.  The list of coordinate types may be
 * empty.
 *
 *
 * Factory methods that produce or [ lookup][java.lang.invoke.MethodHandles.Lookup] VarHandle instances document the supported variable type and the list
 * of coordinate types.
 *
 *
 * Each access mode is associated with one *access mode method*, a
 * [signature polymorphic](MethodHandle.html#sigpoly) method named
 * for the access mode.  When an access mode method is invoked on a VarHandle
 * instance, the initial arguments to the invocation are coordinate expressions
 * that indicate in precisely which object the variable is to be accessed.
 * Trailing arguments to the invocation represent values of importance to the
 * access mode.  For example, the various compare-and-set or compare-and-exchange
 * access modes require two trailing arguments for the variable's expected value
 * and new value.
 *
 *
 * The arity and types of arguments to the invocation of an access mode
 * method are not checked statically.  Instead, each access mode method
 * specifies an [access mode type][.accessModeType],
 * represented as an instance of [MethodType], that serves as a kind of
 * method signature against which the arguments are checked dynamically.  An
 * access mode type gives formal parameter types in terms of the coordinate
 * types of a VarHandle instance and the types for values of importance to the
 * access mode.  An access mode type also gives a return type, often in terms of
 * the variable type of a VarHandle instance.  When an access mode method is
 * invoked on a VarHandle instance, the symbolic type descriptor at the
 * call site, the run time types of arguments to the invocation, and the run
 * time type of the return value, must [match](#invoke) the types
 * given in the access mode type.  A runtime exception will be thrown if the
 * match fails.
 *
 * For example, the access mode method [.compareAndSet] specifies that if
 * its receiver is a VarHandle instance with coordinate types
 * `CT1, ..., CTn` and variable type `T`, then its access mode type
 * is `(CT1 c1, ..., CTn cn, T expectedValue, T newValue)boolean`.
 * Suppose that a VarHandle instance can access array elements, and that its
 * coordinate types are `String[]` and `int` while its variable type
 * is `String`.  The access mode type for `compareAndSet` on this
 * VarHandle instance would be
 * `(String[] c1, int c2, String expectedValue, String newValue)boolean`.
 * Such a VarHandle instance may be produced by the
 * [array factory method][MethodHandles.arrayElementVarHandle] and
 * access array elements as follows:
 * <pre> `String[] sa = ...
 * VarHandle avh = MethodHandles.arrayElementVarHandle(String[].class);
 * boolean r = avh.compareAndSet(sa, 10, "expected", "new");
`</pre> *
 *
 *
 * Access modes control atomicity and consistency properties.
 * *Plain* read (`get`) and write (`set`)
 * accesses are guaranteed to be bitwise atomic only for references
 * and for primitive values of at most 32 bits, and impose no observable
 * ordering constraints with respect to threads other than the
 * executing thread. *Opaque* operations are bitwise atomic and
 * coherently ordered with respect to accesses to the same variable.
 * In addition to obeying Opaque properties, *Acquire* mode
 * reads and their subsequent accesses are ordered after matching
 * *Release* mode writes and their previous accesses.  In
 * addition to obeying Acquire and Release properties, all
 * *Volatile* operations are totally ordered with respect to
 * each other.
 *
 *
 * Access modes are grouped into the following categories:
 *
 *  * read access modes that get the value of a variable under specified
 * memory ordering effects.
 * The set of corresponding access mode methods belonging to this group
 * consists of the methods
 * [get][.get],
 * [getVolatile][.getVolatile],
 * [getAcquire][.getAcquire],
 * [getOpaque][.getOpaque].
 *  * write access modes that set the value of a variable under specified
 * memory ordering effects.
 * The set of corresponding access mode methods belonging to this group
 * consists of the methods
 * [set][.set],
 * [setVolatile][.setVolatile],
 * [setRelease][.setRelease],
 * [setOpaque][.setOpaque].
 *  * atomic update access modes that, for example, atomically compare and set
 * the value of a variable under specified memory ordering effects.
 * The set of corresponding access mode methods belonging to this group
 * consists of the methods
 * [compareAndSet][.compareAndSet],
 * [weakCompareAndSetPlain][.weakCompareAndSetPlain],
 * [weakCompareAndSet][.weakCompareAndSet],
 * [weakCompareAndSetAcquire][.weakCompareAndSetAcquire],
 * [weakCompareAndSetRelease][.weakCompareAndSetRelease],
 * [compareAndExchangeAcquire][.compareAndExchangeAcquire],
 * [compareAndExchange][.compareAndExchange],
 * [compareAndExchangeRelease][.compareAndExchangeRelease],
 * [getAndSet][.getAndSet],
 * [getAndSetAcquire][.getAndSetAcquire],
 * [getAndSetRelease][.getAndSetRelease].
 *  * numeric atomic update access modes that, for example, atomically get and
 * set with addition the value of a variable under specified memory ordering
 * effects.
 * The set of corresponding access mode methods belonging to this group
 * consists of the methods
 * [getAndAdd][.getAndAdd],
 * [getAndAddAcquire][.getAndAddAcquire],
 * [getAndAddRelease][.getAndAddRelease],
 *  * bitwise atomic update access modes that, for example, atomically get and
 * bitwise OR the value of a variable under specified memory ordering
 * effects.
 * The set of corresponding access mode methods belonging to this group
 * consists of the methods
 * [getAndBitwiseOr][.getAndBitwiseOr],
 * [getAndBitwiseOrAcquire][.getAndBitwiseOrAcquire],
 * [getAndBitwiseOrRelease][.getAndBitwiseOrRelease],
 * [getAndBitwiseAnd][.getAndBitwiseAnd],
 * [getAndBitwiseAndAcquire][.getAndBitwiseAndAcquire],
 * [getAndBitwiseAndRelease][.getAndBitwiseAndRelease],
 * [getAndBitwiseXor][.getAndBitwiseXor],
 * [getAndBitwiseXorAcquire][.getAndBitwiseXorAcquire],
 * [getAndBitwiseXorRelease][.getAndBitwiseXorRelease].
 *
 *
 *
 * Factory methods that produce or [ lookup][java.lang.invoke.MethodHandles.Lookup] VarHandle instances document the set of access modes that are
 * supported, which may also include documenting restrictions based on the
 * variable type and whether a variable is read-only.  If an access mode is not
 * supported then the corresponding access mode method will on invocation throw
 * an `UnsupportedOperationException`.  Factory methods should document
 * any additional undeclared exceptions that may be thrown by access mode
 * methods.
 * The [get][.get] access mode is supported for all
 * VarHandle instances and the corresponding method never throws
 * `UnsupportedOperationException`.
 * If a VarHandle references a read-only variable (for example a `final`
 * field) then write, atomic update, numeric atomic update, and bitwise atomic
 * update access modes are not supported and corresponding methods throw
 * `UnsupportedOperationException`.
 * Read/write access modes (if supported), with the exception of
 * `get` and `set`, provide atomic access for
 * reference types and all primitive types.
 * Unless stated otherwise in the documentation of a factory method, the access
 * modes `get` and `set` (if supported) provide atomic access for
 * reference types and all primitives types, with the exception of `long`
 * and `double` on 32-bit platforms.
 *
 *
 * Access modes will override any memory ordering effects specified at
 * the declaration site of a variable.  For example, a VarHandle accessing
 * a field using the `get` access mode will access the field as
 * specified *by its access mode* even if that field is declared
 * `volatile`.  When mixed access is performed extreme care should be
 * taken since the Java Memory Model may permit surprising results.
 *
 *
 * In addition to supporting access to variables under various access modes,
 * a set of static methods, referred to as memory fence methods, is also
 * provided for fine-grained control of memory ordering.
 *
 * The Java Language Specification permits other threads to observe operations
 * as if they were executed in orders different than are apparent in program
 * source code, subject to constraints arising, for example, from the use of
 * locks, `volatile` fields or VarHandles.  The static methods,
 * [fullFence][.fullFence], [acquireFence][.acquireFence],
 * [releaseFence][.releaseFence], [loadLoadFence][.loadLoadFence] and
 * [storeStoreFence][.storeStoreFence], can also be used to impose
 * constraints.  Their specifications, as is the case for certain access modes,
 * are phrased in terms of the lack of "reorderings" -- observable ordering
 * effects that might otherwise occur if the fence was not present.  More
 * precise phrasing of the specification of access mode methods and memory fence
 * methods may accompany future updates of the Java Language Specification.
 *
 * <h1>Compiling invocation of access mode methods</h1>
 * A Java method call expression naming an access mode method can invoke a
 * VarHandle from Java source code.  From the viewpoint of source code, these
 * methods can take any arguments and their polymorphic result (if expressed)
 * can be cast to any return type.  Formally this is accomplished by giving the
 * access mode methods variable arity `Object` arguments and
 * `Object` return types (if the return type is polymorphic), but they
 * have an additional quality called *signature polymorphism* which
 * connects this freedom of invocation directly to the JVM execution stack.
 *
 *
 * As is usual with virtual methods, source-level calls to access mode methods
 * compile to an `invokevirtual` instruction.  More unusually, the
 * compiler must record the actual argument types, and may not perform method
 * invocation conversions on the arguments.  Instead, it must generate
 * instructions to push them on the stack according to their own unconverted
 * types.  The VarHandle object itself will be pushed on the stack before the
 * arguments.  The compiler then generates an `invokevirtual` instruction
 * that invokes the access mode method with a symbolic type descriptor which
 * describes the argument and return types.
 *
 *
 * To issue a complete symbolic type descriptor, the compiler must also
 * determine the return type (if polymorphic).  This is based on a cast on the
 * method invocation expression, if there is one, or else `Object` if the
 * invocation is an expression, or else `void` if the invocation is a
 * statement.  The cast may be to a primitive type (but not `void`).
 *
 *
 * As a corner case, an uncasted `null` argument is given a symbolic type
 * descriptor of `java.lang.Void`.  The ambiguity with the type
 * `Void` is harmless, since there are no references of type `Void`
 * except the null reference.
 *
 *
 * <h1><a id="invoke">Performing invocation of access mode methods</a></h1>
 * The first time an `invokevirtual` instruction is executed it is linked
 * by symbolically resolving the names in the instruction and verifying that
 * the method call is statically legal.  This also holds for calls to access mode
 * methods.  In this case, the symbolic type descriptor emitted by the compiler
 * is checked for correct syntax, and names it contains are resolved.  Thus, an
 * `invokevirtual` instruction which invokes an access mode method will
 * always link, as long as the symbolic type descriptor is syntactically
 * well-formed and the types exist.
 *
 *
 * When the `invokevirtual` is executed after linking, the receiving
 * VarHandle's access mode type is first checked by the JVM to ensure that it
 * matches the symbolic type descriptor.  If the type
 * match fails, it means that the access mode method which the caller is
 * invoking is not present on the individual VarHandle being invoked.
 *
 *
 *
 * Invocation of an access mode method behaves as if an invocation of
 * [MethodHandle.invoke], where the receiving method handle accepts the
 * VarHandle instance as the leading argument.  More specifically, the
 * following, where `{access-mode}` corresponds to the access mode method
 * name:
 * <pre> `VarHandle vh = ..
 * R r = (R) vh.{access-mode}(p1, p2, ..., pN);
`</pre> *
 * behaves as if:
 * <pre> `VarHandle vh = ..
 * VarHandle.AccessMode am = VarHandle.AccessMode.valueFromMethodName("{access-mode}");
 * MethodHandle mh = MethodHandles.varHandleExactInvoker(
 * am,
 * vh.accessModeType(am));
 *
 * R r = (R) mh.invoke(vh, p1, p2, ..., pN)
`</pre> *
 * (modulo access mode methods do not declare throwing of `Throwable`).
 * This is equivalent to:
 * <pre> `MethodHandle mh = MethodHandles.lookup().findVirtual(
 * VarHandle.class,
 * "{access-mode}",
 * MethodType.methodType(R, p1, p2, ..., pN));
 *
 * R r = (R) mh.invokeExact(vh, p1, p2, ..., pN)
`</pre> *
 * where the desired method type is the symbolic type descriptor and a
 * [MethodHandle.invokeExact] is performed, since before invocation of the
 * target, the handle will apply reference casts as necessary and box, unbox, or
 * widen primitive values, as if by [asType][MethodHandle.asType] (see also
 * [MethodHandles.varHandleInvoker]).
 *
 * More concisely, such behaviour is equivalent to:
 * <pre> `VarHandle vh = ..
 * VarHandle.AccessMode am = VarHandle.AccessMode.valueFromMethodName("{access-mode}");
 * MethodHandle mh = vh.toMethodHandle(am);
 *
 * R r = (R) mh.invoke(p1, p2, ..., pN)
`</pre> *
 * Where, in this case, the method handle is bound to the VarHandle instance.
 *
 *
 * <h1>Invocation checking</h1>
 * In typical programs, VarHandle access mode type matching will usually
 * succeed.  But if a match fails, the JVM will throw a
 * [WrongMethodTypeException].
 *
 *
 * Thus, an access mode type mismatch which might show up as a linkage error
 * in a statically typed program can show up as a dynamic
 * `WrongMethodTypeException` in a program which uses VarHandles.
 *
 *
 * Because access mode types contain "live" `Class` objects, method type
 * matching takes into account both type names and class loaders.
 * Thus, even if a VarHandle `VH` is created in one class loader
 * `L1` and used in another `L2`, VarHandle access mode method
 * calls are type-safe, because the caller's symbolic type descriptor, as
 * resolved in `L2`, is matched against the original callee method's
 * symbolic type descriptor, as resolved in `L1`.  The resolution in
 * `L1` happens when `VH` is created and its access mode types are
 * assigned, while the resolution in `L2` happens when the
 * `invokevirtual` instruction is linked.
 *
 *
 * Apart from type descriptor checks, a VarHandles's capability to
 * access it's variables is unrestricted.
 * If a VarHandle is formed on a non-public variable by a class that has access
 * to that variable, the resulting VarHandle can be used in any place by any
 * caller who receives a reference to it.
 *
 *
 * Unlike with the Core Reflection API, where access is checked every time a
 * reflective method is invoked, VarHandle access checking is performed
 * [when the VarHandle is
 * created](MethodHandles.Lookup.html#access).
 * Thus, VarHandles to non-public variables, or to variables in non-public
 * classes, should generally be kept secret.  They should not be passed to
 * untrusted code unless their use from the untrusted code would be harmless.
 *
 *
 * <h1>VarHandle creation</h1>
 * Java code can create a VarHandle that directly accesses any field that is
 * accessible to that code.  This is done via a reflective, capability-based
 * API called [ MethodHandles.Lookup][java.lang.invoke.MethodHandles.Lookup].
 * For example, a VarHandle for a non-static field can be obtained
 * from [ Lookup.findVarHandle][java.lang.invoke.MethodHandles.Lookup.findVarHandle].
 * There is also a conversion method from Core Reflection API objects,
 * [ Lookup.unreflectVarHandle][java.lang.invoke.MethodHandles.Lookup.unreflectVarHandle].
 *
 *
 * Access to protected field members is restricted to receivers only of the
 * accessing class, or one of its subclasses, and the accessing class must in
 * turn be a subclass (or package sibling) of the protected member's defining
 * class.  If a VarHandle refers to a protected non-static field of a declaring
 * class outside the current package, the receiver argument will be narrowed to
 * the type of the accessing class.
 *
 * <h1>Interoperation between VarHandles and the Core Reflection API</h1>
 * Using factory methods in the [ Lookup][java.lang.invoke.MethodHandles.Lookup] API, any field represented by a Core Reflection API object
 * can be converted to a behaviorally equivalent VarHandle.
 * For example, a reflective [Field][java.lang.reflect.Field] can
 * be converted to a VarHandle using
 * [ Lookup.unreflectVarHandle][java.lang.invoke.MethodHandles.Lookup.unreflectVarHandle].
 * The resulting VarHandles generally provide more direct and efficient
 * access to the underlying fields.
 *
 *
 * As a special case, when the Core Reflection API is used to view the
 * signature polymorphic access mode methods in this class, they appear as
 * ordinary non-polymorphic methods.  Their reflective appearance, as viewed by
 * [Class.getDeclaredMethod][java.lang.Class.getDeclaredMethod],
 * is unaffected by their special status in this API.
 * For example, [ Method.getModifiers][java.lang.reflect.Method.getModifiers]
 * will report exactly those modifier bits required for any similarly
 * declared method, including in this case `native` and `varargs`
 * bits.
 *
 *
 * As with any reflected method, these methods (when reflected) may be invoked
 * directly via [java.lang.reflect.Method.invoke],
 * via JNI, or indirectly via
 * [Lookup.unreflect][java.lang.invoke.MethodHandles.Lookup.unreflect].
 * However, such reflective calls do not result in access mode method
 * invocations.  Such a call, if passed the required argument (a single one, of
 * type `Object[]`), will ignore the argument and will throw an
 * `UnsupportedOperationException`.
 *
 *
 * Since `invokevirtual` instructions can natively invoke VarHandle
 * access mode methods under any symbolic type descriptor, this reflective view
 * conflicts with the normal presentation of these methods via bytecodes.
 * Thus, these native methods, when reflectively viewed by
 * `Class.getDeclaredMethod`, may be regarded as placeholders only.
 *
 *
 * In order to obtain an invoker method for a particular access mode type,
 * use [java.lang.invoke.MethodHandles.varHandleExactInvoker] or
 * [java.lang.invoke.MethodHandles.varHandleInvoker].  The
 * [Lookup.findVirtual][java.lang.invoke.MethodHandles.Lookup.findVirtual]
 * API is also able to return a method handle to call an access mode method for
 * any specified access mode type and is equivalent in behaviour to
 * [java.lang.invoke.MethodHandles.varHandleInvoker].
 *
 * <h1>Interoperation between VarHandles and Java generics</h1>
 * A VarHandle can be obtained for a variable, such as a field, which is
 * declared with Java generic types.  As with the Core Reflection API, the
 * VarHandle's variable type will be constructed from the erasure of the
 * source-level type.  When a VarHandle access mode method is invoked, the
 * types
 * of its arguments or the return value cast type may be generic types or type
 * instances.  If this occurs, the compiler will replace those types by their
 * erasures when it constructs the symbolic type descriptor for the
 * `invokevirtual` instruction.
 *
 * @see MethodHandle
 *
 * @see MethodHandles
 *
 * @see MethodType
 *
 * @since 9
 */
abstract class VarHandle {
    // BEGIN Android-removed: No VarForm in Android implementation.
    /*
    final VarForm vform;

    VarHandle(VarForm vform) {
        this.vform = vform;
    }
    */
    // END Android-removed: No VarForm in Android implementation.
    // BEGIN Android-added: fields for common metadata.
    /** The target type for accesses.  */
    private val varType: java.lang.Class<*>

    /** This VarHandle's first coordinate, or null if this VarHandle has no coordinates.  */
    private val coordinateType0: java.lang.Class<*>?

    /** This VarHandle's second coordinate, or null if this VarHandle has less than two
     * coordinates.  */
    private val coordinateType1: java.lang.Class<*>?

    /** BitMask of supported access mode indexed by AccessMode.ordinal().  */
    private val accessModesBitMask: Int
    // END Android-added: fields for common metadata.
    // Plain accessors
    /**
     * Returns the value of a variable, with memory semantics of reading as
     * if the variable was declared non-`volatile`.  Commonly referred to
     * as plain read access.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn)T`.
     *
     *
     * The symbolic type descriptor at the call site of `get`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET)` on this VarHandle.
     *
     *
     * This access mode is supported by all VarHandle instances and never
     * throws `UnsupportedOperationException`.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the value of the
     * variable
     * , statically represented using `Object`.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external operator fun get(vararg args: Any?): Any?

    /**
     * Sets the value of a variable to the `newValue`, with memory
     * semantics of setting as if the variable was declared non-`volatile`
     * and non-`final`.  Commonly referred to as plain write access.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T newValue)void`
     *
     *
     * The symbolic type descriptor at the call site of `set`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.SET)` on this VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T newValue)`
     * , statically represented using varargs.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun set(vararg args: Any?)
    // Volatile accessors
    /**
     * Returns the value of a variable, with memory semantics of reading as if
     * the variable was declared `volatile`.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getVolatile`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_VOLATILE)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the value of the
     * variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getVolatile(vararg args: Any?): Any?

    /**
     * Sets the value of a variable to the `newValue`, with memory
     * semantics of setting as if the variable was declared `volatile`.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T newValue)void`.
     *
     *
     * The symbolic type descriptor at the call site of `setVolatile`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.SET_VOLATILE)` on this
     * VarHandle.
     *
     * @apiNote
     * Ignoring the many semantic differences from C and C++, this method has
     * memory ordering effects compatible with `memory_order_seq_cst`.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T newValue)`
     * , statically represented using varargs.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun setVolatile(vararg args: Any?)

    /**
     * Returns the value of a variable, accessed in program order, but with no
     * assurance of memory ordering effects with respect to other threads.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getOpaque`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_OPAQUE)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the value of the
     * variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getOpaque(vararg args: Any?): Any?

    /**
     * Sets the value of a variable to the `newValue`, in program order,
     * but with no assurance of memory ordering effects with respect to other
     * threads.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T newValue)void`.
     *
     *
     * The symbolic type descriptor at the call site of `setOpaque`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.SET_OPAQUE)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T newValue)`
     * , statically represented using varargs.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun setOpaque(vararg args: Any?)
    // Lazy accessors
    /**
     * Returns the value of a variable, and ensures that subsequent loads and
     * stores are not reordered before this access.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getAcquire`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_ACQUIRE)` on this
     * VarHandle.
     *
     * @apiNote
     * Ignoring the many semantic differences from C and C++, this method has
     * memory ordering effects compatible with `memory_order_acquire`
     * ordering.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the value of the
     * variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getAcquire(vararg args: Any?): Any?

    /**
     * Sets the value of a variable to the `newValue`, and ensures that
     * prior loads and stores are not reordered after this access.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T newValue)void`.
     *
     *
     * The symbolic type descriptor at the call site of `setRelease`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.SET_RELEASE)` on this
     * VarHandle.
     *
     * @apiNote
     * Ignoring the many semantic differences from C and C++, this method has
     * memory ordering effects compatible with `memory_order_release`
     * ordering.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T newValue)`
     * , statically represented using varargs.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun setRelease(vararg args: Any?)
    // Compare and set accessors
    /**
     * Atomically sets the value of a variable to the `newValue` with the
     * memory semantics of [.setVolatile] if the variable's current value,
     * referred to as the *witness value*, `==` the
     * `expectedValue`, as accessed with the memory semantics of
     * [.getVolatile].
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T expectedValue, T newValue)boolean`.
     *
     *
     * The symbolic type descriptor at the call site of `compareAndSet` must match the access mode type that is the result of
     * calling `accessModeType(VarHandle.AccessMode.COMPARE_AND_SET)` on
     * this VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T expectedValue, T newValue)`
     * , statically represented using varargs.
     * @return `true` if successful, otherwise `false` if the
     * witness value was not the same as the `expectedValue`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .setVolatile
     * @see .getVolatile
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun compareAndSet(vararg args: Any?): Boolean

    /**
     * Atomically sets the value of a variable to the `newValue` with the
     * memory semantics of [.setVolatile] if the variable's current value,
     * referred to as the *witness value*, `==` the
     * `expectedValue`, as accessed with the memory semantics of
     * [.getVolatile].
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T expectedValue, T newValue)T`.
     *
     *
     * The symbolic type descriptor at the call site of `compareAndExchange`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.COMPARE_AND_EXCHANGE)`
     * on this VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T expectedValue, T newValue)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the witness value, which
     * will be the same as the `expectedValue` if successful
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type is not
     * compatible with the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type is compatible with the
     * caller's symbolic type descriptor, but a reference cast fails.
     * @see .setVolatile
     * @see .getVolatile
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun compareAndExchange(vararg args: Any?): Any?

    /**
     * Atomically sets the value of a variable to the `newValue` with the
     * memory semantics of [.set] if the variable's current value,
     * referred to as the *witness value*, `==` the
     * `expectedValue`, as accessed with the memory semantics of
     * [.getAcquire].
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T expectedValue, T newValue)T`.
     *
     *
     * The symbolic type descriptor at the call site of `compareAndExchangeAcquire`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.COMPARE_AND_EXCHANGE_ACQUIRE)` on
     * this VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T expectedValue, T newValue)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the witness value, which
     * will be the same as the `expectedValue` if successful
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .set
     * @see .getAcquire
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun compareAndExchangeAcquire(vararg args: Any?): Any?

    /**
     * Atomically sets the value of a variable to the `newValue` with the
     * memory semantics of [.setRelease] if the variable's current value,
     * referred to as the *witness value*, `==` the
     * `expectedValue`, as accessed with the memory semantics of
     * [.get].
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T expectedValue, T newValue)T`.
     *
     *
     * The symbolic type descriptor at the call site of `compareAndExchangeRelease`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.COMPARE_AND_EXCHANGE_RELEASE)`
     * on this VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T expectedValue, T newValue)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the witness value, which
     * will be the same as the `expectedValue` if successful
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .setRelease
     * @see .get
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun compareAndExchangeRelease(vararg args: Any?): Any?
    // Weak (spurious failures allowed)
    /**
     * Possibly atomically sets the value of a variable to the `newValue`
     * with the semantics of [.set] if the variable's current value,
     * referred to as the *witness value*, `==` the
     * `expectedValue`, as accessed with the memory semantics of
     * [.get].
     *
     *
     * This operation may fail spuriously (typically, due to memory
     * contention) even if the witness value does match the expected value.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T expectedValue, T newValue)boolean`.
     *
     *
     * The symbolic type descriptor at the call site of `weakCompareAndSetPlain` must match the access mode type that is the result of
     * calling `accessModeType(VarHandle.AccessMode.WEAK_COMPARE_AND_SET_PLAIN)`
     * on this VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T expectedValue, T newValue)`
     * , statically represented using varargs.
     * @return `true` if successful, otherwise `false` if the
     * witness value was not the same as the `expectedValue` or if this
     * operation spuriously failed.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .set
     * @see .get
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun weakCompareAndSetPlain(vararg args: Any?): Boolean

    /**
     * Possibly atomically sets the value of a variable to the `newValue`
     * with the memory semantics of [.setVolatile] if the variable's
     * current value, referred to as the *witness value*, `==` the
     * `expectedValue`, as accessed with the memory semantics of
     * [.getVolatile].
     *
     *
     * This operation may fail spuriously (typically, due to memory
     * contention) even if the witness value does match the expected value.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T expectedValue, T newValue)boolean`.
     *
     *
     * The symbolic type descriptor at the call site of `weakCompareAndSet` must match the access mode type that is the
     * result of calling `accessModeType(VarHandle.AccessMode.WEAK_COMPARE_AND_SET)`
     * on this VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T expectedValue, T newValue)`
     * , statically represented using varargs.
     * @return `true` if successful, otherwise `false` if the
     * witness value was not the same as the `expectedValue` or if this
     * operation spuriously failed.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .setVolatile
     * @see .getVolatile
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun weakCompareAndSet(vararg args: Any?): Boolean

    /**
     * Possibly atomically sets the value of a variable to the `newValue`
     * with the semantics of [.set] if the variable's current value,
     * referred to as the *witness value*, `==` the
     * `expectedValue`, as accessed with the memory semantics of
     * [.getAcquire].
     *
     *
     * This operation may fail spuriously (typically, due to memory
     * contention) even if the witness value does match the expected value.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T expectedValue, T newValue)boolean`.
     *
     *
     * The symbolic type descriptor at the call site of `weakCompareAndSetAcquire`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.WEAK_COMPARE_AND_SET_ACQUIRE)`
     * on this VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T expectedValue, T newValue)`
     * , statically represented using varargs.
     * @return `true` if successful, otherwise `false` if the
     * witness value was not the same as the `expectedValue` or if this
     * operation spuriously failed.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .set
     * @see .getAcquire
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun weakCompareAndSetAcquire(vararg args: Any?): Boolean

    /**
     * Possibly atomically sets the value of a variable to the `newValue`
     * with the semantics of [.setRelease] if the variable's current
     * value, referred to as the *witness value*, `==` the
     * `expectedValue`, as accessed with the memory semantics of
     * [.get].
     *
     *
     * This operation may fail spuriously (typically, due to memory
     * contention) even if the witness value does match the expected value.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T expectedValue, T newValue)boolean`.
     *
     *
     * The symbolic type descriptor at the call site of `weakCompareAndSetRelease`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.WEAK_COMPARE_AND_SET_RELEASE)`
     * on this VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T expectedValue, T newValue)`
     * , statically represented using varargs.
     * @return `true` if successful, otherwise `false` if the
     * witness value was not the same as the `expectedValue` or if this
     * operation spuriously failed.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .setRelease
     * @see .get
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun weakCompareAndSetRelease(vararg args: Any?): Boolean

    /**
     * Atomically sets the value of a variable to the `newValue` with the
     * memory semantics of [.setVolatile] and returns the variable's
     * previous value, as accessed with the memory semantics of
     * [.getVolatile].
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T newValue)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getAndSet`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_AND_SET)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T newValue)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the previous value of
     * the variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .setVolatile
     * @see .getVolatile
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getAndSet(vararg args: Any?): Any?

    /**
     * Atomically sets the value of a variable to the `newValue` with the
     * memory semantics of [.set] and returns the variable's
     * previous value, as accessed with the memory semantics of
     * [.getAcquire].
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T newValue)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getAndSetAcquire`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_AND_SET_ACQUIRE)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T newValue)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the previous value of
     * the variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .setVolatile
     * @see .getVolatile
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getAndSetAcquire(vararg args: Any?): Any?

    /**
     * Atomically sets the value of a variable to the `newValue` with the
     * memory semantics of [.setRelease] and returns the variable's
     * previous value, as accessed with the memory semantics of
     * [.get].
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T newValue)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getAndSetRelease`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_AND_SET_RELEASE)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T newValue)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the previous value of
     * the variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .setVolatile
     * @see .getVolatile
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getAndSetRelease(vararg args: Any?): Any?
    // Primitive adders
    // Throw UnsupportedOperationException for refs
    /**
     * Atomically adds the `value` to the current value of a variable with
     * the memory semantics of [.setVolatile], and returns the variable's
     * previous value, as accessed with the memory semantics of
     * [.getVolatile].
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T value)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getAndAdd`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_AND_ADD)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T value)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the previous value of
     * the variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .setVolatile
     * @see .getVolatile
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getAndAdd(vararg args: Any?): Any?

    /**
     * Atomically adds the `value` to the current value of a variable with
     * the memory semantics of [.set], and returns the variable's
     * previous value, as accessed with the memory semantics of
     * [.getAcquire].
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T value)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getAndAddAcquire`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_AND_ADD_ACQUIRE)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T value)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the previous value of
     * the variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .setVolatile
     * @see .getVolatile
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getAndAddAcquire(vararg args: Any?): Any?

    /**
     * Atomically adds the `value` to the current value of a variable with
     * the memory semantics of [.setRelease], and returns the variable's
     * previous value, as accessed with the memory semantics of
     * [.get].
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T value)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getAndAddRelease`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_AND_ADD_RELEASE)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T value)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the previous value of
     * the variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .setVolatile
     * @see .getVolatile
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getAndAddRelease(vararg args: Any?): Any?
    // Bitwise operations
    // Throw UnsupportedOperationException for refs
    /**
     * Atomically sets the value of a variable to the result of
     * bitwise OR between the variable's current value and the `mask`
     * with the memory semantics of [.setVolatile] and returns the
     * variable's previous value, as accessed with the memory semantics of
     * [.getVolatile].
     *
     *
     * If the variable type is the non-integral `boolean` type then a
     * logical OR is performed instead of a bitwise OR.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T mask)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getAndBitwiseOr`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_AND_BITWISE_OR)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T mask)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the previous value of
     * the variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .setVolatile
     * @see .getVolatile
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getAndBitwiseOr(vararg args: Any?): Any?

    /**
     * Atomically sets the value of a variable to the result of
     * bitwise OR between the variable's current value and the `mask`
     * with the memory semantics of [.set] and returns the
     * variable's previous value, as accessed with the memory semantics of
     * [.getAcquire].
     *
     *
     * If the variable type is the non-integral `boolean` type then a
     * logical OR is performed instead of a bitwise OR.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T mask)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getAndBitwiseOrAcquire`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_AND_BITWISE_OR_ACQUIRE)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T mask)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the previous value of
     * the variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .set
     * @see .getAcquire
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getAndBitwiseOrAcquire(vararg args: Any?): Any?

    /**
     * Atomically sets the value of a variable to the result of
     * bitwise OR between the variable's current value and the `mask`
     * with the memory semantics of [.setRelease] and returns the
     * variable's previous value, as accessed with the memory semantics of
     * [.get].
     *
     *
     * If the variable type is the non-integral `boolean` type then a
     * logical OR is performed instead of a bitwise OR.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T mask)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getAndBitwiseOrRelease`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_AND_BITWISE_OR_RELEASE)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T mask)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the previous value of
     * the variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .setRelease
     * @see .get
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getAndBitwiseOrRelease(vararg args: Any?): Any?

    /**
     * Atomically sets the value of a variable to the result of
     * bitwise AND between the variable's current value and the `mask`
     * with the memory semantics of [.setVolatile] and returns the
     * variable's previous value, as accessed with the memory semantics of
     * [.getVolatile].
     *
     *
     * If the variable type is the non-integral `boolean` type then a
     * logical AND is performed instead of a bitwise AND.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T mask)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getAndBitwiseAnd`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_AND_BITWISE_AND)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T mask)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the previous value of
     * the variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .setVolatile
     * @see .getVolatile
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getAndBitwiseAnd(vararg args: Any?): Any?

    /**
     * Atomically sets the value of a variable to the result of
     * bitwise AND between the variable's current value and the `mask`
     * with the memory semantics of [.set] and returns the
     * variable's previous value, as accessed with the memory semantics of
     * [.getAcquire].
     *
     *
     * If the variable type is the non-integral `boolean` type then a
     * logical AND is performed instead of a bitwise AND.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T mask)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getAndBitwiseAndAcquire`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_AND_BITWISE_AND_ACQUIRE)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T mask)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the previous value of
     * the variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .set
     * @see .getAcquire
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getAndBitwiseAndAcquire(vararg args: Any?): Any?

    /**
     * Atomically sets the value of a variable to the result of
     * bitwise AND between the variable's current value and the `mask`
     * with the memory semantics of [.setRelease] and returns the
     * variable's previous value, as accessed with the memory semantics of
     * [.get].
     *
     *
     * If the variable type is the non-integral `boolean` type then a
     * logical AND is performed instead of a bitwise AND.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T mask)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getAndBitwiseAndRelease`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_AND_BITWISE_AND_RELEASE)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T mask)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the previous value of
     * the variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .setRelease
     * @see .get
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getAndBitwiseAndRelease(vararg args: Any?): Any?

    /**
     * Atomically sets the value of a variable to the result of
     * bitwise XOR between the variable's current value and the `mask`
     * with the memory semantics of [.setVolatile] and returns the
     * variable's previous value, as accessed with the memory semantics of
     * [.getVolatile].
     *
     *
     * If the variable type is the non-integral `boolean` type then a
     * logical XOR is performed instead of a bitwise XOR.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T mask)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getAndBitwiseXor`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_AND_BITWISE_XOR)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T mask)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the previous value of
     * the variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .setVolatile
     * @see .getVolatile
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getAndBitwiseXor(vararg args: Any?): Any?

    /**
     * Atomically sets the value of a variable to the result of
     * bitwise XOR between the variable's current value and the `mask`
     * with the memory semantics of [.set] and returns the
     * variable's previous value, as accessed with the memory semantics of
     * [.getAcquire].
     *
     *
     * If the variable type is the non-integral `boolean` type then a
     * logical XOR is performed instead of a bitwise XOR.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T mask)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getAndBitwiseXorAcquire`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_AND_BITWISE_XOR_ACQUIRE)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T mask)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the previous value of
     * the variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .set
     * @see .getAcquire
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getAndBitwiseXorAcquire(vararg args: Any?): Any?

    /**
     * Atomically sets the value of a variable to the result of
     * bitwise XOR between the variable's current value and the `mask`
     * with the memory semantics of [.setRelease] and returns the
     * variable's previous value, as accessed with the memory semantics of
     * [.get].
     *
     *
     * If the variable type is the non-integral `boolean` type then a
     * logical XOR is performed instead of a bitwise XOR.
     *
     *
     * The method signature is of the form `(CT1 ct1, ..., CTn ctn, T mask)T`.
     *
     *
     * The symbolic type descriptor at the call site of `getAndBitwiseXorRelease`
     * must match the access mode type that is the result of calling
     * `accessModeType(VarHandle.AccessMode.GET_AND_BITWISE_XOR_RELEASE)` on this
     * VarHandle.
     *
     * @param args the signature-polymorphic parameter list of the form
     * `(CT1 ct1, ..., CTn ctn, T mask)`
     * , statically represented using varargs.
     * @return the signature-polymorphic result that is the previous value of
     * the variable
     * , statically represented using `Object`.
     * @throws UnsupportedOperationException if the access mode is unsupported
     * for this VarHandle.
     * @throws WrongMethodTypeException if the access mode type does not
     * match the caller's symbolic type descriptor.
     * @throws ClassCastException if the access mode type matches the caller's
     * symbolic type descriptor, but a reference cast fails.
     * @see .setRelease
     * @see .get
     */
    @PolymorphicSignature
    @IntrinsicCandidate
    external fun getAndBitwiseXorRelease(vararg args: Any?): Any?

    // Android-changed: remove unused return type in AccessType constructor.
    internal enum class AccessType {
        GET,
        SET,
        COMPARE_AND_SET,
        COMPARE_AND_EXCHANGE,
        GET_AND_UPDATE,

        // Android-added: Finer grained access types.
        // These are used to help categorize the access modes that a VarHandle supports.
        GET_AND_UPDATE_BITWISE,
        GET_AND_UPDATE_NUMERIC;

        fun accessModeType(
            receiver: java.lang.Class<*>?, value: java.lang.Class<*>,
            vararg intermediate: java.lang.Class<*>?
        ): java.lang.invoke.MethodType {
            val ps: Array<java.lang.Class<*>>
            var i: Int
            return when (this) {
                VarHandle.AccessType.GET -> {
                    ps = VarHandle.AccessType.Companion.allocateParameters(
                        0,
                        receiver,
                        *intermediate
                    )
                    VarHandle.AccessType.Companion.fillParameters(
                        ps,
                        receiver,
                        *intermediate
                    )
                    java.lang.invoke.MethodType.methodType(value, ps)
                }

                VarHandle.AccessType.SET -> {
                    ps = VarHandle.AccessType.Companion.allocateParameters(
                        1,
                        receiver,
                        *intermediate
                    )
                    i = VarHandle.AccessType.Companion.fillParameters(
                        ps,
                        receiver,
                        *intermediate
                    )
                    ps[i] = value
                    java.lang.invoke.MethodType.methodType(Void.TYPE, ps)
                }

                VarHandle.AccessType.COMPARE_AND_SET -> {
                    ps = VarHandle.AccessType.Companion.allocateParameters(
                        2,
                        receiver,
                        *intermediate
                    )
                    i = VarHandle.AccessType.Companion.fillParameters(
                        ps,
                        receiver,
                        *intermediate
                    )
                    ps[i++] = value
                    ps[i] = value
                    java.lang.invoke.MethodType.methodType(Boolean::class.javaPrimitiveType, ps)
                }

                VarHandle.AccessType.COMPARE_AND_EXCHANGE -> {
                    ps = VarHandle.AccessType.Companion.allocateParameters(
                        2,
                        receiver,
                        *intermediate
                    )
                    i = VarHandle.AccessType.Companion.fillParameters(
                        ps,
                        receiver,
                        *intermediate
                    )
                    ps[i++] = value
                    ps[i] = value
                    java.lang.invoke.MethodType.methodType(value, ps)
                }

                VarHandle.AccessType.GET_AND_UPDATE, VarHandle.AccessType.GET_AND_UPDATE_BITWISE, VarHandle.AccessType.GET_AND_UPDATE_NUMERIC -> {
                    ps = VarHandle.AccessType.Companion.allocateParameters(
                        1,
                        receiver,
                        *intermediate
                    )
                    i = VarHandle.AccessType.Companion.fillParameters(
                        ps,
                        receiver,
                        *intermediate
                    )
                    ps[i] = value
                    java.lang.invoke.MethodType.methodType(value, ps)
                }

                else -> throw java.lang.InternalError("Unknown AccessType")
            }
        }

        companion object {
            private fun allocateParameters(
                values: Int,
                receiver: java.lang.Class<*>?, vararg intermediate: java.lang.Class<*>
            ): Array<java.lang.Class<*>> {
                val size = (if (receiver != null) 1 else 0) + intermediate.size + values
                return arrayOfNulls<java.lang.Class<*>>(size)
            }

            private fun fillParameters(
                ps: Array<java.lang.Class<*>>,
                receiver: java.lang.Class<*>?, vararg intermediate: java.lang.Class<*>
            ): Int {
                var i = 0
                if (receiver != null) ps[i++] = receiver
                for (j in intermediate.indices) ps[i++] = intermediate[j]
                return i
            }
        }
    }

    /**
     * The set of access modes that specify how a variable, referenced by a
     * VarHandle, is accessed.
     */
    enum class AccessMode(val methodName: String, at: VarHandle.AccessType) {
        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.get]
         */
        GET("get", VarHandle.AccessType.GET),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.set]
         */
        SET("set", VarHandle.AccessType.SET),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getVolatile]
         */
        GET_VOLATILE("getVolatile", VarHandle.AccessType.GET),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.setVolatile]
         */
        SET_VOLATILE("setVolatile", VarHandle.AccessType.SET),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getAcquire]
         */
        GET_ACQUIRE("getAcquire", VarHandle.AccessType.GET),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.setRelease]
         */
        SET_RELEASE("setRelease", VarHandle.AccessType.SET),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getOpaque]
         */
        GET_OPAQUE("getOpaque", VarHandle.AccessType.GET),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.setOpaque]
         */
        SET_OPAQUE("setOpaque", VarHandle.AccessType.SET),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.compareAndSet]
         */
        COMPARE_AND_SET("compareAndSet", VarHandle.AccessType.COMPARE_AND_SET),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.compareAndExchange]
         */
        COMPARE_AND_EXCHANGE(
            "compareAndExchange",
            VarHandle.AccessType.COMPARE_AND_EXCHANGE
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.compareAndExchangeAcquire]
         */
        COMPARE_AND_EXCHANGE_ACQUIRE(
            "compareAndExchangeAcquire",
            VarHandle.AccessType.COMPARE_AND_EXCHANGE
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.compareAndExchangeRelease]
         */
        COMPARE_AND_EXCHANGE_RELEASE(
            "compareAndExchangeRelease",
            VarHandle.AccessType.COMPARE_AND_EXCHANGE
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.weakCompareAndSetPlain]
         */
        WEAK_COMPARE_AND_SET_PLAIN(
            "weakCompareAndSetPlain",
            VarHandle.AccessType.COMPARE_AND_SET
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.weakCompareAndSet]
         */
        WEAK_COMPARE_AND_SET(
            "weakCompareAndSet",
            VarHandle.AccessType.COMPARE_AND_SET
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.weakCompareAndSetAcquire]
         */
        WEAK_COMPARE_AND_SET_ACQUIRE(
            "weakCompareAndSetAcquire",
            VarHandle.AccessType.COMPARE_AND_SET
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.weakCompareAndSetRelease]
         */
        WEAK_COMPARE_AND_SET_RELEASE(
            "weakCompareAndSetRelease",
            VarHandle.AccessType.COMPARE_AND_SET
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getAndSet]
         */
        GET_AND_SET("getAndSet", VarHandle.AccessType.GET_AND_UPDATE),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getAndSetAcquire]
         */
        GET_AND_SET_ACQUIRE(
            "getAndSetAcquire",
            VarHandle.AccessType.GET_AND_UPDATE
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getAndSetRelease]
         */
        GET_AND_SET_RELEASE(
            "getAndSetRelease",
            VarHandle.AccessType.GET_AND_UPDATE
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getAndAdd]
         */
        GET_AND_ADD("getAndAdd", VarHandle.AccessType.GET_AND_UPDATE_NUMERIC),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getAndAddAcquire]
         */
        GET_AND_ADD_ACQUIRE(
            "getAndAddAcquire",
            VarHandle.AccessType.GET_AND_UPDATE_NUMERIC
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getAndAddRelease]
         */
        GET_AND_ADD_RELEASE(
            "getAndAddRelease",
            VarHandle.AccessType.GET_AND_UPDATE_NUMERIC
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getAndBitwiseOr]
         */
        GET_AND_BITWISE_OR(
            "getAndBitwiseOr",
            VarHandle.AccessType.GET_AND_UPDATE_BITWISE
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getAndBitwiseOrRelease]
         */
        GET_AND_BITWISE_OR_RELEASE(
            "getAndBitwiseOrRelease",
            VarHandle.AccessType.GET_AND_UPDATE_BITWISE
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getAndBitwiseOrAcquire]
         */
        GET_AND_BITWISE_OR_ACQUIRE(
            "getAndBitwiseOrAcquire",
            VarHandle.AccessType.GET_AND_UPDATE_BITWISE
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getAndBitwiseAnd]
         */
        GET_AND_BITWISE_AND(
            "getAndBitwiseAnd",
            VarHandle.AccessType.GET_AND_UPDATE_BITWISE
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getAndBitwiseAndRelease]
         */
        GET_AND_BITWISE_AND_RELEASE(
            "getAndBitwiseAndRelease",
            VarHandle.AccessType.GET_AND_UPDATE_BITWISE
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getAndBitwiseAndAcquire]
         */
        GET_AND_BITWISE_AND_ACQUIRE(
            "getAndBitwiseAndAcquire",
            VarHandle.AccessType.GET_AND_UPDATE_BITWISE
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getAndBitwiseXor]
         */
        GET_AND_BITWISE_XOR(
            "getAndBitwiseXor",
            VarHandle.AccessType.GET_AND_UPDATE_BITWISE
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getAndBitwiseXorRelease]
         */
        GET_AND_BITWISE_XOR_RELEASE(
            "getAndBitwiseXorRelease",
            VarHandle.AccessType.GET_AND_UPDATE_BITWISE
        ),

        /**
         * The access mode whose access is specified by the corresponding
         * method
         * [VarHandle.getAndBitwiseXorAcquire]
         */
        GET_AND_BITWISE_XOR_ACQUIRE(
            "getAndBitwiseXorAcquire",
            VarHandle.AccessType.GET_AND_UPDATE_BITWISE
        );

        val at: VarHandle.AccessType

        init {
            this.at = at
        }

        /**
         * Returns the `VarHandle` signature-polymorphic method name
         * associated with this `AccessMode` value.
         *
         * @return the signature-polymorphic method name
         * @see .valueFromMethodName
         */
        fun methodName(): String {
            return methodName
        }

        companion object {
            val methodNameToAccessMode: Map<String, VarHandle.AccessMode>? = null

            init {
                val values: Array<VarHandle.AccessMode> =
                    VarHandle.AccessMode.entries.toTypedArray()
                // Initial capacity of # values divided by the load factor is sufficient
                // to avoid resizes for the smallest table size (64)
                val initialCapacity = (values.size / 0.75f).toInt() + 1
                VarHandle.AccessMode.Companion.methodNameToAccessMode =
                    java.util.HashMap<String, VarHandle.AccessMode>(initialCapacity)
                for (am in values) {
                    VarHandle.AccessMode.Companion.methodNameToAccessMode.put(
                        am.methodName,
                        am
                    )
                }
            }

            /**
             * Returns the `AccessMode` value associated with the specified
             * `VarHandle` signature-polymorphic method name.
             *
             * @param methodName the signature-polymorphic method name
             * @return the `AccessMode` value
             * @throws IllegalArgumentException if there is no `AccessMode`
             * value associated with method name (indicating the method
             * name does not correspond to a `VarHandle`
             * signature-polymorphic method name).
             * @see .methodName
             */
            fun valueFromMethodName(methodName: String): VarHandle.AccessMode {
                val am: VarHandle.AccessMode =
                    VarHandle.AccessMode.Companion.methodNameToAccessMode.get(
                        methodName
                    )
                if (am != null) return am
                throw java.lang.IllegalArgumentException("No AccessMode value for method name $methodName")
            } // BEGIN Android-removed: MemberName and VarForm are not used in the Android implementation.
            /*
        @ForceInline
        static MemberName getMemberName(int ordinal, VarForm vform) {
            return vform.memberName_table[ordinal];
        }
        */
            // END Android-removed: MemberName and VarForm are not used in the Android implementation.
        }
    }
    // BEGIN Android-removed: AccessDescriptor not used in Android implementation.
    /*
    static final class AccessDescriptor {
        final MethodType symbolicMethodTypeErased;
        final MethodType symbolicMethodTypeInvoker;
        final Class<?> returnType;
        final int type;
        final int mode;

        public AccessDescriptor(MethodType symbolicMethodType, int type, int mode) {
            this.symbolicMethodTypeErased = symbolicMethodType.erase();
            this.symbolicMethodTypeInvoker = symbolicMethodType.insertParameterTypes(0, VarHandle.class);
            this.returnType = symbolicMethodType.returnType();
            this.type = type;
            this.mode = mode;
        }
    }
    */
    // END Android-removed: AccessDescriptor not used in Android implementation.
    /**
     * Returns the variable type of variables referenced by this VarHandle.
     *
     * @return the variable type of variables referenced by this VarHandle
     */
    fun varType(): java.lang.Class<*> {
        // Android-removed: existing implementation.
        // MethodType typeSet = accessModeType(AccessMode.SET);
        // return typeSet.parameterType(typeSet.parameterCount() - 1)
        // Android-added: return instance field.
        return varType
    }

    /**
     * Returns the coordinate types for this VarHandle.
     *
     * @return the coordinate types for this VarHandle. The returned
     * list is unmodifiable
     */
    fun coordinateTypes(): List<java.lang.Class<*>> {
        // Android-removed: existing implementation.
        // MethodType typeGet = accessModeType(AccessMode.GET);
        // return typeGet.parameterList();
        // Android-added: Android specific implementation.
        return if (coordinateType0 == null) {
            java.util.Collections.EMPTY_LIST
        } else if (coordinateType1 == null) {
            listOf<java.lang.Class<*>>(coordinateType0)
        } else {
            java.util.Collections.unmodifiableList<java.lang.Class<*>>(
                java.util.Arrays.asList<java.lang.Class<out Any>>(
                    coordinateType0,
                    coordinateType1
                )
            )
        }
    }

    /**
     * Obtains the access mode type for this VarHandle and a given access mode.
     *
     *
     * The access mode type's parameter types will consist of a prefix that
     * is the coordinate types of this VarHandle followed by further
     * types as defined by the access mode method.
     * The access mode type's return type is defined by the return type of the
     * access mode method.
     *
     * @param accessMode the access mode, corresponding to the
     * signature-polymorphic method of the same name
     * @return the access mode type for the given access mode
     */
    fun accessModeType(accessMode: VarHandle.AccessMode): java.lang.invoke.MethodType {
        // BEGIN Android-removed: Relies on internal class that is not part of the
        // Android implementation.
        /*
        TypesAndInvokers tis = getTypesAndInvokers();
        MethodType mt = tis.methodType_table[accessMode.at.ordinal()];
        if (mt == null) {
            mt = tis.methodType_table[accessMode.at.ordinal()] =
                    accessModeTypeUncached(accessMode);
        }
        return mt;
        */
        // END Android-removed: Relies on internal class that is not part of the
        // Android implementation.
        // Android-added: alternative implementation.
        return if (coordinateType1 == null) {
            // accessModeType() treats the first argument as the
            // receiver and adapts accordingly if it is null.
            accessMode.at.accessModeType(coordinateType0, varType)
        } else {
            accessMode.at.accessModeType(coordinateType0, varType, coordinateType1)
        }
    }
    // Android-removed: Not part of the Android implementation.
    // abstract MethodType accessModeTypeUncached(AccessMode accessMode);
    /**
     * Returns `true` if the given access mode is supported, otherwise
     * `false`.
     *
     *
     * The return of a `false` value for a given access mode indicates
     * that an `UnsupportedOperationException` is thrown on invocation
     * of the corresponding access mode method.
     *
     * @param accessMode the access mode, corresponding to the
     * signature-polymorphic method of the same name
     * @return `true` if the given access mode is supported, otherwise
     * `false`.
     */
    fun isAccessModeSupported(accessMode: VarHandle.AccessMode): Boolean {
        // Android-removed: Refers to unused field vform.
        // return AccessMode.getMemberName(accessMode.ordinal(), vform) != null;
        // Android-added: use accessModesBitsMask field.
        val testBit = 1 shl accessMode.ordinal
        return accessModesBitMask and testBit == testBit
    }

    /**
     * Obtains a method handle bound to this VarHandle and the given access
     * mode.
     *
     * @apiNote This method, for a VarHandle `vh` and access mode
     * `{access-mode}`, returns a method handle that is equivalent to
     * method handle `bmh` in the following code (though it may be more
     * efficient):
     * <pre>`MethodHandle mh = MethodHandles.varHandleExactInvoker(
     * vh.accessModeType(VarHandle.AccessMode.{access-mode}));
     *
     * MethodHandle bmh = mh.bindTo(vh);
    `</pre> *
     *
     * @param accessMode the access mode, corresponding to the
     * signature-polymorphic method of the same name
     * @return a method handle bound to this VarHandle and the given access mode
     */
    fun toMethodHandle(accessMode: VarHandle.AccessMode): java.lang.invoke.MethodHandle {
        // BEGIN Android-removed: no vform field in Android implementation.
        /*
        MemberName mn = AccessMode.getMemberName(accessMode.ordinal(), vform);
        if (mn != null) {
            MethodHandle mh = getMethodHandle(accessMode.ordinal());
            return mh.bindTo(this);
        }
        else {
            // Ensure an UnsupportedOperationException is thrown
            return MethodHandles.varHandleInvoker(accessMode, accessModeType(accessMode)).
                    bindTo(this);
        }
        */
        // END Android-removed: no vform field in Android implementation.

        // Android-added: basic implementation following description in javadoc for this method.
        val type: java.lang.invoke.MethodType = accessModeType(accessMode)
        return java.lang.invoke.MethodHandles.varHandleExactInvoker(accessMode, type).bindTo(this)
    }
    // BEGIN Android-added: package private constructors.
    /**
     * Constructor for VarHandle with no coordinates.
     *
     * @param varType the variable type of variables to be referenced
     * @param isFinal whether the target variables are final (non-modifiable)
     * @hide
     */
    internal constructor(varType: java.lang.Class<*>?, isFinal: Boolean) {
        this.varType = java.util.Objects.requireNonNull(varType)
        coordinateType0 = null
        coordinateType1 = null
        accessModesBitMask =
            alignedAccessModesBitMask(varType, isFinal)
    }

    /**
     * Constructor for VarHandle with one coordinate.
     *
     * @param varType the variable type of variables to be referenced
     * @param isFinal  whether the target variables are final (non-modifiable)
     * @param coordinateType the coordinate
     * @hide
     */
    internal constructor(
        varType: java.lang.Class<*>?,
        isFinal: Boolean,
        coordinateType: java.lang.Class<*>?
    ) {
        this.varType = java.util.Objects.requireNonNull(varType)
        coordinateType0 = java.util.Objects.requireNonNull(coordinateType)
        coordinateType1 = null
        accessModesBitMask =
            alignedAccessModesBitMask(varType, isFinal)
    }

    /**
     * Constructor for VarHandle with two coordinates.
     *
     * @param varType the variable type of variables to be referenced
     * @param backingArrayType the type of the array accesses will be performed on
     * @param isFinal whether the target variables are final (non-modifiable)
     * @param coordinateType0 the first coordinate
     * @param coordinateType1 the second coordinate
     * @hide
     */
    internal constructor(
        varType: java.lang.Class<*>, backingArrayType: java.lang.Class<*>, isFinal: Boolean,
        coordinateType0: java.lang.Class<*>?, coordinateType1: java.lang.Class<*>?
    ) {
        this.varType = java.util.Objects.requireNonNull(varType)
        this.coordinateType0 = java.util.Objects.requireNonNull(coordinateType0)
        this.coordinateType1 = java.util.Objects.requireNonNull(coordinateType1)
        java.util.Objects.requireNonNull(backingArrayType)
        val backingArrayComponentType: java.lang.Class<*> = backingArrayType.getComponentType()
        if (backingArrayComponentType != varType && backingArrayComponentType != Byte::class.javaPrimitiveType) {
            throw java.lang.InternalError("Unsupported backingArrayType: $backingArrayType")
        }
        if (backingArrayType.getComponentType() == varType) {
            accessModesBitMask =
                alignedAccessModesBitMask(varType, isFinal)
        } else {
            accessModesBitMask =
                unalignedAccessModesBitMask(varType)
        }
    }
    // END Android-added: helper state for VarHandle properties.
    // BEGIN Android-added: Add VarHandleDesc from OpenJDK 17. http://b/270028670
    /**
     * A [nominal descriptor]({@docRoot}/java.base/java/lang/constant/package-summary.html#nominal) for a
     * [VarHandle] constant.
     *
     * @since 12
     * @hide
     */
    class VarHandleDesc private constructor(
        kind: VarHandle.VarHandleDesc.Kind,
        name: String,
        declaringClass: java.lang.constant.ClassDesc,
        varType: java.lang.constant.ClassDesc
    ) :
        java.lang.constant.DynamicConstantDesc<VarHandle?>(
            kind.bootstrapMethod, name,
            java.lang.constant.ConstantDescs.CD_VarHandle,
            *kind.toBSMArgs(declaringClass, varType)
        ) {
        /**
         * Kinds of variable handle descs
         */
        private enum class Kind(bootstrapMethod: java.lang.constant.DirectMethodHandleDesc) {
            FIELD(java.lang.constant.ConstantDescs.BSM_VARHANDLE_FIELD),
            STATIC_FIELD(java.lang.constant.ConstantDescs.BSM_VARHANDLE_STATIC_FIELD),
            ARRAY(java.lang.constant.ConstantDescs.BSM_VARHANDLE_ARRAY);

            val bootstrapMethod: java.lang.constant.DirectMethodHandleDesc

            init {
                this.bootstrapMethod = bootstrapMethod
            }

            fun toBSMArgs(
                declaringClass: java.lang.constant.ClassDesc?,
                varType: java.lang.constant.ClassDesc?
            ): Array<java.lang.constant.ConstantDesc> {
                return when (this) {
                    VarHandle.VarHandleDesc.Kind.FIELD, VarHandle.VarHandleDesc.Kind.STATIC_FIELD -> arrayOf<java.lang.constant.ConstantDesc>(
                        declaringClass,
                        varType
                    )

                    VarHandle.VarHandleDesc.Kind.ARRAY -> arrayOf<java.lang.constant.ConstantDesc>(
                        declaringClass
                    )

                    else -> throw java.lang.InternalError("Cannot reach here")
                }
            }
        }

        private val kind: VarHandle.VarHandleDesc.Kind
        private val declaringClass: java.lang.constant.ClassDesc
        private val varType: java.lang.constant.ClassDesc

        /**
         * Construct a [VarHandleDesc] given a kind, name, and declaring
         * class.
         *
         * @param kind the kind of the var handle
         * @param name the unqualified name of the field, for field var handles; otherwise ignored
         * @param declaringClass a [ClassDesc] describing the declaring class,
         * for field var handles
         * @param varType a [ClassDesc] describing the type of the variable
         * @throws NullPointerException if any required argument is null
         * @jvms 4.2.2 Unqualified Names
         */
        init {
            this.kind = kind
            this.declaringClass = declaringClass
            this.varType = varType
        }

        /**
         * Returns a [ClassDesc] describing the type of the variable described
         * by this descriptor.
         *
         * @return the variable type
         */
        fun varType(): java.lang.constant.ClassDesc {
            return varType
        }

        @Throws(java.lang.ReflectiveOperationException::class)
        override fun resolveConstantDesc(lookup: java.lang.invoke.MethodHandles.Lookup): VarHandle {
            return when (kind) {
                VarHandle.VarHandleDesc.Kind.FIELD -> lookup.findVarHandle(
                    declaringClass.resolveConstantDesc(lookup) as java.lang.Class<*>,
                    constantName(),
                    varType.resolveConstantDesc(lookup) as java.lang.Class<*>
                )

                VarHandle.VarHandleDesc.Kind.STATIC_FIELD -> lookup.findStaticVarHandle(
                    declaringClass.resolveConstantDesc(lookup) as java.lang.Class<*>,
                    constantName(),
                    varType.resolveConstantDesc(lookup) as java.lang.Class<*>
                )

                VarHandle.VarHandleDesc.Kind.ARRAY -> java.lang.invoke.MethodHandles.arrayElementVarHandle(
                    declaringClass.resolveConstantDesc(lookup) as java.lang.Class<*>
                )

                else -> throw java.lang.InternalError("Cannot reach here")
            }
        }

        /**
         * Returns a compact textual description of this constant description.
         * For a field [VarHandle], includes the owner, name, and type
         * of the field, and whether it is static; for an array [VarHandle],
         * the name of the component type.
         *
         * @return A compact textual description of this descriptor
         */
        override fun toString(): String {
            return when (kind) {
                VarHandle.VarHandleDesc.Kind.FIELD, VarHandle.VarHandleDesc.Kind.STATIC_FIELD -> String.format(
                    "VarHandleDesc[%s%s.%s:%s]",
                    if (kind == VarHandle.VarHandleDesc.Kind.STATIC_FIELD) "static " else "",
                    declaringClass.displayName(), constantName(), varType.displayName()
                )

                VarHandle.VarHandleDesc.Kind.ARRAY -> String.format(
                    "VarHandleDesc[%s[]]",
                    declaringClass.displayName()
                )

                else -> throw java.lang.InternalError("Cannot reach here")
            }
        }

        companion object {
            /**
             * Returns a [VarHandleDesc] corresponding to a [VarHandle]
             * for an instance field.
             *
             * @param name the unqualified name of the field
             * @param declaringClass a [ClassDesc] describing the declaring class,
             * for field var handles
             * @param fieldType a [ClassDesc] describing the type of the field
             * @return the [VarHandleDesc]
             * @throws NullPointerException if any of the arguments are null
             * @jvms 4.2.2 Unqualified Names
             */
            fun ofField(
                declaringClass: java.lang.constant.ClassDesc?,
                name: String?,
                fieldType: java.lang.constant.ClassDesc?
            ): VarHandle.VarHandleDesc {
                java.util.Objects.requireNonNull<java.lang.constant.ClassDesc>(declaringClass)
                java.util.Objects.requireNonNull<String>(name)
                java.util.Objects.requireNonNull<java.lang.constant.ClassDesc>(fieldType)
                return VarHandle.VarHandleDesc(
                    VarHandle.VarHandleDesc.Kind.FIELD,
                    name,
                    declaringClass,
                    fieldType
                )
            }

            /**
             * Returns a [VarHandleDesc] corresponding to a [VarHandle]
             * for a static field.
             *
             * @param name the unqualified name of the field
             * @param declaringClass a [ClassDesc] describing the declaring class,
             * for field var handles
             * @param fieldType a [ClassDesc] describing the type of the field
             * @return the [VarHandleDesc]
             * @throws NullPointerException if any of the arguments are null
             * @jvms 4.2.2 Unqualified Names
             */
            fun ofStaticField(
                declaringClass: java.lang.constant.ClassDesc?,
                name: String?,
                fieldType: java.lang.constant.ClassDesc?
            ): VarHandle.VarHandleDesc {
                java.util.Objects.requireNonNull<java.lang.constant.ClassDesc>(declaringClass)
                java.util.Objects.requireNonNull<String>(name)
                java.util.Objects.requireNonNull<java.lang.constant.ClassDesc>(fieldType)
                return VarHandle.VarHandleDesc(
                    VarHandle.VarHandleDesc.Kind.STATIC_FIELD,
                    name,
                    declaringClass,
                    fieldType
                )
            }

            /**
             * Returns a [VarHandleDesc] corresponding to a [VarHandle]
             * for an array type.
             *
             * @param arrayClass a [ClassDesc] describing the type of the array
             * @return the [VarHandleDesc]
             * @throws NullPointerException if any of the arguments are null
             */
            fun ofArray(arrayClass: java.lang.constant.ClassDesc): VarHandle.VarHandleDesc {
                java.util.Objects.requireNonNull<java.lang.constant.ClassDesc>(arrayClass)
                if (!arrayClass.isArray()) throw java.lang.IllegalArgumentException("Array class argument not an array: $arrayClass")
                return VarHandle.VarHandleDesc(
                    VarHandle.VarHandleDesc.Kind.ARRAY,
                    java.lang.constant.ConstantDescs.DEFAULT_NAME,
                    arrayClass,
                    arrayClass.componentType()
                )
            }
        }
    } // END Android-added: Add VarHandleDesc from OpenJDK 17. http://b/270028670

    companion object {
        // Android-added: Using sun.misc.Unsafe for fence implementation.
        private val UNSAFE: sun.misc.Unsafe = sun.misc.Unsafe.getUnsafe()
        // BEGIN Android-removed: Not used in Android implementation.
        /*
    @Stable
    TypesAndInvokers typesAndInvokers;

    static class TypesAndInvokers {
        final @Stable
        MethodType[] methodType_table =
                new MethodType[VarHandle.AccessType.values().length];

        final @Stable
        MethodHandle[] methodHandle_table =
                new MethodHandle[AccessMode.values().length];
    }

    @ForceInline
    private final TypesAndInvokers getTypesAndInvokers() {
        TypesAndInvokers tis = typesAndInvokers;
        if (tis == null) {
            tis = typesAndInvokers = new TypesAndInvokers();
        }
        return tis;
    }

    @ForceInline
    final MethodHandle getMethodHandle(int mode) {
        TypesAndInvokers tis = getTypesAndInvokers();
        MethodHandle mh = tis.methodHandle_table[mode];
        if (mh == null) {
            mh = tis.methodHandle_table[mode] = getMethodHandleUncached(mode);
        }
        return mh;
    }
    private final MethodHandle getMethodHandleUncached(int mode) {
        MethodType mt = accessModeType(AccessMode.values()[mode]).
                insertParameterTypes(0, VarHandle.class);
        MemberName mn = vform.getMemberName(mode);
        DirectMethodHandle dmh = DirectMethodHandle.make(mn);
        // Such a method handle must not be publically exposed directly
        // otherwise it can be cracked, it must be transformed or rebound
        // before exposure
        MethodHandle mh = dmh.copyWith(mt, dmh.form);
        assert mh.type().erase() == mn.getMethodType().erase();
        return mh;
    }
    */
        // END Android-removed: Not used in Android implementation.
        // BEGIN Android-removed: No VarForm in Android implementation.
        /*non-public*/ /*
    final void updateVarForm(VarForm newVForm) {
        if (vform == newVForm) return;
        UNSAFE.putObject(this, VFORM_OFFSET, newVForm);
        UNSAFE.fullFence();
    }

    static final BiFunction<String, List<Integer>, ArrayIndexOutOfBoundsException>
            AIOOBE_SUPPLIER = Preconditions.outOfBoundsExceptionFormatter(
            new Function<String, ArrayIndexOutOfBoundsException>() {
                @Override
                public ArrayIndexOutOfBoundsException apply(String s) {
                    return new ArrayIndexOutOfBoundsException(s);
                }
            });

    private static final long VFORM_OFFSET;

    static {
        VFORM_OFFSET = UNSAFE.objectFieldOffset(VarHandle.class, "vform");

        // The VarHandleGuards must be initialized to ensure correct
        // compilation of the guard methods
        UNSAFE.ensureClassInitialized(VarHandleGuards.class);
    }
    */
        // END Android-removed: No VarForm in Android implementation.
        // Fence methods
        /**
         * Ensures that loads and stores before the fence will not be reordered
         * with
         * loads and stores after the fence.
         *
         * @apiNote Ignoring the many semantic differences from C and C++, this
         * method has memory ordering effects compatible with
         * `atomic_thread_fence(memory_order_seq_cst)`
         */
        // Android-removed: @ForceInline is an unsupported attribute.
        // @ForceInline
        fun fullFence() {
            UNSAFE.fullFence()
        }

        /**
         * Ensures that loads before the fence will not be reordered with loads and
         * stores after the fence.
         *
         * @apiNote Ignoring the many semantic differences from C and C++, this
         * method has memory ordering effects compatible with
         * `atomic_thread_fence(memory_order_acquire)`
         */
        // Android-removed: @ForceInline is an unsupported attribute.
        // @ForceInline
        fun acquireFence() {
            UNSAFE.loadFence()
        }

        /**
         * Ensures that loads and stores before the fence will not be
         * reordered with stores after the fence.
         *
         * @apiNote Ignoring the many semantic differences from C and C++, this
         * method has memory ordering effects compatible with
         * `atomic_thread_fence(memory_order_release)`
         */
        // Android-removed: @ForceInline is an unsupported attribute.
        // @ForceInline
        fun releaseFence() {
            UNSAFE.storeFence()
        }

        /**
         * Ensures that loads before the fence will not be reordered with
         * loads after the fence.
         */
        // Android-removed: @ForceInline is an unsupported attribute.
        // @ForceInline
        fun loadLoadFence() {
            // Android-changed: Not using UNSAFE.loadLoadFence() as not present on Android.
            // NB The compiler recognizes all the fences here as intrinsics.
            UNSAFE.loadFence()
        }

        /**
         * Ensures that stores before the fence will not be reordered with
         * stores after the fence.
         */
        // Android-removed: @ForceInline is an unsupported attribute.
        // @ForceInline
        fun storeStoreFence() {
            // Android-changed: Not using UNSAFE.storeStoreFence() as not present on Android.
            // NB The compiler recognizes all the fences here as intrinsics.
            UNSAFE.storeFence()
        }
        // END Android-added: package private constructors.
        // BEGIN Android-added: helper state for VarHandle properties.
        /** BitMask of access modes that do not change the memory referenced by a VarHandle.
         * An example being a read of a variable with volatile ordering effects.  */
        private const val READ_ACCESS_MODES_BIT_MASK = 0

        /** BitMask of access modes that write to the memory referenced by
         * a VarHandle.  This does not include any compare and update
         * access modes, nor any bitwise or numeric access modes. An
         * example being a write to variable with release ordering
         * effects.
         */
        private const val WRITE_ACCESS_MODES_BIT_MASK = 0

        /** BitMask of access modes that are applicable to types
         * supporting for atomic updates.  This includes access modes that
         * both read and write a variable such as compare-and-set.
         */
        private const val ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK = 0

        /** BitMask of access modes that are applicable to types
         * supporting numeric atomic update operations.  */
        private const val NUMERIC_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK = 0

        /** BitMask of access modes that are applicable to types
         * supporting bitwise atomic update operations.  */
        private const val BITWISE_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK = 0

        /** BitMask of all access modes.  */
        private const val ALL_MODES_BIT_MASK = 0

        init {
            // Check we're not about to overflow the storage of the
            // bitmasks here and in the accessModesBitMask field.
            if (VarHandle.AccessMode.entries.size > java.lang.Integer.SIZE) {
                throw java.lang.InternalError("accessModes overflow")
            }

            // Access modes bit mask declarations and initialization order
            // follows the presentation order in JEP193.
            READ_ACCESS_MODES_BIT_MASK =
                accessTypesToBitMask(java.util.EnumSet.of(VarHandle.AccessType.GET))
            WRITE_ACCESS_MODES_BIT_MASK =
                accessTypesToBitMask(java.util.EnumSet.of(VarHandle.AccessType.SET))
            ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK =
                accessTypesToBitMask(
                    java.util.EnumSet.of(
                        VarHandle.AccessType.COMPARE_AND_EXCHANGE,
                        VarHandle.AccessType.COMPARE_AND_SET,
                        VarHandle.AccessType.GET_AND_UPDATE
                    )
                )
            NUMERIC_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK =
                accessTypesToBitMask(java.util.EnumSet.of(VarHandle.AccessType.GET_AND_UPDATE_NUMERIC))
            BITWISE_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK =
                accessTypesToBitMask(java.util.EnumSet.of(VarHandle.AccessType.GET_AND_UPDATE_BITWISE))
            ALL_MODES_BIT_MASK =
                READ_ACCESS_MODES_BIT_MASK or
                        WRITE_ACCESS_MODES_BIT_MASK or
                        ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK or
                        NUMERIC_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK or BITWISE_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK
        }

        fun accessTypesToBitMask(accessTypes: java.util.EnumSet<VarHandle.AccessType?>): Int {
            var m = 0
            for (accessMode in VarHandle.AccessMode.entries) {
                if (accessTypes.contains(accessMode.at)) {
                    m = m or (1 shl accessMode.ordinal)
                }
            }
            return m
        }

        fun alignedAccessModesBitMask(varType: java.lang.Class<*>, isFinal: Boolean): Int {
            // For aligned accesses, the supported access modes are described in:
            // @see java.lang.invoke.MethodHandles.Lookup#findVarHandle
            var bitMask: Int = ALL_MODES_BIT_MASK

            // If the field is declared final, keep only the read access modes.
            if (isFinal) {
                bitMask =
                    bitMask and READ_ACCESS_MODES_BIT_MASK
            }

            // If the field is anything other than byte, short, char, int,
            // long, float, double then remove the numeric atomic update
            // access modes.
            if (varType != Byte::class && varType != Short::class && varType != Char::class.javaPrimitiveType && varType != Int::class.javaPrimitiveType && varType != Long::class.javaPrimitiveType && varType != Float::class.javaPrimitiveType && varType != Double::class.javaPrimitiveType) {
                bitMask =
                    bitMask and NUMERIC_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK.inv()
            }

            // If the field is not integral, remove the bitwise atomic update access modes.
            if (varType != Boolean::class && varType != Byte::class && varType != Short::class.javaPrimitiveType && varType != Char::class.javaPrimitiveType && varType != Int::class.javaPrimitiveType && varType != Long::class.javaPrimitiveType) {
                bitMask =
                    bitMask and BITWISE_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK.inv()
            }
            return bitMask
        }

        fun unalignedAccessModesBitMask(varType: java.lang.Class<*>): Int {
            // The VarHandle refers to a view of byte array or a
            // view of a byte buffer.  The corresponding accesses
            // maybe unaligned so the access modes are more
            // restrictive than field or array element accesses.
            //
            // The supported access modes are described in:
            // @see java.lang.invoke.MethodHandles#byteArrayViewVarHandle

            // Read/write access modes supported for all types including
            // long and double on 32-bit platforms (though these accesses
            // may not be atomic).
            var bitMask: Int =
                READ_ACCESS_MODES_BIT_MASK or WRITE_ACCESS_MODES_BIT_MASK

            // int, long, float, double support atomic update modes per documentation.
            if (varType == Int::class.javaPrimitiveType || varType == Long::class.javaPrimitiveType || varType == Float::class.javaPrimitiveType || varType == Double::class.javaPrimitiveType) {
                bitMask =
                    bitMask or ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK
            }

            // int and long support numeric updates per documentation.
            if (varType == Int::class.javaPrimitiveType || varType == Long::class.javaPrimitiveType) {
                bitMask =
                    bitMask or NUMERIC_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK
            }

            // int and long support bitwise updates per documentation.
            if (varType == Int::class.javaPrimitiveType || varType == Long::class.javaPrimitiveType) {
                bitMask =
                    bitMask or BITWISE_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK
            }
            return bitMask
        }
    }
}

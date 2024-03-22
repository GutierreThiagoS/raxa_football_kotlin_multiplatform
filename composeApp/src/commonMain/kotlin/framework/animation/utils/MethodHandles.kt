package framework.animation.utils

import sun.reflect.Reflection
import kotlin.jvm.Synchronized

/*
* Copyright (c) 2008, 2017, Oracle and/or its affiliates. All rights reserved.
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
package java.lang.invoke
import sun.reflect.Reflection

/**
 * This class consists exclusively of static methods that operate on or return
 * method handles. They fall into several categories:
 *
 *  * Lookup methods which help create method handles for methods and fields.
 *  * Combinator methods, which combine or transform pre-existing method handles into new ones.
 *  * Other factory methods to create method handles that emulate other common JVM operations or control flow patterns.
 *
 *
 *
 * @author John Rose, JSR 292 EG
 * @since 1.7
 */
object MethodHandles {
    // Android-changed: We do not use MemberName / MethodHandleImpl.
    //
    // private static final MemberName.Factory IMPL_NAMES = MemberName.getFactory();
    // static { MethodHandleImpl.initStatics(); }
    // See IMPL_LOOKUP below.
    //// Method handle creation from ordinary methods.
    /**
     * Returns a [lookup object][Lookup] with
     * full capabilities to emulate all supported bytecode behaviors of the caller.
     * These capabilities include [private access](MethodHandles.Lookup.html#privacc) to the caller.
     * Factory methods on the lookup object can create
     * [direct method handles](MethodHandleInfo.html#directmh)
     * for any member that the caller has access to via bytecodes,
     * including protected and private fields and methods.
     * This lookup object is a *capability* which may be delegated to trusted agents.
     * Do not store it in place where untrusted code can access it.
     *
     *
     * This method is caller sensitive, which means that it may return different
     * values to different callers.
     *
     *
     * For any given caller class `C`, the lookup object returned by this call
     * has equivalent capabilities to any lookup object
     * supplied by the JVM to the bootstrap method of an
     * [invokedynamic instruction](package-summary.html#indyinsn)
     * executing in the same caller class `C`.
     * @return a lookup object for the caller of this method, with private access
     */
    // Android-changed: Remove caller sensitive.
    // @CallerSensitive
    fun lookup(): java.lang.invoke.MethodHandles.Lookup {
        return java.lang.invoke.MethodHandles.Lookup(Reflection.getCallerClass())
    }

    /**
     * Returns a [lookup object][Lookup] which is trusted minimally.
     * It can only be used to create method handles to
     * publicly accessible fields and methods.
     *
     *
     * As a matter of pure convention, the [lookup class][Lookup.lookupClass]
     * of this lookup object will be [java.lang.Object].
     *
     *
     *
     * *Discussion:*
     * The lookup class can be changed to any other class `C` using an expression of the form
     * [publicLookup().in(C.class)][Lookup. in].
     * Since all classes have equal access to public names,
     * such a change would confer no new access rights.
     * A public lookup object is always subject to
     * [security manager checks](MethodHandles.Lookup.html#secmgr).
     * Also, it cannot access
     * [caller sensitive methods](MethodHandles.Lookup.html#callsens).
     * @return a lookup object which is trusted minimally
     */
    fun publicLookup(): java.lang.invoke.MethodHandles.Lookup {
        return java.lang.invoke.MethodHandles.Lookup.Companion.PUBLIC_LOOKUP
    }
    // Android-removed: Documentation related to the security manager and module checks
    /**
     * Returns a [lookup object][Lookup] with full capabilities to emulate all
     * supported bytecode behaviors, including [
 * private access](MethodHandles.Lookup.html#privacc), on a target class.
     * @param targetClass the target class
     * @param lookup the caller lookup object
     * @return a lookup object for the target class, with private access
     * @throws IllegalArgumentException if `targetClass` is a primitive type or array class
     * @throws NullPointerException if `targetClass` or `caller` is `null`
     * @throws IllegalAccessException is not thrown on Android
     * @since 9
     */
    @Throws(java.lang.IllegalAccessException::class)
    fun privateLookupIn(
        targetClass: java.lang.Class<*>,
        lookup: java.lang.invoke.MethodHandles.Lookup?
    ): java.lang.invoke.MethodHandles.Lookup {
        // Android-removed: SecurityManager calls
        // SecurityManager sm = System.getSecurityManager();
        // if (sm != null) sm.checkPermission(ACCESS_PERMISSION);
        if (targetClass.isPrimitive()) throw java.lang.IllegalArgumentException(targetClass.toString() + " is a primitive class")
        if (targetClass.isArray()) throw java.lang.IllegalArgumentException(targetClass.toString() + " is an array class")
        // BEGIN Android-removed: There is no module information on Android
        /**
         * Module targetModule = targetClass.getModule();
         * Module callerModule = lookup.lookupClass().getModule();
         * if (!callerModule.canRead(targetModule))
         * throw new IllegalAccessException(callerModule + " does not read " + targetModule);
         * if (targetModule.isNamed()) {
         * String pn = targetClass.getPackageName();
         * assert pn.length() > 0 : "unnamed package cannot be in named module";
         * if (!targetModule.isOpen(pn, callerModule))
         * throw new IllegalAccessException(targetModule + " does not open " + pn + " to " + callerModule);
         * }
         * if ((lookup.lookupModes() & Lookup.MODULE) == 0)
         * throw new IllegalAccessException("lookup does not have MODULE lookup mode");
         * if (!callerModule.isNamed() && targetModule.isNamed()) {
         * IllegalAccessLogger logger = IllegalAccessLogger.illegalAccessLogger();
         * if (logger != null) {
         * logger.logIfOpenedForIllegalAccess(lookup, targetClass);
         * }
         * }
         */
        // END Android-removed: There is no module information on Android
        return java.lang.invoke.MethodHandles.Lookup(targetClass)
    }

    /**
     * Performs an unchecked "crack" of a
     * [direct method handle](MethodHandleInfo.html#directmh).
     * The result is as if the user had obtained a lookup object capable enough
     * to crack the target method handle, called
     * [Lookup.revealDirect][java.lang.invoke.MethodHandles.Lookup.revealDirect]
     * on the target to obtain its symbolic reference, and then called
     * [MethodHandleInfo.reflectAs][java.lang.invoke.MethodHandleInfo.reflectAs]
     * to resolve the symbolic reference to a member.
     *
     *
     * If there is a security manager, its `checkPermission` method
     * is called with a `ReflectPermission("suppressAccessChecks")` permission.
     * @param <T> the desired type of the result, either [Member] or a subtype
     * @param target a direct method handle to crack into symbolic reference components
     * @param expected a class object representing the desired result type `T`
     * @return a reference to the method, constructor, or field object
     * @exception SecurityException if the caller is not privileged to call `setAccessible`
     * @exception NullPointerException if either argument is `null`
     * @exception IllegalArgumentException if the target is not a direct method handle
     * @exception ClassCastException if the member is not of the expected type
     * @since 1.8
    </T> */
    fun <T : java.lang.reflect.Member?> reflectAs(
        expected: java.lang.Class<T>,
        target: java.lang.invoke.MethodHandle?
    ): T {
        val directTarget: java.lang.invoke.MethodHandleImpl =
            java.lang.invoke.MethodHandles.getMethodHandleImpl(target)
        // Given that this is specified to be an "unchecked" crack, we can directly allocate
        // a member from the underlying ArtField / Method and bypass all associated access checks.
        return expected.cast(directTarget.getMemberInternal())
    }

    /**
     * "Cracks" `target` to reveal the underlying `MethodHandleImpl`.
     */
    private fun getMethodHandleImpl(target: java.lang.invoke.MethodHandle): java.lang.invoke.MethodHandleImpl {
        // Special case : We implement handles to constructors as transformers,
        // so we must extract the underlying handle from the transformer.
        var target: java.lang.invoke.MethodHandle = target
        if (target is Transformers.Construct) {
            target = (target as Transformers.Construct).getConstructorHandle()
        }

        // Special case: Var-args methods are also implemented as Transformers,
        // so we should get the underlying handle in that case as well.
        if (target is Transformers.VarargsCollector) {
            target = target.asFixedArity()
        }
        if (target is java.lang.invoke.MethodHandleImpl) {
            return target as java.lang.invoke.MethodHandleImpl
        }
        throw java.lang.IllegalArgumentException(target.toString() + " is not a direct handle")
    }
    // Android-removed: unsupported @jvms tag in doc-comment.
    /**
     * Produces a method handle constructing arrays of a desired type,
     * as if by the `anewarray` bytecode.
     * The return type of the method handle will be the array type.
     * The type of its sole argument will be `int`, which specifies the size of the array.
     *
     *
     *  If the returned method handle is invoked with a negative
     * array size, a `NegativeArraySizeException` will be thrown.
     *
     * @param arrayClass an array type
     * @return a method handle which can create arrays of the given type
     * @throws NullPointerException if the argument is `null`
     * @throws IllegalArgumentException if `arrayClass` is not an array type
     * @see java.lang.reflect.Array.newInstance
     * @since 9
     */
    @Throws(java.lang.IllegalArgumentException::class)
    fun arrayConstructor(arrayClass: java.lang.Class<*>): java.lang.invoke.MethodHandle {
        if (!arrayClass.isArray()) {
            throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException("not an array class: " + arrayClass.getName())
        }
        // Android-changed: transformer based implementation.
        // MethodHandle ani = MethodHandleImpl.getConstantHandle(MethodHandleImpl.MH_Array_newInstance).
        // bindTo(arrayClass.getComponentType());
        // return ani.asType(ani.type().changeReturnType(arrayClass))
        return ArrayConstructor(arrayClass)
    }
    // Android-removed: unsupported @jvms tag in doc-comment.
    /**
     * Produces a method handle returning the length of an array,
     * as if by the `arraylength` bytecode.
     * The type of the method handle will have `int` as return type,
     * and its sole argument will be the array type.
     *
     *
     *  If the returned method handle is invoked with a `null`
     * array reference, a `NullPointerException` will be thrown.
     *
     * @param arrayClass an array type
     * @return a method handle which can retrieve the length of an array of the given array type
     * @throws NullPointerException if the argument is `null`
     * @throws IllegalArgumentException if arrayClass is not an array type
     * @since 9
     */
    @Throws(java.lang.IllegalArgumentException::class)
    fun arrayLength(arrayClass: java.lang.Class<*>): java.lang.invoke.MethodHandle {
        // Android-changed: transformer based implementation.
        // return MethodHandleImpl.makeArrayElementAccessor(arrayClass, MethodHandleImpl.ArrayAccess.LENGTH);
        if (!arrayClass.isArray()) {
            throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException("not an array class: " + arrayClass.getName())
        }
        return ArrayLength(arrayClass)
    }

    // BEGIN Android-added: method to check if a class is an array.
    private fun checkClassIsArray(c: java.lang.Class<*>) {
        if (!c.isArray()) {
            throw java.lang.IllegalArgumentException("Not an array type: $c")
        }
    }

    private fun checkTypeIsViewable(componentType: java.lang.Class<*>) {
        if (componentType == Short::class.javaPrimitiveType || componentType == Char::class.javaPrimitiveType || componentType == Int::class.javaPrimitiveType || componentType == Long::class.javaPrimitiveType || componentType == Float::class.javaPrimitiveType || componentType == Double::class.javaPrimitiveType) {
            return
        }
        throw java.lang.UnsupportedOperationException("Component type not supported: $componentType")
    }
    // END Android-added: method to check if a class is an array.
    /**
     * Produces a method handle giving read access to elements of an array.
     * The type of the method handle will have a return type of the array's
     * element type.  Its first argument will be the array type,
     * and the second will be `int`.
     * @param arrayClass an array type
     * @return a method handle which can load values from the given array type
     * @throws NullPointerException if the argument is null
     * @throws  IllegalArgumentException if arrayClass is not an array type
     */
    @Throws(java.lang.IllegalArgumentException::class)
    fun arrayElementGetter(arrayClass: java.lang.Class<*>): java.lang.invoke.MethodHandle {
        java.lang.invoke.MethodHandles.checkClassIsArray(arrayClass)
        val componentType: java.lang.Class<*> = arrayClass.getComponentType()
        if (componentType.isPrimitive()) {
            try {
                return java.lang.invoke.MethodHandles.Lookup.Companion.PUBLIC_LOOKUP.findStatic(
                    MethodHandles::class.java,
                    "arrayElementGetter",
                    java.lang.invoke.MethodType.methodType(
                        componentType,
                        arrayClass,
                        Int::class.javaPrimitiveType
                    )
                )
            } catch (exception: java.lang.NoSuchMethodException) {
                throw java.lang.AssertionError(exception)
            } catch (exception: java.lang.IllegalAccessException) {
                throw java.lang.AssertionError(exception)
            }
        }
        return ReferenceArrayElementGetter(arrayClass)
    }

    /** @hide
     */
    fun arrayElementGetter(array: ByteArray, i: Int): Byte {
        return array[i]
    }

    /** @hide
     */
    fun arrayElementGetter(array: BooleanArray, i: Int): Boolean {
        return array[i]
    }

    /** @hide
     */
    fun arrayElementGetter(array: CharArray, i: Int): Char {
        return array[i]
    }

    /** @hide
     */
    fun arrayElementGetter(array: ShortArray, i: Int): Short {
        return array[i]
    }

    /** @hide
     */
    fun arrayElementGetter(array: IntArray, i: Int): Int {
        return array[i]
    }

    /** @hide
     */
    fun arrayElementGetter(array: LongArray, i: Int): Long {
        return array[i]
    }

    /** @hide
     */
    fun arrayElementGetter(array: FloatArray, i: Int): Float {
        return array[i]
    }

    /** @hide
     */
    fun arrayElementGetter(array: DoubleArray, i: Int): Double {
        return array[i]
    }

    /**
     * Produces a method handle giving write access to elements of an array.
     * The type of the method handle will have a void return type.
     * Its last argument will be the array's element type.
     * The first and second arguments will be the array type and int.
     * @param arrayClass the class of an array
     * @return a method handle which can store values into the array type
     * @throws NullPointerException if the argument is null
     * @throws IllegalArgumentException if arrayClass is not an array type
     */
    @Throws(java.lang.IllegalArgumentException::class)
    fun arrayElementSetter(arrayClass: java.lang.Class<*>): java.lang.invoke.MethodHandle {
        java.lang.invoke.MethodHandles.checkClassIsArray(arrayClass)
        val componentType: java.lang.Class<*> = arrayClass.getComponentType()
        if (componentType.isPrimitive()) {
            try {
                return java.lang.invoke.MethodHandles.Lookup.Companion.PUBLIC_LOOKUP.findStatic(
                    MethodHandles::class.java,
                    "arrayElementSetter",
                    java.lang.invoke.MethodType.methodType(
                        Void.TYPE, arrayClass,
                        Int::class.javaPrimitiveType, componentType
                    )
                )
            } catch (exception: java.lang.NoSuchMethodException) {
                throw java.lang.AssertionError(exception)
            } catch (exception: java.lang.IllegalAccessException) {
                throw java.lang.AssertionError(exception)
            }
        }
        return ReferenceArrayElementSetter(arrayClass)
    }

    /** @hide
     */
    fun arrayElementSetter(array: ByteArray, i: Int, `val`: Byte) {
        array[i] = `val`
    }

    /** @hide
     */
    fun arrayElementSetter(array: BooleanArray, i: Int, `val`: Boolean) {
        array[i] = `val`
    }

    /** @hide
     */
    fun arrayElementSetter(array: CharArray, i: Int, `val`: Char) {
        array[i] = `val`
    }

    /** @hide
     */
    fun arrayElementSetter(array: ShortArray, i: Int, `val`: Short) {
        array[i] = `val`
    }

    /** @hide
     */
    fun arrayElementSetter(array: IntArray, i: Int, `val`: Int) {
        array[i] = `val`
    }

    /** @hide
     */
    fun arrayElementSetter(array: LongArray, i: Int, `val`: Long) {
        array[i] = `val`
    }

    /** @hide
     */
    fun arrayElementSetter(array: FloatArray, i: Int, `val`: Float) {
        array[i] = `val`
    }

    /** @hide
     */
    fun arrayElementSetter(array: DoubleArray, i: Int, `val`: Double) {
        array[i] = `val`
    }
    // BEGIN Android-changed: OpenJDK 9+181 VarHandle API factory methods.
    /**
     * Produces a VarHandle giving access to elements of an array of type
     * `arrayClass`.  The VarHandle's variable type is the component type
     * of `arrayClass` and the list of coordinate types is
     * `(arrayClass, int)`, where the `int` coordinate type
     * corresponds to an argument that is an index into an array.
     *
     *
     * Certain access modes of the returned VarHandle are unsupported under
     * the following conditions:
     *
     *  * if the component type is anything other than `byte`,
     * `short`, `char`, `int`, `long`,
     * `float`, or `double` then numeric atomic update access
     * modes are unsupported.
     *  * if the field type is anything other than `boolean`,
     * `byte`, `short`, `char`, `int` or
     * `long` then bitwise atomic update access modes are
     * unsupported.
     *
     *
     *
     * If the component type is `float` or `double` then numeric
     * and atomic update access modes compare values using their bitwise
     * representation (see [Float.floatToRawIntBits] and
     * [Double.doubleToRawLongBits], respectively).
     * @apiNote
     * Bitwise comparison of `float` values or `double` values,
     * as performed by the numeric and atomic update access modes, differ
     * from the primitive `==` operator and the [Float.equals]
     * and [Double.equals] methods, specifically with respect to
     * comparing NaN values or comparing `-0.0` with `+0.0`.
     * Care should be taken when performing a compare and set or a compare
     * and exchange operation with such values since the operation may
     * unexpectedly fail.
     * There are many possible NaN values that are considered to be
     * `NaN` in Java, although no IEEE 754 floating-point operation
     * provided by Java can distinguish between them.  Operation failure can
     * occur if the expected or witness value is a NaN value and it is
     * transformed (perhaps in a platform specific manner) into another NaN
     * value, and thus has a different bitwise representation (see
     * [Float.intBitsToFloat] or [Double.longBitsToDouble] for more
     * details).
     * The values `-0.0` and `+0.0` have different bitwise
     * representations but are considered equal when using the primitive
     * `==` operator.  Operation failure can occur if, for example, a
     * numeric algorithm computes an expected value to be say `-0.0`
     * and previously computed the witness value to be say `+0.0`.
     * @param arrayClass the class of an array, of type `T[]`
     * @return a VarHandle giving access to elements of an array
     * @throws NullPointerException if the arrayClass is null
     * @throws IllegalArgumentException if arrayClass is not an array type
     * @since 9
     */
    @Throws(java.lang.IllegalArgumentException::class)
    fun arrayElementVarHandle(arrayClass: java.lang.Class<*>?): java.lang.invoke.VarHandle {
        java.lang.invoke.MethodHandles.checkClassIsArray(arrayClass)
        return ArrayElementVarHandle.create(arrayClass)
    }

    /**
     * Produces a VarHandle giving access to elements of a `byte[]` array
     * viewed as if it were a different primitive array type, such as
     * `int[]` or `long[]`.
     * The VarHandle's variable type is the component type of
     * `viewArrayClass` and the list of coordinate types is
     * `(byte[], int)`, where the `int` coordinate type
     * corresponds to an argument that is an index into a `byte[]` array.
     * The returned VarHandle accesses bytes at an index in a `byte[]`
     * array, composing bytes to or from a value of the component type of
     * `viewArrayClass` according to the given endianness.
     *
     *
     * The supported component types (variables types) are `short`,
     * `char`, `int`, `long`, `float` and
     * `double`.
     *
     *
     * Access of bytes at a given index will result in an
     * `IndexOutOfBoundsException` if the index is less than `0`
     * or greater than the `byte[]` array length minus the size (in bytes)
     * of `T`.
     *
     *
     * Access of bytes at an index may be aligned or misaligned for `T`,
     * with respect to the underlying memory address, `A` say, associated
     * with the array and index.
     * If access is misaligned then access for anything other than the
     * `get` and `set` access modes will result in an
     * `IllegalStateException`.  In such cases atomic access is only
     * guaranteed with respect to the largest power of two that divides the GCD
     * of `A` and the size (in bytes) of `T`.
     * If access is aligned then following access modes are supported and are
     * guaranteed to support atomic access:
     *
     *  * read write access modes for all `T`, with the exception of
     * access modes `get` and `set` for `long` and
     * `double` on 32-bit platforms.
     *  * atomic update access modes for `int`, `long`,
     * `float` or `double`.
     * (Future major platform releases of the JDK may support additional
     * types for certain currently unsupported access modes.)
     *  * numeric atomic update access modes for `int` and `long`.
     * (Future major platform releases of the JDK may support additional
     * numeric types for certain currently unsupported access modes.)
     *  * bitwise atomic update access modes for `int` and `long`.
     * (Future major platform releases of the JDK may support additional
     * numeric types for certain currently unsupported access modes.)
     *
     *
     *
     * Misaligned access, and therefore atomicity guarantees, may be determined
     * for `byte[]` arrays without operating on a specific array.  Given
     * an `index`, `T` and it's corresponding boxed type,
     * `T_BOX`, misalignment may be determined as follows:
     * <pre>`int sizeOfT = T_BOX.BYTES;  // size in bytes of T
     * int misalignedAtZeroIndex = ByteBuffer.wrap(new byte[0]).
     * alignmentOffset(0, sizeOfT);
     * int misalignedAtIndex = (misalignedAtZeroIndex + index) % sizeOfT;
     * boolean isMisaligned = misalignedAtIndex != 0;
    `</pre> *
     *
     *
     * If the variable type is `float` or `double` then atomic
     * update access modes compare values using their bitwise representation
     * (see [Float.floatToRawIntBits] and
     * [Double.doubleToRawLongBits], respectively).
     * @param viewArrayClass the view array class, with a component type of
     * type `T`
     * @param byteOrder the endianness of the view array elements, as
     * stored in the underlying `byte` array
     * @return a VarHandle giving access to elements of a `byte[]` array
     * viewed as if elements corresponding to the components type of the view
     * array class
     * @throws NullPointerException if viewArrayClass or byteOrder is null
     * @throws IllegalArgumentException if viewArrayClass is not an array type
     * @throws UnsupportedOperationException if the component type of
     * viewArrayClass is not supported as a variable type
     * @since 9
     */
    @Throws(java.lang.IllegalArgumentException::class)
    fun byteArrayViewVarHandle(
        viewArrayClass: java.lang.Class<*>,
        byteOrder: java.nio.ByteOrder?
    ): java.lang.invoke.VarHandle {
        java.lang.invoke.MethodHandles.checkClassIsArray(viewArrayClass)
        java.lang.invoke.MethodHandles.checkTypeIsViewable(viewArrayClass.getComponentType())
        return ByteArrayViewVarHandle.create(viewArrayClass, byteOrder)
    }

    /**
     * Produces a VarHandle giving access to elements of a `ByteBuffer`
     * viewed as if it were an array of elements of a different primitive
     * component type to that of `byte`, such as `int[]` or
     * `long[]`.
     * The VarHandle's variable type is the component type of
     * `viewArrayClass` and the list of coordinate types is
     * `(ByteBuffer, int)`, where the `int` coordinate type
     * corresponds to an argument that is an index into a `byte[]` array.
     * The returned VarHandle accesses bytes at an index in a
     * `ByteBuffer`, composing bytes to or from a value of the component
     * type of `viewArrayClass` according to the given endianness.
     *
     *
     * The supported component types (variables types) are `short`,
     * `char`, `int`, `long`, `float` and
     * `double`.
     *
     *
     * Access will result in a `ReadOnlyBufferException` for anything
     * other than the read access modes if the `ByteBuffer` is read-only.
     *
     *
     * Access of bytes at a given index will result in an
     * `IndexOutOfBoundsException` if the index is less than `0`
     * or greater than the `ByteBuffer` limit minus the size (in bytes) of
     * `T`.
     *
     *
     * Access of bytes at an index may be aligned or misaligned for `T`,
     * with respect to the underlying memory address, `A` say, associated
     * with the `ByteBuffer` and index.
     * If access is misaligned then access for anything other than the
     * `get` and `set` access modes will result in an
     * `IllegalStateException`.  In such cases atomic access is only
     * guaranteed with respect to the largest power of two that divides the GCD
     * of `A` and the size (in bytes) of `T`.
     * If access is aligned then following access modes are supported and are
     * guaranteed to support atomic access:
     *
     *  * read write access modes for all `T`, with the exception of
     * access modes `get` and `set` for `long` and
     * `double` on 32-bit platforms.
     *  * atomic update access modes for `int`, `long`,
     * `float` or `double`.
     * (Future major platform releases of the JDK may support additional
     * types for certain currently unsupported access modes.)
     *  * numeric atomic update access modes for `int` and `long`.
     * (Future major platform releases of the JDK may support additional
     * numeric types for certain currently unsupported access modes.)
     *  * bitwise atomic update access modes for `int` and `long`.
     * (Future major platform releases of the JDK may support additional
     * numeric types for certain currently unsupported access modes.)
     *
     *
     *
     * Misaligned access, and therefore atomicity guarantees, may be determined
     * for a `ByteBuffer`, `bb` (direct or otherwise), an
     * `index`, `T` and it's corresponding boxed type,
     * `T_BOX`, as follows:
     * <pre>`int sizeOfT = T_BOX.BYTES;  // size in bytes of T
     * ByteBuffer bb = ...
     * int misalignedAtIndex = bb.alignmentOffset(index, sizeOfT);
     * boolean isMisaligned = misalignedAtIndex != 0;
    `</pre> *
     *
     *
     * If the variable type is `float` or `double` then atomic
     * update access modes compare values using their bitwise representation
     * (see [Float.floatToRawIntBits] and
     * [Double.doubleToRawLongBits], respectively).
     * @param viewArrayClass the view array class, with a component type of
     * type `T`
     * @param byteOrder the endianness of the view array elements, as
     * stored in the underlying `ByteBuffer` (Note this overrides the
     * endianness of a `ByteBuffer`)
     * @return a VarHandle giving access to elements of a `ByteBuffer`
     * viewed as if elements corresponding to the components type of the view
     * array class
     * @throws NullPointerException if viewArrayClass or byteOrder is null
     * @throws IllegalArgumentException if viewArrayClass is not an array type
     * @throws UnsupportedOperationException if the component type of
     * viewArrayClass is not supported as a variable type
     * @since 9
     */
    @Throws(java.lang.IllegalArgumentException::class)
    fun byteBufferViewVarHandle(
        viewArrayClass: java.lang.Class<*>,
        byteOrder: java.nio.ByteOrder?
    ): java.lang.invoke.VarHandle {
        java.lang.invoke.MethodHandles.checkClassIsArray(viewArrayClass)
        java.lang.invoke.MethodHandles.checkTypeIsViewable(viewArrayClass.getComponentType())
        return ByteBufferViewVarHandle.create(viewArrayClass, byteOrder)
    }
    // END Android-changed: OpenJDK 9+181 VarHandle API factory methods.
    /// method handle invocation (reflective style)
    /**
     * Produces a method handle which will invoke any method handle of the
     * given `type`, with a given number of trailing arguments replaced by
     * a single trailing `Object[]` array.
     * The resulting invoker will be a method handle with the following
     * arguments:
     *
     *  * a single `MethodHandle` target
     *  * zero or more leading values (counted by `leadingArgCount`)
     *  * an `Object[]` array containing trailing arguments
     *
     *
     *
     * The invoker will invoke its target like a call to [invoke][MethodHandle.invoke] with
     * the indicated `type`.
     * That is, if the target is exactly of the given `type`, it will behave
     * like `invokeExact`; otherwise it behave as if [asType][MethodHandle.asType]
     * is used to convert the target to the required `type`.
     *
     *
     * The type of the returned invoker will not be the given `type`, but rather
     * will have all parameters except the first `leadingArgCount`
     * replaced by a single array of type `Object[]`, which will be
     * the final parameter.
     *
     *
     * Before invoking its target, the invoker will spread the final array, apply
     * reference casts as necessary, and unbox and widen primitive arguments.
     * If, when the invoker is called, the supplied array argument does
     * not have the correct number of elements, the invoker will throw
     * an [IllegalArgumentException] instead of invoking the target.
     *
     *
     * This method is equivalent to the following code (though it may be more efficient):
     * <blockquote><pre>`MethodHandle invoker = MethodHandles.invoker(type);
     * int spreadArgCount = type.parameterCount() - leadingArgCount;
     * invoker = invoker.asSpreader(Object[].class, spreadArgCount);
     * return invoker;
    `</pre></blockquote> *
     * This method throws no reflective or security exceptions.
     * @param type the desired target type
     * @param leadingArgCount number of fixed arguments, to be passed unchanged to the target
     * @return a method handle suitable for invoking any method handle of the given type
     * @throws NullPointerException if `type` is null
     * @throws IllegalArgumentException if `leadingArgCount` is not in
     * the range from 0 to `type.parameterCount()` inclusive,
     * or if the resulting method handle's type would have
     * [too many parameters](MethodHandle.html#maxarity)
     */
    fun spreadInvoker(
        type: java.lang.invoke.MethodType,
        leadingArgCount: Int
    ): java.lang.invoke.MethodHandle {
        if (leadingArgCount < 0 || leadingArgCount > type.parameterCount()) throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
            "bad argument count",
            leadingArgCount
        )
        var invoker: java.lang.invoke.MethodHandle = java.lang.invoke.MethodHandles.invoker(type)
        val spreadArgCount: Int = type.parameterCount() - leadingArgCount
        invoker = invoker.asSpreader(Array<Any>::class.java, spreadArgCount)
        return invoker
    }

    /**
     * Produces a special *invoker method handle* which can be used to
     * invoke any method handle of the given type, as if by [invokeExact][MethodHandle.invokeExact].
     * The resulting invoker will have a type which is
     * exactly equal to the desired type, except that it will accept
     * an additional leading argument of type `MethodHandle`.
     *
     *
     * This method is equivalent to the following code (though it may be more efficient):
     * `publicLookup().findVirtual(MethodHandle.class, "invokeExact", type)`
     *
     *
     *
     * *Discussion:*
     * Invoker method handles can be useful when working with variable method handles
     * of unknown types.
     * For example, to emulate an `invokeExact` call to a variable method
     * handle `M`, extract its type `T`,
     * look up the invoker method `X` for `T`,
     * and call the invoker method, as `X.invoke(T, A...)`.
     * (It would not work to call `X.invokeExact`, since the type `T`
     * is unknown.)
     * If spreading, collecting, or other argument transformations are required,
     * they can be applied once to the invoker `X` and reused on many `M`
     * method handle values, as long as they are compatible with the type of `X`.
     *
     *
     * *(Note:  The invoker method is not available via the Core Reflection API.
     * An attempt to call [java.lang.reflect.Method.invoke]
     * on the declared `invokeExact` or `invoke` method will raise an
     * [UnsupportedOperationException][java.lang.UnsupportedOperationException].)*
     *
     *
     * This method throws no reflective or security exceptions.
     * @param type the desired target type
     * @return a method handle suitable for invoking any method handle of the given type
     * @throws IllegalArgumentException if the resulting method handle's type would have
     * [too many parameters](MethodHandle.html#maxarity)
     */
    fun exactInvoker(type: java.lang.invoke.MethodType?): java.lang.invoke.MethodHandle {
        return Invoker(type, true /* isExactInvoker */)
    }

    /**
     * Produces a special *invoker method handle* which can be used to
     * invoke any method handle compatible with the given type, as if by [invoke][MethodHandle.invoke].
     * The resulting invoker will have a type which is
     * exactly equal to the desired type, except that it will accept
     * an additional leading argument of type `MethodHandle`.
     *
     *
     * Before invoking its target, if the target differs from the expected type,
     * the invoker will apply reference casts as
     * necessary and box, unbox, or widen primitive values, as if by [asType][MethodHandle.asType].
     * Similarly, the return value will be converted as necessary.
     * If the target is a [variable arity method handle][MethodHandle.asVarargsCollector],
     * the required arity conversion will be made, again as if by [asType][MethodHandle.asType].
     *
     *
     * This method is equivalent to the following code (though it may be more efficient):
     * `publicLookup().findVirtual(MethodHandle.class, "invoke", type)`
     *
     *
     * *Discussion:*
     * A [general method type][MethodType.genericMethodType] is one which
     * mentions only `Object` arguments and return values.
     * An invoker for such a type is capable of calling any method handle
     * of the same arity as the general type.
     *
     *
     * *(Note:  The invoker method is not available via the Core Reflection API.
     * An attempt to call [java.lang.reflect.Method.invoke]
     * on the declared `invokeExact` or `invoke` method will raise an
     * [UnsupportedOperationException][java.lang.UnsupportedOperationException].)*
     *
     *
     * This method throws no reflective or security exceptions.
     * @param type the desired target type
     * @return a method handle suitable for invoking any method handle convertible to the given type
     * @throws IllegalArgumentException if the resulting method handle's type would have
     * [too many parameters](MethodHandle.html#maxarity)
     */
    fun invoker(type: java.lang.invoke.MethodType?): java.lang.invoke.MethodHandle {
        return Invoker(type, false /* isExactInvoker */)
    }

    // BEGIN Android-added: resolver for VarHandle accessor methods.
    private fun methodHandleForVarHandleAccessor(
        accessMode: java.lang.invoke.VarHandle.AccessMode,
        type: java.lang.invoke.MethodType,
        isExactInvoker: Boolean
    ): java.lang.invoke.MethodHandle {
        val refc: java.lang.Class<*> = java.lang.invoke.VarHandle::class.java
        val method: java.lang.reflect.Method
        try {
            method = refc.getDeclaredMethod(accessMode.methodName(), Array<Any>::class.java)
        } catch (e: java.lang.NoSuchMethodException) {
            throw java.lang.InternalError("No method for AccessMode $accessMode", e)
        }
        val methodType: java.lang.invoke.MethodType =
            type.insertParameterTypes(0, java.lang.invoke.VarHandle::class.java)
        val kind: Int =
            if (isExactInvoker) java.lang.invoke.MethodHandle.INVOKE_VAR_HANDLE_EXACT else java.lang.invoke.MethodHandle.INVOKE_VAR_HANDLE
        return java.lang.invoke.MethodHandleImpl(method.getArtMethod(), kind, methodType)
    }
    // END Android-added: resolver for VarHandle accessor methods.
    /**
     * Produces a special *invoker method handle* which can be used to
     * invoke a signature-polymorphic access mode method on any VarHandle whose
     * associated access mode type is compatible with the given type.
     * The resulting invoker will have a type which is exactly equal to the
     * desired given type, except that it will accept an additional leading
     * argument of type `VarHandle`.
     *
     * @param accessMode the VarHandle access mode
     * @param type the desired target type
     * @return a method handle suitable for invoking an access mode method of
     * any VarHandle whose access mode type is of the given type.
     * @since 9
     */
    fun varHandleExactInvoker(
        accessMode: java.lang.invoke.VarHandle.AccessMode?,
        type: java.lang.invoke.MethodType?
    ): java.lang.invoke.MethodHandle {
        return java.lang.invoke.MethodHandles.methodHandleForVarHandleAccessor(
            accessMode,
            type,
            true /* isExactInvoker */
        )
    }

    /**
     * Produces a special *invoker method handle* which can be used to
     * invoke a signature-polymorphic access mode method on any VarHandle whose
     * associated access mode type is compatible with the given type.
     * The resulting invoker will have a type which is exactly equal to the
     * desired given type, except that it will accept an additional leading
     * argument of type `VarHandle`.
     *
     *
     * Before invoking its target, if the access mode type differs from the
     * desired given type, the invoker will apply reference casts as necessary
     * and box, unbox, or widen primitive values, as if by
     * [asType][MethodHandle.asType].  Similarly, the return value will be
     * converted as necessary.
     *
     *
     * This method is equivalent to the following code (though it may be more
     * efficient): `publicLookup().findVirtual(VarHandle.class, accessMode.name(), type)`
     *
     * @param accessMode the VarHandle access mode
     * @param type the desired target type
     * @return a method handle suitable for invoking an access mode method of
     * any VarHandle whose access mode type is convertible to the given
     * type.
     * @since 9
     */
    fun varHandleInvoker(
        accessMode: java.lang.invoke.VarHandle.AccessMode?,
        type: java.lang.invoke.MethodType?
    ): java.lang.invoke.MethodHandle {
        return java.lang.invoke.MethodHandles.methodHandleForVarHandleAccessor(
            accessMode,
            type,
            false /* isExactInvoker */
        )
    }
    // Android-changed: Basic invokers are not supported.
    //
    // static /*non-public*/
    // MethodHandle basicInvoker(MethodType type) {
    //     return type.invokers().basicInvoker();
    // }
    /// method handle modification (creation from other method handles)
    /**
     * Produces a method handle which adapts the type of the
     * given method handle to a new type by pairwise argument and return type conversion.
     * The original type and new type must have the same number of arguments.
     * The resulting method handle is guaranteed to report a type
     * which is equal to the desired new type.
     *
     *
     * If the original type and new type are equal, returns target.
     *
     *
     * The same conversions are allowed as for [MethodHandle.asType],
     * and some additional conversions are also applied if those conversions fail.
     * Given types *T0*, *T1*, one of the following conversions is applied
     * if possible, before or instead of any conversions done by `asType`:
     *
     *  * If *T0* and *T1* are references, and *T1* is an interface type,
     * then the value of type *T0* is passed as a *T1* without a cast.
     * (This treatment of interfaces follows the usage of the bytecode verifier.)
     *  * If *T0* is boolean and *T1* is another primitive,
     * the boolean is converted to a byte value, 1 for true, 0 for false.
     * (This treatment follows the usage of the bytecode verifier.)
     *  * If *T1* is boolean and *T0* is another primitive,
     * *T0* is converted to byte via Java casting conversion (JLS 5.5),
     * and the low order bit of the result is tested, as if by `(x & 1) != 0`.
     *  * If *T0* and *T1* are primitives other than boolean,
     * then a Java casting conversion (JLS 5.5) is applied.
     * (Specifically, *T0* will convert to *T1* by
     * widening and/or narrowing.)
     *  * If *T0* is a reference and *T1* a primitive, an unboxing
     * conversion will be applied at runtime, possibly followed
     * by a Java casting conversion (JLS 5.5) on the primitive value,
     * possibly followed by a conversion from byte to boolean by testing
     * the low-order bit.
     *  * If *T0* is a reference and *T1* a primitive,
     * and if the reference is null at runtime, a zero value is introduced.
     *
     * @param target the method handle to invoke after arguments are retyped
     * @param newType the expected type of the new method handle
     * @return a method handle which delegates to the target after performing
     * any necessary argument conversions, and arranges for any
     * necessary return value conversions
     * @throws NullPointerException if either argument is null
     * @throws WrongMethodTypeException if the conversion cannot be made
     * @see MethodHandle.asType
     */
    fun explicitCastArguments(
        target: java.lang.invoke.MethodHandle,
        newType: java.lang.invoke.MethodType
    ): java.lang.invoke.MethodHandle {
        java.lang.invoke.MethodHandles.explicitCastArgumentsChecks(target, newType)
        // use the asTypeCache when possible:
        val oldType: java.lang.invoke.MethodType = target.type()
        if (oldType === newType) return target
        if (oldType.explicitCastEquivalentToAsType(newType)) {
            return if (Transformers.Transformer::class.java.isAssignableFrom(target.javaClass)) {
                // The StackFrameReader and StackFrameWriter used to perform transforms on
                // EmulatedStackFrames (in Transformers.java) do not how to perform asType()
                // conversions, but we know here that an explicit cast transform is the same as
                // having called asType() on the method handle.
                ExplicitCastArguments(target.asFixedArity(), newType)
            } else {
                // Runtime will perform asType() conversion during invocation.
                target.asFixedArity().asType(newType)
            }
        }
        return ExplicitCastArguments(target, newType)
    }

    private fun explicitCastArgumentsChecks(
        target: java.lang.invoke.MethodHandle,
        newType: java.lang.invoke.MethodType
    ) {
        if (target.type().parameterCount() != newType.parameterCount()) {
            throw java.lang.invoke.WrongMethodTypeException(
                "cannot explicitly cast " + target +
                        " to " + newType
            )
        }
    }

    /**
     * Produces a method handle which adapts the calling sequence of the
     * given method handle to a new type, by reordering the arguments.
     * The resulting method handle is guaranteed to report a type
     * which is equal to the desired new type.
     *
     *
     * The given array controls the reordering.
     * Call `#I` the number of incoming parameters (the value
     * `newType.parameterCount()`, and call `#O` the number
     * of outgoing parameters (the value `target.type().parameterCount()`).
     * Then the length of the reordering array must be `#O`,
     * and each element must be a non-negative number less than `#I`.
     * For every `N` less than `#O`, the `N`-th
     * outgoing argument will be taken from the `I`-th incoming
     * argument, where `I` is `reorder[N]`.
     *
     *
     * No argument or return value conversions are applied.
     * The type of each incoming argument, as determined by `newType`,
     * must be identical to the type of the corresponding outgoing parameter
     * or parameters in the target method handle.
     * The return type of `newType` must be identical to the return
     * type of the original target.
     *
     *
     * The reordering array need not specify an actual permutation.
     * An incoming argument will be duplicated if its index appears
     * more than once in the array, and an incoming argument will be dropped
     * if its index does not appear in the array.
     * As in the case of [dropArguments][.dropArguments],
     * incoming arguments which are not mentioned in the reordering array
     * are may be any type, as determined only by `newType`.
     * <blockquote><pre>`import static java.lang.invoke.MethodHandles.*;
     * import static java.lang.invoke.MethodType.*;
     * ...
     * MethodType intfn1 = methodType(int.class, int.class);
     * MethodType intfn2 = methodType(int.class, int.class, int.class);
     * MethodHandle sub = ... (int x, int y) -> (x-y) ...;
     * assert(sub.type().equals(intfn2));
     * MethodHandle sub1 = permuteArguments(sub, intfn2, 0, 1);
     * MethodHandle rsub = permuteArguments(sub, intfn2, 1, 0);
     * assert((int)rsub.invokeExact(1, 100) == 99);
     * MethodHandle add = ... (int x, int y) -> (x+y) ...;
     * assert(add.type().equals(intfn2));
     * MethodHandle twice = permuteArguments(add, intfn1, 0, 0);
     * assert(twice.type().equals(intfn1));
     * assert((int)twice.invokeExact(21) == 42);
    `</pre></blockquote> *
     * @param target the method handle to invoke after arguments are reordered
     * @param newType the expected type of the new method handle
     * @param reorder an index array which controls the reordering
     * @return a method handle which delegates to the target after it
     * drops unused arguments and moves and/or duplicates the other arguments
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if the index array length is not equal to
     * the arity of the target, or if any index array element
     * not a valid index for a parameter of `newType`,
     * or if two corresponding parameter types in
     * `target.type()` and `newType` are not identical,
     */
    fun permuteArguments(
        target: java.lang.invoke.MethodHandle,
        newType: java.lang.invoke.MethodType?,
        vararg reorder: Int
    ): java.lang.invoke.MethodHandle {
        var reorder = reorder
        reorder = reorder.clone() // get a private copy
        val oldType: java.lang.invoke.MethodType = target.type()
        java.lang.invoke.MethodHandles.permuteArgumentChecks(reorder, newType, oldType)
        return PermuteArguments(newType, target, reorder)
    }

    // Android-changed: findFirstDupOrDrop is unused and removed.
    // private static int findFirstDupOrDrop(int[] reorder, int newArity);
    private fun permuteArgumentChecks(
        reorder: IntArray,
        newType: java.lang.invoke.MethodType,
        oldType: java.lang.invoke.MethodType
    ): Boolean {
        if (newType.returnType() != oldType.returnType()) throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
            "return types do not match",
            oldType, newType
        )
        if (reorder.size == oldType.parameterCount()) {
            val limit: Int = newType.parameterCount()
            var bad = false
            for (j in reorder.indices) {
                val i = reorder[j]
                if (i < 0 || i >= limit) {
                    bad = true
                    break
                }
                val src: java.lang.Class<*> = newType.parameterType(i)
                val dst: java.lang.Class<*> = oldType.parameterType(j)
                if (src != dst) throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
                    "parameter types do not match after reorder",
                    oldType, newType
                )
            }
            if (!bad) return true
        }
        throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException("bad reorder array: " + reorder.contentToString())
    }

    /**
     * Produces a method handle of the requested return type which returns the given
     * constant value every time it is invoked.
     *
     *
     * Before the method handle is returned, the passed-in value is converted to the requested type.
     * If the requested type is primitive, widening primitive conversions are attempted,
     * else reference conversions are attempted.
     *
     * The returned method handle is equivalent to `identity(type).bindTo(value)`.
     * @param type the return type of the desired method handle
     * @param value the value to return
     * @return a method handle of the given return type and no arguments, which always returns the given value
     * @throws NullPointerException if the `type` argument is null
     * @throws ClassCastException if the value cannot be converted to the required return type
     * @throws IllegalArgumentException if the given type is `void.class`
     */
    fun constant(type: java.lang.Class<*>, value: Any?): java.lang.invoke.MethodHandle {
        var value = value
        if (type.isPrimitive()) {
            if (type == Void.TYPE) throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
                "void type"
            )
            val w: sun.invoke.util.Wrapper = sun.invoke.util.Wrapper.forPrimitiveType(type)
            value = w.convert(value, type)
            return if ((w.zero() == value)) java.lang.invoke.MethodHandles.zero(
                w,
                type
            ) else java.lang.invoke.MethodHandles.insertArguments(
                java.lang.invoke.MethodHandles.identity(
                    type
                ), 0, value
            )
        } else {
            return if (value == null) java.lang.invoke.MethodHandles.zero(
                sun.invoke.util.Wrapper.OBJECT,
                type
            ) else java.lang.invoke.MethodHandles.identity(type).bindTo(value)
        }
    }

    /**
     * Produces a method handle which returns its sole argument when invoked.
     * @param type the type of the sole parameter and return value of the desired method handle
     * @return a unary method handle which accepts and returns the given type
     * @throws NullPointerException if the argument is null
     * @throws IllegalArgumentException if the given type is `void.class`
     */
    fun identity(type: java.lang.Class<*>): java.lang.invoke.MethodHandle? {
        // Android-added: explicit non-null check.
        java.util.Objects.requireNonNull(type)
        val btw: sun.invoke.util.Wrapper =
            (if (type.isPrimitive()) sun.invoke.util.Wrapper.forPrimitiveType(type) else sun.invoke.util.Wrapper.OBJECT)
        val pos: Int = btw.ordinal
        var ident: java.lang.invoke.MethodHandle =
            java.lang.invoke.MethodHandles.IDENTITY_MHS.get(pos)
        if (ident == null) {
            ident = java.lang.invoke.MethodHandles.setCachedMethodHandle(
                java.lang.invoke.MethodHandles.IDENTITY_MHS,
                pos,
                java.lang.invoke.MethodHandles.makeIdentity(btw.primitiveType())
            )
        }
        if (ident.type().returnType() == type) return ident
        assert((btw == sun.invoke.util.Wrapper.OBJECT))
        return java.lang.invoke.MethodHandles.makeIdentity(type)
    }

    /**
     * Produces a constant method handle of the requested return type which
     * returns the default value for that type every time it is invoked.
     * The resulting constant method handle will have no side effects.
     *
     * The returned method handle is equivalent to `empty(methodType(type))`.
     * It is also equivalent to `explicitCastArguments(constant(Object.class, null), methodType(type))`,
     * since `explicitCastArguments` converts `null` to default values.
     * @param type the expected return type of the desired method handle
     * @return a constant method handle that takes no arguments
     * and returns the default value of the given type (or void, if the type is void)
     * @throws NullPointerException if the argument is null
     * @see MethodHandles.constant
     *
     * @see MethodHandles.empty
     *
     * @see MethodHandles.explicitCastArguments
     *
     * @since 9
     */
    fun zero(type: java.lang.Class<*>): java.lang.invoke.MethodHandle {
        java.util.Objects.requireNonNull(type)
        return if (type.isPrimitive()) java.lang.invoke.MethodHandles.zero(
            sun.invoke.util.Wrapper.forPrimitiveType(
                type
            ), type
        ) else java.lang.invoke.MethodHandles.zero(sun.invoke.util.Wrapper.OBJECT, type)
    }

    private fun identityOrVoid(type: java.lang.Class<*>): java.lang.invoke.MethodHandle {
        return if (type == Void.TYPE) java.lang.invoke.MethodHandles.zero(type) else java.lang.invoke.MethodHandles.identity(
            type
        )
    }

    /**
     * Produces a method handle of the requested type which ignores any arguments, does nothing,
     * and returns a suitable default depending on the return type.
     * That is, it returns a zero primitive value, a `null`, or `void`.
     *
     * The returned method handle is equivalent to
     * `dropArguments(zero(type.returnType()), 0, type.parameterList())`.
     *
     * @apiNote Given a predicate and target, a useful "if-then" construct can be produced as
     * `guardWithTest(pred, target, empty(target.type())`.
     * @param type the type of the desired method handle
     * @return a constant method handle of the given type, which returns a default value of the given return type
     * @throws NullPointerException if the argument is null
     * @see MethodHandles.zero
     *
     * @see MethodHandles.constant
     *
     * @since 9
     */
    fun empty(type: java.lang.invoke.MethodType): java.lang.invoke.MethodHandle {
        java.util.Objects.requireNonNull<java.lang.invoke.MethodType>(type)
        return java.lang.invoke.MethodHandles.dropArguments(
            java.lang.invoke.MethodHandles.zero(type.returnType()),
            0,
            type.parameterList()
        )
    }

    private val IDENTITY_MHS: Array<java.lang.invoke.MethodHandle?> =
        arrayOfNulls<java.lang.invoke.MethodHandle>(sun.invoke.util.Wrapper.COUNT)

    private fun makeIdentity(ptype: java.lang.Class<*>): java.lang.invoke.MethodHandle {
        // Android-changed: Android implementation using identity() functions and transformers.
        // MethodType mtype = methodType(ptype, ptype);
        // LambdaForm lform = LambdaForm.identityForm(BasicType.basicType(ptype));
        // return MethodHandleImpl.makeIntrinsic(mtype, lform, Intrinsic.IDENTITY);
        return if (ptype.isPrimitive()) {
            try {
                val mt: java.lang.invoke.MethodType =
                    java.lang.invoke.MethodType.methodType(ptype, ptype)
                java.lang.invoke.MethodHandles.Lookup.Companion.PUBLIC_LOOKUP.findStatic(
                    MethodHandles::class.java, "identity", mt
                )
            } catch (e: java.lang.NoSuchMethodException) {
                throw java.lang.AssertionError(e)
            } catch (e: java.lang.IllegalAccessException) {
                throw java.lang.AssertionError(e)
            }
        } else {
            ReferenceIdentity(ptype)
        }
    }
    // Android-added: helper methods for identity().
    /** @hide
     */
    fun identity(`val`: Byte): Byte {
        return `val`
    }

    /** @hide
     */
    fun identity(`val`: Boolean): Boolean {
        return `val`
    }

    /** @hide
     */
    fun identity(`val`: Char): Char {
        return `val`
    }

    /** @hide
     */
    fun identity(`val`: Short): Short {
        return `val`
    }

    /** @hide
     */
    fun identity(`val`: Int): Int {
        return `val`
    }

    /** @hide
     */
    fun identity(`val`: Long): Long {
        return `val`
    }

    /** @hide
     */
    fun identity(`val`: Float): Float {
        return `val`
    }

    /** @hide
     */
    fun identity(`val`: Double): Double {
        return `val`
    }

    private fun zero(
        btw: sun.invoke.util.Wrapper,
        rtype: java.lang.Class<*>
    ): java.lang.invoke.MethodHandle? {
        val pos: Int = btw.ordinal
        var zero: java.lang.invoke.MethodHandle = java.lang.invoke.MethodHandles.ZERO_MHS.get(pos)
        if (zero == null) {
            zero = java.lang.invoke.MethodHandles.setCachedMethodHandle(
                java.lang.invoke.MethodHandles.ZERO_MHS,
                pos,
                java.lang.invoke.MethodHandles.makeZero(btw.primitiveType())
            )
        }
        if (zero.type().returnType() == rtype) return zero
        assert((btw == sun.invoke.util.Wrapper.OBJECT))
        return java.lang.invoke.MethodHandles.makeZero(rtype)
    }

    private val ZERO_MHS: Array<java.lang.invoke.MethodHandle?> =
        arrayOfNulls<java.lang.invoke.MethodHandle>(sun.invoke.util.Wrapper.COUNT)

    private fun makeZero(rtype: java.lang.Class<*>): java.lang.invoke.MethodHandle {
        // Android-changed: use Android specific implementation.
        // MethodType mtype = methodType(rtype);
        // LambdaForm lform = LambdaForm.zeroForm(BasicType.basicType(rtype));
        // return MethodHandleImpl.makeIntrinsic(mtype, lform, Intrinsic.ZERO);
        return ZeroValue(rtype)
    }

    @Synchronized
    private fun setCachedMethodHandle(
        cache: Array<java.lang.invoke.MethodHandle>,
        pos: Int,
        value: java.lang.invoke.MethodHandle
    ): java.lang.invoke.MethodHandle {
        // Simulate a CAS, to avoid racy duplication of results.
        val prev: java.lang.invoke.MethodHandle = cache[pos]
        return if (prev != null) prev else value.also { cache.get(pos) = it }
    }

    /**
     * Provides a target method handle with one or more *bound arguments*
     * in advance of the method handle's invocation.
     * The formal parameters to the target corresponding to the bound
     * arguments are called *bound parameters*.
     * Returns a new method handle which saves away the bound arguments.
     * When it is invoked, it receives arguments for any non-bound parameters,
     * binds the saved arguments to their corresponding parameters,
     * and calls the original target.
     *
     *
     * The type of the new method handle will drop the types for the bound
     * parameters from the original target type, since the new method handle
     * will no longer require those arguments to be supplied by its callers.
     *
     *
     * Each given argument object must match the corresponding bound parameter type.
     * If a bound parameter type is a primitive, the argument object
     * must be a wrapper, and will be unboxed to produce the primitive value.
     *
     *
     * The `pos` argument selects which parameters are to be bound.
     * It may range between zero and *N-L* (inclusively),
     * where *N* is the arity of the target method handle
     * and *L* is the length of the values array.
     * @param target the method handle to invoke after the argument is inserted
     * @param pos where to insert the argument (zero for the first)
     * @param values the series of arguments to insert
     * @return a method handle which inserts an additional argument,
     * before calling the original method handle
     * @throws NullPointerException if the target or the `values` array is null
     * @see MethodHandle.bindTo
     */
    fun insertArguments(
        target: java.lang.invoke.MethodHandle,
        pos: Int,
        vararg values: Any?
    ): java.lang.invoke.MethodHandle {
        val insCount = values.size
        val ptypes: Array<java.lang.Class<*>> =
            java.lang.invoke.MethodHandles.insertArgumentsChecks(target, insCount, pos)
        if (insCount == 0) {
            return target
        }

        // Throw ClassCastExceptions early if we can't cast any of the provided values
        // to the required type.
        for (i in 0 until insCount) {
            val ptype: java.lang.Class<*> = ptypes[pos + i]
            if (!ptype.isPrimitive()) {
                ptypes[pos + i].cast(values[i])
            } else {
                // Will throw a ClassCastException if something terrible happens.
                values[i] =
                    sun.invoke.util.Wrapper.forPrimitiveType(ptype).convert(values[i], ptype)
            }
        }
        return InsertArguments(target, pos, values)
    }

    // Android-changed: insertArgumentPrimitive is unused.
    //
    // private static BoundMethodHandle insertArgumentPrimitive(BoundMethodHandle result, int pos,
    //                                                          Class<?> ptype, Object value) {
    //     Wrapper w = Wrapper.forPrimitiveType(ptype);
    //     // perform unboxing and/or primitive conversion
    //     value = w.convert(value, ptype);
    //     switch (w) {
    //     case INT:     return result.bindArgumentI(pos, (int)value);
    //     case LONG:    return result.bindArgumentJ(pos, (long)value);
    //     case FLOAT:   return result.bindArgumentF(pos, (float)value);
    //     case DOUBLE:  return result.bindArgumentD(pos, (double)value);
    //     default:      return result.bindArgumentI(pos, ValueConversions.widenSubword(value));
    //     }
    // }
    @Throws(java.lang.RuntimeException::class)
    private fun insertArgumentsChecks(
        target: java.lang.invoke.MethodHandle,
        insCount: Int,
        pos: Int
    ): Array<java.lang.Class<*>> {
        val oldType: java.lang.invoke.MethodType = target.type()
        val outargs: Int = oldType.parameterCount()
        val inargs = outargs - insCount
        if (inargs < 0) throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException("too many values to insert")
        if (pos < 0 || pos > inargs) throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
            "no argument type to append"
        )
        return oldType.ptypes()
    }
    // Android-changed: inclusive language preference for 'placeholder'.
    /**
     * Produces a method handle which will discard some placeholder arguments
     * before calling some other specified *target* method handle.
     * The type of the new method handle will be the same as the target's type,
     * except it will also include the placeholder argument types,
     * at some given position.
     *
     *
     * The `pos` argument may range between zero and *N*,
     * where *N* is the arity of the target.
     * If `pos` is zero, the placeholder arguments will precede
     * the target's real arguments; if `pos` is *N*
     * they will come after.
     *
     *
     * **Example:**
     * <blockquote><pre>`import static java.lang.invoke.MethodHandles.*;
     * import static java.lang.invoke.MethodType.*;
     * ...
     * MethodHandle cat = lookup().findVirtual(String.class,
     * "concat", methodType(String.class, String.class));
     * assertEquals("xy", (String) cat.invokeExact("x", "y"));
     * MethodType bigType = cat.type().insertParameterTypes(0, int.class, String.class);
     * MethodHandle d0 = dropArguments(cat, 0, bigType.parameterList().subList(0,2));
     * assertEquals(bigType, d0.type());
     * assertEquals("yz", (String) d0.invokeExact(123, "x", "y", "z"));
    `</pre></blockquote> *
     *
     *
     * This method is also equivalent to the following code:
     * <blockquote><pre>
     * [dropArguments][.dropArguments]`(target, pos, valueTypes.toArray(new Class[0]))`
    </pre></blockquote> *
     * @param target the method handle to invoke after the arguments are dropped
     * @param valueTypes the type(s) of the argument(s) to drop
     * @param pos position of first argument to drop (zero for the leftmost)
     * @return a method handle which drops arguments of the given types,
     * before calling the original method handle
     * @throws NullPointerException if the target is null,
     * or if the `valueTypes` list or any of its elements is null
     * @throws IllegalArgumentException if any element of `valueTypes` is `void.class`,
     * or if `pos` is negative or greater than the arity of the target,
     * or if the new method handle's type would have too many parameters
     */
    fun dropArguments(
        target: java.lang.invoke.MethodHandle?,
        pos: Int,
        valueTypes: List<java.lang.Class<*>?>
    ): java.lang.invoke.MethodHandle {
        return java.lang.invoke.MethodHandles.dropArguments0(
            target,
            pos,
            java.lang.invoke.MethodHandles.copyTypes(valueTypes.toTypedArray())
        )
    }

    private fun copyTypes(array: Array<Any>): List<java.lang.Class<*>> {
        return java.util.Arrays.asList<java.lang.Class<*>>(
            *java.util.Arrays.copyOf<java.lang.Class, Any>(
                array, array.size,
                Array<java.lang.Class>::class.java
            )
        )
    }

    private fun dropArguments0(
        target: java.lang.invoke.MethodHandle,
        pos: Int,
        valueTypes: List<java.lang.Class<*>>
    ): java.lang.invoke.MethodHandle {
        val oldType: java.lang.invoke.MethodType = target.type() // get NPE
        val dropped: Int =
            java.lang.invoke.MethodHandles.dropArgumentChecks(oldType, pos, valueTypes)
        val newType: java.lang.invoke.MethodType = oldType.insertParameterTypes(pos, valueTypes)
        return if (dropped == 0) target else DropArguments(newType, target, pos, dropped)
        // Android-changed: transformer implementation.
        // BoundMethodHandle result = target.rebind();
        // LambdaForm lform = result.form;
        // int insertFormArg = 1 + pos;
        // for (Class<?> ptype : valueTypes) {
        //     lform = lform.editor().addArgumentForm(insertFormArg++, BasicType.basicType(ptype));
        // }
        // result = result.copyWith(newType, lform);
        // return result;
    }

    private fun dropArgumentChecks(
        oldType: java.lang.invoke.MethodType,
        pos: Int,
        valueTypes: List<java.lang.Class<*>>
    ): Int {
        val dropped = valueTypes.size
        java.lang.invoke.MethodType.checkSlotCount(dropped)
        val outargs: Int = oldType.parameterCount()
        val inargs = outargs + dropped
        if (pos < 0 || pos > outargs) throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
            ("no argument type to remove"
                    + java.util.Arrays.asList<Any>(oldType, pos, valueTypes, inargs, outargs)
                    )
        )
        return dropped
    }
    // Android-changed: inclusive language preference for 'placeholder'.
    /**
     * Produces a method handle which will discard some placeholder arguments
     * before calling some other specified *target* method handle.
     * The type of the new method handle will be the same as the target's type,
     * except it will also include the placeholder argument types,
     * at some given position.
     *
     *
     * The `pos` argument may range between zero and *N*,
     * where *N* is the arity of the target.
     * If `pos` is zero, the placeholder arguments will precede
     * the target's real arguments; if `pos` is *N*
     * they will come after.
     * @apiNote
     * <blockquote><pre>`import static java.lang.invoke.MethodHandles.*;
     * import static java.lang.invoke.MethodType.*;
     * ...
     * MethodHandle cat = lookup().findVirtual(String.class,
     * "concat", methodType(String.class, String.class));
     * assertEquals("xy", (String) cat.invokeExact("x", "y"));
     * MethodHandle d0 = dropArguments(cat, 0, String.class);
     * assertEquals("yz", (String) d0.invokeExact("x", "y", "z"));
     * MethodHandle d1 = dropArguments(cat, 1, String.class);
     * assertEquals("xz", (String) d1.invokeExact("x", "y", "z"));
     * MethodHandle d2 = dropArguments(cat, 2, String.class);
     * assertEquals("xy", (String) d2.invokeExact("x", "y", "z"));
     * MethodHandle d12 = dropArguments(cat, 1, int.class, boolean.class);
     * assertEquals("xz", (String) d12.invokeExact("x", 12, true, "z"));
    `</pre></blockquote> *
     *
     *
     * This method is also equivalent to the following code:
     * <blockquote><pre>
     * [dropArguments][.dropArguments]`(target, pos, Arrays.asList(valueTypes))`
    </pre></blockquote> *
     * @param target the method handle to invoke after the arguments are dropped
     * @param valueTypes the type(s) of the argument(s) to drop
     * @param pos position of first argument to drop (zero for the leftmost)
     * @return a method handle which drops arguments of the given types,
     * before calling the original method handle
     * @throws NullPointerException if the target is null,
     * or if the `valueTypes` array or any of its elements is null
     * @throws IllegalArgumentException if any element of `valueTypes` is `void.class`,
     * or if `pos` is negative or greater than the arity of the target,
     * or if the new method handle's type would have
     * [too many parameters](MethodHandle.html#maxarity)
     */
    fun dropArguments(
        target: java.lang.invoke.MethodHandle?,
        pos: Int,
        vararg valueTypes: java.lang.Class<*>?
    ): java.lang.invoke.MethodHandle {
        return java.lang.invoke.MethodHandles.dropArguments0(
            target,
            pos,
            java.lang.invoke.MethodHandles.copyTypes(valueTypes)
        )
    }

    // private version which allows caller some freedom with error handling
    private fun dropArgumentsToMatch(
        target: java.lang.invoke.MethodHandle,
        skip: Int,
        newTypes: List<java.lang.Class<*>?>,
        pos: Int,
        nullOnFailure: Boolean
    ): java.lang.invoke.MethodHandle? {
        var newTypes: List<java.lang.Class<*>?> = newTypes
        newTypes = java.lang.invoke.MethodHandles.copyTypes(newTypes.toTypedArray())
        var oldTypes: List<java.lang.Class<*>?> = target.type().parameterList()
        var match = oldTypes.size
        if (skip != 0) {
            if (skip < 0 || skip > match) {
                throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
                    "illegal skip",
                    skip,
                    target
                )
            }
            oldTypes = oldTypes.subList(skip, match)
            match -= skip
        }
        var addTypes: List<java.lang.Class<*>?> = newTypes
        var add = addTypes.size
        if (pos != 0) {
            if (pos < 0 || pos > add) {
                throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
                    "illegal pos",
                    pos,
                    newTypes
                )
            }
            addTypes = addTypes.subList(pos, add)
            add -= pos
            assert((addTypes.size == add))
        }
        // Do not add types which already match the existing arguments.
        if (match > add || oldTypes != addTypes.subList(0, match)) {
            if (nullOnFailure) {
                return null
            }
            throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
                "argument lists do not match",
                oldTypes,
                newTypes
            )
        }
        addTypes = addTypes.subList(match, add)
        add -= match
        assert((addTypes.size == add))
        // newTypes:     (   P*[pos], M*[match], A*[add] )
        // target: ( S*[skip],        M*[match]  )
        var adapter: java.lang.invoke.MethodHandle = target
        if (add > 0) {
            adapter = java.lang.invoke.MethodHandles.dropArguments0(adapter, skip + match, addTypes)
        }
        // adapter: (S*[skip],        M*[match], A*[add] )
        if (pos > 0) {
            adapter = java.lang.invoke.MethodHandles.dropArguments0(
                adapter,
                skip,
                newTypes.subList(0, pos)
            )
        }
        // adapter: (S*[skip], P*[pos], M*[match], A*[add] )
        return adapter
    }
    // Android-changed: inclusive language preference for 'placeholder'.
    /**
     * Adapts a target method handle to match the given parameter type list. If necessary, adds placeholder arguments. Some
     * leading parameters can be skipped before matching begins. The remaining types in the `target`'s parameter
     * type list must be a sub-list of the `newTypes` type list at the starting position `pos`. The
     * resulting handle will have the target handle's parameter type list, with any non-matching parameter types (before
     * or after the matching sub-list) inserted in corresponding positions of the target's original parameters, as if by
     * [.dropArguments].
     *
     *
     * The resulting handle will have the same return type as the target handle.
     *
     *
     * In more formal terms, assume these two type lists:
     *  * The target handle has the parameter type list `S..., M...`, with as many types in `S` as
     * indicated by `skip`. The `M` types are those that are supposed to match part of the given type list,
     * `newTypes`.
     *  * The `newTypes` list contains types `P..., M..., A...`, with as many types in `P` as
     * indicated by `pos`. The `M` types are precisely those that the `M` types in the target handle's
     * parameter type list are supposed to match. The types in `A` are additional types found after the matching
     * sub-list.
     *
     * Given these assumptions, the result of an invocation of `dropArgumentsToMatch` will have the parameter type
     * list `S..., P..., M..., A...`, with the `P` and `A` types inserted as if by
     * [.dropArguments].
     *
     * @apiNote
     * Two method handles whose argument lists are "effectively identical" (i.e., identical in a common prefix) may be
     * mutually converted to a common type by two calls to `dropArgumentsToMatch`, as follows:
     * <blockquote><pre>`import static java.lang.invoke.MethodHandles.*;
     * import static java.lang.invoke.MethodType.*;
     * ...
     * ...
     * MethodHandle h0 = constant(boolean.class, true);
     * MethodHandle h1 = lookup().findVirtual(String.class, "concat", methodType(String.class, String.class));
     * MethodType bigType = h1.type().insertParameterTypes(1, String.class, int.class);
     * MethodHandle h2 = dropArguments(h1, 0, bigType.parameterList());
     * if (h1.type().parameterCount() < h2.type().parameterCount())
     * h1 = dropArgumentsToMatch(h1, 0, h2.type().parameterList(), 0);  // lengthen h1
     * else
     * h2 = dropArgumentsToMatch(h2, 0, h1.type().parameterList(), 0);    // lengthen h2
     * MethodHandle h3 = guardWithTest(h0, h1, h2);
     * assertEquals("xy", h3.invoke("x", "y", 1, "a", "b", "c"));
    `</pre></blockquote> *
     * @param target the method handle to adapt
     * @param skip number of targets parameters to disregard (they will be unchanged)
     * @param newTypes the list of types to match `target`'s parameter type list to
     * @param pos place in `newTypes` where the non-skipped target parameters must occur
     * @return a possibly adapted method handle
     * @throws NullPointerException if either argument is null
     * @throws IllegalArgumentException if any element of `newTypes` is `void.class`,
     * or if `skip` is negative or greater than the arity of the target,
     * or if `pos` is negative or greater than the newTypes list size,
     * or if `newTypes` does not contain the `target`'s non-skipped parameter types at position
     * `pos`.
     * @since 9
     */
    fun dropArgumentsToMatch(
        target: java.lang.invoke.MethodHandle?,
        skip: Int,
        newTypes: List<java.lang.Class<*>?>?,
        pos: Int
    ): java.lang.invoke.MethodHandle {
        java.util.Objects.requireNonNull<java.lang.invoke.MethodHandle>(target)
        java.util.Objects.requireNonNull<List<java.lang.Class<*>>>(newTypes)
        return java.lang.invoke.MethodHandles.dropArgumentsToMatch(
            target,
            skip,
            newTypes,
            pos,
            false
        )
    }

    /**
     * Drop the return value of the target handle (if any).
     * The returned method handle will have a `void` return type.
     *
     * @param target the method handle to adapt
     * @return a possibly adapted method handle
     * @throws NullPointerException if `target` is null
     * @since 16
     */
    fun dropReturn(target: java.lang.invoke.MethodHandle): java.lang.invoke.MethodHandle {
        java.util.Objects.requireNonNull<java.lang.invoke.MethodHandle>(target)
        val oldType: java.lang.invoke.MethodType = target.type()
        val oldReturnType: java.lang.Class<*> = oldType.returnType()
        if (oldReturnType == Void.TYPE) return target
        val newType: java.lang.invoke.MethodType = oldType.changeReturnType(Void.TYPE)
        // Android-changed: no support for BoundMethodHandle or LambdaForm.
        // BoundMethodHandle result = target.rebind();
        // LambdaForm lform = result.editor().filterReturnForm(V_TYPE, true);
        // result = result.copyWith(newType, lform);
        // return result;
        return target.asType(newType)
    }

    /**
     * Adapts a target method handle by pre-processing
     * one or more of its arguments, each with its own unary filter function,
     * and then calling the target with each pre-processed argument
     * replaced by the result of its corresponding filter function.
     *
     *
     * The pre-processing is performed by one or more method handles,
     * specified in the elements of the `filters` array.
     * The first element of the filter array corresponds to the `pos`
     * argument of the target, and so on in sequence.
     * The filter functions are invoked in left to right order.
     *
     *
     * Null arguments in the array are treated as identity functions,
     * and the corresponding arguments left unchanged.
     * (If there are no non-null elements in the array, the original target is returned.)
     * Each filter is applied to the corresponding argument of the adapter.
     *
     *
     * If a filter `F` applies to the `N`th argument of
     * the target, then `F` must be a method handle which
     * takes exactly one argument.  The type of `F`'s sole argument
     * replaces the corresponding argument type of the target
     * in the resulting adapted method handle.
     * The return type of `F` must be identical to the corresponding
     * parameter type of the target.
     *
     *
     * It is an error if there are elements of `filters`
     * (null or not)
     * which do not correspond to argument positions in the target.
     *
     * **Example:**
     * <blockquote><pre>`import static java.lang.invoke.MethodHandles.*;
     * import static java.lang.invoke.MethodType.*;
     * ...
     * MethodHandle cat = lookup().findVirtual(String.class,
     * "concat", methodType(String.class, String.class));
     * MethodHandle upcase = lookup().findVirtual(String.class,
     * "toUpperCase", methodType(String.class));
     * assertEquals("xy", (String) cat.invokeExact("x", "y"));
     * MethodHandle f0 = filterArguments(cat, 0, upcase);
     * assertEquals("Xy", (String) f0.invokeExact("x", "y")); // Xy
     * MethodHandle f1 = filterArguments(cat, 1, upcase);
     * assertEquals("xY", (String) f1.invokeExact("x", "y")); // xY
     * MethodHandle f2 = filterArguments(cat, 0, upcase, upcase);
     * assertEquals("XY", (String) f2.invokeExact("x", "y")); // XY
    `</pre></blockquote> *
     *
     * Here is pseudocode for the resulting adapter. In the code, `T`
     * denotes the return type of both the `target` and resulting adapter.
     * `P`/`p` and `B`/`b` represent the types and values
     * of the parameters and arguments that precede and follow the filter position
     * `pos`, respectively. `A[i]`/`a[i]` stand for the types and
     * values of the filtered parameters and arguments; they also represent the
     * return types of the `filter[i]` handles. The latter accept arguments
     * `v[i]` of type `V[i]`, which also appear in the signature of
     * the resulting adapter.
     * <blockquote><pre>`T target(P... p, A[i]... a[i], B... b);
     * A[i] filter[i](V[i]);
     * T adapter(P... p, V[i]... v[i], B... b) {
     * return target(p..., filter[i](v[i])..., b...);
     * }
    `</pre></blockquote> *
     *
     *
     * *Note:* The resulting adapter is never a [ variable-arity method handle][MethodHandle.asVarargsCollector], even if the original target method handle was.
     *
     * @param target the method handle to invoke after arguments are filtered
     * @param pos the position of the first argument to filter
     * @param filters method handles to call initially on filtered arguments
     * @return method handle which incorporates the specified argument filtering logic
     * @throws NullPointerException if the target is null
     * or if the `filters` array is null
     * @throws IllegalArgumentException if a non-null element of `filters`
     * does not match a corresponding argument type of target as described above,
     * or if the `pos+filters.length` is greater than `target.type().parameterCount()`,
     * or if the resulting method handle's type would have
     * [too many parameters](MethodHandle.html#maxarity)
     */
    fun filterArguments(
        target: java.lang.invoke.MethodHandle,
        pos: Int,
        vararg filters: java.lang.invoke.MethodHandle?
    ): java.lang.invoke.MethodHandle {
        java.lang.invoke.MethodHandles.filterArgumentsCheckArity(target, pos, filters)
        val adapter: java.lang.invoke.MethodHandle = target
        // Android-changed: transformer implementation.
        // process filters in reverse order so that the invocation of
        // the resulting adapter will invoke the filters in left-to-right order
        // for (int i = filters.length - 1; i >= 0; --i) {
        //     MethodHandle filter = filters[i];
        //     if (filter == null)  continue;  // ignore null elements of filters
        //     adapter = filterArgument(adapter, pos + i, filter);
        // }
        // return adapter;
        for (i in filters.indices) {
            java.lang.invoke.MethodHandles.filterArgumentChecks(target, i + pos, filters[i])
        }
        return FilterArguments(target, pos, filters)
    }

    /*non-public*/
    fun filterArgument(
        target: java.lang.invoke.MethodHandle?,
        pos: Int,
        filter: java.lang.invoke.MethodHandle?
    ): java.lang.invoke.MethodHandle {
        java.lang.invoke.MethodHandles.filterArgumentChecks(target, pos, filter)
        // Android-changed: use Transformer implementation.
        // MethodType targetType = target.type();
        // MethodType filterType = filter.type();
        // BoundMethodHandle result = target.rebind();
        // Class<?> newParamType = filterType.parameterType(0);
        // LambdaForm lform = result.editor().filterArgumentForm(1 + pos, BasicType.basicType(newParamType));
        // MethodType newType = targetType.changeParameterType(pos, newParamType);
        // result = result.copyWithExtendL(newType, lform, filter);
        // return result;
        return FilterArguments(target, pos, filter)
    }

    private fun filterArgumentsCheckArity(
        target: java.lang.invoke.MethodHandle,
        pos: Int,
        filters: Array<java.lang.invoke.MethodHandle>
    ) {
        val targetType: java.lang.invoke.MethodType = target.type()
        val maxPos: Int = targetType.parameterCount()
        if (pos + filters.size > maxPos) throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
            "too many filters"
        )
    }

    @Throws(java.lang.RuntimeException::class)
    private fun filterArgumentChecks(
        target: java.lang.invoke.MethodHandle,
        pos: Int,
        filter: java.lang.invoke.MethodHandle
    ) {
        val targetType: java.lang.invoke.MethodType = target.type()
        val filterType: java.lang.invoke.MethodType = filter.type()
        if ((filterType.parameterCount() != 1
                    || filterType.returnType() != targetType.parameterType(pos))
        ) throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
            "target and filter types do not match",
            targetType,
            filterType
        )
    }

    /**
     * Adapts a target method handle by pre-processing
     * a sub-sequence of its arguments with a filter (another method handle).
     * The pre-processed arguments are replaced by the result (if any) of the
     * filter function.
     * The target is then called on the modified (usually shortened) argument list.
     *
     *
     * If the filter returns a value, the target must accept that value as
     * its argument in position `pos`, preceded and/or followed by
     * any arguments not passed to the filter.
     * If the filter returns void, the target must accept all arguments
     * not passed to the filter.
     * No arguments are reordered, and a result returned from the filter
     * replaces (in order) the whole subsequence of arguments originally
     * passed to the adapter.
     *
     *
     * The argument types (if any) of the filter
     * replace zero or one argument types of the target, at position `pos`,
     * in the resulting adapted method handle.
     * The return type of the filter (if any) must be identical to the
     * argument type of the target at position `pos`, and that target argument
     * is supplied by the return value of the filter.
     *
     *
     * In all cases, `pos` must be greater than or equal to zero, and
     * `pos` must also be less than or equal to the target's arity.
     *
     * **Example:**
     * <blockquote><pre>`import static java.lang.invoke.MethodHandles.*;
     * import static java.lang.invoke.MethodType.*;
     * ...
     * MethodHandle deepToString = publicLookup()
     * .findStatic(Arrays.class, "deepToString", methodType(String.class, Object[].class));
     *
     * MethodHandle ts1 = deepToString.asCollector(String[].class, 1);
     * assertEquals("[strange]", (String) ts1.invokeExact("strange"));
     *
     * MethodHandle ts2 = deepToString.asCollector(String[].class, 2);
     * assertEquals("[up, down]", (String) ts2.invokeExact("up", "down"));
     *
     * MethodHandle ts3 = deepToString.asCollector(String[].class, 3);
     * MethodHandle ts3_ts2 = collectArguments(ts3, 1, ts2);
     * assertEquals("[top, [up, down], strange]",
     * (String) ts3_ts2.invokeExact("top", "up", "down", "strange"));
     *
     * MethodHandle ts3_ts2_ts1 = collectArguments(ts3_ts2, 3, ts1);
     * assertEquals("[top, [up, down], [strange]]",
     * (String) ts3_ts2_ts1.invokeExact("top", "up", "down", "strange"));
     *
     * MethodHandle ts3_ts2_ts3 = collectArguments(ts3_ts2, 1, ts3);
     * assertEquals("[top, [[up, down, strange], charm], bottom]",
     * (String) ts3_ts2_ts3.invokeExact("top", "up", "down", "strange", "charm", "bottom"));
    `</pre></blockquote> *
     *
     *  Here is pseudocode for the resulting adapter:
     * <blockquote><pre>`T target(A...,V,C...);
     * V filter(B...);
     * T adapter(A... a,B... b,C... c) {
     * V v = filter(b...);
     * return target(a...,v,c...);
     * }
     * // and if the filter has no arguments:
     * T target2(A...,V,C...);
     * V filter2();
     * T adapter2(A... a,C... c) {
     * V v = filter2();
     * return target2(a...,v,c...);
     * }
     * // and if the filter has a void return:
     * T target3(A...,C...);
     * void filter3(B...);
     * void adapter3(A... a,B... b,C... c) {
     * filter3(b...);
     * return target3(a...,c...);
     * }
    `</pre></blockquote> *
     *
     *
     * A collection adapter `collectArguments(mh, 0, coll)` is equivalent to
     * one which first "folds" the affected arguments, and then drops them, in separate
     * steps as follows:
     * <blockquote><pre>`mh = MethodHandles.dropArguments(mh, 1, coll.type().parameterList()); //step 2
     * mh = MethodHandles.foldArguments(mh, coll); //step 1
    `</pre></blockquote> *
     * If the target method handle consumes no arguments besides than the result
     * (if any) of the filter `coll`, then `collectArguments(mh, 0, coll)`
     * is equivalent to `filterReturnValue(coll, mh)`.
     * If the filter method handle `coll` consumes one argument and produces
     * a non-void result, then `collectArguments(mh, N, coll)`
     * is equivalent to `filterArguments(mh, N, coll)`.
     * Other equivalences are possible but would require argument permutation.
     *
     * @param target the method handle to invoke after filtering the subsequence of arguments
     * @param pos the position of the first adapter argument to pass to the filter,
     * and/or the target argument which receives the result of the filter
     * @param filter method handle to call on the subsequence of arguments
     * @return method handle which incorporates the specified argument subsequence filtering logic
     * @throws NullPointerException if either argument is null
     * @throws IllegalArgumentException if the return type of `filter`
     * is non-void and is not the same as the `pos` argument of the target,
     * or if `pos` is not between 0 and the target's arity, inclusive,
     * or if the resulting method handle's type would have
     * [too many parameters](MethodHandle.html#maxarity)
     * @see MethodHandles.foldArguments
     *
     * @see MethodHandles.filterArguments
     *
     * @see MethodHandles.filterReturnValue
     */
    fun collectArguments(
        target: java.lang.invoke.MethodHandle?,
        pos: Int,
        filter: java.lang.invoke.MethodHandle?
    ): java.lang.invoke.MethodHandle {
        val newType: java.lang.invoke.MethodType =
            java.lang.invoke.MethodHandles.collectArgumentsChecks(target, pos, filter)
        return CollectArguments(target, filter, pos, newType)
    }

    @Throws(java.lang.RuntimeException::class)
    private fun collectArgumentsChecks(
        target: java.lang.invoke.MethodHandle,
        pos: Int,
        filter: java.lang.invoke.MethodHandle
    ): java.lang.invoke.MethodType {
        val targetType: java.lang.invoke.MethodType = target.type()
        val filterType: java.lang.invoke.MethodType = filter.type()
        val rtype: java.lang.Class<*> = filterType.returnType()
        val filterArgs: List<java.lang.Class<*>> = filterType.parameterList()
        if (rtype == Void.TYPE) {
            return targetType.insertParameterTypes(pos, filterArgs)
        }
        if (rtype != targetType.parameterType(pos)) {
            throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
                "target and filter types do not match",
                targetType,
                filterType
            )
        }
        return targetType.dropParameterTypes(pos, pos + 1).insertParameterTypes(pos, filterArgs)
    }

    /**
     * Adapts a target method handle by post-processing
     * its return value (if any) with a filter (another method handle).
     * The result of the filter is returned from the adapter.
     *
     *
     * If the target returns a value, the filter must accept that value as
     * its only argument.
     * If the target returns void, the filter must accept no arguments.
     *
     *
     * The return type of the filter
     * replaces the return type of the target
     * in the resulting adapted method handle.
     * The argument type of the filter (if any) must be identical to the
     * return type of the target.
     *
     * **Example:**
     * <blockquote><pre>`import static java.lang.invoke.MethodHandles.*;
     * import static java.lang.invoke.MethodType.*;
     * ...
     * MethodHandle cat = lookup().findVirtual(String.class,
     * "concat", methodType(String.class, String.class));
     * MethodHandle length = lookup().findVirtual(String.class,
     * "length", methodType(int.class));
     * System.out.println((String) cat.invokeExact("x", "y")); // xy
     * MethodHandle f0 = filterReturnValue(cat, length);
     * System.out.println((int) f0.invokeExact("x", "y")); // 2
    `</pre></blockquote> *
     *
     * Here is pseudocode for the resulting adapter. In the code,
     * `T`/`t` represent the result type and value of the
     * `target`; `V`, the result type of the `filter`; and
     * `A`/`a`, the types and values of the parameters and arguments
     * of the `target` as well as the resulting adapter.
     * <blockquote><pre>`T target(A...);
     * V filter(T);
     * V adapter(A... a) {
     * T t = target(a...);
     * return filter(t);
     * }
     * // and if the target has a void return:
     * void target2(A...);
     * V filter2();
     * V adapter2(A... a) {
     * target2(a...);
     * return filter2();
     * }
     * // and if the filter has a void return:
     * T target3(A...);
     * void filter3(V);
     * void adapter3(A... a) {
     * T t = target3(a...);
     * filter3(t);
     * }
    `</pre></blockquote> *
     *
     *
     * *Note:* The resulting adapter is never a [ variable-arity method handle][MethodHandle.asVarargsCollector], even if the original target method handle was.
     * @param target the method handle to invoke before filtering the return value
     * @param filter method handle to call on the return value
     * @return method handle which incorporates the specified return value filtering logic
     * @throws NullPointerException if either argument is null
     * @throws IllegalArgumentException if the argument list of `filter`
     * does not match the return type of target as described above
     */
    fun filterReturnValue(
        target: java.lang.invoke.MethodHandle,
        filter: java.lang.invoke.MethodHandle
    ): java.lang.invoke.MethodHandle {
        val targetType: java.lang.invoke.MethodType = target.type()
        val filterType: java.lang.invoke.MethodType = filter.type()
        java.lang.invoke.MethodHandles.filterReturnValueChecks(targetType, filterType)
        // Android-changed: use a transformer.
        // BoundMethodHandle result = target.rebind();
        // BasicType rtype = BasicType.basicType(filterType.returnType());
        // LambdaForm lform = result.editor().filterReturnForm(rtype, false);
        // MethodType newType = targetType.changeReturnType(filterType.returnType());
        // result = result.copyWithExtendL(newType, lform, filter);
        // return result;
        return FilterReturnValue(target, filter)
    }

    @Throws(java.lang.RuntimeException::class)
    private fun filterReturnValueChecks(
        targetType: java.lang.invoke.MethodType,
        filterType: java.lang.invoke.MethodType
    ) {
        val rtype: java.lang.Class<*> = targetType.returnType()
        val filterValues: Int = filterType.parameterCount()
        if (if (filterValues == 0) (rtype != Void.TYPE) else (rtype != filterType.parameterType(0) || filterValues != 1)) throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
            "target and filter types do not match",
            targetType,
            filterType
        )
    }

    /**
     * Adapts a target method handle by pre-processing
     * some of its arguments, and then calling the target with
     * the result of the pre-processing, inserted into the original
     * sequence of arguments.
     *
     *
     * The pre-processing is performed by `combiner`, a second method handle.
     * Of the arguments passed to the adapter, the first `N` arguments
     * are copied to the combiner, which is then called.
     * (Here, `N` is defined as the parameter count of the combiner.)
     * After this, control passes to the target, with any result
     * from the combiner inserted before the original `N` incoming
     * arguments.
     *
     *
     * If the combiner returns a value, the first parameter type of the target
     * must be identical with the return type of the combiner, and the next
     * `N` parameter types of the target must exactly match the parameters
     * of the combiner.
     *
     *
     * If the combiner has a void return, no result will be inserted,
     * and the first `N` parameter types of the target
     * must exactly match the parameters of the combiner.
     *
     *
     * The resulting adapter is the same type as the target, except that the
     * first parameter type is dropped,
     * if it corresponds to the result of the combiner.
     *
     *
     * (Note that [dropArguments][.dropArguments] can be used to remove any arguments
     * that either the combiner or the target does not wish to receive.
     * If some of the incoming arguments are destined only for the combiner,
     * consider using [asCollector][MethodHandle.asCollector] instead, since those
     * arguments will not need to be live on the stack on entry to the
     * target.)
     *
     * **Example:**
     * <blockquote><pre>`import static java.lang.invoke.MethodHandles.*;
     * import static java.lang.invoke.MethodType.*;
     * ...
     * MethodHandle trace = publicLookup().findVirtual(java.io.PrintStream.class,
     * "println", methodType(void.class, String.class))
     * .bindTo(System.out);
     * MethodHandle cat = lookup().findVirtual(String.class,
     * "concat", methodType(String.class, String.class));
     * assertEquals("boojum", (String) cat.invokeExact("boo", "jum"));
     * MethodHandle catTrace = foldArguments(cat, trace);
     * // also prints "boo":
     * assertEquals("boojum", (String) catTrace.invokeExact("boo", "jum"));
    `</pre></blockquote> *
     *
     * Here is pseudocode for the resulting adapter. In the code, `T`
     * represents the result type of the `target` and resulting adapter.
     * `V`/`v` represent the type and value of the parameter and argument
     * of `target` that precedes the folding position; `V` also is
     * the result type of the `combiner`. `A`/`a` denote the
     * types and values of the `N` parameters and arguments at the folding
     * position. `B`/`b` represent the types and values of the
     * `target` parameters and arguments that follow the folded parameters
     * and arguments.
     * <blockquote><pre>`// there are N arguments in A...
     * T target(V, A[N]..., B...);
     * V combiner(A...);
     * T adapter(A... a, B... b) {
     * V v = combiner(a...);
     * return target(v, a..., b...);
     * }
     * // and if the combiner has a void return:
     * T target2(A[N]..., B...);
     * void combiner2(A...);
     * T adapter2(A... a, B... b) {
     * combiner2(a...);
     * return target2(a..., b...);
     * }
    `</pre></blockquote> *
     *
     *
     * *Note:* The resulting adapter is never a [ variable-arity method handle][MethodHandle.asVarargsCollector], even if the original target method handle was.
     * @param target the method handle to invoke after arguments are combined
     * @param combiner method handle to call initially on the incoming arguments
     * @return method handle which incorporates the specified argument folding logic
     * @throws NullPointerException if either argument is null
     * @throws IllegalArgumentException if `combiner`'s return type
     * is non-void and not the same as the first argument type of
     * the target, or if the initial `N` argument types
     * of the target
     * (skipping one matching the `combiner`'s return type)
     * are not identical with the argument types of `combiner`
     */
    fun foldArguments(
        target: java.lang.invoke.MethodHandle?,
        combiner: java.lang.invoke.MethodHandle?
    ): java.lang.invoke.MethodHandle {
        return java.lang.invoke.MethodHandles.foldArguments(target, 0, combiner)
    }

    /**
     * Adapts a target method handle by pre-processing some of its arguments, starting at a given position, and then
     * calling the target with the result of the pre-processing, inserted into the original sequence of arguments just
     * before the folded arguments.
     *
     *
     * This method is closely related to [.foldArguments], but allows to control the
     * position in the parameter list at which folding takes place. The argument controlling this, `pos`, is a
     * zero-based index. The aforementioned method [.foldArguments] assumes position
     * 0.
     *
     * @apiNote Example:
     * <blockquote><pre>`import static java.lang.invoke.MethodHandles.*;
     * import static java.lang.invoke.MethodType.*;
     * ...
     * MethodHandle trace = publicLookup().findVirtual(java.io.PrintStream.class,
     * "println", methodType(void.class, String.class))
     * .bindTo(System.out);
     * MethodHandle cat = lookup().findVirtual(String.class,
     * "concat", methodType(String.class, String.class));
     * assertEquals("boojum", (String) cat.invokeExact("boo", "jum"));
     * MethodHandle catTrace = foldArguments(cat, 1, trace);
     * // also prints "jum":
     * assertEquals("boojum", (String) catTrace.invokeExact("boo", "jum"));
    `</pre></blockquote> *
     *
     * Here is pseudocode for the resulting adapter. In the code, `T`
     * represents the result type of the `target` and resulting adapter.
     * `V`/`v` represent the type and value of the parameter and argument
     * of `target` that precedes the folding position; `V` also is
     * the result type of the `combiner`. `A`/`a` denote the
     * types and values of the `N` parameters and arguments at the folding
     * position. `Z`/`z` and `B`/`b` represent the types
     * and values of the `target` parameters and arguments that precede and
     * follow the folded parameters and arguments starting at `pos`,
     * respectively.
     * <blockquote><pre>`// there are N arguments in A...
     * T target(Z..., V, A[N]..., B...);
     * V combiner(A...);
     * T adapter(Z... z, A... a, B... b) {
     * V v = combiner(a...);
     * return target(z..., v, a..., b...);
     * }
     * // and if the combiner has a void return:
     * T target2(Z..., A[N]..., B...);
     * void combiner2(A...);
     * T adapter2(Z... z, A... a, B... b) {
     * combiner2(a...);
     * return target2(z..., a..., b...);
     * }
    `</pre></blockquote> *
     *
     *
     * *Note:* The resulting adapter is never a [ variable-arity method handle][MethodHandle.asVarargsCollector], even if the original target method handle was.
     *
     * @param target the method handle to invoke after arguments are combined
     * @param pos the position at which to start folding and at which to insert the folding result; if this is `0`, the effect is the same as for [.foldArguments].
     * @param combiner method handle to call initially on the incoming arguments
     * @return method handle which incorporates the specified argument folding logic
     * @throws NullPointerException if either argument is null
     * @throws IllegalArgumentException if either of the following two conditions holds:
     * (1) `combiner`'s return type is non-`void` and not the same as the argument type at position
     * `pos` of the target signature;
     * (2) the `N` argument types at position `pos` of the target signature (skipping one matching
     * the `combiner`'s return type) are not identical with the argument types of `combiner`.
     *
     * @see .foldArguments
     * @since 9
     */
    fun foldArguments(
        target: java.lang.invoke.MethodHandle,
        pos: Int,
        combiner: java.lang.invoke.MethodHandle
    ): java.lang.invoke.MethodHandle {
        val targetType: java.lang.invoke.MethodType = target.type()
        val combinerType: java.lang.invoke.MethodType = combiner.type()
        val rtype: java.lang.Class<*> =
            java.lang.invoke.MethodHandles.foldArgumentChecks(pos, targetType, combinerType)
        // Android-changed: // Android-changed: transformer implementation.
        // BoundMethodHandle result = target.rebind();
        // boolean dropResult = rtype == void.class;
        // LambdaForm lform = result.editor().foldArgumentsForm(1 + pos, dropResult, combinerType.basicType());
        // MethodType newType = targetType;
        // if (!dropResult) {
        //     newType = newType.dropParameterTypes(pos, pos + 1);
        // }
        // result = result.copyWithExtendL(newType, lform, combiner);
        // return result;
        return FoldArguments(target, pos, combiner)
    }

    private fun foldArgumentChecks(
        foldPos: Int,
        targetType: java.lang.invoke.MethodType,
        combinerType: java.lang.invoke.MethodType
    ): java.lang.Class<*> {
        val foldArgs: Int = combinerType.parameterCount()
        val rtype: java.lang.Class<*> = combinerType.returnType()
        val foldVals = if (rtype == Void.TYPE) 0 else 1
        val afterInsertPos = foldPos + foldVals
        var ok: Boolean = (targetType.parameterCount() >= afterInsertPos + foldArgs)
        if (ok) {
            for (i in 0 until foldArgs) {
                if (combinerType.parameterType(i) != targetType.parameterType(i + afterInsertPos)) {
                    ok = false
                    break
                }
            }
        }
        if (ok && (foldVals != 0) && (combinerType.returnType() != targetType.parameterType(foldPos))) ok =
            false
        if (!ok) throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
            "target and combiner types",
            targetType,
            combinerType
        )
        return rtype
    }

    /**
     * Makes a method handle which adapts a target method handle,
     * by guarding it with a test, a boolean-valued method handle.
     * If the guard fails, a fallback handle is called instead.
     * All three method handles must have the same corresponding
     * argument and return types, except that the return type
     * of the test must be boolean, and the test is allowed
     * to have fewer arguments than the other two method handles.
     *
     *  Here is pseudocode for the resulting adapter:
     * <blockquote><pre>`boolean test(A...);
     * T target(A...,B...);
     * T fallback(A...,B...);
     * T adapter(A... a,B... b) {
     * if (test(a...))
     * return target(a..., b...);
     * else
     * return fallback(a..., b...);
     * }
    `</pre></blockquote> *
     * Note that the test arguments (`a...` in the pseudocode) cannot
     * be modified by execution of the test, and so are passed unchanged
     * from the caller to the target or fallback as appropriate.
     * @param test method handle used for test, must return boolean
     * @param target method handle to call if test passes
     * @param fallback method handle to call if test fails
     * @return method handle which incorporates the specified if/then/else logic
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if `test` does not return boolean,
     * or if all three method types do not match (with the return
     * type of `test` changed to match that of the target).
     */
    fun guardWithTest(
        test: java.lang.invoke.MethodHandle,
        target: java.lang.invoke.MethodHandle,
        fallback: java.lang.invoke.MethodHandle
    ): java.lang.invoke.MethodHandle {
        var test: java.lang.invoke.MethodHandle = test
        var gtype: java.lang.invoke.MethodType = test.type()
        val ttype: java.lang.invoke.MethodType = target.type()
        val ftype: java.lang.invoke.MethodType = fallback.type()
        if (ttype != ftype) throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
            "target and fallback types",
            ttype,
            ftype
        )
        if (gtype.returnType() != Boolean::class.javaPrimitiveType) throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
            "guard type is not a predicate $gtype"
        )
        val targs: List<java.lang.Class<*>> = ttype.parameterList()
        val gargs: List<java.lang.Class<*>> = gtype.parameterList()
        if (targs != gargs) {
            val gpc = gargs.size
            val tpc = targs.size
            if (gpc >= tpc || targs.subList(
                    0,
                    gpc
                ) != gargs
            ) throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
                "target and test types",
                ttype,
                gtype
            )
            test = java.lang.invoke.MethodHandles.dropArguments(test, gpc, targs.subList(gpc, tpc))
            gtype = test.type()
        }
        return GuardWithTest(test, target, fallback)
    }

    fun <T> misMatchedTypes(what: String, t1: T, t2: T): java.lang.RuntimeException {
        return java.lang.invoke.MethodHandleStatics.newIllegalArgumentException("$what must match: $t1 != $t2")
    }

    /**
     * Makes a method handle which adapts a target method handle,
     * by running it inside an exception handler.
     * If the target returns normally, the adapter returns that value.
     * If an exception matching the specified type is thrown, the fallback
     * handle is called instead on the exception, plus the original arguments.
     *
     *
     * The target and handler must have the same corresponding
     * argument and return types, except that handler may omit trailing arguments
     * (similarly to the predicate in [guardWithTest][.guardWithTest]).
     * Also, the handler must have an extra leading parameter of `exType` or a supertype.
     *
     *
     * Here is pseudocode for the resulting adapter. In the code, `T`
     * represents the return type of the `target` and `handler`,
     * and correspondingly that of the resulting adapter; `A`/`a`,
     * the types and values of arguments to the resulting handle consumed by
     * `handler`; and `B`/`b`, those of arguments to the
     * resulting handle discarded by `handler`.
     * <blockquote><pre>`T target(A..., B...);
     * T handler(ExType, A...);
     * T adapter(A... a, B... b) {
     * try {
     * return target(a..., b...);
     * } catch (ExType ex) {
     * return handler(ex, a...);
     * }
     * }
    `</pre></blockquote> *
     * Note that the saved arguments (`a...` in the pseudocode) cannot
     * be modified by execution of the target, and so are passed unchanged
     * from the caller to the handler, if the handler is invoked.
     *
     *
     * The target and handler must return the same type, even if the handler
     * always throws.  (This might happen, for instance, because the handler
     * is simulating a `finally` clause).
     * To create such a throwing handler, compose the handler creation logic
     * with [throwException][.throwException],
     * in order to create a method handle of the correct return type.
     * @param target method handle to call
     * @param exType the type of exception which the handler will catch
     * @param handler method handle to call if a matching exception is thrown
     * @return method handle which incorporates the specified try/catch logic
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if `handler` does not accept
     * the given exception type, or if the method handle types do
     * not match in their return types and their
     * corresponding parameters
     * @see MethodHandles.tryFinally
     */
    fun catchException(
        target: java.lang.invoke.MethodHandle,
        exType: java.lang.Class<out Throwable?>,
        handler: java.lang.invoke.MethodHandle?
    ): java.lang.invoke.MethodHandle {
        var handler: java.lang.invoke.MethodHandle? = handler
        val ttype: java.lang.invoke.MethodType = target.type()
        val htype: java.lang.invoke.MethodType = handler.type()
        if (!Throwable::class.java.isAssignableFrom(exType)) throw java.lang.ClassCastException(
            exType.getName()
        )
        if (htype.parameterCount() < 1 ||
            !htype.parameterType(0).isAssignableFrom(exType)
        ) throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
            "handler does not accept exception type $exType"
        )
        if (htype.returnType() != ttype.returnType()) throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
            "target and handler return types",
            ttype,
            htype
        )
        handler = java.lang.invoke.MethodHandles.dropArgumentsToMatch(
            handler,
            1,
            ttype.parameterList(),
            0,
            true
        )
        if (handler == null) {
            throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
                "target and handler types",
                ttype,
                htype
            )
        }
        // Android-changed: use Transformer implementation.
        // return MethodHandleImpl.makeGuardWithCatch(target, exType, handler);
        return CatchException(target, handler, exType)
    }

    /**
     * Produces a method handle which will throw exceptions of the given `exType`.
     * The method handle will accept a single argument of `exType`,
     * and immediately throw it as an exception.
     * The method type will nominally specify a return of `returnType`.
     * The return type may be anything convenient:  It doesn't matter to the
     * method handle's behavior, since it will never return normally.
     * @param returnType the return type of the desired method handle
     * @param exType the parameter type of the desired method handle
     * @return method handle which can throw the given exceptions
     * @throws NullPointerException if either argument is null
     */
    fun throwException(
        returnType: java.lang.Class<*>?,
        exType: java.lang.Class<out Throwable?>
    ): java.lang.invoke.MethodHandle {
        if (!Throwable::class.java.isAssignableFrom(exType)) throw java.lang.ClassCastException(
            exType.getName()
        )
        // Android-changed: use Transformer implementation.
        // return MethodHandleImpl.throwException(methodType(returnType, exType));
        return AlwaysThrow(returnType, exType)
    }

    /**
     * Constructs a method handle representing a loop with several loop variables that are updated and checked upon each
     * iteration. Upon termination of the loop due to one of the predicates, a corresponding finalizer is run and
     * delivers the loop's result, which is the return value of the resulting handle.
     *
     *
     * Intuitively, every loop is formed by one or more "clauses", each specifying a local *iteration variable* and/or a loop
     * exit. Each iteration of the loop executes each clause in order. A clause can optionally update its iteration
     * variable; it can also optionally perform a test and conditional loop exit. In order to express this logic in
     * terms of method handles, each clause will specify up to four independent actions:
     *  * *init:* Before the loop executes, the initialization of an iteration variable `v` of type `V`.
     *  * *step:* When a clause executes, an update step for the iteration variable `v`.
     *  * *pred:* When a clause executes, a predicate execution to test for loop exit.
     *  * *fini:* If a clause causes a loop exit, a finalizer execution to compute the loop's return value.
     *
     * The full sequence of all iteration variable types, in clause order, will be notated as `(V...)`.
     * The values themselves will be `(v...)`.  When we speak of "parameter lists", we will usually
     * be referring to types, but in some contexts (describing execution) the lists will be of actual values.
     *
     *
     * Some of these clause parts may be omitted according to certain rules, and useful default behavior is provided in
     * this case. See below for a detailed description.
     *
     *
     * *Parameters optional everywhere:*
     * Each clause function is allowed but not required to accept a parameter for each iteration variable `v`.
     * As an exception, the init functions cannot take any `v` parameters,
     * because those values are not yet computed when the init functions are executed.
     * Any clause function may neglect to take any trailing subsequence of parameters it is entitled to take.
     * In fact, any clause function may take no arguments at all.
     *
     *
     * *Loop parameters:*
     * A clause function may take all the iteration variable values it is entitled to, in which case
     * it may also take more trailing parameters. Such extra values are called *loop parameters*,
     * with their types and values notated as `(A...)` and `(a...)`.
     * These become the parameters of the resulting loop handle, to be supplied whenever the loop is executed.
     * (Since init functions do not accept iteration variables `v`, any parameter to an
     * init function is automatically a loop parameter `a`.)
     * As with iteration variables, clause functions are allowed but not required to accept loop parameters.
     * These loop parameters act as loop-invariant values visible across the whole loop.
     *
     *
     * *Parameters visible everywhere:*
     * Each non-init clause function is permitted to observe the entire loop state, because it can be passed the full
     * list `(v... a...)` of current iteration variable values and incoming loop parameters.
     * The init functions can observe initial pre-loop state, in the form `(a...)`.
     * Most clause functions will not need all of this information, but they will be formally connected to it
     * as if by [.dropArguments].
     * <a id="astar"></a>
     * More specifically, we shall use the notation `(V*)` to express an arbitrary prefix of a full
     * sequence `(V...)` (and likewise for `(v*)`, `(A*)`, `(a*)`).
     * In that notation, the general form of an init function parameter list
     * is `(A*)`, and the general form of a non-init function parameter list is `(V*)` or `(V... A*)`.
     *
     *
     * *Checking clause structure:*
     * Given a set of clauses, there is a number of checks and adjustments performed to connect all the parts of the
     * loop. They are spelled out in detail in the steps below. In these steps, every occurrence of the word "must"
     * corresponds to a place where [IllegalArgumentException] will be thrown if the required constraint is not
     * met by the inputs to the loop combinator.
     *
     *
     * *Effectively identical sequences:*
     * <a id="effid"></a>
     * A parameter list `A` is defined to be *effectively identical* to another parameter list `B`
     * if `A` and `B` are identical, or if `A` is shorter and is identical with a proper prefix of `B`.
     * When speaking of an unordered set of parameter lists, we say they the set is "effectively identical"
     * as a whole if the set contains a longest list, and all members of the set are effectively identical to
     * that longest list.
     * For example, any set of type sequences of the form `(V*)` is effectively identical,
     * and the same is true if more sequences of the form `(V... A*)` are added.
     *
     *
     * *Step 0: Determine clause structure.*
     *  1. The clause array (of type `MethodHandle[][]`) must be non-`null` and contain at least one element.
     *  1. The clause array may not contain `null`s or sub-arrays longer than four elements.
     *  1. Clauses shorter than four elements are treated as if they were padded by `null` elements to length
     * four. Padding takes place by appending elements to the array.
     *  1. Clauses with all `null`s are disregarded.
     *  1. Each clause is treated as a four-tuple of functions, called "init", "step", "pred", and "fini".
     *
     *
     *
     * *Step 1A: Determine iteration variable types `(V...)`.*
     *  1. The iteration variable type for each clause is determined using the clause's init and step return types.
     *  1. If both functions are omitted, there is no iteration variable for the corresponding clause (`void` is
     * used as the type to indicate that). If one of them is omitted, the other's return type defines the clause's
     * iteration variable type. If both are given, the common return type (they must be identical) defines the clause's
     * iteration variable type.
     *  1. Form the list of return types (in clause order), omitting all occurrences of `void`.
     *  1. This list of types is called the "iteration variable types" (`(V...)`).
     *
     *
     *
     * *Step 1B: Determine loop parameters `(A...)`.*
     *  * Examine and collect init function parameter lists (which are of the form `(A*)`).
     *  * Examine and collect the suffixes of the step, pred, and fini parameter lists, after removing the iteration variable types.
     * (They must have the form `(V... A*)`; collect the `(A*)` parts only.)
     *  * Do not collect suffixes from step, pred, and fini parameter lists that do not begin with all the iteration variable types.
     * (These types will be checked in step 2, along with all the clause function types.)
     *  * Omitted clause functions are ignored.  (Equivalently, they are deemed to have empty parameter lists.)
     *  * All of the collected parameter lists must be effectively identical.
     *  * The longest parameter list (which is necessarily unique) is called the "external parameter list" (`(A...)`).
     *  * If there is no such parameter list, the external parameter list is taken to be the empty sequence.
     *  * The combined list consisting of iteration variable types followed by the external parameter types is called
     * the "internal parameter list".
     *
     *
     *
     * *Step 1C: Determine loop return type.*
     *  1. Examine fini function return types, disregarding omitted fini functions.
     *  1. If there are no fini functions, the loop return type is `void`.
     *  1. Otherwise, the common return type `R` of the fini functions (their return types must be identical) defines the loop return
     * type.
     *
     *
     *
     * *Step 1D: Check other types.*
     *  1. There must be at least one non-omitted pred function.
     *  1. Every non-omitted pred function must have a `boolean` return type.
     *
     *
     *
     * *Step 2: Determine parameter lists.*
     *  1. The parameter list for the resulting loop handle will be the external parameter list `(A...)`.
     *  1. The parameter list for init functions will be adjusted to the external parameter list.
     * (Note that their parameter lists are already effectively identical to this list.)
     *  1. The parameter list for every non-omitted, non-init (step, pred, and fini) function must be
     * effectively identical to the internal parameter list `(V... A...)`.
     *
     *
     *
     * *Step 3: Fill in omitted functions.*
     *  1. If an init function is omitted, use a [default value][.empty] for the clause's iteration variable
     * type.
     *  1. If a step function is omitted, use an [identity function][.identity] of the clause's iteration
     * variable type; insert dropped argument parameters before the identity function parameter for the non-`void`
     * iteration variables of preceding clauses. (This will turn the loop variable into a local loop invariant.)
     *  1. If a pred function is omitted, use a constant `true` function. (This will keep the loop going, as far
     * as this clause is concerned.  Note that in such cases the corresponding fini function is unreachable.)
     *  1. If a fini function is omitted, use a [default value][.empty] for the
     * loop return type.
     *
     *
     *
     * *Step 4: Fill in missing parameter types.*
     *  1. At this point, every init function parameter list is effectively identical to the external parameter list `(A...)`,
     * but some lists may be shorter. For every init function with a short parameter list, pad out the end of the list.
     *  1. At this point, every non-init function parameter list is effectively identical to the internal parameter
     * list `(V... A...)`, but some lists may be shorter. For every non-init function with a short parameter list,
     * pad out the end of the list.
     *  1. Argument lists are padded out by [dropping unused trailing arguments][.dropArgumentsToMatch].
     *
     *
     *
     * *Final observations.*
     *  1. After these steps, all clauses have been adjusted by supplying omitted functions and arguments.
     *  1. All init functions have a common parameter type list `(A...)`, which the final loop handle will also have.
     *  1. All fini functions have a common return type `R`, which the final loop handle will also have.
     *  1. All non-init functions have a common parameter type list `(V... A...)`, of
     * (non-`void`) iteration variables `V` followed by loop parameters.
     *  1. Each pair of init and step functions agrees in their return type `V`.
     *  1. Each non-init function will be able to observe the current values `(v...)` of all iteration variables.
     *  1. Every function will be able to observe the incoming values `(a...)` of all loop parameters.
     *
     *
     *
     * *Example.* As a consequence of step 1A above, the `loop` combinator has the following property:
     *
     *  * Given `N` clauses `Cn = {null, Sn, Pn}` with `n = 1..N`.
     *  * Suppose predicate handles `Pn` are either `null` or have no parameters.
     * (Only one `Pn` has to be non-`null`.)
     *  * Suppose step handles `Sn` have signatures `(B1..BX)Rn`, for some constant `X>=N`.
     *  * Suppose `Q` is the count of non-void types `Rn`, and `(V1...VQ)` is the sequence of those types.
     *  * It must be that `Vn == Bn` for `n = 1..min(X,Q)`.
     *  * The parameter types `Vn` will be interpreted as loop-local state elements `(V...)`.
     *  * Any remaining types `BQ+1..BX` (if `Q<X`) will determine
     * the resulting loop handle's parameter types `(A...)`.
     *
     * In this example, the loop handle parameters `(A...)` were derived from the step functions,
     * which is natural if most of the loop computation happens in the steps.  For some loops,
     * the burden of computation might be heaviest in the pred functions, and so the pred functions
     * might need to accept the loop parameter values.  For loops with complex exit logic, the fini
     * functions might need to accept loop parameters, and likewise for loops with complex entry logic,
     * where the init functions will need the extra parameters.  For such reasons, the rules for
     * determining these parameters are as symmetric as possible, across all clause parts.
     * In general, the loop parameters function as common invariant values across the whole
     * loop, while the iteration variables function as common variant values, or (if there is
     * no step function) as internal loop invariant temporaries.
     *
     *
     * *Loop execution.*
     *  1. When the loop is called, the loop input values are saved in locals, to be passed to
     * every clause function. These locals are loop invariant.
     *  1. Each init function is executed in clause order (passing the external arguments `(a...)`)
     * and the non-`void` values are saved (as the iteration variables `(v...)`) into locals.
     * These locals will be loop varying (unless their steps behave as identity functions, as noted above).
     *  1. All function executions (except init functions) will be passed the internal parameter list, consisting of
     * the non-`void` iteration values `(v...)` (in clause order) and then the loop inputs `(a...)`
     * (in argument order).
     *  1. The step and pred functions are then executed, in clause order (step before pred), until a pred function
     * returns `false`.
     *  1. The non-`void` result from a step function call is used to update the corresponding value in the
     * sequence `(v...)` of loop variables.
     * The updated value is immediately visible to all subsequent function calls.
     *  1. If a pred function returns `false`, the corresponding fini function is called, and the resulting value
     * (of type `R`) is returned from the loop as a whole.
     *  1. If all the pred functions always return true, no fini function is ever invoked, and the loop cannot exit
     * except by throwing an exception.
     *
     *
     *
     * *Usage tips.*
     *
     *  * Although each step function will receive the current values of *all* the loop variables,
     * sometimes a step function only needs to observe the current value of its own variable.
     * In that case, the step function may need to explicitly [drop all preceding loop variables][.dropArguments].
     * This will require mentioning their types, in an expression like `dropArguments(step, 0, V0.class, ...)`.
     *  * Loop variables are not required to vary; they can be loop invariant.  A clause can create
     * a loop invariant by a suitable init function with no step, pred, or fini function.  This may be
     * useful to "wire" an incoming loop argument into the step or pred function of an adjacent loop variable.
     *  * If some of the clause functions are virtual methods on an instance, the instance
     * itself can be conveniently placed in an initial invariant loop "variable", using an initial clause
     * like `new MethodHandle[]{identity(ObjType.class)}`.  In that case, the instance reference
     * will be the first iteration variable value, and it will be easy to use virtual
     * methods as clause parts, since all of them will take a leading instance reference matching that value.
     *
     *
     *
     * Here is pseudocode for the resulting loop handle. As above, `V` and `v` represent the types
     * and values of loop variables; `A` and `a` represent arguments passed to the whole loop;
     * and `R` is the common result type of all finalizers as well as of the resulting loop.
     * <blockquote><pre>`V... init...(A...);
     * boolean pred...(V..., A...);
     * V... step...(V..., A...);
     * R fini...(V..., A...);
     * R loop(A... a) {
     * V... v... = init...(a...);
     * for (;;) {
     * for ((v, p, s, f) in (v..., pred..., step..., fini...)) {
     * v = s(v..., a...);
     * if (!p(v..., a...)) {
     * return f(v..., a...);
     * }
     * }
     * }
     * }
    `</pre></blockquote> *
     * Note that the parameter type lists `(V...)` and `(A...)` have been expanded
     * to their full length, even though individual clause functions may neglect to take them all.
     * As noted above, missing parameters are filled in as if by [.dropArgumentsToMatch].
     *
     * @apiNote Example:
     * <blockquote><pre>`// iterative implementation of the factorial function as a loop handle
     * static int one(int k) { return 1; }
     * static int inc(int i, int acc, int k) { return i + 1; }
     * static int mult(int i, int acc, int k) { return i * acc; }
     * static boolean pred(int i, int acc, int k) { return i < k; }
     * static int fin(int i, int acc, int k) { return acc; }
     * // assume MH_one, MH_inc, MH_mult, MH_pred, and MH_fin are handles to the above methods
     * // null initializer for counter, should initialize to 0
     * MethodHandle[] counterClause = new MethodHandle[]{null, MH_inc};
     * MethodHandle[] accumulatorClause = new MethodHandle[]{MH_one, MH_mult, MH_pred, MH_fin};
     * MethodHandle loop = MethodHandles.loop(counterClause, accumulatorClause);
     * assertEquals(120, loop.invoke(5));
    `</pre></blockquote> *
     * The same example, dropping arguments and using combinators:
     * <blockquote><pre>`// simplified implementation of the factorial function as a loop handle
     * static int inc(int i) { return i + 1; } // drop acc, k
     * static int mult(int i, int acc) { return i * acc; } //drop k
     * static boolean cmp(int i, int k) { return i < k; }
     * // assume MH_inc, MH_mult, and MH_cmp are handles to the above methods
     * // null initializer for counter, should initialize to 0
     * MethodHandle MH_one = MethodHandles.constant(int.class, 1);
     * MethodHandle MH_pred = MethodHandles.dropArguments(MH_cmp, 1, int.class); // drop acc
     * MethodHandle MH_fin = MethodHandles.dropArguments(MethodHandles.identity(int.class), 0, int.class); // drop i
     * MethodHandle[] counterClause = new MethodHandle[]{null, MH_inc};
     * MethodHandle[] accumulatorClause = new MethodHandle[]{MH_one, MH_mult, MH_pred, MH_fin};
     * MethodHandle loop = MethodHandles.loop(counterClause, accumulatorClause);
     * assertEquals(720, loop.invoke(6));
    `</pre></blockquote> *
     * A similar example, using a helper object to hold a loop parameter:
     * <blockquote><pre>`// instance-based implementation of the factorial function as a loop handle
     * static class FacLoop {
     * final int k;
     * FacLoop(int k) { this.k = k; }
     * int inc(int i) { return i + 1; }
     * int mult(int i, int acc) { return i * acc; }
     * boolean pred(int i) { return i < k; }
     * int fin(int i, int acc) { return acc; }
     * }
     * // assume MH_FacLoop is a handle to the constructor
     * // assume MH_inc, MH_mult, MH_pred, and MH_fin are handles to the above methods
     * // null initializer for counter, should initialize to 0
     * MethodHandle MH_one = MethodHandles.constant(int.class, 1);
     * MethodHandle[] instanceClause = new MethodHandle[]{MH_FacLoop};
     * MethodHandle[] counterClause = new MethodHandle[]{null, MH_inc};
     * MethodHandle[] accumulatorClause = new MethodHandle[]{MH_one, MH_mult, MH_pred, MH_fin};
     * MethodHandle loop = MethodHandles.loop(instanceClause, counterClause, accumulatorClause);
     * assertEquals(5040, loop.invoke(7));
    `</pre></blockquote> *
     *
     * @param clauses an array of arrays (4-tuples) of [MethodHandle]s adhering to the rules described above.
     *
     * @return a method handle embodying the looping behavior as defined by the arguments.
     *
     * @throws IllegalArgumentException in case any of the constraints described above is violated.
     *
     * @see MethodHandles.whileLoop
     * @see MethodHandles.doWhileLoop
     * @see MethodHandles.countedLoop
     * @see MethodHandles.iteratedLoop
     * @since 9
     */
    fun loop(vararg clauses: Array<java.lang.invoke.MethodHandle?>?): java.lang.invoke.MethodHandle {
        // Step 0: determine clause structure.
        java.lang.invoke.MethodHandles.loopChecks0(clauses)
        val init: MutableList<java.lang.invoke.MethodHandle?> =
            java.util.ArrayList<java.lang.invoke.MethodHandle>()
        val step: MutableList<java.lang.invoke.MethodHandle?> =
            java.util.ArrayList<java.lang.invoke.MethodHandle>()
        val pred: MutableList<java.lang.invoke.MethodHandle?> =
            java.util.ArrayList<java.lang.invoke.MethodHandle>()
        val fini: MutableList<java.lang.invoke.MethodHandle?> =
            java.util.ArrayList<java.lang.invoke.MethodHandle>()
        java.util.stream.Stream.of<Array<java.lang.invoke.MethodHandle>>(*clauses).filter(
            java.util.function.Predicate<Array<java.lang.invoke.MethodHandle>> { c: Array<java.lang.invoke.MethodHandle?> ->
                java.util.stream.Stream.of<java.lang.invoke.MethodHandle>(*c).anyMatch(
                    java.util.function.Predicate<java.lang.invoke.MethodHandle> { obj: Any? ->
                        java.util.Objects.nonNull(
                            obj
                        )
                    })
            }).forEach(
            java.util.function.Consumer<Array<java.lang.invoke.MethodHandle>> { clause: Array<java.lang.invoke.MethodHandle?> ->
                init.add(clause.get(0)) // all clauses have at least length 1
                step.add(if (clause.size <= 1) null else clause.get(1))
                pred.add(if (clause.size <= 2) null else clause.get(2))
                fini.add(if (clause.size <= 3) null else clause.get(3))
            })
        assert(java.util.stream.Stream.of<List<java.lang.invoke.MethodHandle>>(
            init,
            step,
            pred,
            fini
        ).map<Int>(
            java.util.function.Function<List<java.lang.invoke.MethodHandle>, Int> { obj: List<*> -> obj.size })
            .distinct().count() == 1L
        )
        val nclauses = init.size

        // Step 1A: determine iteration variables (V...).
        val iterationVariableTypes: MutableList<java.lang.Class<*>> =
            java.util.ArrayList<java.lang.Class<*>>()
        for (i in 0 until nclauses) {
            val `in`: java.lang.invoke.MethodHandle? = init[i]
            val st: java.lang.invoke.MethodHandle? = step[i]
            if (`in` == null && st == null) {
                iterationVariableTypes.add(Void.TYPE)
            } else if (`in` != null && st != null) {
                java.lang.invoke.MethodHandles.loopChecks1a(i, `in`, st)
                iterationVariableTypes.add(`in`.type().returnType())
            } else {
                iterationVariableTypes.add(
                    if (`in` == null) st.type().returnType() else `in`.type().returnType()
                )
            }
        }
        val commonPrefix: List<java.lang.Class<*>> = iterationVariableTypes.stream()
            .filter(java.util.function.Predicate<java.lang.Class<*>> { t: java.lang.Class<*> -> t != Void.TYPE })
            .collect<List<java.lang.Class<*>>, Any>(java.util.stream.Collectors.toList<java.lang.Class<*>>())

        // Step 1B: determine loop parameters (A...).
        val commonSuffix: List<java.lang.Class<*>> =
            java.lang.invoke.MethodHandles.buildCommonSuffix(
                init,
                step,
                pred,
                fini,
                commonPrefix.size
            )
        java.lang.invoke.MethodHandles.loopChecks1b(init, commonSuffix)

        // Step 1C: determine loop return type.
        // Step 1D: check other types.
        // local variable required here; see JDK-8223553
        val cstream: java.util.stream.Stream<java.lang.Class<*>> = fini.stream()
            .filter(java.util.function.Predicate<java.lang.invoke.MethodHandle> { obj: Any? ->
                java.util.Objects.nonNull(
                    obj
                )
            }).map<java.lang.invoke.MethodType>(
                java.util.function.Function<java.lang.invoke.MethodHandle, java.lang.invoke.MethodType> { obj: java.lang.invoke.MethodHandle -> obj.type() })
            .map<java.lang.Class<*>>(java.util.function.Function<java.lang.invoke.MethodType, java.lang.Class<*>> { obj: java.lang.invoke.MethodType -> obj.returnType() })
        val loopReturnType: java.lang.Class<*> = cstream.findFirst().orElse(Void.TYPE)
        java.lang.invoke.MethodHandles.loopChecks1cd(pred, fini, loopReturnType)

        // Step 2: determine parameter lists.
        val commonParameterSequence: MutableList<java.lang.Class<*>> =
            java.util.ArrayList<java.lang.Class<*>>(commonPrefix)
        commonParameterSequence.addAll(commonSuffix)
        java.lang.invoke.MethodHandles.loopChecks2(step, pred, fini, commonParameterSequence)

        // Step 3: fill in omitted functions.
        for (i in 0 until nclauses) {
            val t: java.lang.Class<*> = iterationVariableTypes[i]
            if (init[i] == null) {
                init[i] = java.lang.invoke.MethodHandles.empty(
                    java.lang.invoke.MethodType.methodType(
                        t,
                        commonSuffix
                    )
                )
            }
            if (step[i] == null) {
                step[i] = java.lang.invoke.MethodHandles.dropArgumentsToMatch(
                    java.lang.invoke.MethodHandles.identityOrVoid(t), 0, commonParameterSequence, i
                )
            }
            if (pred[i] == null) {
                pred[i] = java.lang.invoke.MethodHandles.dropArguments0(
                    java.lang.invoke.MethodHandles.constant(
                        Boolean::class.javaPrimitiveType, true
                    ), 0, commonParameterSequence
                )
            }
            if (fini[i] == null) {
                fini[i] = java.lang.invoke.MethodHandles.empty(
                    java.lang.invoke.MethodType.methodType(
                        t,
                        commonParameterSequence
                    )
                )
            }
        }

        // Step 4: fill in missing parameter types.
        // Also convert all handles to fixed-arity handles.
        val finit: List<java.lang.invoke.MethodHandle> = java.lang.invoke.MethodHandles.fixArities(
            java.lang.invoke.MethodHandles.fillParameterTypes(
                init,
                commonSuffix
            )
        )
        val fstep: List<java.lang.invoke.MethodHandle> = java.lang.invoke.MethodHandles.fixArities(
            java.lang.invoke.MethodHandles.fillParameterTypes(
                step,
                commonParameterSequence
            )
        )
        val fpred: List<java.lang.invoke.MethodHandle> = java.lang.invoke.MethodHandles.fixArities(
            java.lang.invoke.MethodHandles.fillParameterTypes(
                pred,
                commonParameterSequence
            )
        )
        val ffini: List<java.lang.invoke.MethodHandle> = java.lang.invoke.MethodHandles.fixArities(
            java.lang.invoke.MethodHandles.fillParameterTypes(
                fini,
                commonParameterSequence
            )
        )
        assert(
            finit.stream()
                .map<java.lang.invoke.MethodType>(java.util.function.Function<java.lang.invoke.MethodHandle, java.lang.invoke.MethodType> { obj: java.lang.invoke.MethodHandle -> obj.type() })
                .map<List<java.lang.Class<*>>>(
                    java.util.function.Function<java.lang.invoke.MethodType, List<java.lang.Class<*>>> { obj: java.lang.invoke.MethodType -> obj.parameterList() })
                .allMatch(
                    java.util.function.Predicate<List<java.lang.Class<*>>> { pl: List<java.lang.Class<*>?> -> (pl == commonSuffix) })
        )
        assert(
            java.util.stream.Stream.of<List<java.lang.invoke.MethodHandle>>(fstep, fpred, ffini)
                .flatMap<java.lang.invoke.MethodHandle>(
                    java.util.function.Function<List<java.lang.invoke.MethodHandle>, java.util.stream.Stream<out java.lang.invoke.MethodHandle?>> { obj: List<*> -> obj.stream() })
                .map<java.lang.invoke.MethodType>(
                    java.util.function.Function<java.lang.invoke.MethodHandle, java.lang.invoke.MethodType> { obj: java.lang.invoke.MethodHandle -> obj.type() })
                .map<List<java.lang.Class<*>>>(
                    java.util.function.Function<java.lang.invoke.MethodType, List<java.lang.Class<*>>> { obj: java.lang.invoke.MethodType -> obj.parameterList() })
                .allMatch(
                    java.util.function.Predicate<List<java.lang.Class<*>>> { pl: List<java.lang.Class<*>?> -> (pl == commonParameterSequence) })
        )

        // Android-changed: transformer implementation.
        // return MethodHandleImpl.makeLoop(loopReturnType, commonSuffix, finit, fstep, fpred, ffini);
        return Loop(loopReturnType,
            commonSuffix,
            finit.toArray(java.util.function.IntFunction<Array<T>> { _Dummy_.__Array__() }),
            fstep.toArray(java.util.function.IntFunction<Array<T>> { _Dummy_.__Array__() }),
            fpred.toArray(java.util.function.IntFunction<Array<T>> { _Dummy_.__Array__() }),
            ffini.toArray(java.util.function.IntFunction<Array<T>> { _Dummy_.__Array__() })
        )
    }

    private fun loopChecks0(clauses: Array<Array<java.lang.invoke.MethodHandle>>?) {
        if (clauses == null || clauses.size == 0) {
            throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException("null or no clauses passed")
        }
        if (java.util.stream.Stream.of<Array<java.lang.invoke.MethodHandle>>(*clauses).anyMatch(
                java.util.function.Predicate<Array<java.lang.invoke.MethodHandle>> { obj: Any? ->
                    java.util.Objects.isNull(
                        obj
                    )
                })
        ) {
            throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException("null clauses are not allowed")
        }
        if (java.util.stream.Stream.of<Array<java.lang.invoke.MethodHandle>>(*clauses).anyMatch(
                java.util.function.Predicate<Array<java.lang.invoke.MethodHandle>> { c: Array<java.lang.invoke.MethodHandle?> -> c.size > 4 })
        ) {
            throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException("All loop clauses must be represented as MethodHandle arrays with at most 4 elements.")
        }
    }

    private fun loopChecks1a(
        i: Int,
        `in`: java.lang.invoke.MethodHandle,
        st: java.lang.invoke.MethodHandle
    ) {
        if (`in`.type().returnType() != st.type().returnType()) {
            throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.Class<out Any>>(
                "clause $i: init and step return types", `in`.type().returnType(),
                st.type().returnType()
            )
        }
    }

    private fun longestParameterList(
        mhs: java.util.stream.Stream<java.lang.invoke.MethodHandle>,
        skipSize: Int
    ): List<java.lang.Class<*>> {
        val empty: List<java.lang.Class<*>> = listOf<java.lang.Class<*>>()
        val longest: List<java.lang.Class<*>> =
            mhs.filter(java.util.function.Predicate<java.lang.invoke.MethodHandle> { obj: Any? ->
                java.util.Objects.nonNull(
                    obj
                )
            })
                .map<java.lang.invoke.MethodType> // take only those that can contribute to a common suffix because they are longer than the prefix
        (java.util.function.Function<java.lang.invoke.MethodHandle, java.lang.invoke.MethodType> { obj: java.lang.invoke.MethodHandle -> obj.type() }).filter(
            java.util.function.Predicate<java.lang.invoke.MethodType> { t: java.lang.invoke.MethodType -> t.parameterCount() > skipSize })
            .map<List<java.lang.Class<*>>>(
                java.util.function.Function<java.lang.invoke.MethodType, List<java.lang.Class<*>>> { obj: java.lang.invoke.MethodType -> obj.parameterList() })
            .reduce(
                java.util.function.BinaryOperator<List<java.lang.Class<*>>> { p: List<java.lang.Class<*>?>, q: List<java.lang.Class<*>?> -> if (p.size >= q.size) p else q })
            .orElse(empty)
        return if (longest.size == 0) empty else longest.subList(skipSize, longest.size)
    }

    private fun longestParameterList(lists: List<List<java.lang.Class<*>>>): List<java.lang.Class<*>> {
        val empty: List<java.lang.Class<*>> = listOf<java.lang.Class<*>>()
        return lists.stream()
            .reduce(java.util.function.BinaryOperator<List<java.lang.Class<*>>> { p: List<java.lang.Class<*>?>, q: List<java.lang.Class<*>?> -> if (p.size >= q.size) p else q })
            .orElse(empty)
    }

    private fun buildCommonSuffix(
        init: List<java.lang.invoke.MethodHandle>,
        step: List<java.lang.invoke.MethodHandle>,
        pred: List<java.lang.invoke.MethodHandle>,
        fini: List<java.lang.invoke.MethodHandle>,
        cpSize: Int
    ): List<java.lang.Class<*>> {
        val longest1: List<java.lang.Class<*>> =
            java.lang.invoke.MethodHandles.longestParameterList(java.util.stream.Stream.of<List<java.lang.invoke.MethodHandle>>(
                step,
                pred,
                fini
            ).flatMap<java.lang.invoke.MethodHandle>(
                java.util.function.Function<List<java.lang.invoke.MethodHandle>, java.util.stream.Stream<out java.lang.invoke.MethodHandle?>> { obj: List<*> -> obj.stream() }),
                cpSize
            )
        val longest2: List<java.lang.Class<*>> =
            java.lang.invoke.MethodHandles.longestParameterList(init.stream(), 0)
        return java.lang.invoke.MethodHandles.longestParameterList(
            java.util.Arrays.asList<List<java.lang.Class<*>>>(
                longest1,
                longest2
            )
        )
    }

    private fun loopChecks1b(
        init: List<java.lang.invoke.MethodHandle>,
        commonSuffix: List<java.lang.Class<*>>
    ) {
        if (init.stream()
                .filter(java.util.function.Predicate<java.lang.invoke.MethodHandle> { obj: Any? ->
                    java.util.Objects.nonNull(
                        obj
                    )
                }).map<java.lang.invoke.MethodType>(
                    java.util.function.Function<java.lang.invoke.MethodHandle, java.lang.invoke.MethodType> { obj: java.lang.invoke.MethodHandle -> obj.type() })
                .anyMatch(
                    java.util.function.Predicate<java.lang.invoke.MethodType> { t: java.lang.invoke.MethodType ->
                        !t.effectivelyIdenticalParameters(
                            0,
                            commonSuffix
                        )
                    })
        ) {
            throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
                ("found non-effectively identical init parameter type lists: " + init +
                        " (common suffix: " + commonSuffix + ")")
            )
        }
    }

    private fun loopChecks1cd(
        pred: List<java.lang.invoke.MethodHandle>,
        fini: List<java.lang.invoke.MethodHandle>,
        loopReturnType: java.lang.Class<*>
    ) {
        if (fini.stream()
                .filter(java.util.function.Predicate<java.lang.invoke.MethodHandle> { obj: Any? ->
                    java.util.Objects.nonNull(
                        obj
                    )
                }).map<java.lang.invoke.MethodType>(
                    java.util.function.Function<java.lang.invoke.MethodHandle, java.lang.invoke.MethodType> { obj: java.lang.invoke.MethodHandle -> obj.type() })
                .map(
                    java.util.function.Function<java.lang.invoke.MethodType, java.lang.Class<Any>> { obj: java.lang.invoke.MethodType -> obj.returnType() })
                .anyMatch(
                    java.util.function.Predicate<java.lang.Class<Any>> { t: java.lang.Class<Any?> -> t != loopReturnType })
        ) {
            throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
                ("found non-identical finalizer return types: " + fini + " (return type: " +
                        loopReturnType + ")")
            )
        }
        if (!pred.stream()
                .filter(java.util.function.Predicate<java.lang.invoke.MethodHandle> { obj: Any? ->
                    java.util.Objects.nonNull(
                        obj
                    )
                }).findFirst().isPresent()
        ) {
            throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
                "no predicate found",
                pred
            )
        }
        if (pred.stream()
                .filter(java.util.function.Predicate<java.lang.invoke.MethodHandle> { obj: Any? ->
                    java.util.Objects.nonNull(
                        obj
                    )
                }).map<java.lang.invoke.MethodType>(
                    java.util.function.Function<java.lang.invoke.MethodHandle, java.lang.invoke.MethodType> { obj: java.lang.invoke.MethodHandle -> obj.type() })
                .map(
                    java.util.function.Function<java.lang.invoke.MethodType, java.lang.Class<Any>> { obj: java.lang.invoke.MethodType -> obj.returnType() })
                .anyMatch(
                    java.util.function.Predicate<java.lang.Class<Any>> { t: java.lang.Class<Any?> -> t != Boolean::class.javaPrimitiveType })
        ) {
            throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
                "predicates must have boolean return type",
                pred
            )
        }
    }

    private fun loopChecks2(
        step: List<java.lang.invoke.MethodHandle>,
        pred: List<java.lang.invoke.MethodHandle>,
        fini: List<java.lang.invoke.MethodHandle>,
        commonParameterSequence: List<java.lang.Class<*>>
    ) {
        if (java.util.stream.Stream.of<List<java.lang.invoke.MethodHandle>>(step, pred, fini)
                .flatMap<java.lang.invoke.MethodHandle>(
                    java.util.function.Function<List<java.lang.invoke.MethodHandle>, java.util.stream.Stream<out java.lang.invoke.MethodHandle?>> { obj: List<*> -> obj.stream() })
                .filter(
                    java.util.function.Predicate<java.lang.invoke.MethodHandle> { obj: Any? ->
                        java.util.Objects.nonNull(
                            obj
                        )
                    }).map<java.lang.invoke.MethodType>(
                    java.util.function.Function<java.lang.invoke.MethodHandle, java.lang.invoke.MethodType> { obj: java.lang.invoke.MethodHandle -> obj.type() })
                .anyMatch(
                    java.util.function.Predicate<java.lang.invoke.MethodType> { t: java.lang.invoke.MethodType ->
                        !t.effectivelyIdenticalParameters(
                            0,
                            commonParameterSequence
                        )
                    })
        ) {
            throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
                ("found non-effectively identical parameter type lists:\nstep: " + step +
                        "\npred: " + pred + "\nfini: " + fini + " (common parameter sequence: " + commonParameterSequence + ")")
            )
        }
    }

    private fun fillParameterTypes(
        hs: List<java.lang.invoke.MethodHandle>,
        targetParams: List<java.lang.Class<*>>
    ): List<java.lang.invoke.MethodHandle> {
        return hs.stream()
            .map<java.lang.invoke.MethodHandle>(java.util.function.Function<java.lang.invoke.MethodHandle, java.lang.invoke.MethodHandle> { h: java.lang.invoke.MethodHandle ->
                val pc: Int = h.type().parameterCount()
                val tpsize: Int = targetParams.size
                if (pc < tpsize) java.lang.invoke.MethodHandles.dropArguments0(
                    h,
                    pc,
                    targetParams.subList(pc, tpsize)
                ) else h
            })
            .collect<List<java.lang.invoke.MethodHandle>, Any>(java.util.stream.Collectors.toList<java.lang.invoke.MethodHandle>())
    }

    private fun fixArities(hs: List<java.lang.invoke.MethodHandle>): List<java.lang.invoke.MethodHandle> {
        return hs.stream()
            .map<java.lang.invoke.MethodHandle>(java.util.function.Function<java.lang.invoke.MethodHandle, java.lang.invoke.MethodHandle> { obj: java.lang.invoke.MethodHandle -> obj.asFixedArity() })
            .collect<List<java.lang.invoke.MethodHandle>, Any>(java.util.stream.Collectors.toList<java.lang.invoke.MethodHandle>())
    }

    /**
     * Constructs a `while` loop from an initializer, a body, and a predicate.
     * This is a convenience wrapper for the [generic loop combinator][.loop].
     *
     *
     * The `pred` handle describes the loop condition; and `body`, its body. The loop resulting from this
     * method will, in each iteration, first evaluate the predicate and then execute its body (if the predicate
     * evaluates to `true`).
     * The loop will terminate once the predicate evaluates to `false` (the body will not be executed in this case).
     *
     *
     * The `init` handle describes the initial value of an additional optional loop-local variable.
     * In each iteration, this loop-local variable, if present, will be passed to the `body`
     * and updated with the value returned from its invocation. The result of loop execution will be
     * the final value of the additional loop-local variable (if present).
     *
     *
     * The following rules hold for these argument handles:
     *  * The `body` handle must not be `null`; its type must be of the form
     * `(V A...)V`, where `V` is non-`void`, or else `(A...)void`.
     * (In the `void` case, we assign the type `void` to the name `V`,
     * and we will write `(V A...)V` with the understanding that a `void` type `V`
     * is quietly dropped from the parameter list, leaving `(A...)V`.)
     *  * The parameter list `(V A...)` of the body is called the *internal parameter list*.
     * It will constrain the parameter lists of the other loop parts.
     *  * If the iteration variable type `V` is dropped from the internal parameter list, the resulting shorter
     * list `(A...)` is called the *external parameter list*.
     *  * The body return type `V`, if non-`void`, determines the type of an
     * additional state variable of the loop.
     * The body must both accept and return a value of this type `V`.
     *  * If `init` is non-`null`, it must have return type `V`.
     * Its parameter list (of some [form `(A*)`](MethodHandles.html#astar)) must be
     * [effectively identical](MethodHandles.html#effid)
     * to the external parameter list `(A...)`.
     *  * If `init` is `null`, the loop variable will be initialized to its
     * [default value][.empty].
     *  * The `pred` handle must not be `null`.  It must have `boolean` as its return type.
     * Its parameter list (either empty or of the form `(V A*)`) must be
     * effectively identical to the internal parameter list.
     *
     *
     *
     * The resulting loop handle's result type and parameter signature are determined as follows:
     *  * The loop handle's result type is the result type `V` of the body.
     *  * The loop handle's parameter types are the types `(A...)`,
     * from the external parameter list.
     *
     *
     *
     * Here is pseudocode for the resulting loop handle. In the code, `V`/`v` represent the type / value of
     * the sole loop variable as well as the result type of the loop; and `A`/`a`, that of the argument
     * passed to the loop.
     * <blockquote><pre>`V init(A...);
     * boolean pred(V, A...);
     * V body(V, A...);
     * V whileLoop(A... a...) {
     * V v = init(a...);
     * while (pred(v, a...)) {
     * v = body(v, a...);
     * }
     * return v;
     * }
    `</pre></blockquote> *
     *
     * @apiNote Example:
     * <blockquote><pre>`// implement the zip function for lists as a loop handle
     * static List<String> initZip(Iterator<String> a, Iterator<String> b) { return new ArrayList<>(); }
     * static boolean zipPred(List<String> zip, Iterator<String> a, Iterator<String> b) { return a.hasNext() && b.hasNext(); }
     * static List<String> zipStep(List<String> zip, Iterator<String> a, Iterator<String> b) {
     * zip.add(a.next());
     * zip.add(b.next());
     * return zip;
     * }
     * // assume MH_initZip, MH_zipPred, and MH_zipStep are handles to the above methods
     * MethodHandle loop = MethodHandles.whileLoop(MH_initZip, MH_zipPred, MH_zipStep);
     * List<String> a = Arrays.asList("a", "b", "c", "d");
     * List<String> b = Arrays.asList("e", "f", "g", "h");
     * List<String> zipped = Arrays.asList("a", "e", "b", "f", "c", "g", "d", "h");
     * assertEquals(zipped, (List<String>) loop.invoke(a.iterator(), b.iterator()));
    `</pre></blockquote> *
     *
     *
     * @apiNote The implementation of this method can be expressed as follows:
     * <blockquote><pre>`MethodHandle whileLoop(MethodHandle init, MethodHandle pred, MethodHandle body) {
     * MethodHandle fini = (body.type().returnType() == void.class
     * ? null : identity(body.type().returnType()));
     * MethodHandle[]
     * checkExit = { null, null, pred, fini },
     * varBody   = { init, body };
     * return loop(checkExit, varBody);
     * }
    `</pre></blockquote> *
     *
     * @param init optional initializer, providing the initial value of the loop variable.
     * May be `null`, implying a default initial value.  See above for other constraints.
     * @param pred condition for the loop, which may not be `null`. Its result type must be `boolean`. See
     * above for other constraints.
     * @param body body of the loop, which may not be `null`. It controls the loop parameters and result type.
     * See above for other constraints.
     *
     * @return a method handle implementing the `while` loop as described by the arguments.
     * @throws IllegalArgumentException if the rules for the arguments are violated.
     * @throws NullPointerException if `pred` or `body` are `null`.
     *
     * @see .loop
     * @see .doWhileLoop
     * @since 9
     */
    fun whileLoop(
        init: java.lang.invoke.MethodHandle,
        pred: java.lang.invoke.MethodHandle?,
        body: java.lang.invoke.MethodHandle
    ): java.lang.invoke.MethodHandle {
        java.lang.invoke.MethodHandles.whileLoopChecks(init, pred, body)
        val fini: java.lang.invoke.MethodHandle =
            java.lang.invoke.MethodHandles.identityOrVoid(body.type().returnType())
        val checkExit: Array<java.lang.invoke.MethodHandle?> = arrayOf(null, null, pred, fini)
        val varBody: Array<java.lang.invoke.MethodHandle> =
            arrayOf<java.lang.invoke.MethodHandle>(init, body)
        return java.lang.invoke.MethodHandles.loop(checkExit, varBody)
    }

    /**
     * Constructs a `do-while` loop from an initializer, a body, and a predicate.
     * This is a convenience wrapper for the [generic loop combinator][.loop].
     *
     *
     * The `pred` handle describes the loop condition; and `body`, its body. The loop resulting from this
     * method will, in each iteration, first execute its body and then evaluate the predicate.
     * The loop will terminate once the predicate evaluates to `false` after an execution of the body.
     *
     *
     * The `init` handle describes the initial value of an additional optional loop-local variable.
     * In each iteration, this loop-local variable, if present, will be passed to the `body`
     * and updated with the value returned from its invocation. The result of loop execution will be
     * the final value of the additional loop-local variable (if present).
     *
     *
     * The following rules hold for these argument handles:
     *  * The `body` handle must not be `null`; its type must be of the form
     * `(V A...)V`, where `V` is non-`void`, or else `(A...)void`.
     * (In the `void` case, we assign the type `void` to the name `V`,
     * and we will write `(V A...)V` with the understanding that a `void` type `V`
     * is quietly dropped from the parameter list, leaving `(A...)V`.)
     *  * The parameter list `(V A...)` of the body is called the *internal parameter list*.
     * It will constrain the parameter lists of the other loop parts.
     *  * If the iteration variable type `V` is dropped from the internal parameter list, the resulting shorter
     * list `(A...)` is called the *external parameter list*.
     *  * The body return type `V`, if non-`void`, determines the type of an
     * additional state variable of the loop.
     * The body must both accept and return a value of this type `V`.
     *  * If `init` is non-`null`, it must have return type `V`.
     * Its parameter list (of some [form `(A*)`](MethodHandles.html#astar)) must be
     * [effectively identical](MethodHandles.html#effid)
     * to the external parameter list `(A...)`.
     *  * If `init` is `null`, the loop variable will be initialized to its
     * [default value][.empty].
     *  * The `pred` handle must not be `null`.  It must have `boolean` as its return type.
     * Its parameter list (either empty or of the form `(V A*)`) must be
     * effectively identical to the internal parameter list.
     *
     *
     *
     * The resulting loop handle's result type and parameter signature are determined as follows:
     *  * The loop handle's result type is the result type `V` of the body.
     *  * The loop handle's parameter types are the types `(A...)`,
     * from the external parameter list.
     *
     *
     *
     * Here is pseudocode for the resulting loop handle. In the code, `V`/`v` represent the type / value of
     * the sole loop variable as well as the result type of the loop; and `A`/`a`, that of the argument
     * passed to the loop.
     * <blockquote><pre>`V init(A...);
     * boolean pred(V, A...);
     * V body(V, A...);
     * V doWhileLoop(A... a...) {
     * V v = init(a...);
     * do {
     * v = body(v, a...);
     * } while (pred(v, a...));
     * return v;
     * }
    `</pre></blockquote> *
     *
     * @apiNote Example:
     * <blockquote><pre>`// int i = 0; while (i < limit) { ++i; } return i; => limit
     * static int zero(int limit) { return 0; }
     * static int step(int i, int limit) { return i + 1; }
     * static boolean pred(int i, int limit) { return i < limit; }
     * // assume MH_zero, MH_step, and MH_pred are handles to the above methods
     * MethodHandle loop = MethodHandles.doWhileLoop(MH_zero, MH_step, MH_pred);
     * assertEquals(23, loop.invoke(23));
    `</pre></blockquote> *
     *
     *
     * @apiNote The implementation of this method can be expressed as follows:
     * <blockquote><pre>`MethodHandle doWhileLoop(MethodHandle init, MethodHandle body, MethodHandle pred) {
     * MethodHandle fini = (body.type().returnType() == void.class
     * ? null : identity(body.type().returnType()));
     * MethodHandle[] clause = { init, body, pred, fini };
     * return loop(clause);
     * }
    `</pre></blockquote> *
     *
     * @param init optional initializer, providing the initial value of the loop variable.
     * May be `null`, implying a default initial value.  See above for other constraints.
     * @param body body of the loop, which may not be `null`. It controls the loop parameters and result type.
     * See above for other constraints.
     * @param pred condition for the loop, which may not be `null`. Its result type must be `boolean`. See
     * above for other constraints.
     *
     * @return a method handle implementing the `while` loop as described by the arguments.
     * @throws IllegalArgumentException if the rules for the arguments are violated.
     * @throws NullPointerException if `pred` or `body` are `null`.
     *
     * @see .loop
     * @see .whileLoop
     * @since 9
     */
    fun doWhileLoop(
        init: java.lang.invoke.MethodHandle,
        body: java.lang.invoke.MethodHandle,
        pred: java.lang.invoke.MethodHandle
    ): java.lang.invoke.MethodHandle {
        java.lang.invoke.MethodHandles.whileLoopChecks(init, pred, body)
        val fini: java.lang.invoke.MethodHandle =
            java.lang.invoke.MethodHandles.identityOrVoid(body.type().returnType())
        val clause: Array<java.lang.invoke.MethodHandle> =
            arrayOf<java.lang.invoke.MethodHandle>(init, body, pred, fini)
        return java.lang.invoke.MethodHandles.loop(*clause)
    }

    private fun whileLoopChecks(
        init: java.lang.invoke.MethodHandle?,
        pred: java.lang.invoke.MethodHandle,
        body: java.lang.invoke.MethodHandle
    ) {
        java.util.Objects.requireNonNull<java.lang.invoke.MethodHandle>(pred)
        java.util.Objects.requireNonNull<java.lang.invoke.MethodHandle>(body)
        val bodyType: java.lang.invoke.MethodType = body.type()
        val returnType: java.lang.Class<*> = bodyType.returnType()
        val innerList: List<java.lang.Class<*>> = bodyType.parameterList()
        var outerList: List<java.lang.Class<*>> = innerList
        if (returnType == Void.TYPE) {
            // OK
        } else if (innerList.size == 0 || innerList[0] != returnType) {
            // leading V argument missing => error
            val expected: java.lang.invoke.MethodType = bodyType.insertParameterTypes(0, returnType)
            throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
                "body function",
                bodyType,
                expected
            )
        } else {
            outerList = innerList.subList(1, innerList.size)
        }
        val predType: java.lang.invoke.MethodType = pred.type()
        if (predType.returnType() != Boolean::class.javaPrimitiveType ||
            !predType.effectivelyIdenticalParameters(0, innerList)
        ) {
            throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
                "loop predicate", predType, java.lang.invoke.MethodType.methodType(
                    Boolean::class.javaPrimitiveType, innerList
                )
            )
        }
        if (init != null) {
            val initType: java.lang.invoke.MethodType = init.type()
            if (initType.returnType() != returnType ||
                !initType.effectivelyIdenticalParameters(0, outerList)
            ) {
                throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
                    "loop initializer",
                    initType,
                    java.lang.invoke.MethodType.methodType(returnType, outerList)
                )
            }
        }
    }

    /**
     * Constructs a loop that runs a given number of iterations.
     * This is a convenience wrapper for the [generic loop combinator][.loop].
     *
     *
     * The number of iterations is determined by the `iterations` handle evaluation result.
     * The loop counter `i` is an extra loop iteration variable of type `int`.
     * It will be initialized to 0 and incremented by 1 in each iteration.
     *
     *
     * If the `body` handle returns a non-`void` type `V`, a leading loop iteration variable
     * of that type is also present.  This variable is initialized using the optional `init` handle,
     * or to the [default value][.empty] of type `V` if that handle is `null`.
     *
     *
     * In each iteration, the iteration variables are passed to an invocation of the `body` handle.
     * A non-`void` value returned from the body (of type `V`) updates the leading
     * iteration variable.
     * The result of the loop handle execution will be the final `V` value of that variable
     * (or `void` if there is no `V` variable).
     *
     *
     * The following rules hold for the argument handles:
     *  * The `iterations` handle must not be `null`, and must return
     * the type `int`, referred to here as `I` in parameter type lists.
     *  * The `body` handle must not be `null`; its type must be of the form
     * `(V I A...)V`, where `V` is non-`void`, or else `(I A...)void`.
     * (In the `void` case, we assign the type `void` to the name `V`,
     * and we will write `(V I A...)V` with the understanding that a `void` type `V`
     * is quietly dropped from the parameter list, leaving `(I A...)V`.)
     *  * The parameter list `(V I A...)` of the body contributes to a list
     * of types called the *internal parameter list*.
     * It will constrain the parameter lists of the other loop parts.
     *  * As a special case, if the body contributes only `V` and `I` types,
     * with no additional `A` types, then the internal parameter list is extended by
     * the argument types `A...` of the `iterations` handle.
     *  * If the iteration variable types `(V I)` are dropped from the internal parameter list, the resulting shorter
     * list `(A...)` is called the *external parameter list*.
     *  * The body return type `V`, if non-`void`, determines the type of an
     * additional state variable of the loop.
     * The body must both accept a leading parameter and return a value of this type `V`.
     *  * If `init` is non-`null`, it must have return type `V`.
     * Its parameter list (of some [form `(A*)`](MethodHandles.html#astar)) must be
     * [effectively identical](MethodHandles.html#effid)
     * to the external parameter list `(A...)`.
     *  * If `init` is `null`, the loop variable will be initialized to its
     * [default value][.empty].
     *  * The parameter list of `iterations` (of some form `(A*)`) must be
     * effectively identical to the external parameter list `(A...)`.
     *
     *
     *
     * The resulting loop handle's result type and parameter signature are determined as follows:
     *  * The loop handle's result type is the result type `V` of the body.
     *  * The loop handle's parameter types are the types `(A...)`,
     * from the external parameter list.
     *
     *
     *
     * Here is pseudocode for the resulting loop handle. In the code, `V`/`v` represent the type / value of
     * the second loop variable as well as the result type of the loop; and `A...`/`a...` represent
     * arguments passed to the loop.
     * <blockquote><pre>`int iterations(A...);
     * V init(A...);
     * V body(V, int, A...);
     * V countedLoop(A... a...) {
     * int end = iterations(a...);
     * V v = init(a...);
     * for (int i = 0; i < end; ++i) {
     * v = body(v, i, a...);
     * }
     * return v;
     * }
    `</pre></blockquote> *
     *
     * @apiNote Example with a fully conformant body method:
     * <blockquote><pre>`// String s = "Lambdaman!"; for (int i = 0; i < 13; ++i) { s = "na " + s; } return s;
     * // => a variation on a well known theme
     * static String step(String v, int counter, String init) { return "na " + v; }
     * // assume MH_step is a handle to the method above
     * MethodHandle fit13 = MethodHandles.constant(int.class, 13);
     * MethodHandle start = MethodHandles.identity(String.class);
     * MethodHandle loop = MethodHandles.countedLoop(fit13, start, MH_step);
     * assertEquals("na na na na na na na na na na na na na Lambdaman!", loop.invoke("Lambdaman!"));
    `</pre></blockquote> *
     *
     * @apiNote Example with the simplest possible body method type,
     * and passing the number of iterations to the loop invocation:
     * <blockquote><pre>`// String s = "Lambdaman!"; for (int i = 0; i < 13; ++i) { s = "na " + s; } return s;
     * // => a variation on a well known theme
     * static String step(String v, int counter ) { return "na " + v; }
     * // assume MH_step is a handle to the method above
     * MethodHandle count = MethodHandles.dropArguments(MethodHandles.identity(int.class), 1, String.class);
     * MethodHandle start = MethodHandles.dropArguments(MethodHandles.identity(String.class), 0, int.class);
     * MethodHandle loop = MethodHandles.countedLoop(count, start, MH_step);  // (v, i) -> "na " + v
     * assertEquals("na na na na na na na na na na na na na Lambdaman!", loop.invoke(13, "Lambdaman!"));
    `</pre></blockquote> *
     *
     * @apiNote Example that treats the number of iterations, string to append to, and string to append
     * as loop parameters:
     * <blockquote><pre>`// String s = "Lambdaman!", t = "na"; for (int i = 0; i < 13; ++i) { s = t + " " + s; } return s;
     * // => a variation on a well known theme
     * static String step(String v, int counter, int iterations_, String pre, String start_) { return pre + " " + v; }
     * // assume MH_step is a handle to the method above
     * MethodHandle count = MethodHandles.identity(int.class);
     * MethodHandle start = MethodHandles.dropArguments(MethodHandles.identity(String.class), 0, int.class, String.class);
     * MethodHandle loop = MethodHandles.countedLoop(count, start, MH_step);  // (v, i, _, pre, _) -> pre + " " + v
     * assertEquals("na na na na na na na na na na na na na Lambdaman!", loop.invoke(13, "na", "Lambdaman!"));
    `</pre></blockquote> *
     *
     * @apiNote Example that illustrates the usage of [.dropArgumentsToMatch]
     * to enforce a loop type:
     * <blockquote><pre>`// String s = "Lambdaman!", t = "na"; for (int i = 0; i < 13; ++i) { s = t + " " + s; } return s;
     * // => a variation on a well known theme
     * static String step(String v, int counter, String pre) { return pre + " " + v; }
     * // assume MH_step is a handle to the method above
     * MethodType loopType = methodType(String.class, String.class, int.class, String.class);
     * MethodHandle count = MethodHandles.dropArgumentsToMatch(MethodHandles.identity(int.class),    0, loopType.parameterList(), 1);
     * MethodHandle start = MethodHandles.dropArgumentsToMatch(MethodHandles.identity(String.class), 0, loopType.parameterList(), 2);
     * MethodHandle body  = MethodHandles.dropArgumentsToMatch(MH_step,                              2, loopType.parameterList(), 0);
     * MethodHandle loop = MethodHandles.countedLoop(count, start, body);  // (v, i, pre, _, _) -> pre + " " + v
     * assertEquals("na na na na na na na na na na na na na Lambdaman!", loop.invoke("na", 13, "Lambdaman!"));
    `</pre></blockquote> *
     *
     * @apiNote The implementation of this method can be expressed as follows:
     * <blockquote><pre>`MethodHandle countedLoop(MethodHandle iterations, MethodHandle init, MethodHandle body) {
     * return countedLoop(empty(iterations.type()), iterations, init, body);
     * }
    `</pre></blockquote> *
     *
     * @param iterations a non-`null` handle to return the number of iterations this loop should run. The handle's
     * result type must be `int`. See above for other constraints.
     * @param init optional initializer, providing the initial value of the loop variable.
     * May be `null`, implying a default initial value.  See above for other constraints.
     * @param body body of the loop, which may not be `null`.
     * It controls the loop parameters and result type in the standard case (see above for details).
     * It must accept its own return type (if non-void) plus an `int` parameter (for the counter),
     * and may accept any number of additional types.
     * See above for other constraints.
     *
     * @return a method handle representing the loop.
     * @throws NullPointerException if either of the `iterations` or `body` handles is `null`.
     * @throws IllegalArgumentException if any argument violates the rules formulated above.
     *
     * @see .countedLoop
     * @since 9
     */
    fun countedLoop(
        iterations: java.lang.invoke.MethodHandle,
        init: java.lang.invoke.MethodHandle?,
        body: java.lang.invoke.MethodHandle?
    ): java.lang.invoke.MethodHandle {
        return java.lang.invoke.MethodHandles.countedLoop(
            java.lang.invoke.MethodHandles.empty(
                iterations.type()
            ), iterations, init, body
        )
    }

    /**
     * Constructs a loop that counts over a range of numbers.
     * This is a convenience wrapper for the [generic loop combinator][.loop].
     *
     *
     * The loop counter `i` is a loop iteration variable of type `int`.
     * The `start` and `end` handles determine the start (inclusive) and end (exclusive)
     * values of the loop counter.
     * The loop counter will be initialized to the `int` value returned from the evaluation of the
     * `start` handle and run to the value returned from `end` (exclusively) with a step width of 1.
     *
     *
     * If the `body` handle returns a non-`void` type `V`, a leading loop iteration variable
     * of that type is also present.  This variable is initialized using the optional `init` handle,
     * or to the [default value][.empty] of type `V` if that handle is `null`.
     *
     *
     * In each iteration, the iteration variables are passed to an invocation of the `body` handle.
     * A non-`void` value returned from the body (of type `V`) updates the leading
     * iteration variable.
     * The result of the loop handle execution will be the final `V` value of that variable
     * (or `void` if there is no `V` variable).
     *
     *
     * The following rules hold for the argument handles:
     *  * The `start` and `end` handles must not be `null`, and must both return
     * the common type `int`, referred to here as `I` in parameter type lists.
     *  * The `body` handle must not be `null`; its type must be of the form
     * `(V I A...)V`, where `V` is non-`void`, or else `(I A...)void`.
     * (In the `void` case, we assign the type `void` to the name `V`,
     * and we will write `(V I A...)V` with the understanding that a `void` type `V`
     * is quietly dropped from the parameter list, leaving `(I A...)V`.)
     *  * The parameter list `(V I A...)` of the body contributes to a list
     * of types called the *internal parameter list*.
     * It will constrain the parameter lists of the other loop parts.
     *  * As a special case, if the body contributes only `V` and `I` types,
     * with no additional `A` types, then the internal parameter list is extended by
     * the argument types `A...` of the `end` handle.
     *  * If the iteration variable types `(V I)` are dropped from the internal parameter list, the resulting shorter
     * list `(A...)` is called the *external parameter list*.
     *  * The body return type `V`, if non-`void`, determines the type of an
     * additional state variable of the loop.
     * The body must both accept a leading parameter and return a value of this type `V`.
     *  * If `init` is non-`null`, it must have return type `V`.
     * Its parameter list (of some [form `(A*)`](MethodHandles.html#astar)) must be
     * [effectively identical](MethodHandles.html#effid)
     * to the external parameter list `(A...)`.
     *  * If `init` is `null`, the loop variable will be initialized to its
     * [default value][.empty].
     *  * The parameter list of `start` (of some form `(A*)`) must be
     * effectively identical to the external parameter list `(A...)`.
     *  * Likewise, the parameter list of `end` must be effectively identical
     * to the external parameter list.
     *
     *
     *
     * The resulting loop handle's result type and parameter signature are determined as follows:
     *  * The loop handle's result type is the result type `V` of the body.
     *  * The loop handle's parameter types are the types `(A...)`,
     * from the external parameter list.
     *
     *
     *
     * Here is pseudocode for the resulting loop handle. In the code, `V`/`v` represent the type / value of
     * the second loop variable as well as the result type of the loop; and `A...`/`a...` represent
     * arguments passed to the loop.
     * <blockquote><pre>`int start(A...);
     * int end(A...);
     * V init(A...);
     * V body(V, int, A...);
     * V countedLoop(A... a...) {
     * int e = end(a...);
     * int s = start(a...);
     * V v = init(a...);
     * for (int i = s; i < e; ++i) {
     * v = body(v, i, a...);
     * }
     * return v;
     * }
    `</pre></blockquote> *
     *
     * @apiNote The implementation of this method can be expressed as follows:
     * <blockquote><pre>`MethodHandle countedLoop(MethodHandle start, MethodHandle end, MethodHandle init, MethodHandle body) {
     * MethodHandle returnVar = dropArguments(identity(init.type().returnType()), 0, int.class, int.class);
     * // assume MH_increment and MH_predicate are handles to implementation-internal methods with
     * // the following semantics:
     * // MH_increment: (int limit, int counter) -> counter + 1
     * // MH_predicate: (int limit, int counter) -> counter < limit
     * Class<?> counterType = start.type().returnType();  // int
     * Class<?> returnType = body.type().returnType();
     * MethodHandle incr = MH_increment, pred = MH_predicate, retv = null;
     * if (returnType != void.class) {  // ignore the V variable
     * incr = dropArguments(incr, 1, returnType);  // (limit, v, i) => (limit, i)
     * pred = dropArguments(pred, 1, returnType);  // ditto
     * retv = dropArguments(identity(returnType), 0, counterType); // ignore limit
     * }
     * body = dropArguments(body, 0, counterType);  // ignore the limit variable
     * MethodHandle[]
     * loopLimit  = { end, null, pred, retv }, // limit = end(); i < limit || return v
     * bodyClause = { init, body },            // v = init(); v = body(v, i)
     * indexVar   = { start, incr };           // i = start(); i = i + 1
     * return loop(loopLimit, bodyClause, indexVar);
     * }
    `</pre></blockquote> *
     *
     * @param start a non-`null` handle to return the start value of the loop counter, which must be `int`.
     * See above for other constraints.
     * @param end a non-`null` handle to return the end value of the loop counter (the loop will run to
     * `end-1`). The result type must be `int`. See above for other constraints.
     * @param init optional initializer, providing the initial value of the loop variable.
     * May be `null`, implying a default initial value.  See above for other constraints.
     * @param body body of the loop, which may not be `null`.
     * It controls the loop parameters and result type in the standard case (see above for details).
     * It must accept its own return type (if non-void) plus an `int` parameter (for the counter),
     * and may accept any number of additional types.
     * See above for other constraints.
     *
     * @return a method handle representing the loop.
     * @throws NullPointerException if any of the `start`, `end`, or `body` handles is `null`.
     * @throws IllegalArgumentException if any argument violates the rules formulated above.
     *
     * @see .countedLoop
     * @since 9
     */
    fun countedLoop(
        start: java.lang.invoke.MethodHandle,
        end: java.lang.invoke.MethodHandle,
        init: java.lang.invoke.MethodHandle,
        body: java.lang.invoke.MethodHandle
    ): java.lang.invoke.MethodHandle {
        var body: java.lang.invoke.MethodHandle = body
        java.lang.invoke.MethodHandles.countedLoopChecks(start, end, init, body)
        val counterType: java.lang.Class<*> = start.type().returnType() // int, but who's counting?
        val limitType: java.lang.Class<*> = end.type().returnType() // yes, int again
        val returnType: java.lang.Class<*> = body.type().returnType()
        // Android-changed: getConstantHandle is in MethodHandles.
        // MethodHandle incr = MethodHandleImpl.getConstantHandle(MethodHandleImpl.MH_countedLoopStep);
        // MethodHandle pred = MethodHandleImpl.getConstantHandle(MethodHandleImpl.MH_countedLoopPred);
        var incr: java.lang.invoke.MethodHandle =
            java.lang.invoke.MethodHandles.getConstantHandle(java.lang.invoke.MethodHandles.MH_countedLoopStep)
        var pred: java.lang.invoke.MethodHandle =
            java.lang.invoke.MethodHandles.getConstantHandle(java.lang.invoke.MethodHandles.MH_countedLoopPred)
        var retv: java.lang.invoke.MethodHandle? = null
        if (returnType != Void.TYPE) {
            incr = java.lang.invoke.MethodHandles.dropArguments(
                incr,
                1,
                returnType
            ) // (limit, v, i) => (limit, i)
            pred = java.lang.invoke.MethodHandles.dropArguments(pred, 1, returnType) // ditto
            retv = java.lang.invoke.MethodHandles.dropArguments(
                java.lang.invoke.MethodHandles.identity(returnType), 0, counterType
            )
        }
        body = java.lang.invoke.MethodHandles.dropArguments(
            body,
            0,
            counterType
        ) // ignore the limit variable
        val loopLimit: Array<java.lang.invoke.MethodHandle?> = arrayOf(end, null, pred, retv)
        // limit = end(); i < limit || return v
        val bodyClause: Array<java.lang.invoke.MethodHandle> =
            arrayOf<java.lang.invoke.MethodHandle>(init, body)
        // v = init(); v = body(v, i)
        val indexVar: Array<java.lang.invoke.MethodHandle> =
            arrayOf<java.lang.invoke.MethodHandle>(start, incr) // i = start(); i = i + 1
        return java.lang.invoke.MethodHandles.loop(loopLimit, bodyClause, indexVar)
    }

    private fun countedLoopChecks(
        start: java.lang.invoke.MethodHandle,
        end: java.lang.invoke.MethodHandle,
        init: java.lang.invoke.MethodHandle?,
        body: java.lang.invoke.MethodHandle
    ) {
        java.util.Objects.requireNonNull<java.lang.invoke.MethodHandle>(start)
        java.util.Objects.requireNonNull<java.lang.invoke.MethodHandle>(end)
        java.util.Objects.requireNonNull<java.lang.invoke.MethodHandle>(body)
        val counterType: java.lang.Class<*> = start.type().returnType()
        if (counterType != Int::class.javaPrimitiveType) {
            val expected: java.lang.invoke.MethodType =
                start.type().changeReturnType(Int::class.javaPrimitiveType)
            throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
                "start function",
                start.type(),
                expected
            )
        } else if (end.type().returnType() != counterType) {
            val expected: java.lang.invoke.MethodType = end.type().changeReturnType(counterType)
            throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
                "end function",
                end.type(),
                expected
            )
        }
        val bodyType: java.lang.invoke.MethodType = body.type()
        val returnType: java.lang.Class<*> = bodyType.returnType()
        var innerList: List<java.lang.Class<*>> = bodyType.parameterList()
        // strip leading V value if present
        val vsize = (if (returnType == Void.TYPE) 0 else 1)
        if (vsize != 0 && (innerList.size == 0 || innerList[0] != returnType)) {
            // argument list has no "V" => error
            val expected: java.lang.invoke.MethodType = bodyType.insertParameterTypes(0, returnType)
            throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
                "body function",
                bodyType,
                expected
            )
        } else if (innerList.size <= vsize || innerList[vsize] != counterType) {
            // missing I type => error
            val expected: java.lang.invoke.MethodType =
                bodyType.insertParameterTypes(vsize, counterType)
            throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
                "body function",
                bodyType,
                expected
            )
        }
        var outerList: List<java.lang.Class<*>> = innerList.subList(vsize + 1, innerList.size)
        if (outerList.isEmpty()) {
            // special case; take lists from end handle
            outerList = end.type().parameterList()
            innerList = bodyType.insertParameterTypes(vsize + 1, outerList).parameterList()
        }
        val expected: java.lang.invoke.MethodType =
            java.lang.invoke.MethodType.methodType(counterType, outerList)
        if (!start.type().effectivelyIdenticalParameters(0, outerList)) {
            throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
                "start parameter types",
                start.type(),
                expected
            )
        }
        if (end.type() !== start.type() &&
            !end.type().effectivelyIdenticalParameters(0, outerList)
        ) {
            throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
                "end parameter types",
                end.type(),
                expected
            )
        }
        if (init != null) {
            val initType: java.lang.invoke.MethodType = init.type()
            if (initType.returnType() != returnType ||
                !initType.effectivelyIdenticalParameters(0, outerList)
            ) {
                throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
                    "loop initializer",
                    initType,
                    java.lang.invoke.MethodType.methodType(returnType, outerList)
                )
            }
        }
    }

    /**
     * Constructs a loop that ranges over the values produced by an `Iterator<T>`.
     * This is a convenience wrapper for the [generic loop combinator][.loop].
     *
     *
     * The iterator itself will be determined by the evaluation of the `iterator` handle.
     * Each value it produces will be stored in a loop iteration variable of type `T`.
     *
     *
     * If the `body` handle returns a non-`void` type `V`, a leading loop iteration variable
     * of that type is also present.  This variable is initialized using the optional `init` handle,
     * or to the [default value][.empty] of type `V` if that handle is `null`.
     *
     *
     * In each iteration, the iteration variables are passed to an invocation of the `body` handle.
     * A non-`void` value returned from the body (of type `V`) updates the leading
     * iteration variable.
     * The result of the loop handle execution will be the final `V` value of that variable
     * (or `void` if there is no `V` variable).
     *
     *
     * The following rules hold for the argument handles:
     *  * The `body` handle must not be `null`; its type must be of the form
     * `(V T A...)V`, where `V` is non-`void`, or else `(T A...)void`.
     * (In the `void` case, we assign the type `void` to the name `V`,
     * and we will write `(V T A...)V` with the understanding that a `void` type `V`
     * is quietly dropped from the parameter list, leaving `(T A...)V`.)
     *  * The parameter list `(V T A...)` of the body contributes to a list
     * of types called the *internal parameter list*.
     * It will constrain the parameter lists of the other loop parts.
     *  * As a special case, if the body contributes only `V` and `T` types,
     * with no additional `A` types, then the internal parameter list is extended by
     * the argument types `A...` of the `iterator` handle; if it is `null` the
     * single type `Iterable` is added and constitutes the `A...` list.
     *  * If the iteration variable types `(V T)` are dropped from the internal parameter list, the resulting shorter
     * list `(A...)` is called the *external parameter list*.
     *  * The body return type `V`, if non-`void`, determines the type of an
     * additional state variable of the loop.
     * The body must both accept a leading parameter and return a value of this type `V`.
     *  * If `init` is non-`null`, it must have return type `V`.
     * Its parameter list (of some [form `(A*)`](MethodHandles.html#astar)) must be
     * [effectively identical](MethodHandles.html#effid)
     * to the external parameter list `(A...)`.
     *  * If `init` is `null`, the loop variable will be initialized to its
     * [default value][.empty].
     *  * If the `iterator` handle is non-`null`, it must have the return
     * type `java.util.Iterator` or a subtype thereof.
     * The iterator it produces when the loop is executed will be assumed
     * to yield values which can be converted to type `T`.
     *  * The parameter list of an `iterator` that is non-`null` (of some form `(A*)`) must be
     * effectively identical to the external parameter list `(A...)`.
     *  * If `iterator` is `null` it defaults to a method handle which behaves
     * like [java.lang.Iterable.iterator].  In that case, the internal parameter list
     * `(V T A...)` must have at least one `A` type, and the default iterator
     * handle parameter is adjusted to accept the leading `A` type, as if by
     * the [asType][MethodHandle.asType] conversion method.
     * The leading `A` type must be `Iterable` or a subtype thereof.
     * This conversion step, done at loop construction time, must not throw a `WrongMethodTypeException`.
     *
     *
     *
     * The type `T` may be either a primitive or reference.
     * Since type `Iterator<T>` is erased in the method handle representation to the raw type `Iterator`,
     * the `iteratedLoop` combinator adjusts the leading argument type for `body` to `Object`
     * as if by the [asType][MethodHandle.asType] conversion method.
     * Therefore, if an iterator of the wrong type appears as the loop is executed, runtime exceptions may occur
     * as the result of dynamic conversions performed by [MethodHandle.asType].
     *
     *
     * The resulting loop handle's result type and parameter signature are determined as follows:
     *  * The loop handle's result type is the result type `V` of the body.
     *  * The loop handle's parameter types are the types `(A...)`,
     * from the external parameter list.
     *
     *
     *
     * Here is pseudocode for the resulting loop handle. In the code, `V`/`v` represent the type / value of
     * the loop variable as well as the result type of the loop; `T`/`t`, that of the elements of the
     * structure the loop iterates over, and `A...`/`a...` represent arguments passed to the loop.
     * <blockquote><pre>`Iterator<T> iterator(A...);  // defaults to Iterable::iterator
     * V init(A...);
     * V body(V,T,A...);
     * V iteratedLoop(A... a...) {
     * Iterator<T> it = iterator(a...);
     * V v = init(a...);
     * while (it.hasNext()) {
     * T t = it.next();
     * v = body(v, t, a...);
     * }
     * return v;
     * }
    `</pre></blockquote> *
     *
     * @apiNote Example:
     * <blockquote><pre>`// get an iterator from a list
     * static List<String> reverseStep(List<String> r, String e) {
     * r.add(0, e);
     * return r;
     * }
     * static List<String> newArrayList() { return new ArrayList<>(); }
     * // assume MH_reverseStep and MH_newArrayList are handles to the above methods
     * MethodHandle loop = MethodHandles.iteratedLoop(null, MH_newArrayList, MH_reverseStep);
     * List<String> list = Arrays.asList("a", "b", "c", "d", "e");
     * List<String> reversedList = Arrays.asList("e", "d", "c", "b", "a");
     * assertEquals(reversedList, (List<String>) loop.invoke(list));
    `</pre></blockquote> *
     *
     * @apiNote The implementation of this method can be expressed approximately as follows:
     * <blockquote><pre>`MethodHandle iteratedLoop(MethodHandle iterator, MethodHandle init, MethodHandle body) {
     * // assume MH_next, MH_hasNext, MH_startIter are handles to methods of Iterator/Iterable
     * Class<?> returnType = body.type().returnType();
     * Class<?> ttype = body.type().parameterType(returnType == void.class ? 0 : 1);
     * MethodHandle nextVal = MH_next.asType(MH_next.type().changeReturnType(ttype));
     * MethodHandle retv = null, step = body, startIter = iterator;
     * if (returnType != void.class) {
     * // the simple thing first:  in (I V A...), drop the I to get V
     * retv = dropArguments(identity(returnType), 0, Iterator.class);
     * // body type signature (V T A...), internal loop types (I V A...)
     * step = swapArguments(body, 0, 1);  // swap V <-> T
     * }
     * if (startIter == null)  startIter = MH_getIter;
     * MethodHandle[]
     * iterVar    = { startIter, null, MH_hasNext, retv }, // it = iterator; while (it.hasNext())
     * bodyClause = { init, filterArguments(step, 0, nextVal) };  // v = body(v, t, a)
     * return loop(iterVar, bodyClause);
     * }
    `</pre></blockquote> *
     *
     * @param iterator an optional handle to return the iterator to start the loop.
     * If non-`null`, the handle must return [java.util.Iterator] or a subtype.
     * See above for other constraints.
     * @param init optional initializer, providing the initial value of the loop variable.
     * May be `null`, implying a default initial value.  See above for other constraints.
     * @param body body of the loop, which may not be `null`.
     * It controls the loop parameters and result type in the standard case (see above for details).
     * It must accept its own return type (if non-void) plus a `T` parameter (for the iterated values),
     * and may accept any number of additional types.
     * See above for other constraints.
     *
     * @return a method handle embodying the iteration loop functionality.
     * @throws NullPointerException if the `body` handle is `null`.
     * @throws IllegalArgumentException if any argument violates the above requirements.
     *
     * @since 9
     */
    fun iteratedLoop(
        iterator: java.lang.invoke.MethodHandle?,
        init: java.lang.invoke.MethodHandle,
        body: java.lang.invoke.MethodHandle
    ): java.lang.invoke.MethodHandle {
        val iterableType: java.lang.Class<*> =
            java.lang.invoke.MethodHandles.iteratedLoopChecks(iterator, init, body)
        val returnType: java.lang.Class<*> = body.type().returnType()
        // Android-changed: getConstantHandle is in MethodHandles.
        // MethodHandle hasNext = MethodHandleImpl.getConstantHandle(MethodHandleImpl.MH_iteratePred);
        // MethodHandle nextRaw = MethodHandleImpl.getConstantHandle(MethodHandleImpl.MH_iterateNext);
        val hasNext: java.lang.invoke.MethodHandle =
            java.lang.invoke.MethodHandles.getConstantHandle(java.lang.invoke.MethodHandles.MH_iteratePred)
        val nextRaw: java.lang.invoke.MethodHandle =
            java.lang.invoke.MethodHandles.getConstantHandle(java.lang.invoke.MethodHandles.MH_iterateNext)
        var startIter: java.lang.invoke.MethodHandle?
        var nextVal: java.lang.invoke.MethodHandle
        run {
            val iteratorType: java.lang.invoke.MethodType
            if (iterator == null) {
                // derive argument type from body, if available, else use Iterable
                // Android-changed: getConstantHandle is in MethodHandles.
                // startIter = MethodHandleImpl.getConstantHandle(MethodHandleImpl.MH_initIterator);
                startIter =
                    java.lang.invoke.MethodHandles.getConstantHandle(java.lang.invoke.MethodHandles.MH_initIterator)
                iteratorType = startIter.type().changeParameterType(0, iterableType)
            } else {
                // force return type to the internal iterator class
                iteratorType = iterator.type().changeReturnType(MutableIterator::class.java)
                startIter = iterator
            }
            val ttype: java.lang.Class<*> =
                body.type().parameterType(if (returnType == Void.TYPE) 0 else 1)
            val nextValType: java.lang.invoke.MethodType = nextRaw.type().changeReturnType(ttype)

            // perform the asType transforms under an exception transformer, as per spec.:
            try {
                startIter = startIter.asType(iteratorType)
                nextVal = nextRaw.asType(nextValType)
            } catch (ex: java.lang.invoke.WrongMethodTypeException) {
                throw java.lang.IllegalArgumentException(ex)
            }
        }
        var retv: java.lang.invoke.MethodHandle? = null
        var step: java.lang.invoke.MethodHandle = body
        if (returnType != Void.TYPE) {
            // the simple thing first:  in (I V A...), drop the I to get V
            retv = java.lang.invoke.MethodHandles.dropArguments(
                java.lang.invoke.MethodHandles.identity(returnType), 0,
                MutableIterator::class.java
            )
            // body type signature (V T A...), internal loop types (I V A...)
            step = java.lang.invoke.MethodHandles.swapArguments(body, 0, 1) // swap V <-> T
        }
        val iterVar: Array<java.lang.invoke.MethodHandle?> = arrayOf(startIter, null, hasNext, retv)
        val bodyClause: Array<java.lang.invoke.MethodHandle> =
            arrayOf<java.lang.invoke.MethodHandle>(
                init,
                java.lang.invoke.MethodHandles.filterArgument(step, 0, nextVal)
            )
        return java.lang.invoke.MethodHandles.loop(iterVar, bodyClause)
    }

    private fun iteratedLoopChecks(
        iterator: java.lang.invoke.MethodHandle?,
        init: java.lang.invoke.MethodHandle?,
        body: java.lang.invoke.MethodHandle
    ): java.lang.Class<*>? {
        java.util.Objects.requireNonNull<java.lang.invoke.MethodHandle>(body)
        val bodyType: java.lang.invoke.MethodType = body.type()
        val returnType: java.lang.Class<*> = bodyType.returnType()
        val internalParamList: List<java.lang.Class<*>> = bodyType.parameterList()
        // strip leading V value if present
        val vsize = (if (returnType == Void.TYPE) 0 else 1)
        if (vsize != 0 && (internalParamList.size == 0 || internalParamList[0] != returnType)) {
            // argument list has no "V" => error
            val expected: java.lang.invoke.MethodType = bodyType.insertParameterTypes(0, returnType)
            throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
                "body function",
                bodyType,
                expected
            )
        } else if (internalParamList.size <= vsize) {
            // missing T type => error
            val expected: java.lang.invoke.MethodType =
                bodyType.insertParameterTypes(vsize, Any::class.java)
            throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
                "body function",
                bodyType,
                expected
            )
        }
        var externalParamList: List<java.lang.Class<*>> =
            internalParamList.subList(vsize + 1, internalParamList.size)
        var iterableType: java.lang.Class<*>? = null
        if (iterator != null) {
            // special case; if the body handle only declares V and T then
            // the external parameter list is obtained from iterator handle
            if (externalParamList.isEmpty()) {
                externalParamList = iterator.type().parameterList()
            }
            val itype: java.lang.invoke.MethodType = iterator.type()
            if (!MutableIterator::class.java.isAssignableFrom(itype.returnType())) {
                throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException("iteratedLoop first argument must have Iterator return type")
            }
            if (!itype.effectivelyIdenticalParameters(0, externalParamList)) {
                val expected: java.lang.invoke.MethodType =
                    java.lang.invoke.MethodType.methodType(itype.returnType(), externalParamList)
                throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
                    "iterator parameters",
                    itype,
                    expected
                )
            }
        } else {
            if (externalParamList.isEmpty()) {
                // special case; if the iterator handle is null and the body handle
                // only declares V and T then the external parameter list consists
                // of Iterable
                externalParamList =
                    java.util.Arrays.asList<java.lang.Class<*>>(Iterable::class.java)
                iterableType = Iterable::class.java
            } else {
                // special case; if the iterator handle is null and the external
                // parameter list is not empty then the first parameter must be
                // assignable to Iterable
                iterableType = externalParamList[0]
                if (!Iterable::class.java.isAssignableFrom(iterableType)) {
                    throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
                        "inferred first loop argument must inherit from Iterable: $iterableType"
                    )
                }
            }
        }
        if (init != null) {
            val initType: java.lang.invoke.MethodType = init.type()
            if (initType.returnType() != returnType ||
                !initType.effectivelyIdenticalParameters(0, externalParamList)
            ) {
                throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
                    "loop initializer",
                    initType,
                    java.lang.invoke.MethodType.methodType(returnType, externalParamList)
                )
            }
        }
        return iterableType // help the caller a bit
    }

    /*non-public*/
    fun swapArguments(
        mh: java.lang.invoke.MethodHandle,
        i: Int,
        j: Int
    ): java.lang.invoke.MethodHandle {
        // there should be a better way to uncross my wires
        val arity: Int = mh.type().parameterCount()
        val order = IntArray(arity)
        for (k in 0 until arity) order[k] = k
        order[i] = j
        order[j] = i
        val types: Array<java.lang.Class<*>> = mh.type().parameterArray()
        val ti: java.lang.Class<*> = types[i]
        types[i] = types[j]
        types[j] = ti
        val swapType: java.lang.invoke.MethodType =
            java.lang.invoke.MethodType.methodType(mh.type().returnType(), types)
        return java.lang.invoke.MethodHandles.permuteArguments(mh, swapType, *order)
    }

    /**
     * Makes a method handle that adapts a `target` method handle by wrapping it in a `try-finally` block.
     * Another method handle, `cleanup`, represents the functionality of the `finally` block. Any exception
     * thrown during the execution of the `target` handle will be passed to the `cleanup` handle. The
     * exception will be rethrown, unless `cleanup` handle throws an exception first.  The
     * value returned from the `cleanup` handle's execution will be the result of the execution of the
     * `try-finally` handle.
     *
     *
     * The `cleanup` handle will be passed one or two additional leading arguments.
     * The first is the exception thrown during the
     * execution of the `target` handle, or `null` if no exception was thrown.
     * The second is the result of the execution of the `target` handle, or, if it throws an exception,
     * a `null`, zero, or `false` value of the required type is supplied as a placeholder.
     * The second argument is not present if the `target` handle has a `void` return type.
     * (Note that, except for argument type conversions, combinators represent `void` values in parameter lists
     * by omitting the corresponding paradoxical arguments, not by inserting `null` or zero values.)
     *
     *
     * The `target` and `cleanup` handles must have the same corresponding argument and return types, except
     * that the `cleanup` handle may omit trailing arguments. Also, the `cleanup` handle must have one or
     * two extra leading parameters:
     *  * a `Throwable`, which will carry the exception thrown by the `target` handle (if any); and
     *  * a parameter of the same type as the return type of both `target` and `cleanup`, which will carry
     * the result from the execution of the `target` handle.
     * This parameter is not present if the `target` returns `void`.
     *
     *
     *
     * The pseudocode for the resulting adapter looks as follows. In the code, `V` represents the result type of
     * the `try/finally` construct; `A`/`a`, the types and values of arguments to the resulting
     * handle consumed by the cleanup; and `B`/`b`, those of arguments to the resulting handle discarded by
     * the cleanup.
     * <blockquote><pre>`V target(A..., B...);
     * V cleanup(Throwable, V, A...);
     * V adapter(A... a, B... b) {
     * V result = (zero value for V);
     * Throwable throwable = null;
     * try {
     * result = target(a..., b...);
     * } catch (Throwable t) {
     * throwable = t;
     * throw t;
     * } finally {
     * result = cleanup(throwable, result, a...);
     * }
     * return result;
     * }
    `</pre></blockquote> *
     *
     *
     * Note that the saved arguments (`a...` in the pseudocode) cannot
     * be modified by execution of the target, and so are passed unchanged
     * from the caller to the cleanup, if it is invoked.
     *
     *
     * The target and cleanup must return the same type, even if the cleanup
     * always throws.
     * To create such a throwing cleanup, compose the cleanup logic
     * with [throwException][.throwException],
     * in order to create a method handle of the correct return type.
     *
     *
     * Note that `tryFinally` never converts exceptions into normal returns.
     * In rare cases where exceptions must be converted in that way, first wrap
     * the target with [.catchException]
     * to capture an outgoing exception, and then wrap with `tryFinally`.
     *
     *
     * It is recommended that the first parameter type of `cleanup` be
     * declared `Throwable` rather than a narrower subtype.  This ensures
     * `cleanup` will always be invoked with whatever exception that
     * `target` throws.  Declaring a narrower type may result in a
     * `ClassCastException` being thrown by the `try-finally`
     * handle if the type of the exception thrown by `target` is not
     * assignable to the first parameter type of `cleanup`.  Note that
     * various exception types of `VirtualMachineError`,
     * `LinkageError`, and `RuntimeException` can in principle be
     * thrown by almost any kind of Java code, and a finally clause that
     * catches (say) only `IOException` would mask any of the others
     * behind a `ClassCastException`.
     *
     * @param target the handle whose execution is to be wrapped in a `try` block.
     * @param cleanup the handle that is invoked in the finally block.
     *
     * @return a method handle embodying the `try-finally` block composed of the two arguments.
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if `cleanup` does not accept
     * the required leading arguments, or if the method handle types do
     * not match in their return types and their
     * corresponding trailing parameters
     *
     * @see MethodHandles.catchException
     * @since 9
     */
    fun tryFinally(
        target: java.lang.invoke.MethodHandle,
        cleanup: java.lang.invoke.MethodHandle
    ): java.lang.invoke.MethodHandle {
        var cleanup: java.lang.invoke.MethodHandle = cleanup
        val targetParamTypes: List<java.lang.Class<*>> = target.type().parameterList()
        val rtype: java.lang.Class<*> = target.type().returnType()
        java.lang.invoke.MethodHandles.tryFinallyChecks(target, cleanup)

        // Match parameter lists: if the cleanup has a shorter parameter list than the target, add ignored arguments.
        // The cleanup parameter list (minus the leading Throwable and result parameters) must be a sublist of the
        // target parameter list.
        cleanup = java.lang.invoke.MethodHandles.dropArgumentsToMatch(
            cleanup,
            (if (rtype == Void.TYPE) 1 else 2),
            targetParamTypes,
            0
        )

        // Ensure that the intrinsic type checks the instance thrown by the
        // target against the first parameter of cleanup
        cleanup = cleanup.asType(cleanup.type().changeParameterType(0, Throwable::class.java))

        // Use asFixedArity() to avoid unnecessary boxing of last argument for VarargsCollector case.
        // Android-changed: use Transformer implementation.
        // return MethodHandleImpl.makeTryFinally(target.asFixedArity(), cleanup.asFixedArity(), rtype, targetParamTypes);
        return TryFinally(target.asFixedArity(), cleanup.asFixedArity())
    }

    private fun tryFinallyChecks(
        target: java.lang.invoke.MethodHandle,
        cleanup: java.lang.invoke.MethodHandle
    ) {
        val rtype: java.lang.Class<*> = target.type().returnType()
        if (rtype != cleanup.type().returnType()) {
            throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.Class<out Any>>(
                "target and return types",
                cleanup.type().returnType(),
                rtype
            )
        }
        val cleanupType: java.lang.invoke.MethodType = cleanup.type()
        if (!Throwable::class.java.isAssignableFrom(cleanupType.parameterType(0))) {
            throw java.lang.invoke.MethodHandles.misMatchedTypes(
                "cleanup first argument and Throwable", cleanup.type(),
                Throwable::class.java
            )
        }
        if (rtype != Void.TYPE && cleanupType.parameterType(1) != rtype) {
            throw java.lang.invoke.MethodHandles.misMatchedTypes(
                "cleanup second argument and target return type",
                cleanup.type(),
                rtype
            )
        }
        // The cleanup parameter list (minus the leading Throwable and result parameters) must be a sublist of the
        // target parameter list.
        val cleanupArgIndex = if (rtype == Void.TYPE) 1 else 2
        if (!cleanupType.effectivelyIdenticalParameters(
                cleanupArgIndex,
                target.type().parameterList()
            )
        ) {
            throw java.lang.invoke.MethodHandles.misMatchedTypes<java.lang.invoke.MethodType>(
                "cleanup parameters after (Throwable,result) and target parameter list prefix",
                cleanup.type(), target.type()
            )
        }
    }

    /**
     * Creates a table switch method handle, which can be used to switch over a set of target
     * method handles, based on a given target index, called selector.
     *
     *
     * For a selector value of `n`, where `n` falls in the range `[0, N)`,
     * and where `N` is the number of target method handles, the table switch method
     * handle will invoke the n-th target method handle from the list of target method handles.
     *
     *
     * For a selector value that does not fall in the range `[0, N)`, the table switch
     * method handle will invoke the given fallback method handle.
     *
     *
     * All method handles passed to this method must have the same type, with the additional
     * requirement that the leading parameter be of type `int`. The leading parameter
     * represents the selector.
     *
     *
     * Any trailing parameters present in the type will appear on the returned table switch
     * method handle as well. Any arguments assigned to these parameters will be forwarded,
     * together with the selector value, to the selected method handle when invoking it.
     *
     * @apiNote Example:
     * The cases each drop the `selector` value they are given, and take an additional
     * `String` argument, which is concatenated (using [String.concat])
     * to a specific constant label string for each case:
     * <blockquote><pre>`MethodHandles.Lookup lookup = MethodHandles.lookup();
     * MethodHandle caseMh = lookup.findVirtual(String.class, "concat",
     * MethodType.methodType(String.class, String.class));
     * caseMh = MethodHandles.dropArguments(caseMh, 0, int.class);
     *
     * MethodHandle caseDefault = MethodHandles.insertArguments(caseMh, 1, "default: ");
     * MethodHandle case0 = MethodHandles.insertArguments(caseMh, 1, "case 0: ");
     * MethodHandle case1 = MethodHandles.insertArguments(caseMh, 1, "case 1: ");
     *
     * MethodHandle mhSwitch = MethodHandles.tableSwitch(
     * caseDefault,
     * case0,
     * case1
     * );
     *
     * assertEquals("default: data", (String) mhSwitch.invokeExact(-1, "data"));
     * assertEquals("case 0: data", (String) mhSwitch.invokeExact(0, "data"));
     * assertEquals("case 1: data", (String) mhSwitch.invokeExact(1, "data"));
     * assertEquals("default: data", (String) mhSwitch.invokeExact(2, "data"));
    `</pre></blockquote> *
     *
     * @param fallback the fallback method handle that is called when the selector is not
     * within the range `[0, N)`.
     * @param targets array of target method handles.
     * @return the table switch method handle.
     * @throws NullPointerException if `fallback`, the `targets` array, or any
     * any of the elements of the `targets` array are
     * `null`.
     * @throws IllegalArgumentException if the `targets` array is empty, if the leading
     * parameter of the fallback handle or any of the target
     * handles is not `int`, or if the types of
     * the fallback handle and all of target handles are
     * not the same.
     */
    fun tableSwitch(
        fallback: java.lang.invoke.MethodHandle?,
        vararg targets: java.lang.invoke.MethodHandle?
    ): java.lang.invoke.MethodHandle {
        var targets: Array<out java.lang.invoke.MethodHandle?> = targets
        java.util.Objects.requireNonNull<java.lang.invoke.MethodHandle>(fallback)
        java.util.Objects.requireNonNull<Array<java.lang.invoke.MethodHandle>>(targets)
        targets = targets.clone()
        val type: java.lang.invoke.MethodType =
            java.lang.invoke.MethodHandles.tableSwitchChecks(fallback, targets)
        // Android-changed: use a Transformer for the implementation.
        // return MethodHandleImpl.makeTableSwitch(type, fallback, targets);
        return TableSwitch(type, fallback, targets)
    }

    private fun tableSwitchChecks(
        defaultCase: java.lang.invoke.MethodHandle,
        caseActions: Array<java.lang.invoke.MethodHandle>
    ): java.lang.invoke.MethodType {
        if (caseActions.size == 0) throw java.lang.IllegalArgumentException("Not enough cases: " + caseActions.contentToString())
        val expectedType: java.lang.invoke.MethodType = defaultCase.type()
        if (expectedType.parameterCount() < 1 || expectedType.parameterType(0) != Int::class.javaPrimitiveType) throw java.lang.IllegalArgumentException(
            "Case actions must have int as leading parameter: " + caseActions.contentToString()
        )
        for (mh: java.lang.invoke.MethodHandle in caseActions) {
            java.util.Objects.requireNonNull<java.lang.invoke.MethodHandle>(mh)
            // Android-changed: MethodType's not interned.
            // if (mh.type() != expectedType)
            if (mh.type() != expectedType) throw java.lang.IllegalArgumentException(
                "Case actions must have the same type: " + caseActions.contentToString()
            )
        }
        return expectedType
    }
    // BEGIN Android-added: Code from OpenJDK's MethodHandleImpl.
    /**
     * This method is bound as the predicate in [counting loops][MethodHandles.countedLoop].
     *
     * @param limit the upper bound of the parameter, statically bound at loop creation time.
     * @param counter the counter parameter, passed in during loop execution.
     *
     * @return whether the counter has reached the limit.
     * @hide
     */
    fun countedLoopPredicate(limit: Int, counter: Int): Boolean {
        return counter < limit
    }

    /**
     * This method is bound as the step function in [counting loops][MethodHandles.countedLoop] to increment the counter.
     *
     * @param limit the upper bound of the loop counter (ignored).
     * @param counter the loop counter.
     *
     * @return the loop counter incremented by 1.
     * @hide
     */
    fun countedLoopStep(limit: Int, counter: Int): Int {
        return counter + 1
    }

    /**
     * This is bound to initialize the loop-local iterator in [iterating loops][MethodHandles.iteratedLoop].
     *
     * @param it the [Iterable] over which the loop iterates.
     *
     * @return an [Iterator] over the argument's elements.
     * @hide
     */
    fun initIterator(it: Iterable<*>): Iterator<*> {
        return it.iterator()
    }

    /**
     * This method is bound as the predicate in [iterating loops][MethodHandles.iteratedLoop].
     *
     * @param it the iterator to be checked.
     *
     * @return `true` iff there are more elements to iterate over.
     * @hide
     */
    fun iteratePredicate(it: Iterator<*>): Boolean {
        return it.hasNext()
    }

    /**
     * This method is bound as the step for retrieving the current value from the iterator in [ ][MethodHandles.iteratedLoop].
     *
     * @param it the iterator.
     *
     * @return the next element from the iterator.
     * @hide
     */
    fun iterateNext(it: Iterator<*>): Any {
        return (it.next())!!
    }

    // Indexes into constant method handles:
    val MH_cast = 0
    val MH_selectAlternative = 1
    val MH_copyAsPrimitiveArray = 2
    val MH_fillNewTypedArray = 3
    val MH_fillNewArray = 4
    val MH_arrayIdentity = 5
    val MH_countedLoopPred = 6
    val MH_countedLoopStep = 7
    val MH_initIterator = 8
    val MH_iteratePred = 9
    val MH_iterateNext = 10
    val MH_Array_newInstance = 11
    val MH_LIMIT = 12
    fun getConstantHandle(idx: Int): java.lang.invoke.MethodHandle {
        val handle: java.lang.invoke.MethodHandle = java.lang.invoke.MethodHandles.HANDLES.get(idx)
        return if (handle != null) {
            handle
        } else java.lang.invoke.MethodHandles.setCachedHandle(
            idx,
            java.lang.invoke.MethodHandles.makeConstantHandle(idx)
        )
    }

    @Synchronized
    private fun setCachedHandle(
        idx: Int,
        method: java.lang.invoke.MethodHandle
    ): java.lang.invoke.MethodHandle {
        // Simulate a CAS, to avoid racy duplication of results.
        val prev: java.lang.invoke.MethodHandle = java.lang.invoke.MethodHandles.HANDLES.get(idx)
        if (prev != null) {
            return prev
        }
        java.lang.invoke.MethodHandles.HANDLES.get(idx) = method
        return method
    }

    // Local constant method handles:
    @Stable
    private val HANDLES: Array<java.lang.invoke.MethodHandle?> =
        arrayOfNulls<java.lang.invoke.MethodHandle>(java.lang.invoke.MethodHandles.MH_LIMIT)

    private fun makeConstantHandle(idx: Int): java.lang.invoke.MethodHandle {
        try {
            // Android-added: local IMPL_LOOKUP.
            val IMPL_LOOKUP: java.lang.invoke.MethodHandles.Lookup =
                java.lang.invoke.MethodHandles.Lookup.Companion.IMPL_LOOKUP
            when (idx) {
                java.lang.invoke.MethodHandles.MH_countedLoopPred ->                     // Android-changed: methods moved to this file.
                    // return IMPL_LOOKUP.findStatic(MethodHandleImpl.class, "countedLoopPredicate",
                    //         MethodType.methodType(boolean.class, int.class, int.class));
                    return IMPL_LOOKUP.findStatic(
                        MethodHandles::class.java, "countedLoopPredicate",
                        java.lang.invoke.MethodType.methodType(
                            Boolean::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType
                        )
                    )

                java.lang.invoke.MethodHandles.MH_countedLoopStep ->                     // Android-changed: methods moved to this file.
                    // return IMPL_LOOKUP.findStatic(MethodHandleImpl.class, "countedLoopStep",
                    //         MethodType.methodType(int.class, int.class, int.class));
                    return IMPL_LOOKUP.findStatic(
                        MethodHandles::class.java, "countedLoopStep",
                        java.lang.invoke.MethodType.methodType(
                            Int::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType
                        )
                    )

                java.lang.invoke.MethodHandles.MH_initIterator ->                     // Android-changed: methods moved to this file.
                    // return IMPL_LOOKUP.findStatic(MethodHandleImpl.class, "initIterator",
                    //         MethodType.methodType(Iterator.class, Iterable.class));
                    return IMPL_LOOKUP.findStatic(
                        MethodHandles::class.java, "initIterator",
                        java.lang.invoke.MethodType.methodType(
                            MutableIterator::class.java,
                            Iterable::class.java
                        )
                    )

                java.lang.invoke.MethodHandles.MH_iteratePred ->                     // Android-changed: methods moved to this file.
                    // return IMPL_LOOKUP.findStatic(MethodHandleImpl.class, "iteratePredicate",
                    //         MethodType.methodType(boolean.class, Iterator.class));
                    return IMPL_LOOKUP.findStatic(
                        MethodHandles::class.java, "iteratePredicate",
                        java.lang.invoke.MethodType.methodType(
                            Boolean::class.javaPrimitiveType,
                            MutableIterator::class.java
                        )
                    )

                java.lang.invoke.MethodHandles.MH_iterateNext ->                     // Android-changed: methods moved to this file.
                    // return IMPL_LOOKUP.findStatic(MethodHandleImpl.class, "iterateNext",
                    //         MethodType.methodType(Object.class, Iterator.class));
                    return IMPL_LOOKUP.findStatic(
                        MethodHandles::class.java, "iterateNext",
                        java.lang.invoke.MethodType.methodType(
                            Any::class.java,
                            MutableIterator::class.java
                        )
                    )
            }
        } catch (ex: java.lang.ReflectiveOperationException) {
            throw java.lang.invoke.MethodHandleStatics.newInternalError(ex)
        }
        throw java.lang.invoke.MethodHandleStatics.newInternalError("Unknown function index: $idx")
    } // END Android-added: Code from OpenJDK's MethodHandleImpl.

    /**
     * A *lookup object* is a factory for creating method handles,
     * when the creation requires access checking.
     * Method handles do not perform
     * access checks when they are called, but rather when they are created.
     * Therefore, method handle access
     * restrictions must be enforced when a method handle is created.
     * The caller class against which those restrictions are enforced
     * is known as the [lookup class][.lookupClass].
     *
     *
     * A lookup class which needs to create method handles will call
     * [MethodHandles.lookup][.lookup] to create a factory for itself.
     * When the `Lookup` factory object is created, the identity of the lookup class is
     * determined, and securely stored in the `Lookup` object.
     * The lookup class (or its delegates) may then use factory methods
     * on the `Lookup` object to create method handles for access-checked members.
     * This includes all methods, constructors, and fields which are allowed to the lookup class,
     * even private ones.
     *
     * <h1><a name="lookups"></a>Lookup Factory Methods</h1>
     * The factory methods on a `Lookup` object correspond to all major
     * use cases for methods, constructors, and fields.
     * Each method handle created by a factory method is the functional
     * equivalent of a particular *bytecode behavior*.
     * (Bytecode behaviors are described in section 5.4.3.5 of the Java Virtual Machine Specification.)
     * Here is a summary of the correspondence between these factory methods and
     * the behavior the resulting method handles:
     * <table border=1 cellpadding=5 summary="lookup method behaviors">
     * <tr>
     * <th><a name="equiv"></a>lookup expression</th>
     * <th>member</th>
     * <th>bytecode behavior</th>
    </tr> *
     * <tr>
     * <td>[lookup.findGetter(C.class,&quot;f&quot;,FT.class)][java.lang.invoke.MethodHandles.Lookup.findGetter]</td>
     * <td>`FT f;`</td><td>`(T) this.f;`</td>
    </tr> *
     * <tr>
     * <td>[lookup.findStaticGetter(C.class,&quot;f&quot;,FT.class)][java.lang.invoke.MethodHandles.Lookup.findStaticGetter]</td>
     * <td>`static`<br></br>`FT f;`</td><td>`(T) C.f;`</td>
    </tr> *
     * <tr>
     * <td>[lookup.findSetter(C.class,&quot;f&quot;,FT.class)][java.lang.invoke.MethodHandles.Lookup.findSetter]</td>
     * <td>`FT f;`</td><td>`this.f = x;`</td>
    </tr> *
     * <tr>
     * <td>[lookup.findStaticSetter(C.class,&quot;f&quot;,FT.class)][java.lang.invoke.MethodHandles.Lookup.findStaticSetter]</td>
     * <td>`static`<br></br>`FT f;`</td><td>`C.f = arg;`</td>
    </tr> *
     * <tr>
     * <td>[lookup.findVirtual(C.class,&quot;m&quot;,MT)][java.lang.invoke.MethodHandles.Lookup.findVirtual]</td>
     * <td>`T m(A*);`</td><td>`(T) this.m(arg*);`</td>
    </tr> *
     * <tr>
     * <td>[lookup.findStatic(C.class,&quot;m&quot;,MT)][java.lang.invoke.MethodHandles.Lookup.findStatic]</td>
     * <td>`static`<br></br>`T m(A*);`</td><td>`(T) C.m(arg*);`</td>
    </tr> *
     * <tr>
     * <td>[lookup.findSpecial(C.class,&quot;m&quot;,MT,this.class)][java.lang.invoke.MethodHandles.Lookup.findSpecial]</td>
     * <td>`T m(A*);`</td><td>`(T) super.m(arg*);`</td>
    </tr> *
     * <tr>
     * <td>[lookup.findConstructor(C.class,MT)][java.lang.invoke.MethodHandles.Lookup.findConstructor]</td>
     * <td>`C(A*);`</td><td>`new C(arg*);`</td>
    </tr> *
     * <tr>
     * <td>[lookup.unreflectGetter(aField)][java.lang.invoke.MethodHandles.Lookup.unreflectGetter]</td>
     * <td>(`static`)?<br></br>`FT f;`</td><td>`(FT) aField.get(thisOrNull);`</td>
    </tr> *
     * <tr>
     * <td>[lookup.unreflectSetter(aField)][java.lang.invoke.MethodHandles.Lookup.unreflectSetter]</td>
     * <td>(`static`)?<br></br>`FT f;`</td><td>`aField.set(thisOrNull, arg);`</td>
    </tr> *
     * <tr>
     * <td>[lookup.unreflect(aMethod)][java.lang.invoke.MethodHandles.Lookup.unreflect]</td>
     * <td>(`static`)?<br></br>`T m(A*);`</td><td>`(T) aMethod.invoke(thisOrNull, arg*);`</td>
    </tr> *
     * <tr>
     * <td>[lookup.unreflectConstructor(aConstructor)][java.lang.invoke.MethodHandles.Lookup.unreflectConstructor]</td>
     * <td>`C(A*);`</td><td>`(C) aConstructor.newInstance(arg*);`</td>
    </tr> *
     * <tr>
     * <td>[lookup.unreflect(aMethod)][java.lang.invoke.MethodHandles.Lookup.unreflect]</td>
     * <td>(`static`)?<br></br>`T m(A*);`</td><td>`(T) aMethod.invoke(thisOrNull, arg*);`</td>
    </tr> *
    </table> *
     *
     * Here, the type `C` is the class or interface being searched for a member,
     * documented as a parameter named `refc` in the lookup methods.
     * The method type `MT` is composed from the return type `T`
     * and the sequence of argument types `A*`.
     * The constructor also has a sequence of argument types `A*` and
     * is deemed to return the newly-created object of type `C`.
     * Both `MT` and the field type `FT` are documented as a parameter named `type`.
     * The formal parameter `this` stands for the self-reference of type `C`;
     * if it is present, it is always the leading argument to the method handle invocation.
     * (In the case of some `protected` members, `this` may be
     * restricted in type to the lookup class; see below.)
     * The name `arg` stands for all the other method handle arguments.
     * In the code examples for the Core Reflection API, the name `thisOrNull`
     * stands for a null reference if the accessed method or field is static,
     * and `this` otherwise.
     * The names `aMethod`, `aField`, and `aConstructor` stand
     * for reflective objects corresponding to the given members.
     *
     *
     * In cases where the given member is of variable arity (i.e., a method or constructor)
     * the returned method handle will also be of [variable arity][MethodHandle.asVarargsCollector].
     * In all other cases, the returned method handle will be of fixed arity.
     *
     *
     * *Discussion:*
     * The equivalence between looked-up method handles and underlying
     * class members and bytecode behaviors
     * can break down in a few ways:
     *
     *  * If `C` is not symbolically accessible from the lookup class's loader,
     * the lookup can still succeed, even when there is no equivalent
     * Java expression or bytecoded constant.
     *  * Likewise, if `T` or `MT`
     * is not symbolically accessible from the lookup class's loader,
     * the lookup can still succeed.
     * For example, lookups for `MethodHandle.invokeExact` and
     * `MethodHandle.invoke` will always succeed, regardless of requested type.
     *  * If there is a security manager installed, it can forbid the lookup
     * on various grounds ([see below](MethodHandles.Lookup.html#secmgr)).
     * By contrast, the `ldc` instruction on a `CONSTANT_MethodHandle`
     * constant is not subject to security manager checks.
     *  * If the looked-up method has a
     * [very large arity](MethodHandle.html#maxarity),
     * the method handle creation may fail, due to the method handle
     * type having too many parameters.
     *
     *
     * <h1><a name="access"></a>Access checking</h1>
     * Access checks are applied in the factory methods of `Lookup`,
     * when a method handle is created.
     * This is a key difference from the Core Reflection API, since
     * [java.lang.reflect.Method.invoke]
     * performs access checking against every caller, on every call.
     *
     *
     * All access checks start from a `Lookup` object, which
     * compares its recorded lookup class against all requests to
     * create method handles.
     * A single `Lookup` object can be used to create any number
     * of access-checked method handles, all checked against a single
     * lookup class.
     *
     *
     * A `Lookup` object can be shared with other trusted code,
     * such as a metaobject protocol.
     * A shared `Lookup` object delegates the capability
     * to create method handles on private members of the lookup class.
     * Even if privileged code uses the `Lookup` object,
     * the access checking is confined to the privileges of the
     * original lookup class.
     *
     *
     * A lookup can fail, because
     * the containing class is not accessible to the lookup class, or
     * because the desired class member is missing, or because the
     * desired class member is not accessible to the lookup class, or
     * because the lookup object is not trusted enough to access the member.
     * In any of these cases, a `ReflectiveOperationException` will be
     * thrown from the attempted lookup.  The exact class will be one of
     * the following:
     *
     *  * NoSuchMethodException  if a method is requested but does not exist
     *  * NoSuchFieldException  if a field is requested but does not exist
     *  * IllegalAccessException  if the member exists but an access check fails
     *
     *
     *
     * In general, the conditions under which a method handle may be
     * looked up for a method `M` are no more restrictive than the conditions
     * under which the lookup class could have compiled, verified, and resolved a call to `M`.
     * Where the JVM would raise exceptions like `NoSuchMethodError`,
     * a method handle lookup will generally raise a corresponding
     * checked exception, such as `NoSuchMethodException`.
     * And the effect of invoking the method handle resulting from the lookup
     * is [exactly equivalent](MethodHandles.Lookup.html#equiv)
     * to executing the compiled, verified, and resolved call to `M`.
     * The same point is true of fields and constructors.
     *
     *
     * *Discussion:*
     * Access checks only apply to named and reflected methods,
     * constructors, and fields.
     * Other method handle creation methods, such as
     * [MethodHandle.asType],
     * do not require any access checks, and are used
     * independently of any `Lookup` object.
     *
     *
     * If the desired member is `protected`, the usual JVM rules apply,
     * including the requirement that the lookup class must be either be in the
     * same package as the desired member, or must inherit that member.
     * (See the Java Virtual Machine Specification, sections 4.9.2, 5.4.3.5, and 6.4.)
     * In addition, if the desired member is a non-static field or method
     * in a different package, the resulting method handle may only be applied
     * to objects of the lookup class or one of its subclasses.
     * This requirement is enforced by narrowing the type of the leading
     * `this` parameter from `C`
     * (which will necessarily be a superclass of the lookup class)
     * to the lookup class itself.
     *
     *
     * The JVM imposes a similar requirement on `invokespecial` instruction,
     * that the receiver argument must match both the resolved method *and*
     * the current class.  Again, this requirement is enforced by narrowing the
     * type of the leading parameter to the resulting method handle.
     * (See the Java Virtual Machine Specification, section 4.10.1.9.)
     *
     *
     * The JVM represents constructors and static initializer blocks as internal methods
     * with special names (`"<init>"` and `"<clinit>"`).
     * The internal syntax of invocation instructions allows them to refer to such internal
     * methods as if they were normal methods, but the JVM bytecode verifier rejects them.
     * A lookup of such an internal method will produce a `NoSuchMethodException`.
     *
     *
     * In some cases, access between nested classes is obtained by the Java compiler by creating
     * an wrapper method to access a private method of another class
     * in the same top-level declaration.
     * For example, a nested class `C.D`
     * can access private members within other related classes such as
     * `C`, `C.D.E`, or `C.B`,
     * but the Java compiler may need to generate wrapper methods in
     * those related classes.  In such cases, a `Lookup` object on
     * `C.E` would be unable to those private members.
     * A workaround for this limitation is the [Lookup. in] method,
     * which can transform a lookup on `C.E` into one on any of those other
     * classes, without special elevation of privilege.
     *
     *
     * The accesses permitted to a given lookup object may be limited,
     * according to its set of [lookupModes][.lookupModes],
     * to a subset of members normally accessible to the lookup class.
     * For example, the [publicLookup][.publicLookup]
     * method produces a lookup object which is only allowed to access
     * public members in public classes.
     * The caller sensitive method [lookup][.lookup]
     * produces a lookup object with full capabilities relative to
     * its caller class, to emulate all supported bytecode behaviors.
     * Also, the [Lookup. in] method may produce a lookup object
     * with fewer access modes than the original lookup object.
     *
     *
     *
     * <a name="privacc"></a>
     * *Discussion of private access:*
     * We say that a lookup has *private access*
     * if its [lookup modes][.lookupModes]
     * include the possibility of accessing `private` members.
     * As documented in the relevant methods elsewhere,
     * only lookups with private access possess the following capabilities:
     *
     *  * access private fields, methods, and constructors of the lookup class
     *  * create method handles which invoke [caller sensitive](MethodHandles.Lookup.html#callsens) methods,
     * such as `Class.forName`
     *  * create method handles which [emulate invokespecial][Lookup.findSpecial] instructions
     *  * avoid [package access checks](MethodHandles.Lookup.html#secmgr)
     * for classes accessible to the lookup class
     *  * create [delegated lookup objects][Lookup. in] which have private access to other classes
     * within the same package member
     *
     *
     *
     * Each of these permissions is a consequence of the fact that a lookup object
     * with private access can be securely traced back to an originating class,
     * whose [bytecode behaviors](MethodHandles.Lookup.html#equiv) and Java language access permissions
     * can be reliably determined and emulated by method handles.
     *
     * <h1><a name="secmgr"></a>Security manager interactions</h1>
     * Although bytecode instructions can only refer to classes in
     * a related class loader, this API can search for methods in any
     * class, as long as a reference to its `Class` object is
     * available.  Such cross-loader references are also possible with the
     * Core Reflection API, and are impossible to bytecode instructions
     * such as `invokestatic` or `getfield`.
     * There is a [security manager API][java.lang.SecurityManager]
     * to allow applications to check such cross-loader references.
     * These checks apply to both the `MethodHandles.Lookup` API
     * and the Core Reflection API
     * (as found on [Class][java.lang.Class]).
     *
     *
     * If a security manager is present, member lookups are subject to
     * additional checks.
     * From one to three calls are made to the security manager.
     * Any of these calls can refuse access by throwing a
     * [SecurityException][java.lang.SecurityException].
     * Define `smgr` as the security manager,
     * `lookc` as the lookup class of the current lookup object,
     * `refc` as the containing class in which the member
     * is being sought, and `defc` as the class in which the
     * member is actually defined.
     * The value `lookc` is defined as *not present*
     * if the current lookup object does not have
     * [private access](MethodHandles.Lookup.html#privacc).
     * The calls are made according to the following rules:
     *
     *  * **Step 1:**
     * If `lookc` is not present, or if its class loader is not
     * the same as or an ancestor of the class loader of `refc`,
     * then [     smgr.checkPackageAccess(refcPkg)][SecurityManager.checkPackageAccess] is called,
     * where `refcPkg` is the package of `refc`.
     *  * **Step 2:**
     * If the retrieved member is not public and
     * `lookc` is not present, then
     * [smgr.checkPermission][SecurityManager.checkPermission]
     * with `RuntimePermission("accessDeclaredMembers")` is called.
     *  * **Step 3:**
     * If the retrieved member is not public,
     * and if `lookc` is not present,
     * and if `defc` and `refc` are different,
     * then [     smgr.checkPackageAccess(defcPkg)][SecurityManager.checkPackageAccess] is called,
     * where `defcPkg` is the package of `defc`.
     *
     * Security checks are performed after other access checks have passed.
     * Therefore, the above rules presuppose a member that is public,
     * or else that is being accessed from a lookup class that has
     * rights to access the member.
     *
     * <h1><a name="callsens"></a>Caller sensitive methods</h1>
     * A small number of Java methods have a special property called caller sensitivity.
     * A *caller-sensitive* method can behave differently depending on the
     * identity of its immediate caller.
     *
     *
     * If a method handle for a caller-sensitive method is requested,
     * the general rules for [bytecode behaviors](MethodHandles.Lookup.html#equiv) apply,
     * but they take account of the lookup class in a special way.
     * The resulting method handle behaves as if it were called
     * from an instruction contained in the lookup class,
     * so that the caller-sensitive method detects the lookup class.
     * (By contrast, the invoker of the method handle is disregarded.)
     * Thus, in the case of caller-sensitive methods,
     * different lookup classes may give rise to
     * differently behaving method handles.
     *
     *
     * In cases where the lookup object is
     * [publicLookup()][.publicLookup],
     * or some other lookup object without
     * [private access](MethodHandles.Lookup.html#privacc),
     * the lookup class is disregarded.
     * In such cases, no caller-sensitive method handle can be created,
     * access is forbidden, and the lookup fails with an
     * `IllegalAccessException`.
     *
     *
     * *Discussion:*
     * For example, the caller-sensitive method
     * [Class.forName(x)][java.lang.Class.forName]
     * can return varying classes or throw varying exceptions,
     * depending on the class loader of the class that calls it.
     * A public lookup of `Class.forName` will fail, because
     * there is no reasonable way to determine its bytecode behavior.
     *
     *
     * If an application caches method handles for broad sharing,
     * it should use `publicLookup()` to create them.
     * If there is a lookup of `Class.forName`, it will fail,
     * and the application must take appropriate action in that case.
     * It may be that a later lookup, perhaps during the invocation of a
     * bootstrap method, can incorporate the specific identity
     * of the caller, making the method accessible.
     *
     *
     * The function `MethodHandles.lookup` is caller sensitive
     * so that there can be a secure foundation for lookups.
     * Nearly all other methods in the JSR 292 API rely on lookup
     * objects to check access requests.
     */
    // Android-changed: Change link targets from MethodHandles#[public]Lookup to
    // #[public]Lookup to work around complaints from javadoc.
    class Lookup private constructor(lookupClass: java.lang.Class<*>, allowedModes: Int) {
        /** The class on behalf of whom the lookup is being performed.  */ /* @NonNull */
        private val lookupClass: java.lang.Class<*>

        /** The allowed sorts of members which may be looked up (PUBLIC, etc.).  */
        private val allowedModes: Int

        /** Tells which class is performing the lookup.  It is this class against
         * which checks are performed for visibility and access permissions.
         *
         *
         * The class implies a maximum level of access permission,
         * but the permissions may be additionally limited by the bitmask
         * [lookupModes][.lookupModes], which controls whether non-public members
         * can be accessed.
         * @return the lookup class, on behalf of which this lookup object finds members
         */
        fun lookupClass(): java.lang.Class<*> {
            return lookupClass
        }

        /** Tells which access-protection classes of members this lookup object can produce.
         * The result is a bit-mask of the bits
         * [PUBLIC (0x01)][.PUBLIC],
         * [PRIVATE (0x02)][.PRIVATE],
         * [PROTECTED (0x04)][.PROTECTED],
         * and [PACKAGE (0x08)][.PACKAGE].
         *
         *
         * A freshly-created lookup object
         * on the [caller&#39;s class][java.lang.invoke.MethodHandles.lookup]
         * has all possible bits set, since the caller class can access all its own members.
         * A lookup object on a new lookup class
         * [created from a previous lookup object][java.lang.invoke.MethodHandles.Lookup. in]
         * may have some mode bits set to zero.
         * The purpose of this is to restrict access via the new lookup object,
         * so that it can access only names which can be reached by the original
         * lookup object, and also by the new lookup class.
         * @return the lookup modes, which limit the kinds of access performed by this lookup object
         */
        fun lookupModes(): Int {
            return allowedModes and java.lang.invoke.MethodHandles.Lookup.Companion.ALL_MODES
        }

        /** Embody the current class (the lookupClass) as a lookup class
         * for method handle creation.
         * Must be called by from a method in this package,
         * which in turn is called by a method not in this package.
         */
        internal constructor(lookupClass: java.lang.Class<*>) : this(
            lookupClass,
            java.lang.invoke.MethodHandles.Lookup.Companion.ALL_MODES
        ) {
            // make sure we haven't accidentally picked up a privileged class:
            java.lang.invoke.MethodHandles.Lookup.Companion.checkUnprivilegedlookupClass(
                lookupClass,
                java.lang.invoke.MethodHandles.Lookup.Companion.ALL_MODES
            )
        }

        /**
         * Creates a lookup on the specified new lookup class.
         * The resulting object will report the specified
         * class as its own [lookupClass][.lookupClass].
         *
         *
         * However, the resulting `Lookup` object is guaranteed
         * to have no more access capabilities than the original.
         * In particular, access capabilities can be lost as follows:
         *  * If the new lookup class differs from the old one,
         * protected members will not be accessible by virtue of inheritance.
         * (Protected members may continue to be accessible because of package sharing.)
         *  * If the new lookup class is in a different package
         * than the old one, protected and default (package) members will not be accessible.
         *  * If the new lookup class is not within the same package member
         * as the old one, private members will not be accessible.
         *  * If the new lookup class is not accessible to the old lookup class,
         * then no members, not even public members, will be accessible.
         * (In all other cases, public members will continue to be accessible.)
         *
         *
         * @param requestedLookupClass the desired lookup class for the new lookup object
         * @return a lookup object which reports the desired lookup class
         * @throws NullPointerException if the argument is null
         */
        fun `in`(requestedLookupClass: java.lang.Class<*>): java.lang.invoke.MethodHandles.Lookup {
            requestedLookupClass.javaClass // null check
            // Android-changed: There's no notion of a trusted lookup.
            // if (allowedModes == TRUSTED)  // IMPL_LOOKUP can make any lookup at all
            //    return new Lookup(requestedLookupClass, ALL_MODES);
            if (requestedLookupClass == lookupClass) return this // keep same capabilities
            var newModes =
                (allowedModes and (java.lang.invoke.MethodHandles.Lookup.Companion.ALL_MODES and java.lang.invoke.MethodHandles.Lookup.Companion.PROTECTED.inv()))
            if (((newModes and java.lang.invoke.MethodHandles.Lookup.Companion.PACKAGE) != 0
                        && !VerifyAccess.isSamePackage(lookupClass, requestedLookupClass))
            ) {
                newModes =
                    newModes and (java.lang.invoke.MethodHandles.Lookup.Companion.PACKAGE or java.lang.invoke.MethodHandles.Lookup.Companion.PRIVATE).inv()
            }
            // Allow nestmate lookups to be created without special privilege:
            if (((newModes and java.lang.invoke.MethodHandles.Lookup.Companion.PRIVATE) != 0
                        && !VerifyAccess.isSamePackageMember(lookupClass, requestedLookupClass))
            ) {
                newModes =
                    newModes and java.lang.invoke.MethodHandles.Lookup.Companion.PRIVATE.inv()
            }
            if (((newModes and java.lang.invoke.MethodHandles.Lookup.Companion.PUBLIC) != 0
                        && !VerifyAccess.isClassAccessible(
                    requestedLookupClass,
                    lookupClass,
                    allowedModes
                ))
            ) {
                // The requested class it not accessible from the lookup class.
                // No permissions.
                newModes = 0
            }
            java.lang.invoke.MethodHandles.Lookup.Companion.checkUnprivilegedlookupClass(
                requestedLookupClass,
                newModes
            )
            return java.lang.invoke.MethodHandles.Lookup(requestedLookupClass, newModes)
        }

        init {
            this.lookupClass = lookupClass
            this.allowedModes = allowedModes
        }

        /**
         * Displays the name of the class from which lookups are to be made.
         * (The name is the one reported by [Class.getName][java.lang.Class.getName].)
         * If there are restrictions on the access permitted to this lookup,
         * this is indicated by adding a suffix to the class name, consisting
         * of a slash and a keyword.  The keyword represents the strongest
         * allowed access, and is chosen as follows:
         *
         *  * If no access is allowed, the suffix is "/noaccess".
         *  * If only public access is allowed, the suffix is "/public".
         *  * If only public and package access are allowed, the suffix is "/package".
         *  * If only public, package, and private access are allowed, the suffix is "/private".
         *
         * If none of the above cases apply, it is the case that full
         * access (public, package, private, and protected) is allowed.
         * In this case, no suffix is added.
         * This is true only of an object obtained originally from
         * [MethodHandles.lookup][java.lang.invoke.MethodHandles.lookup].
         * Objects created by [Lookup.in][java.lang.invoke.MethodHandles.Lookup. in]
         * always have restricted access, and will display a suffix.
         *
         *
         * (It may seem strange that protected access should be
         * stronger than private access.  Viewed independently from
         * package access, protected access is the first to be lost,
         * because it requires a direct subclass relationship between
         * caller and callee.)
         * @see .in
         */
        override fun toString(): String {
            var cname: String = lookupClass.getName()
            when (allowedModes) {
                0 -> return "$cname/noaccess"
                java.lang.invoke.MethodHandles.Lookup.Companion.PUBLIC -> return "$cname/public"
                java.lang.invoke.MethodHandles.Lookup.Companion.PUBLIC or java.lang.invoke.MethodHandles.Lookup.Companion.PACKAGE -> return "$cname/package"
                java.lang.invoke.MethodHandles.Lookup.Companion.ALL_MODES and java.lang.invoke.MethodHandles.Lookup.Companion.PROTECTED.inv() -> return "$cname/private"
                java.lang.invoke.MethodHandles.Lookup.Companion.ALL_MODES -> return cname
                else -> {
                    cname = cname + "/" + java.lang.Integer.toHexString(allowedModes)
                    assert((false)) { cname }
                    return cname
                }
            }
        }

        /**
         * Produces a method handle for a static method.
         * The type of the method handle will be that of the method.
         * (Since static methods do not take receivers, there is no
         * additional receiver argument inserted into the method handle type,
         * as there would be with [findVirtual][.findVirtual] or [findSpecial][.findSpecial].)
         * The method and all its argument types must be accessible to the lookup object.
         *
         *
         * The returned method handle will have
         * [variable arity][MethodHandle.asVarargsCollector] if and only if
         * the method's variable arity modifier bit (`0x0080`) is set.
         *
         *
         * If the returned method handle is invoked, the method's class will
         * be initialized, if it has not already been initialized.
         *
         * **Example:**
         * <blockquote><pre>`import static java.lang.invoke.MethodHandles.*;
         * import static java.lang.invoke.MethodType.*;
         * ...
         * MethodHandle MH_asList = publicLookup().findStatic(Arrays.class,
         * "asList", methodType(List.class, Object[].class));
         * assertEquals("[x, y]", MH_asList.invoke("x", "y").toString());
        `</pre></blockquote> *
         * @param refc the class from which the method is accessed
         * @param name the name of the method
         * @param type the type of the method
         * @return the desired method handle
         * @throws NoSuchMethodException if the method does not exist
         * @throws IllegalAccessException if access checking fails,
         * or if the method is not `static`,
         * or if the method's variable arity modifier bit
         * is set and `asVarargsCollector` fails
         * @exception SecurityException if a security manager is present and it
         * [refuses access](MethodHandles.Lookup.html#secmgr)
         * @throws NullPointerException if any argument is null
         */
        @Throws(java.lang.NoSuchMethodException::class, java.lang.IllegalAccessException::class)
        fun findStatic(
            refc: java.lang.Class<*>,
            name: String?,
            type: java.lang.invoke.MethodType
        ): java.lang.invoke.MethodHandle {
            val method: java.lang.reflect.Method = refc.getDeclaredMethod(name, type.ptypes())
            val modifiers: Int = method.getModifiers()
            if (!java.lang.reflect.Modifier.isStatic(modifiers)) {
                throw java.lang.IllegalAccessException("Method$method is not static")
            }
            checkReturnType(method, type)
            checkAccess(refc, method.getDeclaringClass(), modifiers, method.getName())
            return java.lang.invoke.MethodHandles.Lookup.Companion.createMethodHandle(
                method,
                java.lang.invoke.MethodHandle.INVOKE_STATIC,
                type
            )
        }

        private fun findVirtualForMH(
            name: String,
            type: java.lang.invoke.MethodType
        ): java.lang.invoke.MethodHandle? {
            // these names require special lookups because of the implicit MethodType argument
            if (("invoke" == name)) return java.lang.invoke.MethodHandles.invoker(type)
            return if (("invokeExact" == name)) java.lang.invoke.MethodHandles.exactInvoker(type) else null
        }

        private fun findVirtualForVH(
            name: String,
            type: java.lang.invoke.MethodType
        ): java.lang.invoke.MethodHandle? {
            val accessMode: java.lang.invoke.VarHandle.AccessMode
            try {
                accessMode = java.lang.invoke.VarHandle.AccessMode.valueFromMethodName(name)
            } catch (e: java.lang.IllegalArgumentException) {
                return null
            }
            return java.lang.invoke.MethodHandles.varHandleInvoker(accessMode, type)
        }

        /**
         * Produces a method handle for a virtual method.
         * The type of the method handle will be that of the method,
         * with the receiver type (usually `refc`) prepended.
         * The method and all its argument types must be accessible to the lookup object.
         *
         *
         * When called, the handle will treat the first argument as a receiver
         * and dispatch on the receiver's type to determine which method
         * implementation to enter.
         * (The dispatching action is identical with that performed by an
         * `invokevirtual` or `invokeinterface` instruction.)
         *
         *
         * The first argument will be of type `refc` if the lookup
         * class has full privileges to access the member.  Otherwise
         * the member must be `protected` and the first argument
         * will be restricted in type to the lookup class.
         *
         *
         * The returned method handle will have
         * [variable arity][MethodHandle.asVarargsCollector] if and only if
         * the method's variable arity modifier bit (`0x0080`) is set.
         *
         *
         * Because of the general [equivalence](MethodHandles.Lookup.html#equiv) between `invokevirtual`
         * instructions and method handles produced by `findVirtual`,
         * if the class is `MethodHandle` and the name string is
         * `invokeExact` or `invoke`, the resulting
         * method handle is equivalent to one produced by
         * [MethodHandles.exactInvoker][java.lang.invoke.MethodHandles.exactInvoker] or
         * [MethodHandles.invoker][java.lang.invoke.MethodHandles.invoker]
         * with the same `type` argument.
         *
         * **Example:**
         * <blockquote><pre>`import static java.lang.invoke.MethodHandles.*;
         * import static java.lang.invoke.MethodType.*;
         * ...
         * MethodHandle MH_concat = publicLookup().findVirtual(String.class,
         * "concat", methodType(String.class, String.class));
         * MethodHandle MH_hashCode = publicLookup().findVirtual(Object.class,
         * "hashCode", methodType(int.class));
         * MethodHandle MH_hashCode_String = publicLookup().findVirtual(String.class,
         * "hashCode", methodType(int.class));
         * assertEquals("xy", (String) MH_concat.invokeExact("x", "y"));
         * assertEquals("xy".hashCode(), (int) MH_hashCode.invokeExact((Object)"xy"));
         * assertEquals("xy".hashCode(), (int) MH_hashCode_String.invokeExact("xy"));
         * // interface method:
         * MethodHandle MH_subSequence = publicLookup().findVirtual(CharSequence.class,
         * "subSequence", methodType(CharSequence.class, int.class, int.class));
         * assertEquals("def", MH_subSequence.invoke("abcdefghi", 3, 6).toString());
         * // constructor "internal method" must be accessed differently:
         * MethodType MT_newString = methodType(void.class); //()V for new String()
         * try { assertEquals("impossible", lookup()
         * .findVirtual(String.class, "<init>", MT_newString));
         * } catch (NoSuchMethodException ex) { } // OK
         * MethodHandle MH_newString = publicLookup()
         * .findConstructor(String.class, MT_newString);
         * assertEquals("", (String) MH_newString.invokeExact());
        `</pre></blockquote> *
         *
         * @param refc the class or interface from which the method is accessed
         * @param name the name of the method
         * @param type the type of the method, with the receiver argument omitted
         * @return the desired method handle
         * @throws NoSuchMethodException if the method does not exist
         * @throws IllegalAccessException if access checking fails,
         * or if the method is `static`
         * or if the method's variable arity modifier bit
         * is set and `asVarargsCollector` fails
         * @exception SecurityException if a security manager is present and it
         * [refuses access](MethodHandles.Lookup.html#secmgr)
         * @throws NullPointerException if any argument is null
         */
        @Throws(java.lang.NoSuchMethodException::class, java.lang.IllegalAccessException::class)
        fun findVirtual(
            refc: java.lang.Class<*>,
            name: String,
            type: java.lang.invoke.MethodType
        ): java.lang.invoke.MethodHandle {
            // Special case : when we're looking up a virtual method on the MethodHandles class
            // itself, we can return one of our specialized invokers.
            if (refc == java.lang.invoke.MethodHandle::class.java) {
                val mh: java.lang.invoke.MethodHandle? = findVirtualForMH(name, type)
                if (mh != null) {
                    return mh
                }
            } else if (refc == java.lang.invoke.VarHandle::class.java) {
                // Returns an non-exact invoker.
                val mh: java.lang.invoke.MethodHandle? = findVirtualForVH(name, type)
                if (mh != null) {
                    return mh
                }
            }
            val method: java.lang.reflect.Method = refc.getInstanceMethod(name, type.ptypes())
            if (method == null) {
                // This is pretty ugly and a consequence of the MethodHandles API. We have to throw
                // an IAE and not an NSME if the method exists but is static (even though the RI's
                // IAE has a message that says "no such method"). We confine the ugliness and
                // slowness to the failure case, and allow getInstanceMethod to remain fairly
                // general.
                try {
                    val m: java.lang.reflect.Method = refc.getDeclaredMethod(name, type.ptypes())
                    if (java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
                        throw java.lang.IllegalAccessException("Method$m is static")
                    }
                } catch (ignored: java.lang.NoSuchMethodException) {
                }
                throw java.lang.NoSuchMethodException(name + " " + java.util.Arrays.toString(type.ptypes()))
            }
            checkReturnType(method, type)

            // We have a valid method, perform access checks.
            checkAccess(refc, method.getDeclaringClass(), method.getModifiers(), method.getName())

            // Insert the leading reference parameter.
            val handleType: java.lang.invoke.MethodType = type.insertParameterTypes(0, refc)
            return java.lang.invoke.MethodHandles.Lookup.Companion.createMethodHandle(
                method,
                java.lang.invoke.MethodHandle.INVOKE_VIRTUAL,
                handleType
            )
        }

        /**
         * Produces a method handle which creates an object and initializes it, using
         * the constructor of the specified type.
         * The parameter types of the method handle will be those of the constructor,
         * while the return type will be a reference to the constructor's class.
         * The constructor and all its argument types must be accessible to the lookup object.
         *
         *
         * The requested type must have a return type of `void`.
         * (This is consistent with the JVM's treatment of constructor type descriptors.)
         *
         *
         * The returned method handle will have
         * [variable arity][MethodHandle.asVarargsCollector] if and only if
         * the constructor's variable arity modifier bit (`0x0080`) is set.
         *
         *
         * If the returned method handle is invoked, the constructor's class will
         * be initialized, if it has not already been initialized.
         *
         * **Example:**
         * <blockquote><pre>`import static java.lang.invoke.MethodHandles.*;
         * import static java.lang.invoke.MethodType.*;
         * ...
         * MethodHandle MH_newArrayList = publicLookup().findConstructor(
         * ArrayList.class, methodType(void.class, Collection.class));
         * Collection orig = Arrays.asList("x", "y");
         * Collection copy = (ArrayList) MH_newArrayList.invokeExact(orig);
         * assert(orig != copy);
         * assertEquals(orig, copy);
         * // a variable-arity constructor:
         * MethodHandle MH_newProcessBuilder = publicLookup().findConstructor(
         * ProcessBuilder.class, methodType(void.class, String[].class));
         * ProcessBuilder pb = (ProcessBuilder)
         * MH_newProcessBuilder.invoke("x", "y", "z");
         * assertEquals("[x, y, z]", pb.command().toString());
        `</pre></blockquote> *
         * @param refc the class or interface from which the method is accessed
         * @param type the type of the method, with the receiver argument omitted, and a void return type
         * @return the desired method handle
         * @throws NoSuchMethodException if the constructor does not exist
         * @throws IllegalAccessException if access checking fails
         * or if the method's variable arity modifier bit
         * is set and `asVarargsCollector` fails
         * @exception SecurityException if a security manager is present and it
         * [refuses access](MethodHandles.Lookup.html#secmgr)
         * @throws NullPointerException if any argument is null
         */
        @Throws(java.lang.NoSuchMethodException::class, java.lang.IllegalAccessException::class)
        fun findConstructor(
            refc: java.lang.Class<*>,
            type: java.lang.invoke.MethodType
        ): java.lang.invoke.MethodHandle {
            if (refc.isArray()) {
                throw java.lang.NoSuchMethodException("no constructor for array class: " + refc.getName())
            }
            // The queried |type| is (PT1,PT2,..)V
            val constructor: java.lang.reflect.Constructor =
                refc.getDeclaredConstructor(type.ptypes())
            if (constructor == null) {
                throw java.lang.NoSuchMethodException(
                    "No constructor for " + constructor.getDeclaringClass() + " matching " + type
                )
            }
            checkAccess(
                refc, constructor.getDeclaringClass(), constructor.getModifiers(),
                constructor.getName()
            )
            return createMethodHandleForConstructor(constructor)
        }
        // BEGIN Android-added: Add findClass(String) from OpenJDK 17. http://b/270028670
        // TODO: Unhide this method.
        /**
         * Looks up a class by name from the lookup context defined by this `Lookup` object,
         * [as if resolved](MethodHandles.Lookup.html#equiv) by an `ldc` instruction.
         * Such a resolution, as specified in JVMS 5.4.3.1 section, attempts to locate and load the class,
         * and then determines whether the class is accessible to this lookup object.
         *
         *
         * The lookup context here is determined by the [lookup class][.lookupClass],
         * its class loader, and the [lookup modes][.lookupModes].
         *
         * @param targetName the fully qualified name of the class to be looked up.
         * @return the requested class.
         * @throws SecurityException if a security manager is present and it
         * [refuses access](MethodHandles.Lookup.html#secmgr)
         * @throws LinkageError if the linkage fails
         * @throws ClassNotFoundException if the class cannot be loaded by the lookup class' loader.
         * @throws IllegalAccessException if the class is not accessible, using the allowed access
         * modes.
         * @throws NullPointerException if `targetName` is null
         * @since 9
         * @jvms 5.4.3.1 Class and Interface Resolution
         * @hide
         */
        @Throws(java.lang.ClassNotFoundException::class, java.lang.IllegalAccessException::class)
        fun findClass(targetName: String?): java.lang.Class<*> {
            val targetClass: java.lang.Class<*> =
                java.lang.Class.forName(targetName, false, lookupClass.getClassLoader())
            return accessClass(targetClass)
        }

        // END Android-added: Add findClass(String) from OpenJDK 17. http://b/270028670
        private fun createMethodHandleForConstructor(constructor: java.lang.reflect.Constructor): java.lang.invoke.MethodHandle {
            val refc: java.lang.Class<*> = constructor.getDeclaringClass()
            val constructorType: java.lang.invoke.MethodType =
                java.lang.invoke.MethodType.methodType(refc, constructor.getParameterTypes())
            var mh: java.lang.invoke.MethodHandle
            if (refc == String::class.java) {
                // String constructors have optimized StringFactory methods
                // that matches returned type. These factory methods combine the
                // memory allocation and initialization calls for String objects.
                mh = java.lang.invoke.MethodHandleImpl(
                    constructor.getArtMethod(), java.lang.invoke.MethodHandle.INVOKE_DIRECT,
                    constructorType
                )
            } else {
                // Constructors for all other classes use a Construct transformer to perform
                // their memory allocation and call to <init>.
                val initType: java.lang.invoke.MethodType =
                    java.lang.invoke.MethodHandles.Lookup.Companion.initMethodType(constructorType)
                val initHandle: java.lang.invoke.MethodHandle = java.lang.invoke.MethodHandleImpl(
                    constructor.getArtMethod(),
                    java.lang.invoke.MethodHandle.INVOKE_DIRECT,
                    initType
                )
                mh = Construct(initHandle, constructorType)
            }
            if (constructor.isVarArgs()) {
                mh = VarargsCollector(mh)
            }
            return mh
        }

        // BEGIN Android-added: Add accessClass(Class) from OpenJDK 17. http://b/270028670
        /*
         * Returns IllegalAccessException due to access violation to the given targetClass.
         *
         * This method is called by {@link Lookup#accessClass} and {@link Lookup#ensureInitialized}
         * which verifies access to a class rather a member.
         */
        private fun makeAccessException(targetClass: java.lang.Class<*>): java.lang.IllegalAccessException {
            var message: String = "access violation: $targetClass"
            if (this == java.lang.invoke.MethodHandles.publicLookup()) {
                message += ", from public Lookup"
            } else {
                // Android-changed: Remove unsupported module name.
                // Module m = lookupClass().getModule();
                // message += ", from " + lookupClass() + " (" + m + ")";
                message += ", from " + lookupClass()
                // Android-removed: Remove prevLookupClass until supported by Lookup in OpenJDK 17.
                // if (prevLookupClass != null) {
                //    message += ", previous lookup " +
                //            prevLookupClass.getName() + " (" + prevLookupClass.getModule() + ")";
                // }
            }
            return java.lang.IllegalAccessException(message)
        }
        // TODO: Unhide this method.
        /**
         * Determines if a class can be accessed from the lookup context defined by
         * this `Lookup` object. The static initializer of the class is not run.
         * If `targetClass` is an array class, `targetClass` is accessible
         * if the element type of the array class is accessible.  Otherwise,
         * `targetClass` is determined as accessible as follows.
         *
         *
         *
         * If `targetClass` is in the same module as the lookup class,
         * the lookup class is `LC` in module `M1` and
         * the previous lookup class is in module `M0` or
         * `null` if not present,
         * `targetClass` is accessible if and only if one of the following is true:
         *
         *  * If this lookup has [.PRIVATE] access, `targetClass` is
         * `LC` or other class in the same nest of `LC`.
         *  * If this lookup has [.PACKAGE] access, `targetClass` is
         * in the same runtime package of `LC`.
         *  * If this lookup has [.MODULE] access, `targetClass` is
         * a public type in `M1`.
         *  * If this lookup has [.PUBLIC] access, `targetClass` is
         * a public type in a package exported by `M1` to at least  `M0`
         * if the previous lookup class is present; otherwise, `targetClass`
         * is a public type in a package exported by `M1` unconditionally.
         *
         *
         *
         *
         * Otherwise, if this lookup has [.UNCONDITIONAL] access, this lookup
         * can access public types in all modules when the type is in a package
         * that is exported unconditionally.
         *
         *
         * Otherwise, `targetClass` is in a different module from `lookupClass`,
         * and if this lookup does not have `PUBLIC` access, `lookupClass`
         * is inaccessible.
         *
         *
         * Otherwise, if this lookup has no [previous lookup class][.previousLookupClass],
         * `M1` is the module containing `lookupClass` and
         * `M2` is the module containing `targetClass`,
         * then `targetClass` is accessible if and only if
         *
         *  * `M1` reads `M2`, and
         *  * `targetClass` is public and in a package exported by
         * `M2` at least to `M1`.
         *
         *
         *
         * Otherwise, if this lookup has a [previous lookup class][.previousLookupClass],
         * `M1` and `M2` are as before, and `M0` is the module
         * containing the previous lookup class, then `targetClass` is accessible
         * if and only if one of the following is true:
         *
         *  * `targetClass` is in `M0` and `M1`
         * [reads][Module.reads] `M0` and the type is
         * in a package that is exported to at least `M1`.
         *  * `targetClass` is in `M1` and `M0`
         * [reads][Module.reads] `M1` and the type is
         * in a package that is exported to at least `M0`.
         *  * `targetClass` is in a third module `M2` and both `M0`
         * and `M1` reads `M2` and the type is in a package
         * that is exported to at least both `M0` and `M2`.
         *
         *
         *
         * Otherwise, `targetClass` is not accessible.
         *
         * @param targetClass the class to be access-checked
         * @return the class that has been access-checked
         * @throws IllegalAccessException if the class is not accessible from the lookup class
         * and previous lookup class, if present, using the allowed access modes.
         * @throws SecurityException if a security manager is present and it
         * [refuses access](MethodHandles.Lookup.html#secmgr)
         * @throws NullPointerException if `targetClass` is `null`
         * @since 9
         * @see [Cross-module lookups](.cross-module-lookup)
         *
         * @hide
         */
        @Throws(java.lang.IllegalAccessException::class)
        fun accessClass(targetClass: java.lang.Class<*>): java.lang.Class<*> {
            if (!isClassAccessible(targetClass)) {
                throw makeAccessException(targetClass)
            }
            // Android-removed: SecurityManager is unnecessary on Android.
            // checkSecurityManager(targetClass);
            return targetClass
        }

        fun isClassAccessible(refc: java.lang.Class<*>): Boolean {
            java.util.Objects.requireNonNull(refc)
            val caller: java.lang.Class<*> = lookupClassOrNull()
            var type: java.lang.Class<*> = refc
            while (type.isArray()) {
                type = type.getComponentType()
            }
            // Android-removed: Remove prevLookupClass until supported by Lookup in OpenJDK 17.
            // return caller == null || VerifyAccess.isClassAccessible(type, caller, prevLookupClass, allowedModes);
            return caller == null || VerifyAccess.isClassAccessible(type, caller, allowedModes)
        }

        // This is just for calling out to MethodHandleImpl.
        private fun lookupClassOrNull(): java.lang.Class<*> {
            // Android-changed: Android always returns lookupClass and has no concept of TRUSTED.
            // return (allowedModes == TRUSTED) ? null : lookupClass;
            return lookupClass
        }
        // END Android-added: Add accessClass(Class) from OpenJDK 17. http://b/270028670
        /**
         * Produces an early-bound method handle for a virtual method.
         * It will bypass checks for overriding methods on the receiver,
         * [as if called](MethodHandles.Lookup.html#equiv) from an `invokespecial`
         * instruction from within the explicitly specified `specialCaller`.
         * The type of the method handle will be that of the method,
         * with a suitably restricted receiver type prepended.
         * (The receiver type will be `specialCaller` or a subtype.)
         * The method and all its argument types must be accessible
         * to the lookup object.
         *
         *
         * Before method resolution,
         * if the explicitly specified caller class is not identical with the
         * lookup class, or if this lookup object does not have
         * [private access](MethodHandles.Lookup.html#privacc)
         * privileges, the access fails.
         *
         *
         * The returned method handle will have
         * [variable arity][MethodHandle.asVarargsCollector] if and only if
         * the method's variable arity modifier bit (`0x0080`) is set.
         *
         *
         * *(Note:  JVM internal methods named `"<init>"` are not visible to this API,
         * even though the `invokespecial` instruction can refer to them
         * in special circumstances.  Use [findConstructor][.findConstructor]
         * to access instance initialization methods in a safe manner.)*
         *
         * **Example:**
         * <blockquote><pre>`import static java.lang.invoke.MethodHandles.*;
         * import static java.lang.invoke.MethodType.*;
         * ...
         * static class Listie extends ArrayList {
         * public String toString() { return "[wee Listie]"; }
         * static Lookup lookup() { return MethodHandles.lookup(); }
         * }
         * ...
         * // no access to constructor via invokeSpecial:
         * MethodHandle MH_newListie = Listie.lookup()
         * .findConstructor(Listie.class, methodType(void.class));
         * Listie l = (Listie) MH_newListie.invokeExact();
         * try { assertEquals("impossible", Listie.lookup().findSpecial(
         * Listie.class, "<init>", methodType(void.class), Listie.class));
         * } catch (NoSuchMethodException ex) { } // OK
         * // access to super and self methods via invokeSpecial:
         * MethodHandle MH_super = Listie.lookup().findSpecial(
         * ArrayList.class, "toString" , methodType(String.class), Listie.class);
         * MethodHandle MH_this = Listie.lookup().findSpecial(
         * Listie.class, "toString" , methodType(String.class), Listie.class);
         * MethodHandle MH_duper = Listie.lookup().findSpecial(
         * Object.class, "toString" , methodType(String.class), Listie.class);
         * assertEquals("[]", (String) MH_super.invokeExact(l));
         * assertEquals(""+l, (String) MH_this.invokeExact(l));
         * assertEquals("[]", (String) MH_duper.invokeExact(l)); // ArrayList method
         * try { assertEquals("inaccessible", Listie.lookup().findSpecial(
         * String.class, "toString", methodType(String.class), Listie.class));
         * } catch (IllegalAccessException ex) { } // OK
         * Listie subl = new Listie() { public String toString() { return "[subclass]"; } };
         * assertEquals(""+l, (String) MH_this.invokeExact(subl)); // Listie method
        `</pre></blockquote> *
         *
         * @param refc the class or interface from which the method is accessed
         * @param name the name of the method (which must not be "&lt;init&gt;")
         * @param type the type of the method, with the receiver argument omitted
         * @param specialCaller the proposed calling class to perform the `invokespecial`
         * @return the desired method handle
         * @throws NoSuchMethodException if the method does not exist
         * @throws IllegalAccessException if access checking fails
         * or if the method's variable arity modifier bit
         * is set and `asVarargsCollector` fails
         * @exception SecurityException if a security manager is present and it
         * [refuses access](MethodHandles.Lookup.html#secmgr)
         * @throws NullPointerException if any argument is null
         */
        @Throws(java.lang.NoSuchMethodException::class, java.lang.IllegalAccessException::class)
        fun findSpecial(
            refc: java.lang.Class<*>?,
            name: String?,
            type: java.lang.invoke.MethodType?,
            specialCaller: java.lang.Class<*>?
        ): java.lang.invoke.MethodHandle {
            if (specialCaller == null) {
                throw java.lang.NullPointerException("specialCaller == null")
            }
            if (type == null) {
                throw java.lang.NullPointerException("type == null")
            }
            if (name == null) {
                throw java.lang.NullPointerException("name == null")
            }
            if (refc == null) {
                throw java.lang.NullPointerException("ref == null")
            }

            // Make sure that the special caller is identical to the lookup class or that we have
            // private access.
            // Android-changed: Also allow access to any interface methods.
            checkSpecialCaller(specialCaller, refc)

            // Even though constructors are invoked using a "special" invoke, handles to them can't
            // be created using findSpecial. Callers must use findConstructor instead. Similarly,
            // there is no path for calling static class initializers.
            if (name.startsWith("<")) {
                throw java.lang.NoSuchMethodException("$name is not a valid method name.")
            }
            val method: java.lang.reflect.Method = refc.getDeclaredMethod(name, type.ptypes())
            checkReturnType(method, type)
            return findSpecial(method, type, refc, specialCaller)
        }

        @Throws(java.lang.IllegalAccessException::class)
        private fun findSpecial(
            method: java.lang.reflect.Method, type: java.lang.invoke.MethodType,
            refc: java.lang.Class<*>, specialCaller: java.lang.Class<*>
        ): java.lang.invoke.MethodHandle {
            if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                throw java.lang.IllegalAccessException("expected a non-static method:$method")
            }
            if (java.lang.reflect.Modifier.isPrivate(method.getModifiers())) {
                // Since this is a private method, we'll need to also make sure that the
                // lookup class is the same as the refering class. We've already checked that
                // the specialCaller is the same as the special lookup class, both of these must
                // be the same as the declaring class(*) in order to access the private method.
                //
                // (*) Well, this isn't true for nested classes but OpenJDK doesn't support those
                // either.
                if (refc != lookupClass()) {
                    throw java.lang.IllegalAccessException(
                        ("no private access for invokespecial : "
                                + refc + ", from" + this)
                    )
                }

                // This is a private method, so there's nothing special to do.
                val handleType: java.lang.invoke.MethodType = type.insertParameterTypes(0, refc)
                return java.lang.invoke.MethodHandles.Lookup.Companion.createMethodHandle(
                    method,
                    java.lang.invoke.MethodHandle.INVOKE_DIRECT,
                    handleType
                )
            }

            // This is a public, protected or package-private method, which means we're expecting
            // invoke-super semantics. We'll have to restrict the receiver type appropriately on the
            // handle once we check that there really is a "super" relationship between them.
            if (!method.getDeclaringClass().isAssignableFrom(specialCaller)) {
                throw java.lang.IllegalAccessException(refc.toString() + "is not assignable from " + specialCaller)
            }

            // Note that we restrict the receiver to "specialCaller" instances.
            val handleType: java.lang.invoke.MethodType =
                type.insertParameterTypes(0, specialCaller)
            return java.lang.invoke.MethodHandles.Lookup.Companion.createMethodHandle(
                method,
                java.lang.invoke.MethodHandle.INVOKE_SUPER,
                handleType
            )
        }

        /**
         * Produces a method handle giving read access to a non-static field.
         * The type of the method handle will have a return type of the field's
         * value type.
         * The method handle's single argument will be the instance containing
         * the field.
         * Access checking is performed immediately on behalf of the lookup class.
         * @param refc the class or interface from which the method is accessed
         * @param name the field's name
         * @param type the field's type
         * @return a method handle which can load values from the field
         * @throws NoSuchFieldException if the field does not exist
         * @throws IllegalAccessException if access checking fails, or if the field is `static`
         * @exception SecurityException if a security manager is present and it
         * [refuses access](MethodHandles.Lookup.html#secmgr)
         * @throws NullPointerException if any argument is null
         */
        @Throws(java.lang.NoSuchFieldException::class, java.lang.IllegalAccessException::class)
        fun findGetter(
            refc: java.lang.Class<*>,
            name: String,
            type: java.lang.Class<*>
        ): java.lang.invoke.MethodHandle {
            return findAccessor(refc, name, type, java.lang.invoke.MethodHandle.IGET)
        }

        @Throws(java.lang.NoSuchFieldException::class, java.lang.IllegalAccessException::class)
        private fun findAccessor(
            refc: java.lang.Class<*>,
            name: String,
            type: java.lang.Class<*>,
            kind: Int
        ): java.lang.invoke.MethodHandle {
            val field: java.lang.reflect.Field? = findFieldOfType(refc, name, type)
            return findAccessor(field, refc, type, kind, true /* performAccessChecks */)
        }

        @Throws(java.lang.IllegalAccessException::class)
        private fun findAccessor(
            field: java.lang.reflect.Field?,
            refc: java.lang.Class<*>,
            type: java.lang.Class<*>,
            kind: Int,
            performAccessChecks: Boolean
        ): java.lang.invoke.MethodHandle {
            val isSetterKind =
                kind == java.lang.invoke.MethodHandle.IPUT || kind == java.lang.invoke.MethodHandle.SPUT
            val isStaticKind =
                kind == java.lang.invoke.MethodHandle.SGET || kind == java.lang.invoke.MethodHandle.SPUT
            commonFieldChecks(field, refc, type, isStaticKind, performAccessChecks)
            if (performAccessChecks) {
                val modifiers: Int = field.getModifiers()
                if (isSetterKind && java.lang.reflect.Modifier.isFinal(modifiers)) {
                    throw java.lang.IllegalAccessException("Field $field is final")
                }
            }
            val methodType: java.lang.invoke.MethodType
            when (kind) {
                java.lang.invoke.MethodHandle.SGET -> methodType =
                    java.lang.invoke.MethodType.methodType(type)

                java.lang.invoke.MethodHandle.SPUT -> methodType =
                    java.lang.invoke.MethodType.methodType(Void.TYPE, type)

                java.lang.invoke.MethodHandle.IGET -> methodType =
                    java.lang.invoke.MethodType.methodType(type, refc)

                java.lang.invoke.MethodHandle.IPUT -> methodType =
                    java.lang.invoke.MethodType.methodType(Void.TYPE, refc, type)

                else -> throw java.lang.IllegalArgumentException("Invalid kind $kind")
            }
            return java.lang.invoke.MethodHandleImpl(field.getArtField(), kind, methodType)
        }

        /**
         * Produces a method handle giving write access to a non-static field.
         * The type of the method handle will have a void return type.
         * The method handle will take two arguments, the instance containing
         * the field, and the value to be stored.
         * The second argument will be of the field's value type.
         * Access checking is performed immediately on behalf of the lookup class.
         * @param refc the class or interface from which the method is accessed
         * @param name the field's name
         * @param type the field's type
         * @return a method handle which can store values into the field
         * @throws NoSuchFieldException if the field does not exist
         * @throws IllegalAccessException if access checking fails, or if the field is `static`
         * @exception SecurityException if a security manager is present and it
         * [refuses access](MethodHandles.Lookup.html#secmgr)
         * @throws NullPointerException if any argument is null
         */
        @Throws(java.lang.NoSuchFieldException::class, java.lang.IllegalAccessException::class)
        fun findSetter(
            refc: java.lang.Class<*>,
            name: String,
            type: java.lang.Class<*>
        ): java.lang.invoke.MethodHandle {
            return findAccessor(refc, name, type, java.lang.invoke.MethodHandle.IPUT)
        }
        // BEGIN Android-changed: OpenJDK 9+181 VarHandle API factory method.
        /**
         * Produces a VarHandle giving access to a non-static field `name`
         * of type `type` declared in a class of type `recv`.
         * The VarHandle's variable type is `type` and it has one
         * coordinate type, `recv`.
         *
         *
         * Access checking is performed immediately on behalf of the lookup
         * class.
         *
         *
         * Certain access modes of the returned VarHandle are unsupported under
         * the following conditions:
         *
         *  * if the field is declared `final`, then the write, atomic
         * update, numeric atomic update, and bitwise atomic update access
         * modes are unsupported.
         *  * if the field type is anything other than `byte`,
         * `short`, `char`, `int`, `long`,
         * `float`, or `double` then numeric atomic update
         * access modes are unsupported.
         *  * if the field type is anything other than `boolean`,
         * `byte`, `short`, `char`, `int` or
         * `long` then bitwise atomic update access modes are
         * unsupported.
         *
         *
         *
         * If the field is declared `volatile` then the returned VarHandle
         * will override access to the field (effectively ignore the
         * `volatile` declaration) in accordance to its specified
         * access modes.
         *
         *
         * If the field type is `float` or `double` then numeric
         * and atomic update access modes compare values using their bitwise
         * representation (see [Float.floatToRawIntBits] and
         * [Double.doubleToRawLongBits], respectively).
         * @apiNote
         * Bitwise comparison of `float` values or `double` values,
         * as performed by the numeric and atomic update access modes, differ
         * from the primitive `==` operator and the [Float.equals]
         * and [Double.equals] methods, specifically with respect to
         * comparing NaN values or comparing `-0.0` with `+0.0`.
         * Care should be taken when performing a compare and set or a compare
         * and exchange operation with such values since the operation may
         * unexpectedly fail.
         * There are many possible NaN values that are considered to be
         * `NaN` in Java, although no IEEE 754 floating-point operation
         * provided by Java can distinguish between them.  Operation failure can
         * occur if the expected or witness value is a NaN value and it is
         * transformed (perhaps in a platform specific manner) into another NaN
         * value, and thus has a different bitwise representation (see
         * [Float.intBitsToFloat] or [Double.longBitsToDouble] for more
         * details).
         * The values `-0.0` and `+0.0` have different bitwise
         * representations but are considered equal when using the primitive
         * `==` operator.  Operation failure can occur if, for example, a
         * numeric algorithm computes an expected value to be say `-0.0`
         * and previously computed the witness value to be say `+0.0`.
         * @param recv the receiver class, of type `R`, that declares the
         * non-static field
         * @param name the field's name
         * @param type the field's type, of type `T`
         * @return a VarHandle giving access to non-static fields.
         * @throws NoSuchFieldException if the field does not exist
         * @throws IllegalAccessException if access checking fails, or if the field is `static`
         * @exception SecurityException if a security manager is present and it
         * [refuses access](MethodHandles.Lookup.html#secmgr)
         * @throws NullPointerException if any argument is null
         * @since 9
         */
        @Throws(java.lang.NoSuchFieldException::class, java.lang.IllegalAccessException::class)
        fun findVarHandle(
            recv: java.lang.Class<*>,
            name: String,
            type: java.lang.Class<*>
        ): java.lang.invoke.VarHandle {
            val field: java.lang.reflect.Field? = findFieldOfType(recv, name, type)
            val isStatic = false
            val performAccessChecks = true
            commonFieldChecks(field, recv, type, isStatic, performAccessChecks)
            return FieldVarHandle.create(field)
        }

        // END Android-changed: OpenJDK 9+181 VarHandle API factory method.
        // BEGIN Android-added: Common field resolution and access check methods.
        @Throws(java.lang.NoSuchFieldException::class)
        private fun findFieldOfType(
            refc: java.lang.Class<*>,
            name: String,
            type: java.lang.Class<*>
        ): java.lang.reflect.Field? {
            var field: java.lang.reflect.Field? = null

            // Search refc and super classes for the field.
            var cls: java.lang.Class<*> = refc
            while (cls != null) {
                try {
                    field = cls.getDeclaredField(name)
                    break
                } catch (e: java.lang.NoSuchFieldException) {
                }
                cls = cls.getSuperclass()
            }
            if (field == null) {
                // Force failure citing refc.
                field = refc.getDeclaredField(name)
            }
            val fieldType: java.lang.Class<*> = field.getType()
            if (fieldType != type) {
                throw java.lang.NoSuchFieldException(name)
            }
            return field
        }

        @Throws(java.lang.IllegalAccessException::class)
        private fun commonFieldChecks(
            field: java.lang.reflect.Field?, refc: java.lang.Class<*>, type: java.lang.Class<*>,
            isStatic: Boolean, performAccessChecks: Boolean
        ) {
            val modifiers: Int = field.getModifiers()
            if (performAccessChecks) {
                checkAccess(refc, field.getDeclaringClass(), modifiers, field.getName())
            }
            if (java.lang.reflect.Modifier.isStatic(modifiers) != isStatic) {
                val reason = ("Field " + field + " is " +
                        (if (isStatic) "not " else "") + "static")
                throw java.lang.IllegalAccessException(reason)
            }
        }
        // END Android-added: Common field resolution and access check methods.
        /**
         * Produces a method handle giving read access to a static field.
         * The type of the method handle will have a return type of the field's
         * value type.
         * The method handle will take no arguments.
         * Access checking is performed immediately on behalf of the lookup class.
         *
         *
         * If the returned method handle is invoked, the field's class will
         * be initialized, if it has not already been initialized.
         * @param refc the class or interface from which the method is accessed
         * @param name the field's name
         * @param type the field's type
         * @return a method handle which can load values from the field
         * @throws NoSuchFieldException if the field does not exist
         * @throws IllegalAccessException if access checking fails, or if the field is not `static`
         * @exception SecurityException if a security manager is present and it
         * [refuses access](MethodHandles.Lookup.html#secmgr)
         * @throws NullPointerException if any argument is null
         */
        @Throws(java.lang.NoSuchFieldException::class, java.lang.IllegalAccessException::class)
        fun findStaticGetter(
            refc: java.lang.Class<*>,
            name: String,
            type: java.lang.Class<*>
        ): java.lang.invoke.MethodHandle {
            return findAccessor(refc, name, type, java.lang.invoke.MethodHandle.SGET)
        }

        /**
         * Produces a method handle giving write access to a static field.
         * The type of the method handle will have a void return type.
         * The method handle will take a single
         * argument, of the field's value type, the value to be stored.
         * Access checking is performed immediately on behalf of the lookup class.
         *
         *
         * If the returned method handle is invoked, the field's class will
         * be initialized, if it has not already been initialized.
         * @param refc the class or interface from which the method is accessed
         * @param name the field's name
         * @param type the field's type
         * @return a method handle which can store values into the field
         * @throws NoSuchFieldException if the field does not exist
         * @throws IllegalAccessException if access checking fails, or if the field is not `static`
         * @exception SecurityException if a security manager is present and it
         * [refuses access](MethodHandles.Lookup.html#secmgr)
         * @throws NullPointerException if any argument is null
         */
        @Throws(java.lang.NoSuchFieldException::class, java.lang.IllegalAccessException::class)
        fun findStaticSetter(
            refc: java.lang.Class<*>,
            name: String,
            type: java.lang.Class<*>
        ): java.lang.invoke.MethodHandle {
            return findAccessor(refc, name, type, java.lang.invoke.MethodHandle.SPUT)
        }
        // BEGIN Android-changed: OpenJDK 9+181 VarHandle API factory method.
        /**
         * Produces a VarHandle giving access to a static field `name` of
         * type `type` declared in a class of type `decl`.
         * The VarHandle's variable type is `type` and it has no
         * coordinate types.
         *
         *
         * Access checking is performed immediately on behalf of the lookup
         * class.
         *
         *
         * If the returned VarHandle is operated on, the declaring class will be
         * initialized, if it has not already been initialized.
         *
         *
         * Certain access modes of the returned VarHandle are unsupported under
         * the following conditions:
         *
         *  * if the field is declared `final`, then the write, atomic
         * update, numeric atomic update, and bitwise atomic update access
         * modes are unsupported.
         *  * if the field type is anything other than `byte`,
         * `short`, `char`, `int`, `long`,
         * `float`, or `double`, then numeric atomic update
         * access modes are unsupported.
         *  * if the field type is anything other than `boolean`,
         * `byte`, `short`, `char`, `int` or
         * `long` then bitwise atomic update access modes are
         * unsupported.
         *
         *
         *
         * If the field is declared `volatile` then the returned VarHandle
         * will override access to the field (effectively ignore the
         * `volatile` declaration) in accordance to its specified
         * access modes.
         *
         *
         * If the field type is `float` or `double` then numeric
         * and atomic update access modes compare values using their bitwise
         * representation (see [Float.floatToRawIntBits] and
         * [Double.doubleToRawLongBits], respectively).
         * @apiNote
         * Bitwise comparison of `float` values or `double` values,
         * as performed by the numeric and atomic update access modes, differ
         * from the primitive `==` operator and the [Float.equals]
         * and [Double.equals] methods, specifically with respect to
         * comparing NaN values or comparing `-0.0` with `+0.0`.
         * Care should be taken when performing a compare and set or a compare
         * and exchange operation with such values since the operation may
         * unexpectedly fail.
         * There are many possible NaN values that are considered to be
         * `NaN` in Java, although no IEEE 754 floating-point operation
         * provided by Java can distinguish between them.  Operation failure can
         * occur if the expected or witness value is a NaN value and it is
         * transformed (perhaps in a platform specific manner) into another NaN
         * value, and thus has a different bitwise representation (see
         * [Float.intBitsToFloat] or [Double.longBitsToDouble] for more
         * details).
         * The values `-0.0` and `+0.0` have different bitwise
         * representations but are considered equal when using the primitive
         * `==` operator.  Operation failure can occur if, for example, a
         * numeric algorithm computes an expected value to be say `-0.0`
         * and previously computed the witness value to be say `+0.0`.
         * @param decl the class that declares the static field
         * @param name the field's name
         * @param type the field's type, of type `T`
         * @return a VarHandle giving access to a static field
         * @throws NoSuchFieldException if the field does not exist
         * @throws IllegalAccessException if access checking fails, or if the field is not `static`
         * @exception SecurityException if a security manager is present and it
         * [refuses access](MethodHandles.Lookup.html#secmgr)
         * @throws NullPointerException if any argument is null
         * @since 9
         */
        @Throws(java.lang.NoSuchFieldException::class, java.lang.IllegalAccessException::class)
        fun findStaticVarHandle(
            decl: java.lang.Class<*>,
            name: String,
            type: java.lang.Class<*>
        ): java.lang.invoke.VarHandle {
            val field: java.lang.reflect.Field? = findFieldOfType(decl, name, type)
            val isStatic = true
            val performAccessChecks = true
            commonFieldChecks(field, decl, type, isStatic, performAccessChecks)
            return StaticFieldVarHandle.create(field)
        }
        // END Android-changed: OpenJDK 9+181 VarHandle API factory method.
        /**
         * Produces an early-bound method handle for a non-static method.
         * The receiver must have a supertype `defc` in which a method
         * of the given name and type is accessible to the lookup class.
         * The method and all its argument types must be accessible to the lookup object.
         * The type of the method handle will be that of the method,
         * without any insertion of an additional receiver parameter.
         * The given receiver will be bound into the method handle,
         * so that every call to the method handle will invoke the
         * requested method on the given receiver.
         *
         *
         * The returned method handle will have
         * [variable arity][MethodHandle.asVarargsCollector] if and only if
         * the method's variable arity modifier bit (`0x0080`) is set
         * *and* the trailing array argument is not the only argument.
         * (If the trailing array argument is the only argument,
         * the given receiver value will be bound to it.)
         *
         *
         * This is equivalent to the following code:
         * <blockquote><pre>`import static java.lang.invoke.MethodHandles.*;
         * import static java.lang.invoke.MethodType.*;
         * ...
         * MethodHandle mh0 = lookup().findVirtual(defc, name, type);
         * MethodHandle mh1 = mh0.bindTo(receiver);
         * MethodType mt1 = mh1.type();
         * if (mh0.isVarargsCollector())
         * mh1 = mh1.asVarargsCollector(mt1.parameterType(mt1.parameterCount()-1));
         * return mh1;
        `</pre></blockquote> *
         * where `defc` is either `receiver.getClass()` or a super
         * type of that class, in which the requested method is accessible
         * to the lookup class.
         * (Note that `bindTo` does not preserve variable arity.)
         * @param receiver the object from which the method is accessed
         * @param name the name of the method
         * @param type the type of the method, with the receiver argument omitted
         * @return the desired method handle
         * @throws NoSuchMethodException if the method does not exist
         * @throws IllegalAccessException if access checking fails
         * or if the method's variable arity modifier bit
         * is set and `asVarargsCollector` fails
         * @exception SecurityException if a security manager is present and it
         * [refuses access](MethodHandles.Lookup.html#secmgr)
         * @throws NullPointerException if any argument is null
         * @see MethodHandle.bindTo
         *
         * @see .findVirtual
         */
        @Throws(java.lang.NoSuchMethodException::class, java.lang.IllegalAccessException::class)
        fun bind(
            receiver: Any,
            name: String,
            type: java.lang.invoke.MethodType
        ): java.lang.invoke.MethodHandle {
            val handle: java.lang.invoke.MethodHandle = findVirtual(receiver.javaClass, name, type)
            var adapter: java.lang.invoke.MethodHandle = handle.bindTo(receiver)
            val adapterType: java.lang.invoke.MethodType = adapter.type()
            if (handle.isVarargsCollector()) {
                adapter = adapter.asVarargsCollector(
                    adapterType.parameterType(adapterType.parameterCount() - 1)
                )
            }
            return adapter
        }

        /**
         * Makes a [direct method handle](MethodHandleInfo.html#directmh)
         * to *m*, if the lookup class has permission.
         * If *m* is non-static, the receiver argument is treated as an initial argument.
         * If *m* is virtual, overriding is respected on every call.
         * Unlike the Core Reflection API, exceptions are *not* wrapped.
         * The type of the method handle will be that of the method,
         * with the receiver type prepended (but only if it is non-static).
         * If the method's `accessible` flag is not set,
         * access checking is performed immediately on behalf of the lookup class.
         * If *m* is not public, do not share the resulting handle with untrusted parties.
         *
         *
         * The returned method handle will have
         * [variable arity][MethodHandle.asVarargsCollector] if and only if
         * the method's variable arity modifier bit (`0x0080`) is set.
         *
         *
         * If *m* is static, and
         * if the returned method handle is invoked, the method's class will
         * be initialized, if it has not already been initialized.
         * @param m the reflected method
         * @return a method handle which can invoke the reflected method
         * @throws IllegalAccessException if access checking fails
         * or if the method's variable arity modifier bit
         * is set and `asVarargsCollector` fails
         * @throws NullPointerException if the argument is null
         */
        @Throws(java.lang.IllegalAccessException::class)
        fun unreflect(m: java.lang.reflect.Method?): java.lang.invoke.MethodHandle {
            if (m == null) {
                throw java.lang.NullPointerException("m == null")
            }
            var methodType: java.lang.invoke.MethodType = java.lang.invoke.MethodType.methodType(
                m.getReturnType(),
                m.getParameterTypes()
            )

            // We should only perform access checks if setAccessible hasn't been called yet.
            if (!m.isAccessible()) {
                checkAccess(
                    m.getDeclaringClass(), m.getDeclaringClass(), m.getModifiers(),
                    m.getName()
                )
            }
            if (java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
                return java.lang.invoke.MethodHandles.Lookup.Companion.createMethodHandle(
                    m,
                    java.lang.invoke.MethodHandle.INVOKE_STATIC,
                    methodType
                )
            } else {
                methodType = methodType.insertParameterTypes(0, m.getDeclaringClass())
                return java.lang.invoke.MethodHandles.Lookup.Companion.createMethodHandle(
                    m,
                    java.lang.invoke.MethodHandle.INVOKE_VIRTUAL,
                    methodType
                )
            }
        }

        /**
         * Produces a method handle for a reflected method.
         * It will bypass checks for overriding methods on the receiver,
         * [as if called](MethodHandles.Lookup.html#equiv) from an `invokespecial`
         * instruction from within the explicitly specified `specialCaller`.
         * The type of the method handle will be that of the method,
         * with a suitably restricted receiver type prepended.
         * (The receiver type will be `specialCaller` or a subtype.)
         * If the method's `accessible` flag is not set,
         * access checking is performed immediately on behalf of the lookup class,
         * as if `invokespecial` instruction were being linked.
         *
         *
         * Before method resolution,
         * if the explicitly specified caller class is not identical with the
         * lookup class, or if this lookup object does not have
         * [private access](MethodHandles.Lookup.html#privacc)
         * privileges, the access fails.
         *
         *
         * The returned method handle will have
         * [variable arity][MethodHandle.asVarargsCollector] if and only if
         * the method's variable arity modifier bit (`0x0080`) is set.
         * @param m the reflected method
         * @param specialCaller the class nominally calling the method
         * @return a method handle which can invoke the reflected method
         * @throws IllegalAccessException if access checking fails
         * or if the method's variable arity modifier bit
         * is set and `asVarargsCollector` fails
         * @throws NullPointerException if any argument is null
         */
        @Throws(java.lang.IllegalAccessException::class)
        fun unreflectSpecial(
            m: java.lang.reflect.Method?,
            specialCaller: java.lang.Class<*>?
        ): java.lang.invoke.MethodHandle {
            if (m == null) {
                throw java.lang.NullPointerException("m == null")
            }
            if (specialCaller == null) {
                throw java.lang.NullPointerException("specialCaller == null")
            }
            if (!m.isAccessible()) {
                // Android-changed: Match Java language 9 behavior where unreflectSpecial continues
                // to require exact caller lookupClass match.
                checkSpecialCaller(specialCaller, null)
            }
            val methodType: java.lang.invoke.MethodType = java.lang.invoke.MethodType.methodType(
                m.getReturnType(),
                m.getParameterTypes()
            )
            return findSpecial(m, methodType, m.getDeclaringClass() /* refc */, specialCaller)
        }

        /**
         * Produces a method handle for a reflected constructor.
         * The type of the method handle will be that of the constructor,
         * with the return type changed to the declaring class.
         * The method handle will perform a `newInstance` operation,
         * creating a new instance of the constructor's class on the
         * arguments passed to the method handle.
         *
         *
         * If the constructor's `accessible` flag is not set,
         * access checking is performed immediately on behalf of the lookup class.
         *
         *
         * The returned method handle will have
         * [variable arity][MethodHandle.asVarargsCollector] if and only if
         * the constructor's variable arity modifier bit (`0x0080`) is set.
         *
         *
         * If the returned method handle is invoked, the constructor's class will
         * be initialized, if it has not already been initialized.
         * @param c the reflected constructor
         * @return a method handle which can invoke the reflected constructor
         * @throws IllegalAccessException if access checking fails
         * or if the method's variable arity modifier bit
         * is set and `asVarargsCollector` fails
         * @throws NullPointerException if the argument is null
         */
        @Throws(java.lang.IllegalAccessException::class)
        fun unreflectConstructor(c: java.lang.reflect.Constructor<*>?): java.lang.invoke.MethodHandle {
            if (c == null) {
                throw java.lang.NullPointerException("c == null")
            }
            if (!c.isAccessible()) {
                checkAccess(
                    c.getDeclaringClass(), c.getDeclaringClass(), c.getModifiers(),
                    c.getName()
                )
            }
            return createMethodHandleForConstructor(c)
        }

        /**
         * Produces a method handle giving read access to a reflected field.
         * The type of the method handle will have a return type of the field's
         * value type.
         * If the field is static, the method handle will take no arguments.
         * Otherwise, its single argument will be the instance containing
         * the field.
         * If the field's `accessible` flag is not set,
         * access checking is performed immediately on behalf of the lookup class.
         *
         *
         * If the field is static, and
         * if the returned method handle is invoked, the field's class will
         * be initialized, if it has not already been initialized.
         * @param f the reflected field
         * @return a method handle which can load values from the reflected field
         * @throws IllegalAccessException if access checking fails
         * @throws NullPointerException if the argument is null
         */
        @Throws(java.lang.IllegalAccessException::class)
        fun unreflectGetter(f: java.lang.reflect.Field): java.lang.invoke.MethodHandle {
            return findAccessor(
                f, f.getDeclaringClass(), f.getType(),
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) java.lang.invoke.MethodHandle.SGET else java.lang.invoke.MethodHandle.IGET,
                !f.isAccessible() /* performAccessChecks */
            )
        }

        /**
         * Produces a method handle giving write access to a reflected field.
         * The type of the method handle will have a void return type.
         * If the field is static, the method handle will take a single
         * argument, of the field's value type, the value to be stored.
         * Otherwise, the two arguments will be the instance containing
         * the field, and the value to be stored.
         * If the field's `accessible` flag is not set,
         * access checking is performed immediately on behalf of the lookup class.
         *
         *
         * If the field is static, and
         * if the returned method handle is invoked, the field's class will
         * be initialized, if it has not already been initialized.
         * @param f the reflected field
         * @return a method handle which can store values into the reflected field
         * @throws IllegalAccessException if access checking fails
         * @throws NullPointerException if the argument is null
         */
        @Throws(java.lang.IllegalAccessException::class)
        fun unreflectSetter(f: java.lang.reflect.Field): java.lang.invoke.MethodHandle {
            return findAccessor(
                f, f.getDeclaringClass(), f.getType(),
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) java.lang.invoke.MethodHandle.SPUT else java.lang.invoke.MethodHandle.IPUT,
                !f.isAccessible() /* performAccessChecks */
            )
        }
        // BEGIN Android-changed: OpenJDK 9+181 VarHandle API factory method.
        /**
         * Produces a VarHandle giving access to a reflected field `f`
         * of type `T` declared in a class of type `R`.
         * The VarHandle's variable type is `T`.
         * If the field is non-static the VarHandle has one coordinate type,
         * `R`.  Otherwise, the field is static, and the VarHandle has no
         * coordinate types.
         *
         *
         * Access checking is performed immediately on behalf of the lookup
         * class, regardless of the value of the field's `accessible`
         * flag.
         *
         *
         * If the field is static, and if the returned VarHandle is operated
         * on, the field's declaring class will be initialized, if it has not
         * already been initialized.
         *
         *
         * Certain access modes of the returned VarHandle are unsupported under
         * the following conditions:
         *
         *  * if the field is declared `final`, then the write, atomic
         * update, numeric atomic update, and bitwise atomic update access
         * modes are unsupported.
         *  * if the field type is anything other than `byte`,
         * `short`, `char`, `int`, `long`,
         * `float`, or `double` then numeric atomic update
         * access modes are unsupported.
         *  * if the field type is anything other than `boolean`,
         * `byte`, `short`, `char`, `int` or
         * `long` then bitwise atomic update access modes are
         * unsupported.
         *
         *
         *
         * If the field is declared `volatile` then the returned VarHandle
         * will override access to the field (effectively ignore the
         * `volatile` declaration) in accordance to its specified
         * access modes.
         *
         *
         * If the field type is `float` or `double` then numeric
         * and atomic update access modes compare values using their bitwise
         * representation (see [Float.floatToRawIntBits] and
         * [Double.doubleToRawLongBits], respectively).
         * @apiNote
         * Bitwise comparison of `float` values or `double` values,
         * as performed by the numeric and atomic update access modes, differ
         * from the primitive `==` operator and the [Float.equals]
         * and [Double.equals] methods, specifically with respect to
         * comparing NaN values or comparing `-0.0` with `+0.0`.
         * Care should be taken when performing a compare and set or a compare
         * and exchange operation with such values since the operation may
         * unexpectedly fail.
         * There are many possible NaN values that are considered to be
         * `NaN` in Java, although no IEEE 754 floating-point operation
         * provided by Java can distinguish between them.  Operation failure can
         * occur if the expected or witness value is a NaN value and it is
         * transformed (perhaps in a platform specific manner) into another NaN
         * value, and thus has a different bitwise representation (see
         * [Float.intBitsToFloat] or [Double.longBitsToDouble] for more
         * details).
         * The values `-0.0` and `+0.0` have different bitwise
         * representations but are considered equal when using the primitive
         * `==` operator.  Operation failure can occur if, for example, a
         * numeric algorithm computes an expected value to be say `-0.0`
         * and previously computed the witness value to be say `+0.0`.
         * @param f the reflected field, with a field of type `T`, and
         * a declaring class of type `R`
         * @return a VarHandle giving access to non-static fields or a static
         * field
         * @throws IllegalAccessException if access checking fails
         * @throws NullPointerException if the argument is null
         * @since 9
         */
        @Throws(java.lang.IllegalAccessException::class)
        fun unreflectVarHandle(f: java.lang.reflect.Field): java.lang.invoke.VarHandle {
            val isStatic: Boolean = java.lang.reflect.Modifier.isStatic(f.getModifiers())
            val performAccessChecks = true
            commonFieldChecks(f, f.getDeclaringClass(), f.getType(), isStatic, performAccessChecks)
            return if (isStatic) StaticFieldVarHandle.create(f) else FieldVarHandle.create(f)
        }
        // END Android-changed: OpenJDK 9+181 VarHandle API factory method.
        /**
         * Cracks a [direct method handle](MethodHandleInfo.html#directmh)
         * created by this lookup object or a similar one.
         * Security and access checks are performed to ensure that this lookup object
         * is capable of reproducing the target method handle.
         * This means that the cracking may fail if target is a direct method handle
         * but was created by an unrelated lookup object.
         * This can happen if the method handle is [caller sensitive](MethodHandles.Lookup.html#callsens)
         * and was created by a lookup object for a different class.
         * @param target a direct method handle to crack into symbolic reference components
         * @return a symbolic reference which can be used to reconstruct this method handle from this lookup object
         * @exception SecurityException if a security manager is present and it
         * [refuses access](MethodHandles.Lookup.html#secmgr)
         * @throws IllegalArgumentException if the target is not a direct method handle or if access checking fails
         * @exception NullPointerException if the target is `null`
         * @see MethodHandleInfo
         *
         * @since 1.8
         */
        fun revealDirect(target: java.lang.invoke.MethodHandle?): java.lang.invoke.MethodHandleInfo {
            val directTarget: java.lang.invoke.MethodHandleImpl =
                java.lang.invoke.MethodHandles.getMethodHandleImpl(target)
            val info: java.lang.invoke.MethodHandleInfo = directTarget.reveal()
            try {
                checkAccess(
                    lookupClass(), info.getDeclaringClass(), info.getModifiers(),
                    info.getName()
                )
            } catch (exception: java.lang.IllegalAccessException) {
                throw java.lang.IllegalArgumentException("Unable to access memeber.", exception)
            }
            return info
        }

        private fun hasPrivateAccess(): Boolean {
            return (allowedModes and java.lang.invoke.MethodHandles.Lookup.Companion.PRIVATE) != 0
        }

        /** Check public/protected/private bits on the symbolic reference class and its member.  */
        @Throws(java.lang.IllegalAccessException::class)
        fun checkAccess(
            refc: java.lang.Class<*>,
            defc: java.lang.Class<*>,
            mods: Int,
            methName: String
        ) {
            var mods = mods
            val allowedModes = allowedModes
            if ((java.lang.reflect.Modifier.isProtected(mods) && (
                        defc == Any::class.java) && ("clone" == methName) &&
                        refc.isArray())
            ) {
                // The JVM does this hack also.
                // (See ClassVerifier::verify_invoke_instructions
                // and LinkResolver::check_method_accessability.)
                // Because the JVM does not allow separate methods on array types,
                // there is no separate method for int[].clone.
                // All arrays simply inherit Object.clone.
                // But for access checking logic, we make Object.clone
                // (normally protected) appear to be public.
                // Later on, when the DirectMethodHandle is created,
                // its leading argument will be restricted to the
                // requested array type.
                // N.B. The return type is not adjusted, because
                // that is *not* the bytecode behavior.
                mods =
                    mods xor (java.lang.reflect.Modifier.PROTECTED or java.lang.reflect.Modifier.PUBLIC)
            }
            if (java.lang.reflect.Modifier.isProtected(mods) && java.lang.reflect.Modifier.isConstructor(
                    mods
                )
            ) {
                // cannot "new" a protected ctor in a different package
                mods = mods xor java.lang.reflect.Modifier.PROTECTED
            }
            if (java.lang.reflect.Modifier.isPublic(mods) && java.lang.reflect.Modifier.isPublic(
                    refc.getModifiers()
                ) && (allowedModes != 0)
            ) return  // common case
            val requestedModes: Int =
                java.lang.invoke.MethodHandles.Lookup.Companion.fixmods(mods) // adjust 0 => PACKAGE
            if ((requestedModes and allowedModes) != 0) {
                if (VerifyAccess.isMemberAccessible(
                        refc,
                        defc,
                        mods,
                        lookupClass(),
                        allowedModes
                    )
                ) return
            } else {
                // Protected members can also be checked as if they were package-private.
                if (((requestedModes and java.lang.invoke.MethodHandles.Lookup.Companion.PROTECTED) != 0) && ((allowedModes and java.lang.invoke.MethodHandles.Lookup.Companion.PACKAGE) != 0
                            ) && VerifyAccess.isSamePackage(defc, lookupClass())
                ) return
            }
            throwMakeAccessException(accessFailedMessage(refc, defc, mods), this)
        }

        fun accessFailedMessage(
            refc: java.lang.Class<*>,
            defc: java.lang.Class<*>,
            mods: Int
        ): String {
            // check the class first:
            var classOK = (java.lang.reflect.Modifier.isPublic(defc.getModifiers()) &&
                    (defc == refc ||
                            java.lang.reflect.Modifier.isPublic(refc.getModifiers())))
            if (!classOK && (allowedModes and java.lang.invoke.MethodHandles.Lookup.Companion.PACKAGE) != 0) {
                classOK = (VerifyAccess.isClassAccessible(
                    defc,
                    lookupClass(),
                    java.lang.invoke.MethodHandles.Lookup.Companion.ALL_MODES
                ) &&
                        (defc == refc ||
                                VerifyAccess.isClassAccessible(
                                    refc,
                                    lookupClass(),
                                    java.lang.invoke.MethodHandles.Lookup.Companion.ALL_MODES
                                )))
            }
            if (!classOK) return "class is not public"
            if (java.lang.reflect.Modifier.isPublic(mods)) return "access to public member failed" // (how?)
            if (java.lang.reflect.Modifier.isPrivate(mods)) return "member is private"
            return if (java.lang.reflect.Modifier.isProtected(mods)) "member is protected" else "member is private to package"
        }

        // Android-changed: checkSpecialCaller assumes that ALLOW_NESTMATE_ACCESS = false,
        // as in upstream OpenJDK.
        //
        // private static final boolean ALLOW_NESTMATE_ACCESS = false;
        // Android-changed: Match java language 9 behavior allowing special access if the reflected
        // class (called 'refc', the class from which the method is being accessed) is an interface
        // and is implemented by the caller.
        @Throws(java.lang.IllegalAccessException::class)
        private fun checkSpecialCaller(
            specialCaller: java.lang.Class<*>,
            refc: java.lang.Class<*>?
        ) {
            // Android-changed: No support for TRUSTED lookups. Also construct the
            // IllegalAccessException by hand because the upstream code implicitly assumes
            // that the lookupClass == specialCaller.
            //
            // if (allowedModes == TRUSTED)  return;
            val isInterfaceLookup = (((refc != null) &&
                    refc.isInterface() &&
                    refc.isAssignableFrom(specialCaller)))
            if (!hasPrivateAccess() || (specialCaller != lookupClass() && !isInterfaceLookup)) {
                throw java.lang.IllegalAccessException(
                    ("no private access for invokespecial : "
                            + specialCaller + ", from" + this)
                )
            }
        }

        @Throws(java.lang.IllegalAccessException::class)
        private fun throwMakeAccessException(message: String, from: Any?) {
            var message = message
            message = message + ": " + toString()
            if (from != null) message += ", from $from"
            throw java.lang.IllegalAccessException(message)
        }

        @Throws(java.lang.NoSuchMethodException::class)
        private fun checkReturnType(
            method: java.lang.reflect.Method,
            methodType: java.lang.invoke.MethodType
        ) {
            if (method.getReturnType() != methodType.rtype()) {
                throw java.lang.NoSuchMethodException(method.getName() + methodType)
            }
        }

        companion object {
            /** A single-bit mask representing `public` access,
             * which may contribute to the result of [lookupModes][.lookupModes].
             * The value, `0x01`, happens to be the same as the value of the
             * `public` [modifier bit][java.lang.reflect.Modifier.PUBLIC].
             */
            val PUBLIC: Int = java.lang.reflect.Modifier.PUBLIC

            /** A single-bit mask representing `private` access,
             * which may contribute to the result of [lookupModes][.lookupModes].
             * The value, `0x02`, happens to be the same as the value of the
             * `private` [modifier bit][java.lang.reflect.Modifier.PRIVATE].
             */
            val PRIVATE: Int = java.lang.reflect.Modifier.PRIVATE

            /** A single-bit mask representing `protected` access,
             * which may contribute to the result of [lookupModes][.lookupModes].
             * The value, `0x04`, happens to be the same as the value of the
             * `protected` [modifier bit][java.lang.reflect.Modifier.PROTECTED].
             */
            val PROTECTED: Int = java.lang.reflect.Modifier.PROTECTED

            /** A single-bit mask representing `package` access (default access),
             * which may contribute to the result of [lookupModes][.lookupModes].
             * The value is `0x08`, which does not correspond meaningfully to
             * any particular [modifier bit][java.lang.reflect.Modifier].
             */
            val PACKAGE: Int = java.lang.reflect.Modifier.STATIC
            private val ALL_MODES: Int =
                (java.lang.invoke.MethodHandles.Lookup.Companion.PUBLIC or java.lang.invoke.MethodHandles.Lookup.Companion.PRIVATE or java.lang.invoke.MethodHandles.Lookup.Companion.PROTECTED or java.lang.invoke.MethodHandles.Lookup.Companion.PACKAGE)

            // Android-note: Android has no notion of a trusted lookup. If required, such lookups
            // are performed by the runtime. As a result, we always use lookupClass, which will always
            // be non-null in our implementation.
            //
            // private static final int TRUSTED   = -1;
            private fun fixmods(mods: Int): Int {
                var mods = mods
                mods =
                    mods and (java.lang.invoke.MethodHandles.Lookup.Companion.ALL_MODES - java.lang.invoke.MethodHandles.Lookup.Companion.PACKAGE)
                return if ((mods != 0)) mods else java.lang.invoke.MethodHandles.Lookup.Companion.PACKAGE
            }
            // Make sure outer class is initialized first.
            //
            // Android-changed: Removed unnecessary reference to IMPL_NAMES.
            // static { IMPL_NAMES.getClass(); }
            /** Version of lookup which is trusted minimally.
             * It can only be used to create method handles to
             * publicly accessible members.
             */
            val PUBLIC_LOOKUP: java.lang.invoke.MethodHandles.Lookup =
                java.lang.invoke.MethodHandles.Lookup(
                    Any::class.java, java.lang.invoke.MethodHandles.Lookup.Companion.PUBLIC
                )

            /** Package-private version of lookup which is trusted.  */
            val IMPL_LOOKUP: java.lang.invoke.MethodHandles.Lookup =
                java.lang.invoke.MethodHandles.Lookup(
                    Any::class.java, java.lang.invoke.MethodHandles.Lookup.Companion.ALL_MODES
                )

            private fun checkUnprivilegedlookupClass(
                lookupClass: java.lang.Class<*>,
                allowedModes: Int
            ) {
                val name: String = lookupClass.getName()
                if (name.startsWith("java.lang.invoke.")) throw java.lang.invoke.MethodHandleStatics.newIllegalArgumentException(
                    "illegal lookupClass: $lookupClass"
                )

                // For caller-sensitive MethodHandles.lookup()
                // disallow lookup more restricted packages
                //
                // Android-changed: The bootstrap classloader isn't null.
                if (allowedModes == java.lang.invoke.MethodHandles.Lookup.Companion.ALL_MODES &&
                    lookupClass.getClassLoader() === Any::class.java.getClassLoader()
                ) {
                    if (((name.startsWith("java.")
                                && !name.startsWith("java.io.ObjectStreamClass")
                                && !name.startsWith("java.util.concurrent.")
                                && name != "java.lang.Daemons\$FinalizerWatchdogDaemon"
                                && name != "java.lang.runtime.ObjectMethods"
                                && name != "java.lang.Thread")) ||
                        ((name.startsWith("sun.")
                                && !name.startsWith("sun.invoke.")
                                && name != "sun.reflect.ReflectionFactory"))
                    ) {
                        throw MethodHandleStatics.newIllegalArgumentException("illegal lookupClass: $lookupClass")
                    }
                }
            }

            private fun createMethodHandle(
                method: java.lang.reflect.Method, handleKind: Int,
                methodType: java.lang.invoke.MethodType
            ): java.lang.invoke.MethodHandle {
                val mh: java.lang.invoke.MethodHandle =
                    java.lang.invoke.MethodHandleImpl(method.getArtMethod(), handleKind, methodType)
                return if (method.isVarArgs()) {
                    VarargsCollector(mh)
                } else {
                    mh
                }
            }

            private fun initMethodType(constructorType: java.lang.invoke.MethodType): java.lang.invoke.MethodType {
                // Returns a MethodType appropriate for class <init>
                // methods. Constructor MethodTypes have the form
                // (PT1,PT2,...)C and class <init> MethodTypes have the
                // form (C,PT1,PT2,...)V.
                assert(constructorType.rtype() !== Void.TYPE)

                // Insert constructorType C as the first parameter type in
                // the MethodType for <init>.
                val initPtypes: Array<java.lang.Class<*>> =
                    arrayOfNulls<java.lang.Class<*>>(constructorType.ptypes().length + 1)
                initPtypes[0] = constructorType.rtype()
                java.lang.System.arraycopy(
                    constructorType.ptypes(), 0, initPtypes, 1,
                    constructorType.ptypes().length
                )

                // Set the return type for the <init> MethodType to be void.
                return java.lang.invoke.MethodType.methodType(Void.TYPE, initPtypes)
            }
        }
    }
}

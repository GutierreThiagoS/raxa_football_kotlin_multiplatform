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
 * A `TimeUnit` represents time durations at a given unit of
 * granularity and provides utility methods to convert across units,
 * and to perform timing and delay operations in these units.  A
 * `TimeUnit` does not maintain time information, but only
 * helps organize and use time representations that may be maintained
 * separately across various contexts.  A nanosecond is defined as one
 * thousandth of a microsecond, a microsecond as one thousandth of a
 * millisecond, a millisecond as one thousandth of a second, a minute
 * as sixty seconds, an hour as sixty minutes, and a day as twenty four
 * hours.
 *
 *
 * A `TimeUnit` is mainly used to inform time-based methods
 * how a given timing parameter should be interpreted. For example,
 * the following code will timeout in 50 milliseconds if the [ ] is not available:
 *
 * <pre> `Lock lock = ...;
 * if (lock.tryLock(50L, TimeUnit.MILLISECONDS)) ...`</pre>
 *
 * while this code will timeout in 50 seconds:
 * <pre> `Lock lock = ...;
 * if (lock.tryLock(50L, TimeUnit.SECONDS)) ...`</pre>
 *
 * Note however, that there is no guarantee that a particular timeout
 * implementation will be able to notice the passage of time at the
 * same granularity as the given `TimeUnit`.
 *
 * @since 1.5
 * @author Doug Lea
 */




class TimeUnit(/*
     * Instances cache conversion ratios and saturation cutoffs for
     * the units up through SECONDS. Other cases compute them, in
     * method cvt.
     */
                    private val scale: Long
) {
    /**
     * Time unit representing one thousandth of a microsecond.
     */
//    NANOSECONDS(NANO_SCALE),

    /**
     * Time unit representing one thousandth of a millisecond.
     */
//    MICROSECONDS(MICRO_SCALE),

    /**
     * Time unit representing one thousandth of a second.
     */
//    MILLISECONDS(MILLI_SCALE),

    /**
     * Time unit representing one second.
     */
//    SECONDS(SECOND_SCALE),

    /**
     * Time unit representing sixty seconds.
     * @since 1.6
     */
//    MINUTES(MINUTE_SCALE),

    /**
     * Time unit representing sixty minutes.
     * @since 1.6
     */
//    HOURS(HOUR_SCALE),

    /**
     * Time unit representing twenty four hours.
     * @since 1.6
     */
//    DAYS(DAY_SCALE);

    private val maxNanos: Long
    private val maxMicros: Long
    private val maxMillis: Long
    private val maxSecs: Long
    private val microRatio: Long
    private val milliRatio: Int // fits in 32 bits

    private val secRatio: Int // fits in 32 bits


    init {
        maxNanos = Long.MAX_VALUE / scale
        val ur: Long = if (scale >= MICRO_SCALE) scale / MICRO_SCALE else MICRO_SCALE / scale
        microRatio = ur
        maxMicros = Long.MAX_VALUE / ur
        val mr: Long =
            if (scale >= MILLI_SCALE) scale / MILLI_SCALE else MILLI_SCALE / scale
        milliRatio = mr.toInt()
        maxMillis = Long.MAX_VALUE / mr
        val sr: Long =
            if (scale >= SECOND_SCALE) scale / SECOND_SCALE else SECOND_SCALE / scale
        secRatio = sr.toInt()
        maxSecs = Long.MAX_VALUE / sr
    }

    /**
     * Converts the given time duration in the given unit to this unit.
     * Conversions from finer to coarser granularities truncate, so
     * lose precision. For example, converting `999` milliseconds
     * to seconds results in `0`. Conversions from coarser to
     * finer granularities with arguments that would numerically
     * overflow saturate to `Long.MIN_VALUE` if negative or
     * `Long.MAX_VALUE` if positive.
     *
     *
     * For example, to convert 10 minutes to milliseconds, use:
     * `TimeUnit.MILLISECONDS.convert(10L, TimeUnit.MINUTES)`
     *
     * @param sourceDuration the time duration in the given `sourceUnit`
     * @param sourceUnit the unit of the `sourceDuration` argument
     * @return the converted duration in this unit,
     * or `Long.MIN_VALUE` if conversion would negatively overflow,
     * or `Long.MAX_VALUE` if it would positively overflow.
     */
    /*fun convert(sourceDuration: Long): Long {
        return when (this) {
            NANOSECONDS -> this.toNanos(sourceDuration)
            MICROSECONDS -> this.toMicros(sourceDuration)
            MILLISECONDS -> this.toMillis(sourceDuration)
            SECONDS -> this.toSeconds(sourceDuration)
            else -> cvt(
                sourceDuration,
                scale,
                this.scale
            )
        }
    }*/

    /**
     * Converts the given time duration to this unit.
     *
     *
     * For any TimeUnit `unit`,
     * `unit.convert(Duration.ofNanos(n))`
     * is equivalent to
     * `unit.convert(n, NANOSECONDS)`, and
     * `unit.convert(Duration.of(n, unit.toChronoUnit()))`
     * is equivalent to `n` (in the absence of overflow).
     *
     * @apiNote
     * This method differs from [Duration.toNanos] in that it
     * does not throw [ArithmeticException] on numeric overflow.
     *
     * @param duration the time duration
     * @return the converted duration in this unit,
     * or `Long.MIN_VALUE` if conversion would negatively overflow,
     * or `Long.MAX_VALUE` if it would positively overflow.
     * @throws NullPointerException if `duration` is null
     * @see Duration.of
     * @since 11
     */
    /*fun convert(duration: java.time.Duration): Long {
        var secs: Long = duration.getSeconds()
        var nano: Int = duration.getNano()
        if (secs < 0 && nano > 0) {
            // use representation compatible with integer division
            secs++
            nano -= SECOND_SCALE.toInt()
        }
        val s: Long
        val nanoVal: Long
        // Optimize for the common case - NANOSECONDS without overflow
        if (this == NANOSECONDS) nanoVal =
            nano.toLong() else if (scale.also {
                s = it
            } < SECOND_SCALE) nanoVal =
            nano / s else return if (this == SECONDS) secs else secs / secRatio
        val `val` = secs * secRatio + nanoVal
        return if (secs < maxSecs && secs > -maxSecs || secs == maxSecs && `val` > 0 || secs == -maxSecs && `val` < 0) `val` else if (secs > 0) Long.MAX_VALUE else Long.MIN_VALUE
    }*/

    /**
     * Equivalent to
     * [NANOSECONDS.convert(duration, this)][.convert].
     * @param duration the duration
     * @return the converted duration,
     * or `Long.MIN_VALUE` if conversion would negatively overflow,
     * or `Long.MAX_VALUE` if it would positively overflow.
     */
    fun toNanos(duration: Long): Long {
        var s: Long
        var m: Long
        return if (scale.also {
                s = it
            } == NANO_SCALE) duration else if (duration > maxNanos.also {
                m = it
            }) Long.MAX_VALUE else if (duration < -m) Long.MIN_VALUE else duration * s
    }

    /**
     * Equivalent to
     * [MICROSECONDS.convert(duration, this)][.convert].
     * @param duration the duration
     * @return the converted duration,
     * or `Long.MIN_VALUE` if conversion would negatively overflow,
     * or `Long.MAX_VALUE` if it would positively overflow.
     */
    fun toMicros(duration: Long): Long {
        var s: Long
        var m: Long
        return if (scale.also {
                s = it
            } <= MICRO_SCALE) if (s == MICRO_SCALE) duration else duration / microRatio else if (duration > maxMicros.also {
                m = it
            }) Long.MAX_VALUE else if (duration < -m) Long.MIN_VALUE else duration * microRatio
    }

    /**
     * Equivalent to
     * [MILLISECONDS.convert(duration, this)][.convert].
     * @param duration the duration
     * @return the converted duration,
     * or `Long.MIN_VALUE` if conversion would negatively overflow,
     * or `Long.MAX_VALUE` if it would positively overflow.
     */
    fun toMillis(duration: Long): Long {
        var s: Long
        var m: Long
        return if (scale.also {
                s = it
            } <= MILLI_SCALE) if (s == MILLI_SCALE) duration else duration / milliRatio else if (duration > maxMillis.also {
                m = it
            }) Long.MAX_VALUE else if (duration < -m) Long.MIN_VALUE else duration * milliRatio
    }

    /**
     * Equivalent to
     * [SECONDS.convert(duration, this)][.convert].
     * @param duration the duration
     * @return the converted duration,
     * or `Long.MIN_VALUE` if conversion would negatively overflow,
     * or `Long.MAX_VALUE` if it would positively overflow.
     */
    fun toSeconds(duration: Long): Long {
        var s: Long
        var m: Long
        return if (scale.also {
                s = it
            } <= SECOND_SCALE) if (s == SECOND_SCALE) duration else duration / secRatio else if (duration > maxSecs.also {
                m = it
            }) Long.MAX_VALUE else if (duration < -m) Long.MIN_VALUE else duration * secRatio
    }

    /**
     * Equivalent to
     * [MINUTES.convert(duration, this)][.convert].
     * @param duration the duration
     * @return the converted duration,
     * or `Long.MIN_VALUE` if conversion would negatively overflow,
     * or `Long.MAX_VALUE` if it would positively overflow.
     * @since 1.6
     */
    fun toMinutes(duration: Long): Long {
        return cvt(
            duration,
            MINUTE_SCALE,
            scale
        )
    }

    /**
     * Equivalent to
     * [HOURS.convert(duration, this)][.convert].
     * @param duration the duration
     * @return the converted duration,
     * or `Long.MIN_VALUE` if conversion would negatively overflow,
     * or `Long.MAX_VALUE` if it would positively overflow.
     * @since 1.6
     */
    fun toHours(duration: Long): Long {
        return cvt(
            duration,
            HOUR_SCALE,
            scale
        )
    }

    /**
     * Equivalent to
     * [DAYS.convert(duration, this)][.convert].
     * @param duration the duration
     * @return the converted duration
     * @since 1.6
     */
    fun toDays(duration: Long): Long {
        return cvt(
            duration,
            DAY_SCALE,
            scale
        )
    }

    /**
     * Utility to compute the excess-nanosecond argument to wait,
     * sleep, join.
     * @param d the duration
     * @param m the number of milliseconds
     * @return the number of nanoseconds
     */
    private fun excessNanos(d: Long, m: Long): Int {
        var s: Long
        return if (scale.also {
                s = it
            } == NANO_SCALE) (d - m * MILLI_SCALE).toInt() else if (s == MICRO_SCALE) (d * 1000L - m * MILLI_SCALE).toInt() else 0
    }

    /**
     * Performs a timed [Object.wait]
     * using this time unit.
     * This is a convenience method that converts timeout arguments
     * into the form required by the `Object.wait` method.
     *
     *
     * For example, you could implement a blocking `poll` method
     * (see [BlockingQueue.poll])
     * using:
     *
     * <pre> `public E poll(long timeout, TimeUnit unit)
     * throws InterruptedException {
     * synchronized (lock) {
     * while (isEmpty()) {
     * unit.timedWait(lock, timeout);
     * ...
     * }
     * }
     * }`</pre>
     *
     * @param obj the object to wait on
     * @param timeout the maximum time to wait. If less than
     * or equal to zero, do not wait at all.
     * @throws InterruptedException if interrupted while waiting
     */
//    @Throws(java.lang.InterruptedException::class)
    /*fun timedWait(obj: Any, timeout: Long) {
        if (timeout > 0) {
            val ms = toMillis(timeout)
            val ns = excessNanos(timeout, ms)
            (obj as Any).wait(ms, ns)
        }
    }*/

    /**
     * Performs a timed [Thread.join]
     * using this time unit.
     * This is a convenience method that converts time arguments into the
     * form required by the `Thread.join` method.
     *
     * @param thread the thread to wait for
     * @param timeout the maximum time to wait. If less than
     * or equal to zero, do not wait at all.
     * @throws InterruptedException if interrupted while waiting
     */
//    @Throws(java.lang.InterruptedException::class)
    /*fun timedJoin(thread: java.lang.Thread, timeout: Long) {
        if (timeout > 0) {
            val ms = toMillis(timeout)
            val ns = excessNanos(timeout, ms)
            thread.join(ms, ns)
        }
    }*/

    /**
     * Performs a [Thread.sleep] using
     * this time unit.
     * This is a convenience method that converts time arguments into the
     * form required by the `Thread.sleep` method.
     *
     * @param timeout the minimum time to sleep. If less than
     * or equal to zero, do not sleep at all.
     * @throws InterruptedException if interrupted while sleeping
     */
//    @Throws(InterruptedException::class)
    /*fun sleep(timeout: Long) {
        if (timeout > 0) {
            val ms = toMillis(timeout)
            val ns = excessNanos(timeout, ms)
            java.lang.Thread.sleep(ms, ns)
        }
    }*/

    /**
     * Converts this `TimeUnit` to the equivalent `ChronoUnit`.
     *
     * @return the converted equivalent ChronoUnit
     * @since 9
     */
    /*fun toChronoUnit(): ChronoUnit {
        return when (this) {
            NANOSECONDS -> ChronoUnit.NANOS
            MICROSECONDS -> ChronoUnit.MICROS
            MILLISECONDS -> ChronoUnit.MILLIS
            SECONDS -> ChronoUnit.SECONDS
            MINUTES -> ChronoUnit.MINUTES
            HOURS -> ChronoUnit.HOURS
            DAYS -> ChronoUnit.DAYS
            else -> throw Exception()
//            else -> throw java.lang.AssertionError()
        }
    }*/

    companion object {
        // Scales as constants
        const val NANO_SCALE = 1L
        const val MICRO_SCALE: Long = 1000L * NANO_SCALE
        const val MILLI_SCALE: Long = 1000L * MICRO_SCALE
        const val SECOND_SCALE: Long = 1000L * MILLI_SCALE
        const val MINUTE_SCALE: Long = 60L * SECOND_SCALE
        const val HOUR_SCALE: Long = 60L * MINUTE_SCALE
        const val DAY_SCALE: Long = 24L * HOUR_SCALE

        const val NANOSECONDS = NANO_SCALE
        const val MICROSECONDS: Long = MICRO_SCALE
        const val MILLISECONDS: Long = MILLI_SCALE
        const val SECONDS: Long = SECOND_SCALE
        const val MINUTES: Long = MINUTE_SCALE
        const val HOURS: Long = HOUR_SCALE
        const val DAYS: Long = DAY_SCALE
        /**
         * Time unit representing one thousandth of a microsecond.
         */
//    NANOSECONDS(NANO_SCALE),

        /**
         * Time unit representing one thousandth of a millisecond.
         */
//    MICROSECONDS(MICRO_SCALE),

        /**
         * Time unit representing one thousandth of a second.
         */
//    MILLISECONDS(MILLI_SCALE),

        /**
         * Time unit representing one second.
         */
//    SECONDS(SECOND_SCALE),

        /**
         * Time unit representing sixty seconds.
         * @since 1.6
         */
//    MINUTES(MINUTE_SCALE),

        /**
         * Time unit representing sixty minutes.
         * @since 1.6
         */
//    HOURS(HOUR_SCALE),

        /**
         * Time unit representing twenty four hours.
         * @since 1.6
         */
//    DAYS(DAY_SCALE);
        /**
         * General conversion utility.
         *
         * @param d duration
         * @param dst result unit scale
         * @param src source unit scale
         */
        private fun cvt(d: Long, dst: Long, src: Long): Long {
            var r: Long
            var m: Long
            return if (src == dst) d else if (src < dst) d / (dst / src) else if (d > (Long.MAX_VALUE / (src / dst).also {
                    r = it
                }).also { m = it }) Long.MAX_VALUE else if (d < -m) Long.MIN_VALUE else d * r
        }

        /**
         * Converts a `ChronoUnit` to the equivalent `TimeUnit`.
         *
         * @param chronoUnit the ChronoUnit to convert
         * @return the converted equivalent TimeUnit
         * @throws IllegalArgumentException if `chronoUnit` has no
         * equivalent TimeUnit
         * @throws NullPointerException if `chronoUnit` is null
         * @since 9
         */
       /* fun of(chronoUnit: ChronoUnit): TimeUnit {
            return when (chronoUnit) {
                ChronoUnit.NANOS -> NANOSECONDS
                ChronoUnit.MICROS -> MICROSECONDS
                ChronoUnit.MILLIS -> MILLISECONDS
                ChronoUnit.SECONDS -> SECONDS
                ChronoUnit.MINUTES -> MINUTES
                ChronoUnit.HOURS -> HOURS
                ChronoUnit.DAYS -> DAYS
                else -> throw IllegalArgumentException(
                    "No TimeUnit equivalent for $chronoUnit"
                )
            }
        }*/
    }
}

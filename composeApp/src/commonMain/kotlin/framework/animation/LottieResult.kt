package framework.animation

/**
 * Contains class to hold the resulting value of an async task or an exception if it failed.
 *
 *
 * Either value or exception will be non-null.
 */
class LottieResult<V> {
    val value: V?
    val exception: Throwable?

    constructor(value: V) {
        this.value = value
        exception = null
    }

    constructor(exception: Throwable?) {
        this.exception = exception
        value = null
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is LottieResult<*>) {
            return false
        }
        val that: LottieResult<*> = o
        if (value != null && value == that.value) {
            return true
        }
        return if (exception != null && that.exception != null) {
            exception.toString() == exception.toString()
        } else false
    }

    override fun hashCode(): Int {
        return arrayOf(value, exception).contentHashCode()
    }
}

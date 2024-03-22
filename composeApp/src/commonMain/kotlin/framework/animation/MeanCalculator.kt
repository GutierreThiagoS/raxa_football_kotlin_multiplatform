package framework.animation

/**
 * Class to calculate the average in a stream of numbers on a continuous basis.
 */
class MeanCalculator {
    private var sum = 0f
    private var n = 0

    fun add(number: Float) {
        sum += number
        n++
        if (n == Int.MAX_VALUE) {
            sum /= 2f
            n /= 2
        }
    }

    fun getMean(): Float {
        return if (n == 0) {
            0f
        } else sum / n.toFloat()
    }
}
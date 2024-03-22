package framework.animation.model

class Marker(val name: String, val startFrame: Float, val durationFrames: Float) {

    fun matchesName(name: String?): Boolean {
        return if (this.name.equals(name, ignoreCase = true)) {
            true
        } else this.name.endsWith(CARRIAGE_RETURN) && this.name.substring(
            0,
            this.name.length - 1
        ).equals(name, ignoreCase = true)

        // It is easy for a designer to accidentally include an extra newline which will cause the name to not match what they would
        // expect. This is a convenience to precent unneccesary confusion.
    }

    companion object {
        private const val CARRIAGE_RETURN = "\r"
    }
}

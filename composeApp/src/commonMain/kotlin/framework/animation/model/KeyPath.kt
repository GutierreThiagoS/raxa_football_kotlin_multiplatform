package framework.animation.model

import framework.animation.annotation.CheckResult
import framework.animation.annotation.RestrictTo

/**
 * Defines which content to target.
 * The keypath can contain wildcards ('*') with match exactly 1 item.
 * or globstars ('**') which match 0 or more items. or KeyPath.COMPOSITION
 * to represent the root composition layer.
 *
 *
 * For example, if your content were arranged like this:
 * Gabriel (Shape Layer)
 * Body (Shape Group)
 * Left Hand (Shape)
 * Fill (Fill)
 * Transform (Transform)
 * ...
 * Brandon (Shape Layer)
 * Body (Shape Group)
 * Left Hand (Shape)
 * Fill (Fill)
 * Transform (Transform)
 * ...
 *
 *
 *
 *
 * You could:
 * Match Gabriel left hand fill:
 * new KeyPath("Gabriel", "Body", "Left Hand", "Fill");
 * Match Gabriel and Brandon's left hand fill:
 * new KeyPath("*", "Body", Left Hand", "Fill");
 * Match anything with the name Fill:
 * new KeyPath("**", "Fill");
 * Target the the root composition layer:
 * KeyPath.COMPOSITION
 *
 *
 *
 *
 * NOTE: Content that are part of merge paths or repeaters cannot currently be resolved with
 * a [KeyPath]. This may be fixed in the future.
 */
class KeyPath {
    private val keys: MutableList<String>
    private var resolvedElement: KeyPathElement? = null

    constructor(vararg keys: String) {
        this.keys = mutableListOf(*keys)
    }

    /**
     * Copy constructor. Copies keys as well.
     */
    private constructor(keyPath: KeyPath) {
        keys = ArrayList(keyPath.keys)
        resolvedElement = keyPath.resolvedElement
    }

    /**
     * Returns a new KeyPath with the key added.
     * This is used during keypath resolution. Children normally don't know about all of their parent
     * elements so this is used to keep track of the fully qualified keypath.
     * This returns a key keypath because during resolution, the full keypath element tree is walked
     * and if this modified the original copy, it would remain after popping back up the element tree.
     */
    @CheckResult
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun addKey(key: String): KeyPath {
        val newKeyPath: KeyPath = KeyPath(this)
        newKeyPath.keys.add(key)
        return newKeyPath
    }

    /**
     * Return a new KeyPath with the element resolved to the specified [KeyPathElement].
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun resolve(element: KeyPathElement): KeyPath {
        val keyPath: KeyPath = KeyPath(this)
        keyPath.resolvedElement = element
        return keyPath
    }

    /**
     * Returns a [KeyPathElement] that this has been resolved to. KeyPaths get resolved with
     * resolveKeyPath on LottieDrawable or LottieAnimationView.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun getResolvedElement(): KeyPathElement? {
        return resolvedElement
    }

    /**
     * Returns whether they key matches at the specified depth.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun matches(key: String, depth: Int): Boolean {
        if (isContainer(key)) {
            // This is an artificial layer we programatically create.
            return true
        }
        if (depth >= keys.size) {
            return false
        }
        return if (keys[depth] == key || keys[depth] == "**" || keys[depth] == "*") {
            true
        } else false
    }

    /**
     * For a given key and depth, returns how much the depth should be incremented by when
     * resolving a keypath to children.
     *
     *
     * This can be 0 or 2 when there is a globstar and the next key either matches or doesn't match
     * the current key.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun incrementDepthBy(key: String, depth: Int): Int {
        if (isContainer(key)) {
            // If it's a container then we added programatically and it isn't a part of the keypath.
            return 0
        }
        if (keys[depth] != "**") {
            // If it's not a globstar then it is part of the keypath.
            return 1
        }
        if (depth == keys.size - 1) {
            // The last key is a globstar.
            return 0
        }
        return if (keys[depth + 1] == key) {
            // We are a globstar and the next key is our current key so consume both.
            2
        } else 0
    }

    /**
     * Returns whether the key at specified depth is fully specific enough to match the full set of
     * keys in this keypath.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun fullyResolvesTo(key: String, depth: Int): Boolean {
        if (depth >= keys.size) {
            return false
        }
        val isLastDepth = depth == keys.size - 1
        val keyAtDepth = keys[depth]
        val isGlobstar = keyAtDepth == "**"
        if (!isGlobstar) {
            val matches = keyAtDepth == key || keyAtDepth == "*"
            return (isLastDepth || depth == keys.size - 2 && endsWithGlobstar()) && matches
        }
        val isGlobstarButNextKeyMatches = !isLastDepth && keys[depth + 1] == key
        if (isGlobstarButNextKeyMatches) {
            return depth == keys.size - 2 || depth == keys.size - 3 && endsWithGlobstar()
        }
        if (isLastDepth) {
            return true
        }
        return if (depth + 1 < keys.size - 1) {
            // We are a globstar but there is more than 1 key after the globstar we we can't fully match.
            false
        } else keys[depth + 1] == key
        // Return whether the next key (which we now know is the last one) is the same as the current
        // key.
    }

    /**
     * Returns whether the keypath resolution should propagate to children. Some keypaths resolve
     * to content other than leaf contents (such as a layer or content group transform) so sometimes
     * this will return false.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun propagateToChildren(key: String, depth: Int): Boolean {
        return if ("__container" == key) {
            true
        } else depth < keys.size - 1 || keys[depth] == "**"
    }

    /**
     * We artificially create some container groups (like a root ContentGroup for the entire animation
     * and for the contents of a ShapeLayer).
     */
    private fun isContainer(key: String): Boolean {
        return "__container" == key
    }

    private fun endsWithGlobstar(): Boolean {
        return keys[keys.size - 1] == "**"
    }

    fun keysToString(): String {
        return keys.toString()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null ) {
            return false
        }
        val keyPath: KeyPath = o as KeyPath
        if (keys != keyPath.keys) {
            return false
        }
        return if (resolvedElement != null) resolvedElement == keyPath.resolvedElement else keyPath.resolvedElement == null
    }

    override fun hashCode(): Int {
        var result = keys.hashCode()
        result = 31 * result + if (resolvedElement != null) resolvedElement.hashCode() else 0
        return result
    }

    override fun toString(): String {
        return "KeyPath{" + "keys=" + keys + ",resolved=" + (resolvedElement != null) + '}'
    }

    companion object {
        /**
         * A singleton KeyPath that targets on the root composition layer.
         * This is useful if you want to apply transformer to the animation as a whole.
         */
        val COMPOSITION: KeyPath =
            KeyPath("COMPOSITION")
    }
}

package framework.animation.model

import framework.animation.annotation.RestrictTo
import framework.animation.value.LottieValueCallback

/**
 * Any item that can be a part of a [KeyPath] should implement this.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
interface KeyPathElement {
    /**
     * Called recursively during keypath resolution.
     *
     *
     * The overridden method should just call:
     * MiscUtils.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath, this);
     *
     * @param keyPath               The full keypath being resolved.
     * @param depth                 The current depth that this element should be checked at in the keypath.
     * @param accumulator           A list of fully resolved keypaths. If this element fully matches the
     * keypath then it should add itself to this list.
     * @param currentPartialKeyPath A keypath that contains all parent element of this one.
     * This element should create a copy of this and append itself
     * with KeyPath#addKey when it adds itself to the accumulator
     * or propagates resolution to its children.
     */
    fun resolveKeyPath(
        keyPath: KeyPath?,
        depth: Int,
        accumulator: List<KeyPath?>?,
        currentPartialKeyPath: KeyPath?
    )

    /**
     * The overridden method should handle appropriate properties and set value callbacks on their
     * animations.
     */
    fun <T> addValueCallback(property: T, callback: LottieValueCallback<T>?)
}
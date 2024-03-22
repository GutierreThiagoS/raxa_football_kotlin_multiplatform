package framework.animation.model.animatable


/**
 * Denotes that the annotated method should only be called on a worker thread. If the annotated
 * element is a class, then all methods in the class should be called on a worker thread.
 *
 * Example:
 * ```
 * @WorkerThread
 * protected abstract FilterResults performFiltering(CharSequence constraint);
 * ```
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.VALUE_PARAMETER
)
annotation class WorkerThread
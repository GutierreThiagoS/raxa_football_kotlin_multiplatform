package framework.animation

import framework.animation.annotation.RestrictTo
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image

/**
 * Data class describing an image asset embedded in a Lottie json file.
 */
class LottieImageAsset @RestrictTo(RestrictTo.Scope.LIBRARY) constructor(
    val width: Int,
    val height: Int,
    /**
     * The reference id in the json file.
     */
    val id: String,
    val fileName: String,
    @get:Suppress("unused") val dirName: String
) {

    /**
     * Pre-set a bitmap for this asset
     */
    private var bitmap: Bitmap? = null

    /**
     * Returns the bitmap that has been stored for this image asset if one was explicitly set.
     */
    fun getBitmap(): Bitmap? {
        return bitmap
    }

    /**
     * Permanently sets the bitmap on this LottieImageAsset. This will:
     * 1) Overwrite any existing Bitmaps.
     * 2) Apply to *all* animations that use this LottieComposition.
     *
     * If you only want to replace the bitmap for this animation, use dynamic properties
     * with [LottieProperty.IMAGE].
     */
    fun setBitmap(bitmap: Bitmap?) {
        this.bitmap = bitmap
    }

    /**
     * Returns a new [LottieImageAsset] with the same properties as this one but with the
     * dimensions and bitmap scaled.
     */
    fun copyWithScale(scale: Float): LottieImageAsset {
        val newAsset = LottieImageAsset(
            (width * scale).toInt(), (height * scale).toInt(),
            id,
            fileName,
            dirName
        )
        if (bitmap != null) {
          /*  val scaledBitmap: Bitmap = Bitmap.createScaledBitmap(
                bitmap,
                newAsset.width,
                newAsset.height,
                true
            )*/
            val scaledBitmap2: Bitmap = Bitmap.makeFromImage(
                Image.makeFromBitmap(bitmap!!)
            )
            newAsset.setBitmap(scaledBitmap2)
        }
        return newAsset
    }

    /**
     * Returns whether this asset has an embedded Bitmap or whether the fileName is a base64 encoded bitmap.
     */
    fun hasBitmap(): Boolean {
        return bitmap != null || fileName.startsWith("data:") && fileName.indexOf("base64,") > 0
    }
}

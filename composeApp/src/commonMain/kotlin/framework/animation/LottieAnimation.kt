package framework.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.skottie.Animation
import org.jetbrains.skia.sksg.InvalidationController
import kotlin.jvm.JvmOverloads
import kotlin.math.roundToInt

// Copy of https://github.com/JetBrains/skia/blob/main/platform_tools/android/apps/skottie/src/main/res/raw/ripple_loading_animation.json
private const val lottieData = """
{"v":"4.10.1","fr":60,"ip":0,"op":120,"w":800,"h":800,"nm":"loading_animation","ddd":0,"assets":[],"layers":[{"ddd":0,"ind":1,"ty":4,"nm":"Shape Layer 5","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":23,"s":[20],"e":[100]},{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":34,"s":[100],"e":[20]},{"t":69}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":0,"k":[400,400,0],"ix":2},"a":{"a":0,"k":[0,0,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"d":1,"ty":"el","s":{"a":1,"k":[{"i":{"x":[0,0],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0_1_0p333_0","0_1_0p333_0"],"t":23,"s":[400,400],"e":[440,440]},{"i":{"x":[0.009,0.009],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0p009_1_0p333_0","0p009_1_0p333_0"],"t":34,"s":[440,440],"e":[400,400]},{"t":59}],"ix":2},"p":{"a":0,"k":[0,0],"ix":3},"nm":"Ellipse Path 1","mn":"ADBE Vector Shape - Ellipse","hd":false},{"ty":"tr","p":{"a":0,"k":[0,0],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Ellipse 1","np":4,"cix":2,"ix":1,"mn":"ADBE Vector Group","hd":false},{"ty":"gs","o":{"a":0,"k":100,"ix":9},"w":{"a":1,"k":[{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":23,"s":[5],"e":[10]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":34,"s":[10],"e":[5]},{"t":59}],"ix":10},"g":{"p":3,"k":{"a":0,"k":[0,0,0.627,1,0.5,0.496,0.314,1,1,0.992,0,1],"ix":8}},"s":{"a":0,"k":[0,0],"ix":4},"e":{"a":0,"k":[100,0],"ix":5},"t":1,"lc":1,"lj":1,"ml":4,"nm":"Gradient Stroke 1","mn":"ADBE Vector Graphic - G-Stroke","hd":false}],"ip":0,"op":120,"st":0,"bm":0},{"ddd":0,"ind":2,"ty":4,"nm":"Shape Layer 4","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":16,"s":[20],"e":[100]},{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":27,"s":[100],"e":[20]},{"t":62}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":0,"k":[400,400,0],"ix":2},"a":{"a":0,"k":[0,0,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"d":1,"ty":"el","s":{"a":1,"k":[{"i":{"x":[0.667,0.667],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0p667_1_0p333_0","0p667_1_0p333_0"],"t":16,"s":[320,320],"e":[360,360]},{"i":{"x":[0.025,0.025],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0p025_1_0p333_0","0p025_1_0p333_0"],"t":27,"s":[360,360],"e":[320,320]},{"t":52}],"ix":2},"p":{"a":0,"k":[0,0],"ix":3},"nm":"Ellipse Path 1","mn":"ADBE Vector Shape - Ellipse","hd":false},{"ty":"tr","p":{"a":0,"k":[0,0],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Ellipse 1","np":3,"cix":2,"ix":1,"mn":"ADBE Vector Group","hd":false},{"ty":"gs","o":{"a":0,"k":100,"ix":9},"w":{"a":1,"k":[{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":16,"s":[5],"e":[10]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":27,"s":[10],"e":[5]},{"t":52}],"ix":10},"g":{"p":3,"k":{"a":0,"k":[0,0,0.627,1,0.5,0.496,0.314,1,1,0.992,0,1],"ix":8}},"s":{"a":0,"k":[0,0],"ix":4},"e":{"a":0,"k":[100,0],"ix":5},"t":1,"lc":1,"lj":1,"ml":4,"nm":"Gradient Stroke 1","mn":"ADBE Vector Graphic - G-Stroke","hd":false}],"ip":0,"op":120,"st":0,"bm":0},{"ddd":0,"ind":3,"ty":4,"nm":"Shape Layer 3","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":9,"s":[20],"e":[100]},{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":20,"s":[100],"e":[20]},{"t":55}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":0,"k":[400,400,0],"ix":2},"a":{"a":0,"k":[0,0,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"d":1,"ty":"el","s":{"a":1,"k":[{"i":{"x":[0.667,0.667],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0p667_1_0p333_0","0p667_1_0p333_0"],"t":9,"s":[240,240],"e":[280,280]},{"i":{"x":[0.051,0.051],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0p051_1_0p333_0","0p051_1_0p333_0"],"t":20,"s":[280,280],"e":[240,240]},{"t":45}],"ix":2},"p":{"a":0,"k":[0,0],"ix":3},"nm":"Ellipse Path 1","mn":"ADBE Vector Shape - Ellipse","hd":false},{"ty":"tr","p":{"a":0,"k":[0,0],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Ellipse 1","np":3,"cix":2,"ix":1,"mn":"ADBE Vector Group","hd":false},{"ty":"gs","o":{"a":0,"k":100,"ix":9},"w":{"a":1,"k":[{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":9,"s":[5],"e":[10]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":20,"s":[10],"e":[5]},{"t":45}],"ix":10},"g":{"p":3,"k":{"a":0,"k":[0,0,0.627,1,0.5,0.496,0.314,1,1,0.992,0,1],"ix":8}},"s":{"a":0,"k":[0,0],"ix":4},"e":{"a":0,"k":[100,0],"ix":5},"t":1,"lc":1,"lj":1,"ml":4,"nm":"Gradient Stroke 1","mn":"ADBE Vector Graphic - G-Stroke","hd":false}],"ip":0,"op":120,"st":0,"bm":0},{"ddd":0,"ind":4,"ty":4,"nm":"Shape Layer 2","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":2,"s":[20],"e":[100]},{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":13,"s":[100],"e":[20]},{"t":48}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":0,"k":[400,400,0],"ix":2},"a":{"a":0,"k":[0,0,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"d":1,"ty":"el","s":{"a":1,"k":[{"i":{"x":[0.667,0.667],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0p667_1_0p333_0","0p667_1_0p333_0"],"t":2,"s":[160,160],"e":[200,200]},{"i":{"x":[0.034,0.034],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0p034_1_0p333_0","0p034_1_0p333_0"],"t":13,"s":[200,200],"e":[160,160]},{"t":38}],"ix":2},"p":{"a":0,"k":[0,0],"ix":3},"nm":"Ellipse Path 1","mn":"ADBE Vector Shape - Ellipse","hd":false},{"ty":"tr","p":{"a":0,"k":[0,0],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Ellipse 1","np":3,"cix":2,"ix":1,"mn":"ADBE Vector Group","hd":false},{"ty":"gs","o":{"a":0,"k":100,"ix":9},"w":{"a":1,"k":[{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":2,"s":[5],"e":[10]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":13,"s":[10],"e":[5]},{"t":38}],"ix":10},"g":{"p":3,"k":{"a":0,"k":[0,0,0.627,1,0.5,0.496,0.314,1,1,0.992,0,1],"ix":8}},"s":{"a":0,"k":[0,0],"ix":4},"e":{"a":0,"k":[100,0],"ix":5},"t":1,"lc":1,"lj":1,"ml":4,"nm":"Gradient Stroke 1","mn":"ADBE Vector Graphic - G-Stroke","hd":false}],"ip":0,"op":120,"st":0,"bm":0},{"ddd":0,"ind":5,"ty":4,"nm":"Shape Layer 1","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":0,"s":[20],"e":[100]},{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":11,"s":[100],"e":[20]},{"t":46}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":0,"k":[400,400,0],"ix":2},"a":{"a":0,"k":[0,0,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"d":1,"ty":"el","s":{"a":1,"k":[{"i":{"x":[0.667,0.667],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0p667_1_0p333_0","0p667_1_0p333_0"],"t":0,"s":[80,80],"e":[120,120]},{"i":{"x":[0,0],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0_1_0p333_0","0_1_0p333_0"],"t":11,"s":[120,120],"e":[80,80]},{"t":36}],"ix":2},"p":{"a":0,"k":[0,0],"ix":3},"nm":"Ellipse Path 1","mn":"ADBE Vector Shape - Ellipse","hd":false},{"ty":"tr","p":{"a":0,"k":[0,0],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Ellipse 1","np":3,"cix":2,"ix":1,"mn":"ADBE Vector Group","hd":false},{"ty":"gs","o":{"a":0,"k":100,"ix":9},"w":{"a":1,"k":[{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":0,"s":[5],"e":[10]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":11,"s":[10],"e":[5]},{"t":35}],"ix":10},"g":{"p":3,"k":{"a":0,"k":[0,0,0.627,1,0.5,0.496,0.314,1,1,0.992,0,1],"ix":8}},"s":{"a":0,"k":[0,0],"ix":4},"e":{"a":0,"k":[100,0],"ix":5},"t":1,"lc":1,"lj":1,"ml":4,"nm":"Gradient Stroke 1","mn":"ADBE Vector Graphic - G-Stroke","hd":false}],"ip":0,"op":120,"st":0,"bm":0}]}
"""

@Composable
fun LottieAnimation() {
    // Please note that it's NOT a part of Compose itself, but API of unstable skiko library that is used under the hood.
    // See:
    // - https://github.com/JetBrains/compose-multiplatform/issues/362
    // - https://github.com/JetBrains/compose-multiplatform/issues/3152
    val animation = Animation.makeFromString(lottieData)
    InfiniteAnimation(animation, Modifier.fillMaxSize())
}

@Composable
fun InfiniteAnimation(animation: Animation, modifier: Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = animation.duration,
        animationSpec = infiniteRepeatable(
            animation = tween((animation.duration * 1000).roundToInt(),
                easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val invalidationController = remember { InvalidationController() }

    animation.seekFrameTime(time, invalidationController)
    Canvas(modifier) {
        drawIntoCanvas {
            animation.render(
                canvas = it.nativeCanvas,
                dst = Rect.makeWH(size.width, size.height)
            )
        }
    }
}

@Composable
@JvmOverloads
fun LottieAnimationMy(
    composition: LottieComposition?,
    progress: () -> Float,
    modifier: Modifier = Modifier,
    outlineMasksAndMattes: Boolean = false,
    applyOpacityToLayers: Boolean = false,
    enableMergePaths: Boolean = false,
    renderMode: RenderMode = RenderMode.AUTOMATIC,
    maintainOriginalImageBounds: Boolean = false,
    dynamicProperties: LottieDynamicProperties? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    clipToCompositionBounds: Boolean = true,
    clipTextToBoundingBox: Boolean = false,
    fontMap: Map<String, Typeface>? = null,
    asyncUpdates: AsyncUpdates = AsyncUpdates.AUTOMATIC,
) {
    val drawable = remember { LottieDrawable() }
    val matrix = remember { Matrix() }
    var setDynamicProperties: LottieDynamicProperties? by remember(composition) { mutableStateOf(null) }

    if (composition == null || composition.duration == 0f) return Box(modifier)

    val bounds = composition.bounds
    Canvas(
        modifier = modifier
            .lottieSize(bounds.width(), bounds.height())
    ) {
        drawIntoCanvas { canvas ->
            val compositionSize = Size(bounds.width().toFloat(), bounds.height().toFloat())
            val intSize = IntSize(size.width.roundToInt(), size.height.roundToInt())

            val scale = contentScale.computeScaleFactor(compositionSize, size)
            val translation = alignment.align(compositionSize * scale, intSize, layoutDirection)
            matrix.reset()
            matrix.preTranslate(translation.x.toFloat(), translation.y.toFloat())
            matrix.preScale(scale.scaleX, scale.scaleY)

            drawable.enableMergePathsForKitKatAndAbove(enableMergePaths)
            drawable.renderMode = renderMode
            drawable.asyncUpdates = asyncUpdates
            drawable.composition = composition
            drawable.setFontMap(fontMap)
            if (dynamicProperties !== setDynamicProperties) {
                setDynamicProperties?.removeFrom(drawable)
                dynamicProperties?.addTo(drawable)
                setDynamicProperties = dynamicProperties
            }
            drawable.setOutlineMasksAndMattes(outlineMasksAndMattes)
            drawable.isApplyingOpacityToLayersEnabled = applyOpacityToLayers
            drawable.maintainOriginalImageBounds = maintainOriginalImageBounds
            drawable.clipToCompositionBounds = clipToCompositionBounds
            drawable.clipTextToBoundingBox = clipTextToBoundingBox
            drawable.progress = progress()
            drawable.setBounds(0, 0, bounds.width(), bounds.height())
            drawable.draw(canvas.nativeCanvas, matrix)
        }
    }
}

/**
 * Controls how Lottie should render.
 * Defaults to [RenderMode.AUTOMATIC].
 *
 * @see LottieAnimationView.setRenderMode
 */
enum class RenderMode {
    AUTOMATIC,
    HARDWARE,
    SOFTWARE;

    fun useSoftwareRendering(
        sdkInt: Int,
        hasDashPattern: Boolean,
        numMasksAndMattes: Int
    ): Boolean {
        return when (this) {
            HARDWARE -> false
            SOFTWARE -> true
            AUTOMATIC -> true/*{
                if (hasDashPattern && sdkInt < android.os.Build.VERSION_CODES.P) {
                    // Hardware acceleration didn't support dash patterns until Pie.
                    return true
                } else if (numMasksAndMattes > 4) {
                    // This was chosen somewhat arbitrarily by trying a handful of animations.
                    // Animations with zero or few masks or mattes tend to perform much better with hardware
                    // acceleration. However, if there are many masks or mattes, it *may* perform worse.
                    // If you are hitting this case with AUTOMATIC set, please manually verify which one
                    // performs better.
                    return true
                }
                // There have been many reported crashes from many device that are running Nougat or below.
                // These devices also support far fewer hardware accelerated canvas operations.
                // https://developer.android.com/guide/topics/graphics/hardware-accel#unsupported
                sdkInt <= android.os.Build.VERSION_CODES.N_MR1
            }*/

            else -> true/*{
                if (hasDashPattern && sdkInt < android.os.Build.VERSION_CODES.P) {
                    return true
                } else if (numMasksAndMattes > 4) {
                    return true
                }
                sdkInt <= android.os.Build.VERSION_CODES.N_MR1
            }*/
        }
    }
}

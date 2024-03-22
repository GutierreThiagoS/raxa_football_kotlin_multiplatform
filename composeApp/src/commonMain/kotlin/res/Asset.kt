@file:OptIn(org.jetbrains.compose.resources.InternalResourceApi::class)

package res


import kotlin.OptIn
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.ResourceItem

@ExperimentalResourceApi
object Asset {
    val BALL: DrawableResource =
        DrawableResource(
            "drawable:bola",
            setOf(
                ResourceItem(setOf(),"drawable/assets/bolaf.png")
            )
        )



    val CAMISA_AZUL: DrawableResource =
        DrawableResource(
            "drawable:camisa_azul",
            setOf(
                ResourceItem(setOf(),
                    "drawable/assets/team/camisa_azul.png"),
            )
        )

    val CAMISA_CE: DrawableResource =
        DrawableResource(
            "drawable:camisa_ce",
            setOf(
                ResourceItem(setOf(),
                    "drawable/assets/team/camisa_ce.png"),
            )
        )

    val SPORTS: DrawableResource =
        DrawableResource(
                    "drawable/baseline_sports.xml"
        )

    val ANIMATION_SOCCER: DrawableResource =
        DrawableResource(
            "drawable/lottie/animation_soccer.json"
        )

}
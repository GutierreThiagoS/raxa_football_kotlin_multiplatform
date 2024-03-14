@file:OptIn(org.jetbrains.compose.resources.InternalResourceApi::class)

package res


import kotlin.OptIn
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.ResourceItem

@ExperimentalResourceApi
private object Assert {
    val bola: DrawableResource =
        DrawableResource(
            "assert:bola",
            setOf(
                ResourceItem(setOf(),
                    "assert/bola.png"),
            )
        )

    val camisaAzul: DrawableResource =
        DrawableResource(
            "assert:camisa_azul",
            setOf(
                ResourceItem(setOf(),
                    "assert/camisa_azul.png"),
            )
        )

    val camisaCe: DrawableResource =
        DrawableResource(
            "assert:camisa_ce",
            setOf(
                ResourceItem(setOf(),
                    "assert/camisa_ce.png"),
            )
        )

    val sports: DrawableResource =
        DrawableResource(
            "drawable:baseline_sports",
            setOf(
                ResourceItem(setOf(),
                    "drawable/baseline_sports.xml"),
            )
        )
}

@ExperimentalResourceApi
internal val ResMain.assert.bola: DrawableResource
    get() = Assert.bola

@ExperimentalResourceApi
internal val ResMain.assert.camisaAzul: DrawableResource
    get() = Assert.camisaAzul

@ExperimentalResourceApi
internal val ResMain.assert.camisaCe: DrawableResource
    get() = Assert.camisaCe

@ExperimentalResourceApi
internal val ResMain.drawable.sports: DrawableResource
    get() = Assert.sports

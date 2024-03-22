package framework.presentation.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.vectorResource
import raxafootballkmp.composeapp.generated.resources.Res
import raxafootballkmp.composeapp.generated.resources.baseline_star_outline

@OptIn(ExperimentalResourceApi::class)
@Composable
fun StarRatingBar(rating: Long) {

    Row {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else vectorResource(
                    resource = Res.drawable.baseline_star_outline
                ),
                contentDescription = "Star",
                tint = if (i <= rating) Color.Yellow else Color.Gray,
            )
        }
    }
}
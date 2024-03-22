package framework.presentation.player_soccer_screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import framework.presentation.compose.StarRatingBar
import org.gutierrethiago.raxafootballkotlinmulti.database.PlayerSoccer

@Composable
fun ItemPlayerSoccer(player: PlayerSoccer) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(10.dp)
    ) {
        Text(player.name)
        StarRatingBar(player.level)
    }
}
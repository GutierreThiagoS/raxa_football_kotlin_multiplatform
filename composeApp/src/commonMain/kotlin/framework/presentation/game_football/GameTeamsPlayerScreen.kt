package framework.presentation.game_football

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import res.ResMain
import res.bola
import res.camisaAzul
import res.camisaCe

@OptIn(ExperimentalResourceApi::class)
@Composable
fun GameTeamsPlayerScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
        ) {
            Image(
                modifier = Modifier.weight(1f),
                painter = painterResource(resource = ResMain.assert.camisaAzul),
                contentDescription = null
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center, // Centraliza horizontalmente
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "0",
                        modifier = Modifier.weight(1f)
                                .align(Alignment.CenterVertically),
                        textAlign = TextAlign.Center
                    )
                    Image(
                        modifier = Modifier.weight(1f),
                        painter = painterResource(resource = ResMain.assert.bola),
                        contentDescription = null
                    )
                    Text("0",
                            modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    "10:00",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Button(
                    onClick = {

                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray, contentColor = Color.White)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                }
            }
            Image(
                modifier = Modifier.weight(1f),
                painter = painterResource(resource = ResMain.assert.camisaCe),
                contentDescription = null
            )
        }

        Row {
            Box(modifier = Modifier.weight(1f)) {
                PlayerSoccerAndTitleList(listOf("Jogador 1", "Jogador 2", "Jogador 3", "Jogador 4"))
            }
            Box(modifier = Modifier.weight(1f)) {
                PlayerSoccerAndTitleList(listOf("Jogador 6", "Jogador 7", "Jogador 8", "Jogador 9"))
            }
        }
    }
}
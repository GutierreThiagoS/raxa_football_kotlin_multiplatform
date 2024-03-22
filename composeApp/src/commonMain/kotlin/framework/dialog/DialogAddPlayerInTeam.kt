package framework.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import domain.model.PlayerSoccerSelected
import framework.presentation.compose.ButtonOutlineFind
import org.gutierrethiago.raxafootballkotlinmulti.database.PlayerSoccer

@Composable
fun DialogAddPlayerInTeam(
    players: List<PlayerSoccer>,
    limitPlayer: Int = 5,
    hideDialog: (List<PlayerSoccer>) -> Unit
) {

    val playersSelected = remember { mutableStateOf(players.map {
        PlayerSoccerSelected(player = it)
    }.toMutableList()) }

    Dialog(
        onDismissRequest = { hideDialog(emptyList()) }) {
        Card(
            shape = MaterialTheme.shapes.medium.copy(
                CornerSize(20.dp)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Selecione o Jogador",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyColumn {
                    itemsIndexed(playersSelected.value) { i, obj ->
                        val selected = remember { mutableStateOf(obj.selected) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                obj.player.name
                            )

                            Checkbox(
                                selected.value,
                                onCheckedChange = { select ->
                                    if (!select || playersSelected.value.filter { it.selected }.size < limitPlayer) {
                                        obj.selected = select
                                        playersSelected.value[i] = obj
                                        selected.value = select
                                    }
                                }
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    ButtonOutlineFind(
                        modifier = Modifier.padding(end = 5.dp),
                        borderSize = 0.dp,
                        backgroundColor = Color(20, 20, 20, 20),
                        onClick = { hideDialog(emptyList()) }
                    ) {
                        Icon(Icons.Filled.Close, null)
                        Text("Cancelar")
                    }

                    ButtonOutlineFind(
                        onClick = { hideDialog(
                            playersSelected.value.filter {
                                it.selected
                            }.map { it.player }
                        ) }
                    ) {
                        Icon(Icons.Filled.Done, null)
                        Text("Adicionar")
                    }
                }
            }
        }
    }
}
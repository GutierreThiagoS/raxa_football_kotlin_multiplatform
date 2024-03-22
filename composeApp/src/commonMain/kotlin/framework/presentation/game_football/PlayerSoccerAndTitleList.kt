package framework.presentation.game_football

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.local.DatabaseDriverFactory
import framework.dialog.DialogAddPlayerInTeam
import framework.presentation.compose.ButtonOutlineFind
import org.gutierrethiago.raxa_football_kotlin_multi.database.AppDatabase
import org.gutierrethiago.raxafootballkotlinmulti.database.Game
import org.gutierrethiago.raxafootballkotlinmulti.database.PlayerInTeam
import org.gutierrethiago.raxafootballkotlinmulti.database.PlayerInTeamQueries
import org.gutierrethiago.raxafootballkotlinmulti.database.Team

@Composable
fun PlayerSoccerAndTitleList(
    driverFactory: DatabaseDriverFactory,
    game: Game,
    team: Team,
    list: List<PlayerInTeam>,
    refreshGame: () -> Unit
) {

    val showDialog = remember { mutableStateOf(false) }

    val driver = driverFactory.createDriver()
    val database = AppDatabase(driver)

    val dao = database.playerSoccerQueries

    Column {
        Box(
            modifier = Modifier.fillMaxWidth()
                .padding(end = 10.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0x12000000))
        ) {
            Text(
                team.name,
                modifier = Modifier.fillMaxWidth()
                    .padding(10.dp),
                fontWeight = FontWeight.Bold
            )
        }
        LazyColumn {
            itemsIndexed(list) { i, item ->
                Text(
                    "$item",
                    modifier = Modifier.fillMaxWidth()
                        .padding(10.dp)
                )
                if (i < list.lastIndex) {
                    Divider(color = Color(0x55000000), thickness = 2.dp)
                }
            }
        }

        if (list.size < 5) {
            ButtonOutlineFind(
                onClick = {
                    showDialog.value = true
                }
            ) {
                Icon(Icons.Filled.Add, null)
                Text("Adicinar Jogador")
            }
        }
    }

    if (showDialog.value) {
        val players = try {
            dao.getAllPlayesSoccerNotTeam().executeAsList()
        } catch (e: Exception) {
            println(e)
            emptyList()
        }

        DialogAddPlayerInTeam(
            players = players
        ) { playersSelected ->
            if (playersSelected.isNotEmpty()) {
                val playerInTeamDao: PlayerInTeamQueries = database.playerInTeamQueries
                playersSelected.forEach {
                    playerInTeamDao.insert(
                        id = null,
                        gameId = game.id,
                        teamId = team.id,
                        playerId = it.id,
                        name = it.name,
                        goals = 0,
                    )
                }
                refreshGame()
            }
            showDialog.value = false
        }

    }
}
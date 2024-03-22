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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.local.DatabaseDriverFactory
import domain.model.GameAndTeams
import domain.model.TeamAndPlayer
import kotlinx.coroutines.delay
import org.gutierrethiago.raxa_football_kotlin_multi.database.AppDatabase
import org.gutierrethiago.raxafootballkotlinmulti.database.GameQueries
import org.gutierrethiago.raxafootballkotlinmulti.database.PlayerInTeamQueries
import org.gutierrethiago.raxafootballkotlinmulti.database.TeamQueries
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import res.Asset


@OptIn(ExperimentalResourceApi::class)
@Composable
fun GameTeamsPlayerScreen(driverFactory: DatabaseDriverFactory) {

    val gameAndTeams = rememberSaveable { mutableStateOf<GameAndTeams?>(null) }

    val isLoading = rememberSaveable { mutableStateOf(true)}

    LaunchedEffect(Unit) {
        delay(2000)
        getService(driverFactory) {
            gameAndTeams.value = it
            isLoading.value = false
        }
    }


    if (gameAndTeams.value != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp)
            ) {
                Image(
                    modifier = Modifier.weight(1f),
                    painter = painterResource(resource = Asset.CAMISA_AZUL),
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
                            painter = painterResource(resource = Asset.BALL),
                            contentDescription = null
                        )
                        Text(
                            "0",
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
                    painter = painterResource(resource = Asset.CAMISA_CE),
                    contentDescription = null
                )
            }

            Row {
                Box(modifier = Modifier.weight(1f)) {
                    PlayerSoccerAndTitleList(
                        driverFactory = driverFactory,
                        game = gameAndTeams.value!!.game,
                        team = gameAndTeams.value!!.teamAndPlayers1.team,
                        list = gameAndTeams.value!!.teamAndPlayers1.players
                    ) {
                        getService(driverFactory) {
                            gameAndTeams.value = it
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    PlayerSoccerAndTitleList(
                        driverFactory = driverFactory,
                        game = gameAndTeams.value!!.game,
                        team = gameAndTeams.value!!.teamAndPlayers2.team,
                        list = gameAndTeams.value!!.teamAndPlayers2.players
                    ) {
                        getService(driverFactory) {
                            gameAndTeams.value = it
                        }
                    }
                }
            }
        }
    } else if (isLoading.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Green
            )
        }
    } else {
        Text("Error Dados não encontrado")
    }

}

private fun getService(driverFactory: DatabaseDriverFactory, result: (GameAndTeams) -> Unit) {
    val driver = driverFactory.createDriver()
    val database = AppDatabase(driver)
    val dao: GameQueries = database.gameQueries

    val game = dao.findGameNotFinished().executeAsOneOrNull()

    if (game != null) {
        val teamDao: TeamQueries = database.teamQueries

        val teamFirst = teamDao.findById(game.team1).executeAsOneOrNull()
        val teamSecond = teamDao.findById(game.team2).executeAsOneOrNull()

        if (teamFirst != null && teamSecond != null) {
            val playerInTeamDao: PlayerInTeamQueries = database.playerInTeamQueries

            val playersFirstTeam = try {
                playerInTeamDao
                    .getAllInTeamAndGame(gameId = game.id, teamId = teamFirst.id)
                    .executeAsList()
            }  catch (e: Exception) {
                println(e)
                emptyList()
            }

            val teamAndPlayersFirst = TeamAndPlayer(
                team = teamFirst,
                players = playersFirstTeam
            )

            val playersSecondTeam = try {
                playerInTeamDao
                    .getAllInTeamAndGame(gameId = game.id, teamId = teamSecond.id)
                    .executeAsList()
            } catch (e: Exception) {
                println(e)
                emptyList()
            }

            val teamAndPlayersSecond = TeamAndPlayer(
                team = teamSecond,
                players = playersSecondTeam
            )

            result(GameAndTeams(
                game = game,
                teamAndPlayers1 = teamAndPlayersFirst,
                teamAndPlayers2 = teamAndPlayersSecond
            ))
        }
    }
}
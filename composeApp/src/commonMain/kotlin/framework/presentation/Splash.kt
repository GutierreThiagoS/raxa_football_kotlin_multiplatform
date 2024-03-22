package framework.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.cash.sqldelight.db.SqlDriver
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import data.local.DatabaseDriverFactory
import framework.animation.LottieAnimation
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.gutierrethiago.raxa_football_kotlin_multi.database.AppDatabase
import org.gutierrethiago.raxafootballkotlinmulti.database.GameQueries
import org.gutierrethiago.raxafootballkotlinmulti.database.PlayerSoccerQueries
import org.gutierrethiago.raxafootballkotlinmulti.database.TeamQueries

data class Splash(
    val id: String = "Splash",
    val drive: DatabaseDriverFactory
): Screen {

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(10.dp),
                    color = Color.Green
                )
                LottieAnimation()
                Text("Carregando dados...")
            }
        }

        LaunchedEffect(Unit) {
            insertAllPlayerSoccer(drive.createDriver())
            insertAllTeams(drive.createDriver())
            insertGame(drive.createDriver())
            delay(2000)
            navigator.push(MenuScreen(driver = drive))
        }

    }

    private fun insertAllPlayerSoccer(driver: SqlDriver) {
        val database = AppDatabase(driver)
        val playerDao: PlayerSoccerQueries = database.playerSoccerQueries
        val players = playerDao.getAll().executeAsList()
        println("Inicio players $players")
        println("Size players ${players.size}")

        if (players.isEmpty()) {
            for (i in 1..15) {
                playerDao.insert(null, "Jogador $i", 0, 1)
            }
            println(playerDao.getAll().executeAsList())
        }
    }

    private fun insertAllTeams(driver: SqlDriver) {
        val database = AppDatabase(driver)
        val teamDao: TeamQueries = database.teamQueries
        val teams = teamDao.getAll().executeAsList()
        println("Inicio teams $teams")
        println("Size teams ${teams.size}")

        if (teams.isEmpty()) {
            for (i in 1..3) {
                teamDao.insert(null, "Team $i", getImageIndex(i))
            }
            println(teamDao.getAll().executeAsList())
        }
    }

    private fun insertGame(driver: SqlDriver) {
        val database = AppDatabase(driver)

        val dao: GameQueries = database.gameQueries

        val games = dao.getAll().executeAsList()

        if (games.isEmpty()) {
            val teamDao: TeamQueries = database.teamQueries
            val teams = teamDao.getAll().executeAsList()
            println("Inicio teams $teams")
            println("Size teams ${teams.size}")

            val firstTeam = teams.firstOrNull()
            val secondTeam = teams.firstOrNull{ it.id != firstTeam?.id }

            if (firstTeam != null && secondTeam != null) {

                val currentMoment: Instant = Clock.System.now()
                val datetimeInSystemZone: LocalDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())

                println("datetimeInSystemZone  $datetimeInSystemZone")

                dao.insert(
                    id = null,
                    team1 = firstTeam.id,
                    team2 = secondTeam.id,
                    dateTimeInit = "",
                    dateTimeFinish = "",
                    minuteTimeGame = 10 * 60,
                    time = 1,
                    maxTime = 2,
                    dateTimeCreated = datetimeInSystemZone.toString(),
                    dateTimeUpdate = datetimeInSystemZone.toString(),
                    isInitGame = 0,
                    isFinishedGame = 0
                )
            }
        }
    }

    private fun getImageIndex(index: Int): String {
        return  when (index) {
            1 -> "camisa_ce.png"
            2 -> "sem_camisa.png"
            else -> "camisa_azul.png"
        }
    }
}

package framework.presentation.player_soccer_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import data.local.DatabaseDriverFactory
import framework.presentation.compose.ButtonOutline
import framework.presentation.compose.SearchComponent
import org.gutierrethiago.raxa_football_kotlin_multi.database.AppDatabase
import org.gutierrethiago.raxafootballkotlinmulti.database.PlayerSoccer
import org.gutierrethiago.raxafootballkotlinmulti.database.PlayerSoccerQueries

@Composable
fun PlayerSoccerListScreen(driverFactory: DatabaseDriverFactory) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        val playerSoccerList = remember { mutableStateListOf<PlayerSoccer>() }

        LaunchedEffect(Unit) {

            val driver = driverFactory.createDriver()

            val database = AppDatabase(driver)
            val playerDao: PlayerSoccerQueries = database.playerSoccerQueries

            playerSoccerList.addAll(playerDao.getAll().executeAsList())
        }

        val searchQuery = remember { mutableStateOf("") }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchComponent(
                    modifier = Modifier.weight(5f)
                ) {
                    searchQuery.value = it
                }

                ButtonOutline(
                    modifier = Modifier.weight(2f),
                    onClick = {}
                ) {
                    Icon(Icons.Default.Add, null)
                    Text(
                        text = "Novo",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LazyColumn {
                items(playerSoccerList.filter { it.name.contains(searchQuery.value) }) {  j ->
                    ItemPlayerSoccer(j)
                }
            }
        }
    }
}
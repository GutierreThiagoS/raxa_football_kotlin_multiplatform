package framework.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import framework.presentation.game_football.GameTeamsPlayerScreen
import model.NavigatorItem
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.vectorResource
import res.ResMain
import res.sports

data class MenuScreen(val id: String = "Home"): Screen {

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    override fun Content() {

        val list = listOf(
            NavigatorItem(
                title = "Inicio",
                icon = Icons.Outlined.Home,
                iconSelected = Icons.Filled.Home,
            ),
            NavigatorItem(
                title = "Jogadores",
                icon = Icons.Outlined.Menu,
                iconSelected = Icons.Filled.List,
            ),
            NavigatorItem(
                title = "Meus Times",
                icon = Icons.Outlined.FavoriteBorder,
                iconSelected = Icons.Filled.Favorite,
            ),
            NavigatorItem(
                title = "Historico",
                icon = vectorResource(resource = ResMain.drawable.sports),
                iconSelected = vectorResource(resource = ResMain.drawable.sports)
            ),
            NavigatorItem(
                title = "Configuration",
                icon = Icons.Outlined.Settings,
                iconSelected = Icons.Filled.Settings
            )
        )

        val selected = remember { mutableIntStateOf(0) }

        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.Gray,
                    contentColor = Color.White
                ) {
                    Text(
                        "Home",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            bottomBar = {
                BottomNavigation(
                    backgroundColor = Color.Gray
                ) {
                    list.forEachIndexed { i, item ->
                        BottomNavigationItem(
                            selected.value == i,
                            icon = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (selected.value == i)
                                        Box(
                                            modifier = Modifier.width(50.dp)
                                                .clip(RoundedCornerShape(30.dp))
                                                .background(Color(0x55000000))
                                        ) {
                                            Icon(
                                                item.iconSelected,
                                                modifier = Modifier.align(Alignment.Center),
                                                tint =   Color.White,
                                                contentDescription = null
                                            )
                                        }
                                        else Icon(
                                            item.icon,
                                            tint =  Color(0x55FFFFFF),
                                            contentDescription = null
                                        )
                                    if (selected.value == i)
                                        Text(
                                            item.title,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                }
                            },
                            onClick = {
                                selected.value = i
                            }
                        )
                    }
                }
            }
        ) {
            when (selected.value) {
                0 -> GameTeamsPlayerScreen()
                else -> Box(modifier = Modifier.background(color = Color.Yellow))
            }
        }
    }
}

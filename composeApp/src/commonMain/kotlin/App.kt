import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScaleTransition
import data.local.DatabaseDriverFactory
import framework.presentation.Splash
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(drive: DatabaseDriverFactory) {
    MaterialTheme {
        Navigator(
            screen = Splash("Home", drive)
        ) { navigator ->
            ScaleTransition(navigator)
        }
    }
}
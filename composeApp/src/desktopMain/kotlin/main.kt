import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.local.DatabaseDriverFactory

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "RaxaFootballKMP") {
        App(DatabaseDriverFactory())
    }
}
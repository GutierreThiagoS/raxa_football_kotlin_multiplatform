import androidx.compose.ui.window.ComposeUIViewController
import data.local.DatabaseDriverFactory

fun MainViewController() = ComposeUIViewController { App(DatabaseDriverFactory()) }
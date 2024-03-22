package org.gutierrethiago.raxa_football_kotlin_multi

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.lottie.compose.LottieAnimation
import data.local.DatabaseDriverFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val drive = DatabaseDriverFactory(this)

        setContent {
            App(drive)
            LottieAnimation()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    val drive = DatabaseDriverFactory(MainActivity())

    App(drive)
}
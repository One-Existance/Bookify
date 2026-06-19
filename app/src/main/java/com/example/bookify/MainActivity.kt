package com.example.bookify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.bookify.ui.screens.HomeFeedScreen
import com.example.bookify.ui.screens.SplashScreen
import com.example.bookify.ui.theme.BookifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookifyTheme {
                var screen by remember { mutableStateOf("splash") }
                when (screen) {
                    "splash" -> SplashScreen(
                        onGetStarted = { screen = "home" },
                        onLogin = { screen = "home" }
                    )
                    "home" -> HomeFeedScreen()
                }
            }
        }
    }
}
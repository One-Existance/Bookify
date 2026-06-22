package com.example.bookify

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.bookify.data.DatabaseHelper
import com.example.bookify.data.User
import com.example.bookify.ui.screens.HomeFeedScreen
import com.example.bookify.ui.screens.LoginScreen
import com.example.bookify.ui.theme.BookifyTheme

class MainActivity : ComponentActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("BookifyLifecycle", "onCreate: App is starting")
        db = DatabaseHelper(this)
        enableEdgeToEdge()
        setContent {
            BookifyTheme {
                BookifyApp(db)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("BookifyLifecycle", "onStart: App is now visible to the user")
    }

    override fun onResume() {
        super.onResume()
        Log.d("BookifyLifecycle", "onResume: App is in the foreground — user can interact")
    }

    override fun onPause() {
        super.onPause()
        Log.d("BookifyLifecycle", "onPause: App is partially hidden")
    }

    override fun onStop() {
        super.onStop()
        Log.d("BookifyLifecycle", "onStop: App is no longer visible")
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
        Log.d("BookifyLifecycle", "onDestroy: App is closing — database closed")
    }
}

@Composable
fun BookifyApp(db: DatabaseHelper) {
    var screen by rememberSaveable { mutableStateOf("login") }
    var currentUser: User? by remember { mutableStateOf(null) }
    val events = remember { db.getAllPublicEvents() }

    when (screen) {
        "login" -> LoginScreen(
            db = db,
            onLoginSuccess = { user -> currentUser = user; screen = "home" },
            onNavigateToRegister = {}
        )
        "home" -> HomeFeedScreen(
            events = events,
            currentUser = currentUser
        )
    }
}

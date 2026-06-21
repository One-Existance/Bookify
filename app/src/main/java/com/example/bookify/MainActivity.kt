package com.example.bookify

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.bookify.data.DatabaseHelper
import com.example.bookify.data.Event
import com.example.bookify.data.User
import com.example.bookify.ui.screens.*
import com.example.bookify.ui.theme.BookifyTheme

class MainActivity : ComponentActivity() {

    private lateinit var db: DatabaseHelper

    // ── 1. CREATED ────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("BookifyLifecycle", "onCreate: App is starting")

        db = DatabaseHelper(this)   // open the database once here
        enableEdgeToEdge()

        setContent {
            BookifyTheme {
                BookifyApp(db)
            }
        }
    }

    // ── 2. STARTED (becomes visible) ──────────────────────────────
    override fun onStart() {
        super.onStart()
        Log.d("BookifyLifecycle", "onStart: App is now visible to the user")
    }

    // ── 3. RESUMED (user can interact) ────────────────────────────
    override fun onResume() {
        super.onResume()
        Log.d("BookifyLifecycle", "onResume: App is in the foreground — user can interact")
    }

    // ── 4. PAUSED (partially hidden, e.g. dialog over app) ────────
    override fun onPause() {
        super.onPause()
        Log.d("BookifyLifecycle", "onPause: App is partially hidden")
    }

    // ── 5. STOPPED (fully hidden, user pressed Home) ──────────────
    override fun onStop() {
        super.onStop()
        Log.d("BookifyLifecycle", "onStop: App is no longer visible")
    }

    // ── 6. DESTROYED (app is closing) ─────────────────────────────
    override fun onDestroy() {
        super.onDestroy()
        db.close()   // release the database connection cleanly
        Log.d("BookifyLifecycle", "onDestroy: App is closing — database closed")
    }
}

@Composable
fun BookifyApp(db: DatabaseHelper) {
    // rememberSaveable keeps screen & nav tab alive across rotation
    var screen by rememberSaveable { mutableStateOf("splash") }
    var selectedNav by rememberSaveable { mutableIntStateOf(0) }

    // User and Event are not Parcelable so we use plain remember
    var currentUser: User? by remember { mutableStateOf(null) }
    var selectedEvent: Event? by remember { mutableStateOf(null) }

    BackHandler(enabled = screen == "detail" || screen == "login" || screen == "register") {
        screen = if (screen == "detail") "home" else "splash"
    }

    when (screen) {
        "splash" -> SplashScreen(
            onGetStarted = { screen = "register" },
            onLogin      = { screen = "login" }
        )

        "login" -> LoginScreen(
            db                   = db,
            onLoginSuccess       = { user -> currentUser = user; screen = "home" },
            onNavigateToRegister = { screen = "register" }
        )

        "register" -> RegisterScreen(
            db                 = db,
            onRegisterSuccess  = { user -> currentUser = user; screen = "home" },
            onNavigateToLogin  = { screen = "login" }
        )

        "home" -> {
            when (selectedNav) {
                0, 1 -> HomeFeedScreen(
                    db            = db,
                    currentUser   = currentUser,
                    selectedNav   = selectedNav,
                    onNavSelected = { selectedNav = it },
                    onEventClick  = { event -> selectedEvent = event; screen = "detail" }
                )
                2 -> TicketsTabScreen(
                    db            = db,
                    currentUser   = currentUser,
                    selectedNav   = selectedNav,
                    onNavSelected = { selectedNav = it },
                    onEventFound  = { event -> selectedEvent = event; screen = "detail" }
                )
                3 -> ProfileScreen(
                    currentUser    = currentUser,
                    onLogout       = { currentUser = null; selectedNav = 0; screen = "splash" },
                    onLoginRequest = { screen = "login" }
                )
            }
        }

        "detail" -> {
            val event = selectedEvent
            if (event != null) {
                EventDetailScreen(
                    db               = db,
                    event            = event,
                    currentUser      = currentUser,
                    onBack           = { screen = "home" },
                    onBookingSuccess = { selectedNav = 2; screen = "home" }
                )
            }
        }
    }
}

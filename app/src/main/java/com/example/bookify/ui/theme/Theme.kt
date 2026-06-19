package com.example.bookify.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BookifyColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    secondary = Amber,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun BookifyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BookifyColorScheme,
        typography = Typography,
        content = content
    )
}
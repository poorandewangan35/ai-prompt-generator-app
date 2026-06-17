package com.aipromptgenerater.aitricker.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    tertiary = BlueTertiary,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface
)


object ThemeManager {
    var isDarkTheme by mutableStateOf(true)

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        isDarkTheme = prefs.getBoolean("is_dark", true)
    }

    fun toggleTheme(context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val newValue = !isDarkTheme
        isDarkTheme = newValue
        prefs.edit().putBoolean("is_dark", newValue).apply()
    }
}

@Composable
fun AiPromptGeneraterAppBuilderTheme(
    darkTheme: Boolean = ThemeManager.isDarkTheme,
    dynamicColor: Boolean = false, // Set to false to enforce our premium brand palette instead of generic system colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

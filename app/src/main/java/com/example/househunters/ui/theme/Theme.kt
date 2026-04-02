package com.example.househunters.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = MintPrimaryDark,
    secondary = MintSecondaryDark,
    tertiary = MintTertiaryDark,
    background = AppBackground,
    surface = AppBackground,
    onSurface = OnAppSurface
)

private val LightColorScheme = lightColorScheme(
    primary = MintPrimary,
    secondary = MintSecondary,
    tertiary = MintTertiary,
    background = AppBackground,
    surface = AppBackground,
    onSurface = OnAppSurface
)

@Composable
fun HouseHuntersTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> LightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
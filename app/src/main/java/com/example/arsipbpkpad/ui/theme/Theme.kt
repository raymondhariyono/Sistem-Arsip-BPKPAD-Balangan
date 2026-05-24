package com.example.arsipbpkpad.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = White,
    primaryContainer = DarkGreen,
    onPrimaryContainer = White,
    secondary = LightGreen,
    onSecondary = PrimaryGreen,
    secondaryContainer = Color(0xFF2E7D32),
    onSecondaryContainer = White,
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onBackground = White,
    onSurface = White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = White,
    primaryContainer = LightGreen,
    onPrimaryContainer = PrimaryGreen,
    secondary = LightGreen,
    onSecondary = PrimaryGreen,
    secondaryContainer = Color(0xFFE8F5E9), // Very light green
    onSecondaryContainer = PrimaryGreen,
    tertiary = ChipBlue,
    onTertiary = White,
    tertiaryContainer = ChipBlueBg,
    onTertiaryContainer = ChipBlue,
    background = BackgroundBlue,
    surface = White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = SurfaceVariant,
    error = ErrorRed
)

@Composable
fun ArsipBPKPADTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled to enforce branding as per Agent.md
    dynamicColor: Boolean = false,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
package com.debazapps.routine.ui.theme

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

// Roxo do gradiente da versão web (#667eea → #764ba2).
private val LightColors = lightColorScheme(
    primary = Color(0xFF5A4FCF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4DFFF),
    onPrimaryContainer = Color(0xFF16006E),
    secondary = Color(0xFF764BA2),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC6BFFF),
    onPrimary = Color(0xFF2A1C8A),
    primaryContainer = Color(0xFF4136B5),
    onPrimaryContainer = Color(0xFFE4DFFF),
    secondary = Color(0xFFDDB8FF),
)

@Composable
fun RoutineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Android 12+ usa as cores do papel de parede do usuário.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

package dev.code93.emvqr.core.designsystem

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

private val LightColors = lightColorScheme(
    primary = Color(0xFF00629D),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFE5FF),
    onPrimaryContainer = Color(0xFF001D33),
    secondary = Color(0xFF526070),
    secondaryContainer = Color(0xFFD6E4F7),
    tertiary = Color(0xFF00696E),
    tertiaryContainer = Color(0xFF6FF6FD),
    surfaceVariant = Color(0xFFDEE3EA),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF99CBFF),
    onPrimary = Color(0xFF003353),
    primaryContainer = Color(0xFF004A76),
    onPrimaryContainer = Color(0xFFCFE5FF),
    secondary = Color(0xFFBAC8DA),
    secondaryContainer = Color(0xFF3B4857),
    tertiary = Color(0xFF4DD9E1),
    tertiaryContainer = Color(0xFF004F53),
)

@Composable
fun QrdTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = QrdTypography,
        content = content
    )
}

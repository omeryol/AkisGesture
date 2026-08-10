package io.github.omeryol.akisgesture.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = AkisPrimary,
    onPrimary = AkisOnPrimary,
    primaryContainer = AkisPrimaryContainer,
    onPrimaryContainer = AkisOnPrimaryContainer,
    secondary = AkisSecondary,
    onSecondary = AkisOnPrimary,
    secondaryContainer = AkisSecondaryContainer,
    onSecondaryContainer = AkisOnSecondaryContainer,
    tertiary = AkisTertiary,
    onTertiary = AkisOnPrimary,
    tertiaryContainer = AkisTertiaryContainer,
    onTertiaryContainer = AkisOnTertiaryContainer,
    error = AkisError,
    errorContainer = AkisErrorContainer,
    background = AkisBackground,
    onBackground = AkisOnSurface,
    surface = AkisSurface,
    onSurface = AkisOnSurface,
    surfaceVariant = AkisSurfaceVariant,
    onSurfaceVariant = AkisOnSurfaceVariant,
    outline = AkisOutline,
    outlineVariant = AkisOutlineVariant,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF536DFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF283A9A),
    onPrimaryContainer = Color(0xFFDDE2FF),
    secondary = Color(0xFF00E5FF),
    onSecondary = Color(0xFF002F35),
    secondaryContainer = Color(0xFF074C57),
    onSecondaryContainer = Color(0xFF9DF2FF),
    tertiary = Color(0xFFFF4081),
    onTertiary = Color(0xFF3B0019),
    tertiaryContainer = Color(0xFF7A1645),
    onTertiaryContainer = AkisTertiaryContainer,
    error = AkisErrorDark,
    errorContainer = Color(0xFF93000A),
    background = Color(0xFF080A10),
    onBackground = Color(0xFFF4F2FA),
    surface = Color(0xFF11152A),
    onSurface = Color(0xFFF4F2FA),
    surfaceVariant = Color(0xFF292D4A),
    onSurfaceVariant = Color(0xFFB8BEDB),
    outline = Color(0xFF6E7CFF),
    outlineVariant = Color(0xFF3E456D),
)

private val AkisShapes = Shapes(
    extraSmall = RoundedCornerShape(14.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(14.dp),
)

@Composable
fun AkisGestureTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        @Suppress("DEPRECATION")
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AkisShapes,
        content = content,
    )
}

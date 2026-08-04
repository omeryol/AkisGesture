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
    primary = AkisPrimaryDark,
    onPrimary = AkisOnPrimaryContainer,
    primaryContainer = Color(0xFF0033A8),
    onPrimaryContainer = AkisPrimaryContainer,
    secondary = AkisSecondaryDark,
    onSecondary = AkisOnSecondaryContainer,
    secondaryContainer = Color(0xFF41455A),
    onSecondaryContainer = AkisSecondaryContainer,
    tertiary = AkisTertiaryDark,
    onTertiary = AkisOnTertiaryContainer,
    tertiaryContainer = Color(0xFF593E5A),
    onTertiaryContainer = AkisTertiaryContainer,
    error = AkisErrorDark,
    errorContainer = Color(0xFF93000A),
    background = AkisBackgroundDark,
    onBackground = AkisOnSurfaceDark,
    surface = AkisSurfaceDark,
    onSurface = AkisOnSurfaceDark,
    surfaceVariant = AkisSurfaceVariantDark,
    onSurfaceVariant = AkisOnSurfaceVariantDark,
    outline = AkisOutlineDark,
    outlineVariant = AkisOutlineVariantDark,
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
    darkTheme: Boolean = isSystemInDarkTheme(),
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

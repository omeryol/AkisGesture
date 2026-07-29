package com.omer.akisgesture.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AkisGesturePrimaryDarkTheme,
    secondary = AkisGestureSecondary,
    tertiary = Pink80,
    background = AkisGestureBackgroundDark,
    surface = AkisGestureSurfaceDark,
    onPrimary = AkisGestureOnPrimaryDark,
    onSecondary = AkisGestureOnSecondary,
    onBackground = AkisGestureOnBackgroundDark,
    onSurface = AkisGestureOnSurfaceDark,
)

private val LightColorScheme = lightColorScheme(
    primary = AkisGesturePrimary,
    secondary = AkisGestureSecondary,
    tertiary = Pink40,
    background = AkisGestureBackground,
    surface = AkisGestureSurface,
    onPrimary = AkisGestureOnPrimary,
    onSecondary = AkisGestureOnSecondary,
    onBackground = AkisGestureOnBackground,
    onSurface = AkisGestureOnSurface,
)

private val AkisShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun AkisGestureTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
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
        shapes = AkisShapes,
        content = content,
    )
}

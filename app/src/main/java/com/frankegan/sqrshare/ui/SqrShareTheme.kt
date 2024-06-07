package com.frankegan.sqrshare.ui

import android.app.Activity
import android.os.Build
import android.view.View
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val defaultColorScheme = darkColorScheme(
    primary = Color(0xFF3C64A6),
    background = Color(0xFF121212),
    onPrimary = Color.White,
)

private fun computedColorScheme(computedColor: Color?): ColorScheme {
    computedColor ?: return defaultColorScheme

    return defaultColorScheme.copy(
        primary = computedColor,
        onPrimary = if (computedColor.isLight()) {
            Color.Black
        } else {
            Color.White
        }
    )
}

fun Color.isLight(): Boolean = luminance() > 0.5f

@Composable
fun SqrShareTheme(computedColor: Color?, content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            setUpEdgeToEdge(view, computedColor?.isLight() ?: false)
        }
    }

    MaterialTheme(
        colorScheme = computedColorScheme(computedColor),
        content = content,
    )
}

/**
 * Sets up edge-to-edge for the window of this [view]. The system icon colors are set to either
 * light or dark depending on whether the [darkTheme] is enabled or not.
 */
private fun setUpEdgeToEdge(view: View, isLightColor: Boolean) {
    val window = (view.context as Activity).window
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = Color.Transparent.toArgb()
    val navigationBarColor = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> Color.Transparent.toArgb()
        else -> Color(0x00, 0x00, 0x00, 0x63).toArgb()
    }
    window.navigationBarColor = navigationBarColor
    val controller = WindowCompat.getInsetsController(window, view)
    controller.isAppearanceLightStatusBars = isLightColor
    controller.isAppearanceLightNavigationBars = isLightColor
}
package com.cloudmail.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = SurfaceLight,
    primaryContainer = BrandDeep,
    onPrimaryContainer = SurfaceLight,
    secondary = BrandCyan,
    onSecondary = SurfaceLight,
    background = BgLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = DividerLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = DividerLight,
    error = Error,
    onError = SurfaceLight
)

private val DarkColors = darkColorScheme(
    primary = BrandBlue,
    onPrimary = SurfaceDark,
    primaryContainer = BrandDeep,
    onPrimaryContainer = TextPrimaryDark,
    secondary = BrandCyan,
    onSecondary = SurfaceDark,
    background = BgDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = DividerDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = DividerDark,
    error = Error,
    onError = SurfaceDark
)

@Composable
fun CloudMailTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = CloudMailShapes,
        typography = CloudMailTypography,
        content = content
    )
}

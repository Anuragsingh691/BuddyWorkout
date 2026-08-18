package com.example.buddyworkout.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Light-only colour scheme.
 *
 * Dynamic colour is deliberately **not** used: on Android 12+ it would replace
 * the brand green with the user's wallpaper palette. The mockups ship no dark
 * variant, so dark mode is out of scope for MVP.
 */
private val BwColorScheme = lightColorScheme(
    primary = BwColors.Primary,
    onPrimary = BwColors.Surface,
    primaryContainer = BwColors.PrimaryTint,
    onPrimaryContainer = BwColors.PrimaryDark,
    secondary = BwColors.PrimaryDark,
    onSecondary = BwColors.Surface,
    tertiary = BwColors.Amber,
    onTertiary = BwColors.Surface,
    tertiaryContainer = BwColors.AmberTint,
    onTertiaryContainer = BwColors.AmberInk,
    background = BwColors.Bg,
    onBackground = BwColors.Ink,
    surface = BwColors.Surface,
    onSurface = BwColors.Ink,
    surfaceVariant = BwColors.Bg,
    onSurfaceVariant = BwColors.Muted,
    outline = BwColors.Line,
    outlineVariant = BwColors.Line,
    error = BwColors.Danger,
    onError = BwColors.Surface,
    errorContainer = BwColors.DangerTint,
    onErrorContainer = BwColors.DangerInk,
)

@Composable
fun BuddyWorkoutTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BwColorScheme,
        typography = BwTypography,
        shapes = BwShapes,
        content = content,
    )
}

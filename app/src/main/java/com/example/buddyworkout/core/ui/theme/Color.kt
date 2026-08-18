package com.example.buddyworkout.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Design tokens lifted verbatim from the `:root` block of the Figma export
 * (`figma-screens/community-workout-screens.html`).
 *
 * The app is light-theme only for MVP — the mockups define no dark palette, so
 * [BuddyWorkoutTheme] pins these regardless of the system setting.
 */
object BwColors {
    val Bg = Color(0xFFF5F6F8)
    val Surface = Color(0xFFFFFFFF)

    val Primary = Color(0xFF0E9F6E)
    val PrimaryDark = Color(0xFF0A7D57)
    val PrimaryTint = Color(0xFFE6F7F0)

    val Ink = Color(0xFF1A1D1F)
    val Muted = Color(0xFF6B7280)
    val Line = Color(0xFFE8EAED)

    val Amber = Color(0xFFF5A623)
    val AmberTint = Color(0xFFFDF3E1)
    val AmberInk = Color(0xFF9A6A08)

    val Danger = Color(0xFFE5484D)
    val DangerTint = Color(0xFFFCEBEB)
    val DangerInk = Color(0xFFA32D2D)

    /** Placeholder / disabled text (`#9aa1ac` in the export). */
    val Placeholder = Color(0xFF9AA1AC)

    /** Field-label grey and the slightly darker frame-label grey. */
    val LabelInk = Color(0xFF4B5563)
    val LabelInkStrong = Color(0xFF374151)

    /** Record-workout camera surface and its pose overlay. */
    val CameraBg = Color(0xFF11161C)
    val PoseJoint = Color(0xFF1DE9A6)
    val CameraSubtle = Color(0xFFC7CDD4)

    /**
     * Avatar backgrounds (`.av.a` … `.av.f`). Pick with [avatarColorFor] so a
     * person keeps the same colour on every screen.
     */
    val Avatar = listOf(
        Color(0xFF6C5CE7),
        Color(0xFF0E9F6E),
        Color(0xFFE17055),
        Color(0xFF0984E3),
        Color(0xFFE84393),
        Color(0xFFF5A623),
    )
}

/**
 * Stable per-user avatar colour: the same [key] always yields the same swatch.
 *
 * [String.hashCode] alone is a poor fit here — it is `31*a + b` for two-letter
 * initials, and 31 ≡ 1 (mod 6), so "RK", "PM" and "SV" all land on the same
 * swatch. The bit-mixing step below scatters those before the modulo.
 */
fun avatarColorFor(key: String): Color =
    BwColors.Avatar[Math.floorMod(mixBits(key.hashCode()), BwColors.Avatar.size)]

/** MurmurHash3 finalizer — cheap avalanche so adjacent hashes do not cluster. */
private fun mixBits(hash: Int): Int {
    var x = hash
    x = x xor (x ushr 16)
    x *= 0x7feb352d
    x = x xor (x ushr 15)
    x *= 0x846ca68b.toInt()
    x = x xor (x ushr 16)
    return x
}

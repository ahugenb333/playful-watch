package com.playfulwatch.foundation

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class PlayfulColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val background: Color,
    val onBackground: Color,
    val accent: Color,
) {
    companion object {
        val Default = PlayfulColorScheme(
            primary = Color(0xFF6C63FF),
            onPrimary = Color.White,
            secondary = Color(0xFF00C2A8),
            onSecondary = Color(0xFF00241F),
            tertiary = Color(0xFFFFB703),
            onTertiary = Color(0xFF2A1800),
            background = Color(0xFF0F1117),
            onBackground = Color(0xFFE8EAFF),
            accent = Color(0xFFFF6B6B),
        )

        val Hourglass = Default.copy(
            primary = Color(0xFFE8C547),
            secondary = Color(0xFF8B5E34),
            tertiary = Color(0xFFF4F1DE),
            accent = Color(0xFFC9A227),
            background = Color(0xFF1A1410),
            onBackground = Color(0xFFF7EED6),
        )

        val LiquidVials = Default.copy(
            primary = Color(0xFF48CAE4),
            secondary = Color(0xFF0077B6),
            tertiary = Color(0xFF90E0EF),
            accent = Color(0xFF00B4D8),
            background = Color(0xFF03045E),
            onBackground = Color(0xFFCAF0F8),
        )
    }
}

val LocalPlayfulColors = compositionLocalOf { PlayfulColorScheme.Default }

fun PlayfulColorScheme.toStyleMap(): Map<String, Int> = mapOf(
    STYLE_PRIMARY to primary.value.toInt(),
    STYLE_SECONDARY to secondary.value.toInt(),
    STYLE_TERTIARY to tertiary.value.toInt(),
    STYLE_BACKGROUND to background.value.toInt(),
    STYLE_ACCENT to accent.value.toInt(),
)

fun playfulColorSchemeFromStyleMap(values: Map<String, Int>, fallback: PlayfulColorScheme): PlayfulColorScheme =
    fallback.copy(
        primary = values[STYLE_PRIMARY]?.let(::Color) ?: fallback.primary,
        secondary = values[STYLE_SECONDARY]?.let(::Color) ?: fallback.secondary,
        tertiary = values[STYLE_TERTIARY]?.let(::Color) ?: fallback.tertiary,
        background = values[STYLE_BACKGROUND]?.let(::Color) ?: fallback.background,
        accent = values[STYLE_ACCENT]?.let(::Color) ?: fallback.accent,
    )

const val STYLE_PRIMARY = "playful_primary"
const val STYLE_SECONDARY = "playful_secondary"
const val STYLE_TERTIARY = "playful_tertiary"
const val STYLE_BACKGROUND = "playful_background"
const val STYLE_ACCENT = "playful_accent"

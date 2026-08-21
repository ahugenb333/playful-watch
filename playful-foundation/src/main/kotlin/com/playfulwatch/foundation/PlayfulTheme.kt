package com.playfulwatch.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

@Composable
fun PlayfulTheme(
    colors: PlayfulColorScheme = PlayfulColorScheme.Default,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalPlayfulColors provides colors, content = content)
}

@Composable
fun rememberPlayfulColorEditor(
    initial: PlayfulColorScheme = PlayfulColorScheme.Default,
): PlayfulColorEditor {
    val schemeState = remember { mutableStateOf(initial) }
    return remember {
        object : PlayfulColorEditor {
            override val colors: PlayfulColorScheme get() = schemeState.value
            override fun updatePrimary(color: Color) {
                schemeState.value = schemeState.value.copy(primary = color)
            }
            override fun updateSecondary(color: Color) {
                schemeState.value = schemeState.value.copy(secondary = color)
            }
            override fun updateTertiary(color: Color) {
                schemeState.value = schemeState.value.copy(tertiary = color)
            }
            override fun updateBackground(color: Color) {
                schemeState.value = schemeState.value.copy(background = color)
            }
            override fun updateAccent(color: Color) {
                schemeState.value = schemeState.value.copy(accent = color)
            }
        }
    }
}

interface PlayfulColorEditor {
    val colors: PlayfulColorScheme
    fun updatePrimary(color: Color)
    fun updateSecondary(color: Color)
    fun updateTertiary(color: Color)
    fun updateBackground(color: Color)
    fun updateAccent(color: Color)
}

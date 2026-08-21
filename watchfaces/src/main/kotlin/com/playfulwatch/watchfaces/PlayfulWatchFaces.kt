package com.playfulwatch.watchfaces

import androidx.compose.runtime.Composable

data class PlayfulWatchFace(
    val id: String,
    val displayName: String,
    val defaultColors: com.playfulwatch.foundation.PlayfulColorScheme,
    val content: @Composable () -> Unit,
)

object PlayfulWatchFaces {
    val all: List<PlayfulWatchFace> = listOf(
        PlayfulWatchFace(
            id = "hourglass",
            displayName = "Hourglass",
            defaultColors = com.playfulwatch.foundation.PlayfulColorScheme.Hourglass,
            content = { HourglassFace() },
        ),
        PlayfulWatchFace(
            id = "liquid_vials",
            displayName = "Liquid Vials",
            defaultColors = com.playfulwatch.foundation.PlayfulColorScheme.LiquidVials,
            content = { LiquidVialsFace() },
        ),
    )

    fun byId(id: String): PlayfulWatchFace? = all.find { it.id == id }
}

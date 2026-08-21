package com.playfulwatch.watchfaces

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import com.playfulwatch.foundation.LocalPlayfulColors
import com.playfulwatch.foundation.PlayfulTheme
import org.splitties.compose.oclock.LocalIsAmbient
import org.splitties.compose.oclock.LocalTime
import org.splitties.compose.oclock.OClockCanvas

@Composable
fun LiquidVialsFace() {
    val colors = LocalPlayfulColors.current
    val time = LocalTime.current
    val isAmbient by LocalIsAmbient.current

    OClockCanvas {
        drawCircle(colors.background, radius = size.minDimension / 2f)

        val vialWidth = size.width * 0.14f
        val vialHeight = size.height * 0.55f
        val gap = size.width * 0.08f
        val startX = (size.width - (vialWidth * 3 + gap * 2)) / 2f
        val baseY = size.height * 0.72f

        val levels = listOf(
            time.hours % 12 / 12f,
            time.minutes / 60f,
            if (isAmbient) time.minutes / 60f else time.seconds / 60f,
        )
        val vialColors = listOf(colors.primary, colors.secondary, colors.tertiary)
        val labels = listOf("H", "M", "S")

        levels.forEachIndexed { index, level ->
            val left = startX + index * (vialWidth + gap)
            val top = baseY - vialHeight
            drawRoundRect(
                color = colors.onBackground.copy(alpha = 0.25f),
                topLeft = Offset(left, top),
                size = Size(vialWidth, vialHeight),
                cornerRadius = CornerRadius(vialWidth / 2f, vialWidth / 2f),
                style = Stroke(width = 3f),
            )
            val fillHeight = vialHeight * level.coerceIn(0f, 1f)
            drawRoundRect(
                color = vialColors[index],
                topLeft = Offset(left + 4f, baseY - fillHeight),
                size = Size(vialWidth - 8f, fillHeight),
                cornerRadius = CornerRadius((vialWidth - 8f) / 2f),
            )
            drawCircle(
                color = colors.accent,
                radius = 6f,
                center = Offset(left + vialWidth / 2f, top - 14f),
            )
        }
    }
}

@Composable
fun LiquidVialsFacePreview() = PlayfulTheme(com.playfulwatch.foundation.PlayfulColorScheme.LiquidVials) {
    LiquidVialsFace()
}

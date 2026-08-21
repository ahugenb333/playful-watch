package com.playfulwatch.watchfaces

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.playfulwatch.foundation.LocalPlayfulColors
import com.playfulwatch.foundation.PlayfulTheme
import org.splitties.compose.oclock.LocalIsAmbient
import org.splitties.compose.oclock.LocalTime
import org.splitties.compose.oclock.OClockCanvas

@Composable
fun HourglassFace() {
    val colors = LocalPlayfulColors.current
    val time = LocalTime.current
    val isAmbient by LocalIsAmbient.current
    val fillProgress = (time.minutes + time.seconds / 60f) / 60f

    OClockCanvas {
        drawCircle(colors.background, radius = size.minDimension / 2f)
        val frameWidth = size.width * 0.35f
        val top = size.height * 0.18f
        val bottom = size.height * 0.82f
        val centerX = size.width / 2f
        val neck = 10.dp.toPx()

        val framePath = Path().apply {
            moveTo(centerX - frameWidth / 2f, top)
            lineTo(centerX + frameWidth / 2f, top)
            lineTo(centerX + neck, size.height / 2f)
            lineTo(centerX + frameWidth / 2f, bottom)
            lineTo(centerX - frameWidth / 2f, bottom)
            lineTo(centerX - neck, size.height / 2f)
            close()
        }
        drawPath(framePath, colors.primary, style = Stroke(width = 4.dp.toPx()))

        val sandTopHeight = (size.height / 2f - top - 16.dp.toPx()) * (1f - fillProgress)
        if (sandTopHeight > 0f) {
            drawRect(
                color = colors.secondary,
                topLeft = Offset(centerX - frameWidth / 2f + 8.dp.toPx(), top + 8.dp.toPx()),
                size = Size(frameWidth - 16.dp.toPx(), sandTopHeight),
            )
        }
        val sandBottomHeight = (bottom - size.height / 2f - 16.dp.toPx()) * fillProgress
        if (sandBottomHeight > 0f) {
            drawRect(
                color = colors.tertiary,
                topLeft = Offset(
                    centerX - frameWidth / 2f + 8.dp.toPx(),
                    bottom - 8.dp.toPx() - sandBottomHeight,
                ),
                size = Size(frameWidth - 16.dp.toPx(), sandBottomHeight),
            )
        }

        if (!isAmbient) {
            rotate(time.seconds * 6f + time.minutes * 360f / 60f) {
                drawLine(
                    color = colors.accent,
                    start = Offset(centerX, centerX),
                    end = Offset(centerX, top + 24.dp.toPx()),
                    strokeWidth = 3.dp.toPx(),
                )
            }
        }

        drawCircle(colors.onBackground.copy(alpha = 0.5f), radius = 4.dp.toPx(), center = Offset(centerX, size.height / 2f))
    }
}

@Composable
fun HourglassFacePreview() = PlayfulTheme(com.playfulwatch.foundation.PlayfulColorScheme.Hourglass) {
    HourglassFace()
}

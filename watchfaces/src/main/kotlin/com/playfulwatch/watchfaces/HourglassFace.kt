package com.playfulwatch.watchfaces

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playfulwatch.foundation.LocalPlayfulColors
import org.splitties.compose.oclock.LocalIsAmbient
import org.splitties.compose.oclock.LocalTime
import org.splitties.compose.oclock.OClockCanvas

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction

private data class HourglassGeometry(
    val centerX: Float,
    val topY: Float,
    val bottomY: Float,
    val neckY: Float,
    val topHalfWidth: Float,
    val bottomHalfWidth: Float,
    val neckHalfWidth: Float,
)

private fun DrawScope.hourglassGeometry(): HourglassGeometry {
    val centerX = size.width / 2f
    val topY = size.height * 0.14f
    val bottomY = size.height * 0.66f
    val neckY = (topY + bottomY) / 2f
    val topHalfWidth = size.width * 0.28f
    val bottomHalfWidth = size.width * 0.28f
    val neckHalfWidth = 5.dp.toPx()
    return HourglassGeometry(centerX, topY, bottomY, neckY, topHalfWidth, bottomHalfWidth, neckHalfWidth)
}

private fun hourglassFramePath(geo: HourglassGeometry): Path = Path().apply {
    val cx = geo.centerX
    moveTo(cx - geo.topHalfWidth, geo.topY)
    lineTo(cx + geo.topHalfWidth, geo.topY)
    lineTo(cx + geo.neckHalfWidth, geo.neckY)
    lineTo(cx + geo.bottomHalfWidth, geo.bottomY)
    lineTo(cx - geo.bottomHalfWidth, geo.bottomY)
    lineTo(cx - geo.neckHalfWidth, geo.neckY)
    close()
}

private fun topChamberPath(geo: HourglassGeometry): Path = Path().apply {
    val cx = geo.centerX
    moveTo(cx - geo.topHalfWidth, geo.topY)
    lineTo(cx + geo.topHalfWidth, geo.topY)
    lineTo(cx + geo.neckHalfWidth, geo.neckY)
    lineTo(cx - geo.neckHalfWidth, geo.neckY)
    close()
}

private fun bottomChamberPath(geo: HourglassGeometry): Path = Path().apply {
    val cx = geo.centerX
    moveTo(cx - geo.neckHalfWidth, geo.neckY)
    lineTo(cx + geo.neckHalfWidth, geo.neckY)
    lineTo(cx + geo.bottomHalfWidth, geo.bottomY)
    lineTo(cx - geo.bottomHalfWidth, geo.bottomY)
    close()
}

private fun topSandPath(geo: HourglassGeometry, fill: Float): Path {
    if (fill <= 0f) return Path()
    val clamped = fill.coerceIn(0f, 1f)
    val cx = geo.centerX
    val surfaceY = geo.neckY - (geo.neckY - geo.topY) * clamped
    val t = (surfaceY - geo.topY) / (geo.neckY - geo.topY)
    val halfWidth = lerp(geo.topHalfWidth, geo.neckHalfWidth, t)
    return Path().apply {
        moveTo(cx - halfWidth, surfaceY)
        lineTo(cx + halfWidth, surfaceY)
        lineTo(cx + geo.neckHalfWidth, geo.neckY)
        lineTo(cx - geo.neckHalfWidth, geo.neckY)
        close()
    }
}

private fun bottomSandPath(geo: HourglassGeometry, fill: Float): Path {
    if (fill <= 0f) return Path()
    val clamped = fill.coerceIn(0f, 1f)
    val cx = geo.centerX
    val surfaceY = geo.bottomY - (geo.bottomY - geo.neckY) * clamped
    val t = (geo.bottomY - surfaceY) / (geo.bottomY - geo.neckY)
    val halfWidth = lerp(geo.bottomHalfWidth, geo.neckHalfWidth, t)
    return Path().apply {
        moveTo(cx - halfWidth, surfaceY)
        lineTo(cx + halfWidth, surfaceY)
        lineTo(cx + geo.bottomHalfWidth, geo.bottomY)
        lineTo(cx - geo.bottomHalfWidth, geo.bottomY)
        close()
    }
}

@Composable
fun HourglassFace() {
    val colors = LocalPlayfulColors.current
    val time = LocalTime.current
    val isAmbient by LocalIsAmbient.current

    val minuteProgress = (time.seconds + time.millis / 1000f) / 60f
    val topFill = 1f - minuteProgress
    val bottomFill = minuteProgress

    OClockCanvas {
        drawCircle(colors.background, radius = size.minDimension / 2f)
    }

    OClockCanvas {
        val geo = hourglassGeometry()
        val frameStroke = if (isAmbient) 2.5.dp.toPx() else 3.5.dp.toPx()
        val sandColor = colors.tertiary.copy(alpha = if (isAmbient) 0.85f else 1f)
        val frameColor = colors.primary.copy(alpha = if (isAmbient) 0.9f else 1f)

        drawPath(topChamberPath(geo), colors.background.copy(alpha = 0.35f))
        drawPath(bottomChamberPath(geo), colors.background.copy(alpha = 0.35f))

        drawPath(topSandPath(geo, topFill), sandColor)
        drawPath(bottomSandPath(geo, bottomFill), sandColor)

        drawPath(
            hourglassFramePath(geo),
            frameColor,
            style = Stroke(width = frameStroke),
        )

        val neckMaskHalfHeight = 1.dp.toPx()
        drawLine(
            color = colors.background,
            start = Offset(geo.centerX, geo.neckY - neckMaskHalfHeight),
            end = Offset(geo.centerX, geo.neckY + neckMaskHalfHeight),
            strokeWidth = geo.neckHalfWidth * 2f,
        )

        if (topFill > 0f) {
            val streamBottom = geo.bottomY - (geo.bottomY - geo.neckY) * bottomFill.coerceIn(0f, 1f)
            if (streamBottom > geo.neckY) {
                drawLine(
                    color = sandColor,
                    start = Offset(geo.centerX, geo.neckY - neckMaskHalfHeight),
                    end = Offset(geo.centerX, streamBottom),
                    strokeWidth = 3.5.dp.toPx(),
                )
            }
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val timeText = remember(time.hours, time.minutes) {
        "%02d:%02d".format(time.hours % 12, time.minutes)
    }
    val timeStyle = remember(isAmbient) {
        TextStyle(
            color = colors.onBackground.copy(alpha = if (isAmbient) 0.75f else 0.9f),
            fontSize = if (isAmbient) 14.sp else 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
    val timeLayout = remember(timeText, timeStyle) {
        textMeasurer.measure(timeText, timeStyle)
    }
    val secondsText = remember(time.seconds) { "%02d".format(time.seconds) }
    val secondsStyle = remember(isAmbient) {
        timeStyle.copy(
            fontSize = 11.sp,
            color = colors.onBackground.copy(alpha = 0.55f),
        )
    }
    val secondsLayout = remember(secondsText, secondsStyle) {
        textMeasurer.measure(secondsText, secondsStyle)
    }

    OClockCanvas {
        drawText(
            textLayoutResult = timeLayout,
            topLeft = Offset(
                (size.width - timeLayout.size.width) / 2f,
                size.height * 0.78f,
            ),
        )

        if (!isAmbient) {
            drawText(
                textLayoutResult = secondsLayout,
                topLeft = Offset(
                    (size.width - secondsLayout.size.width) / 2f,
                    size.height * 0.78f + timeLayout.size.height + 2.dp.toPx(),
                ),
            )
        }
    }
}

package com.playfulwatch.watchfaces

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playfulwatch.foundation.LocalPlayfulColors
import com.playfulwatch.foundation.PlayfulTheme
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
    val surfaceY = geo.topY + (geo.neckY - geo.topY) * (1f - clamped)
    val t = (surfaceY - geo.topY) / (geo.neckY - geo.topY)
    val halfWidth = lerp(geo.topHalfWidth, geo.neckHalfWidth, t)
    return Path().apply {
        moveTo(cx - geo.topHalfWidth, geo.topY)
        lineTo(cx + geo.topHalfWidth, geo.topY)
        lineTo(cx + halfWidth, surfaceY)
        lineTo(cx - halfWidth, surfaceY)
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

private fun flipRotation(minutes: Int, seconds: Int, millis: Int): Float {
    val flipped = minutes % 2 == 1
    val settled = if (flipped) 180f else 0f
    val flipDurationMs = 600f
    val msRemaining = (59 - seconds) * 1000 + (1000 - millis)
    if (msRemaining > flipDurationMs) return settled

    val flipProgress = 1f - msRemaining / flipDurationMs
    val nextFlipped = (minutes + 1) % 2 == 1
    return if (nextFlipped) {
        lerp(0f, 180f, flipProgress)
    } else {
        lerp(180f, 0f, flipProgress)
    }
}

@Composable
fun HourglassFace() {
    val colors = LocalPlayfulColors.current
    val time = LocalTime.current
    val isAmbient by LocalIsAmbient.current

    val minuteProgress = (time.seconds + time.millis / 1000f) / 60f
    val isFlipped = time.minutes % 2 == 1
    val topFill = if (isFlipped) minuteProgress else 1f - minuteProgress
    val bottomFill = if (isFlipped) 1f - minuteProgress else minuteProgress
    val rotation = flipRotation(time.minutes, time.seconds, time.millis)

    OClockCanvas {
        drawCircle(colors.background, radius = size.minDimension / 2f)
    }

    OClockCanvas {
        val geo = hourglassGeometry()
        val frameStroke = if (isAmbient) 2.5.dp.toPx() else 3.5.dp.toPx()
        val sandColor = colors.tertiary.copy(alpha = if (isAmbient) 0.85f else 1f)
        val frameColor = colors.primary.copy(alpha = if (isAmbient) 0.9f else 1f)

        rotate(rotation, pivot = center) {
            drawPath(topChamberPath(geo), colors.background.copy(alpha = 0.35f))
            drawPath(bottomChamberPath(geo), colors.background.copy(alpha = 0.35f))

            drawPath(topSandPath(geo, topFill), sandColor)
            drawPath(bottomSandPath(geo, bottomFill), sandColor)

            drawPath(
                hourglassFramePath(geo),
                frameColor,
                style = Stroke(width = frameStroke),
            )

            drawCircle(
                color = frameColor.copy(alpha = 0.6f),
                radius = geo.neckHalfWidth,
                center = Offset(geo.centerX, geo.neckY),
            )
        }
    }

    if (!isAmbient) {
        OClockCanvas {
            val geo = hourglassGeometry()
            val speckRadius = 2.5.dp.toPx()
            val fallProgress = (time.millis / 1000f).let { it * it }
            val neckGap = 4.dp.toPx()
            val speckY = lerp(geo.neckY - neckGap, geo.neckY + neckGap, fallProgress)

            rotate(rotation, pivot = center) {
                drawCircle(
                    color = colors.tertiary,
                    radius = speckRadius,
                    center = Offset(geo.centerX, speckY),
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

@Composable
fun HourglassFacePreview() = PlayfulTheme(com.playfulwatch.foundation.PlayfulColorScheme.Hourglass) {
    HourglassFace()
}

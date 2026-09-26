package com.playfulwatch.watchfaces

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
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
import com.playfulwatch.foundation.PlayfulTheme
import org.splitties.compose.oclock.LocalIsAmbient
import org.splitties.compose.oclock.LocalTime
import org.splitties.compose.oclock.OClockCanvas

private data class VialScale(
    val max: Int,
    val tickStep: Int,
)

private fun vialShape(left: Float, top: Float, width: Float, height: Float, bottomRadius: Float): RoundRect =
    RoundRect(
        rect = Rect(left, top, left + width, top + height),
        topLeft = CornerRadius.Zero,
        topRight = CornerRadius.Zero,
        bottomLeft = CornerRadius(bottomRadius, bottomRadius),
        bottomRight = CornerRadius(bottomRadius, bottomRadius),
    )

private fun DrawScope.drawVialShape(roundRect: RoundRect, color: androidx.compose.ui.graphics.Color, strokeWidth: Float) {
    val path = Path().apply { addRoundRect(roundRect) }
    drawPath(path, color, style = Stroke(width = strokeWidth))
}

private fun DrawScope.drawVialFill(
    left: Float,
    bottom: Float,
    innerWidth: Float,
    fillHeight: Float,
    bottomRadius: Float,
    color: androidx.compose.ui.graphics.Color,
) {
    if (fillHeight <= 0f) return
    val fillTop = bottom - fillHeight
    val roundRect = vialShape(left, fillTop, innerWidth, fillHeight, bottomRadius.coerceAtMost(fillHeight / 2f))
    drawPath(Path().apply { addRoundRect(roundRect) }, color)
}

@Composable
fun LiquidVialsFace() {
    val colors = LocalPlayfulColors.current
    val time = LocalTime.current
    val isAmbient by LocalIsAmbient.current

    val hourValue = (time.hours % 12).let { if (it == 0) 12 else it }
    val secondValue = if (isAmbient) time.minutes else time.seconds
    val valueStrings = listOf(
        hourValue.toString(),
        "%02d".format(time.minutes),
        "%02d".format(secondValue),
    )
    val scales = listOf(
        VialScale(max = 12, tickStep = 3),
        VialScale(max = 60, tickStep = 15),
        VialScale(max = 60, tickStep = 15),
    )
    val levels = listOf(
        hourValue / 12f,
        time.minutes / 60f,
        secondValue / 60f,
    )
    val vialColors = listOf(colors.primary, colors.secondary, colors.tertiary)

    val textMeasurer = rememberTextMeasurer()
    val valueStyle = remember(isAmbient) {
        TextStyle(
            color = colors.onBackground,
            fontSize = if (isAmbient) 13.sp else 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
    val tickStyle = remember(isAmbient) {
        TextStyle(
            color = colors.onBackground.copy(alpha = if (isAmbient) 0.55f else 0.7f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Start,
        )
    }
    val valueLayouts = remember(valueStrings, valueStyle) {
        valueStrings.map { textMeasurer.measure(it, valueStyle) }
    }

    OClockCanvas {
        drawCircle(colors.background, radius = size.minDimension / 2f)

        val vialWidth = size.width * 0.12f
        val vialHeight = size.height * 0.5f
        val gap = 24.dp.toPx()
        val groupWidth = vialWidth * 3f + gap * 2f
        val startX = (size.width - groupWidth) / 2f - 10.dp.toPx()
        val baseY = size.height * 0.76f
        val top = baseY - vialHeight
        val bottomRadius = vialWidth * 0.22f
        val innerInset = 4.dp.toPx()
        val innerWidth = vialWidth - innerInset * 2f
        val innerLeftInset = innerInset
        val strokeWidth = if (isAmbient) 2f else 2.5f
        val tickLength = 6.dp.toPx()
        val tickGap = 3.dp.toPx()

        levels.forEachIndexed { index, level ->
            val left = startX + index * (vialWidth + gap)
            val outer = vialShape(left, top, vialWidth, vialHeight, bottomRadius)
            drawVialShape(
                outer,
                colors.onBackground.copy(alpha = if (isAmbient) 0.35f else 0.45f),
                strokeWidth,
            )

            val fillHeight = vialHeight * level.coerceIn(0f, 1f)
            drawVialFill(
                left = left + innerLeftInset,
                bottom = baseY,
                innerWidth = innerWidth,
                fillHeight = fillHeight,
                bottomRadius = bottomRadius - innerInset,
                color = vialColors[index].copy(alpha = if (isAmbient) 0.85f else 1f),
            )

            val valueLayout = valueLayouts[index]
            drawText(
                textLayoutResult = valueLayout,
                topLeft = Offset(
                    left + (vialWidth - valueLayout.size.width) / 2f,
                    top - valueLayout.size.height - 6.dp.toPx(),
                ),
            )

            val scale = scales[index]
            for (tick in 0..scale.max step scale.tickStep) {
                val fraction = tick / scale.max.toFloat()
                val y = baseY - vialHeight * fraction
                drawLine(
                    color = colors.onBackground.copy(alpha = if (isAmbient) 0.35f else 0.5f),
                    start = Offset(left + vialWidth + tickGap, y),
                    end = Offset(left + vialWidth + tickGap + tickLength, y),
                    strokeWidth = 1.dp.toPx(),
                )
                val tickLabel = tick.toString()
                val tickLayout = textMeasurer.measure(tickLabel, tickStyle)
                drawText(
                    textLayoutResult = tickLayout,
                    topLeft = Offset(
                        left + vialWidth + tickGap + tickLength + 2.dp.toPx(),
                        y - tickLayout.size.height / 2f,
                    ),
                )
            }
        }
    }
}

@Composable
fun LiquidVialsFacePreview() = PlayfulTheme(com.playfulwatch.foundation.PlayfulColorScheme.LiquidVials) {
    LiquidVialsFace()
}

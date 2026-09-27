package com.playfulwatch.watchfaces

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

internal enum class VitalIconKind {
    Weather,
    Steps,
    Heart,
}

internal fun DrawScope.drawVitalIcon(
    kind: VitalIconKind,
    center: Offset,
    size: Float,
    color: Color,
    strokeWidth: Float = size * 0.08f,
) {
    when (kind) {
        VitalIconKind.Weather -> drawWeatherIcon(center, size, color, strokeWidth)
        VitalIconKind.Steps -> drawStepsIcon(center, size, color)
        VitalIconKind.Heart -> drawHeartIcon(center, size, color)
    }
}

private fun DrawScope.drawWeatherIcon(center: Offset, size: Float, color: Color, strokeWidth: Float) {
    val sunRadius = size * 0.22f
    drawCircle(color = color, radius = sunRadius, center = center)
    repeat(8) { index ->
        val angle = index * (Math.PI / 4)
        val inner = sunRadius + strokeWidth * 0.6f
        val outer = size * 0.34f
        drawLine(
            color = color,
            start = Offset(
                center.x + inner * cos(angle).toFloat(),
                center.y + inner * sin(angle).toFloat(),
            ),
            end = Offset(
                center.x + outer * cos(angle).toFloat(),
                center.y + outer * sin(angle).toFloat(),
            ),
            strokeWidth = strokeWidth * 0.75f,
        )
    }
    val cloudOffset = Offset(center.x + size * 0.18f, center.y + size * 0.12f)
    drawCircle(color = color.copy(alpha = 0.85f), radius = size * 0.14f, center = cloudOffset)
    drawCircle(
        color = color.copy(alpha = 0.85f),
        radius = size * 0.11f,
        center = cloudOffset + Offset(-size * 0.12f, size * 0.03f),
    )
    drawCircle(
        color = color.copy(alpha = 0.85f),
        radius = size * 0.1f,
        center = cloudOffset + Offset(size * 0.1f, size * 0.04f),
    )
}

private fun DrawScope.drawStepsIcon(center: Offset, size: Float, color: Color) {
    val cx = center.x
    val cy = center.y + size * 0.04f
    drawOval(
        color = color,
        topLeft = Offset(cx - size * 0.17f, cy - size * 0.06f),
        size = Size(size * 0.34f, size * 0.40f),
    )
    val toeY = cy - size * 0.20f
    val toeOffsets = listOf(
        -0.13f to 1.12f,
        -0.065f to 1.0f,
        0f to 0.95f,
        0.065f to 0.92f,
        0.115f to 0.88f,
    )
    val baseToeR = size * 0.052f
    toeOffsets.forEach { (xOff, scale) ->
        drawCircle(
            color = color,
            radius = baseToeR * scale,
            center = Offset(cx + size * xOff, toeY),
        )
    }
}

private fun DrawScope.drawHeartIcon(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        val w = size * 0.42f
        val h = size * 0.38f
        moveTo(center.x, center.y + h * 0.95f)
        cubicTo(
            center.x - w * 1.4f, center.y + h * 0.15f,
            center.x - w * 0.55f, center.y - h * 1.1f,
            center.x, center.y - h * 0.35f,
        )
        cubicTo(
            center.x + w * 0.55f, center.y - h * 1.1f,
            center.x + w * 1.4f, center.y + h * 0.15f,
            center.x, center.y + h * 0.95f,
        )
        close()
    }
    drawPath(path, color)
}

internal fun DrawScope.drawProgressArc(
    center: Offset,
    radius: Float,
    progress: Float,
    color: Color,
    trackColor: Color,
    strokeWidth: Float,
    startAngle: Float = 135f,
    sweepSpan: Float = 270f,
) {
    drawArc(
        color = trackColor,
        startAngle = startAngle,
        sweepAngle = sweepSpan,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth),
    )
    if (progress > 0f) {
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepSpan * progress.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round),
        )
    }
}

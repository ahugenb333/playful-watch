package com.playfulwatch.watchfaces

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.EmptyComplicationData
import androidx.wear.watchface.complications.data.GoalProgressComplicationData
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.NotConfiguredComplicationData
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import com.playfulwatch.foundation.LocalPlayfulColors
import java.time.Instant
import kotlinx.coroutines.flow.StateFlow
import org.splitties.compose.oclock.LocalIsAmbient
import org.splitties.compose.oclock.LocalTime
import org.splitties.compose.oclock.OClockCanvas

private data class VitalsSlotLayout(
    val label: String,
    val centerXRatio: Float,
    val centerYRatio: Float,
    val widthRatio: Float,
    val heightRatio: Float,
)

private val slotLayouts = listOf(
    VitalsSlotLayout("Weather", 0.5f, 0.16f, 0.56f, 0.14f),
    VitalsSlotLayout("Steps", 0.28f, 0.82f, 0.38f, 0.14f),
    VitalsSlotLayout("Heart", 0.72f, 0.82f, 0.38f, 0.14f),
)

private fun ComplicationData.displayLine(context: Context, instant: Instant): String? = when (this) {
    is EmptyComplicationData,
    is NoDataComplicationData,
    is NotConfiguredComplicationData -> null
    is ShortTextComplicationData -> {
        val main = text?.getTextAt(context.resources, instant)?.toString()?.trim().orEmpty()
        val title = title?.getTextAt(context.resources, instant)?.toString()?.trim().orEmpty()
        sequenceOf(title, main).filter { it.isNotEmpty() }.joinToString(" · ").ifEmpty { null }
    }
    is LongTextComplicationData -> {
        val main = text?.getTextAt(context.resources, instant)?.toString()?.trim().orEmpty()
        val title = title?.getTextAt(context.resources, instant)?.toString()?.trim().orEmpty()
        sequenceOf(title, main).filter { it.isNotEmpty() }.joinToString(" · ").ifEmpty { null }
    }
    is RangedValueComplicationData -> {
        val valueText = text?.getTextAt(context.resources, instant)?.toString()?.trim().orEmpty()
        if (valueText.isNotEmpty()) valueText else "${value.toInt()}"
    }
    is GoalProgressComplicationData -> {
        val valueText = text?.getTextAt(context.resources, instant)?.toString()?.trim().orEmpty()
        if (valueText.isNotEmpty()) valueText else "${value.toInt()} / ${targetValue.toInt()}"
    }
    else -> null
}

@Composable
private fun rememberComplicationLine(
    flow: StateFlow<ComplicationData>?,
    previewPlaceholder: String,
): String {
    val context = LocalContext.current
    if (flow == null) return previewPlaceholder
    val data by flow.collectAsState()
    return remember(data) {
        data.displayLine(context, Instant.now()) ?: previewPlaceholder
    }
}

@Composable
fun VitalsTriadFace(
    complicationData: Map<Int, StateFlow<ComplicationData>> = emptyMap(),
) {
    val colors = LocalPlayfulColors.current
    val isAmbient by LocalIsAmbient.current
    val time = LocalTime.current
    val textMeasurer = rememberTextMeasurer()

    val weatherLine = rememberComplicationLine(
        complicationData[PlayfulComplicationSlots.WEATHER],
        previewPlaceholder = "72° · Sunny",
    )
    val stepsLine = rememberComplicationLine(
        complicationData[PlayfulComplicationSlots.STEPS],
        previewPlaceholder = "4,820 steps",
    )
    val heartLine = rememberComplicationLine(
        complicationData[PlayfulComplicationSlots.HEART_RATE],
        previewPlaceholder = "68 bpm",
    )
    val slotValues = listOf(weatherLine, stepsLine, heartLine)

    val timeText = remember(time.hours, time.minutes) {
        "%02d:%02d".format(time.hours % 12, time.minutes)
    }
    val timeStyle = remember(isAmbient) {
        TextStyle(
            color = colors.onBackground.copy(alpha = if (isAmbient) 0.85f else 1f),
            fontSize = if (isAmbient) 28.sp else 34.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
    val timeLayout = remember(timeText, timeStyle) {
        textMeasurer.measure(timeText, timeStyle)
    }

    OClockCanvas {
        val ringStroke = 3.dp.toPx()
        drawCircle(
            color = colors.secondary.copy(alpha = if (isAmbient) 0.35f else 0.55f),
            radius = size.minDimension * 0.42f,
            center = center,
            style = Stroke(width = ringStroke),
        )

        slotLayouts.forEachIndexed { index, slot ->
            val value = slotValues[index]
            val boxWidth = size.width * slot.widthRatio
            val boxHeight = size.height * slot.heightRatio
            val left = size.width * slot.centerXRatio - boxWidth / 2f
            val top = size.height * slot.centerYRatio - boxHeight / 2f
            val roundRect = RoundRect(
                rect = Rect(left, top, left + boxWidth, top + boxHeight),
                cornerRadius = CornerRadius(12.dp.toPx()),
            )
            drawPath(
                path = Path().apply { addRoundRect(roundRect) },
                color = colors.primary.copy(alpha = if (isAmbient) 0.18f else 0.28f),
            )
            drawPath(
                path = Path().apply { addRoundRect(roundRect) },
                color = colors.onBackground.copy(alpha = if (isAmbient) 0.35f else 0.5f),
                style = Stroke(width = 1.5.dp.toPx()),
            )

            val labelStyle = TextStyle(
                color = colors.tertiary.copy(alpha = if (isAmbient) 0.65f else 0.85f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
            val valueStyle = TextStyle(
                color = colors.onBackground.copy(alpha = if (isAmbient) 0.75f else 0.95f),
                fontSize = if (isAmbient) 11.sp else 12.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
            )
            val labelLayout = textMeasurer.measure(slot.label, labelStyle, maxLines = 1)
            val valueLayout = textMeasurer.measure(value, valueStyle, maxLines = 2)

            drawText(
                textLayoutResult = labelLayout,
                topLeft = Offset(
                    left + (boxWidth - labelLayout.size.width) / 2f,
                    top + boxHeight * 0.18f,
                ),
            )
            drawText(
                textLayoutResult = valueLayout,
                topLeft = Offset(
                    left + (boxWidth - valueLayout.size.width) / 2f,
                    top + boxHeight * 0.48f,
                ),
            )
        }

        drawText(
            textLayoutResult = timeLayout,
            topLeft = Offset(
                (size.width - timeLayout.size.width) / 2f,
                size.height * 0.44f - timeLayout.size.height / 2f,
            ),
        )
    }
}

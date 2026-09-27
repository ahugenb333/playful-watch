package com.playfulwatch.watchfaces

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.EmptyComplicationData
import androidx.wear.watchface.complications.data.GoalProgressComplicationData
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.NotConfiguredComplicationData
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import com.playfulwatch.foundation.LocalPlayfulColors
import com.playfulwatch.foundation.PlayfulColorScheme
import java.time.Instant
import java.util.Locale
import kotlinx.coroutines.flow.StateFlow
import org.splitties.compose.oclock.LocalIsAmbient
import org.splitties.compose.oclock.OClockCanvas

internal data class VitalCardDisplay(
    val primary: String,
    val secondary: String?,
    val tertiary: String?,
    val progress: Float?,
    val iconKind: VitalIconKind,
)

private fun ComplicationText?.textAt(context: Context, instant: Instant): String? =
    this?.getTextAt(context.resources, instant)?.toString()?.trim()?.takeIf { it.isNotEmpty() }

private fun formatCount(value: Float): String =
    "%,.0f".format(Locale.US, value)

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun ComplicationData.toVitalCard(
    context: Context,
    instant: Instant,
    iconKind: VitalIconKind,
): VitalCardDisplay? {
    return when (this) {
    is EmptyComplicationData,
    is NoDataComplicationData,
    is NotConfiguredComplicationData -> null
    is ShortTextComplicationData -> {
        val main = text.textAt(context, instant)
        val title = title.textAt(context, instant)
        VitalCardDisplay(
            primary = main ?: title ?: return null,
            secondary = main?.let { title?.takeIf { t -> t != main } },
            tertiary = contentDescription.textAt(context, instant),
            progress = null,
            iconKind = iconKind,
        )
    }
    is LongTextComplicationData -> {
        val body = text.textAt(context, instant).orEmpty()
        val lines = body.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val title = title.textAt(context, instant)
        VitalCardDisplay(
            primary = lines.firstOrNull() ?: title ?: return null,
            secondary = title?.takeIf { lines.isEmpty() || it != lines.first() }
                ?: lines.getOrNull(1),
            tertiary = lines.getOrNull(2) ?: lines.drop(2).joinToString(" · ").takeIf { it.isNotEmpty() },
            progress = null,
            iconKind = iconKind,
        )
    }
    is RangedValueComplicationData -> {
        val title = title.textAt(context, instant)
        val label = text.textAt(context, instant)
        val numericPrimary = when (valueType) {
            RangedValueComplicationData.TYPE_PERCENTAGE -> "${value.toInt()}%"
            else -> formatCount(value)
        }
        val progress = if (max > min) ((value - min) / (max - min)).coerceIn(0f, 1f) else null
        val rangeNote = if (max > min && valueType != RangedValueComplicationData.TYPE_PERCENTAGE) {
            "${formatCount(min)} – ${formatCount(max)}"
        } else {
            null
        }
        VitalCardDisplay(
            primary = label ?: numericPrimary,
            secondary = title ?: (label?.let { numericPrimary }),
            tertiary = rangeNote ?: contentDescription.textAt(context, instant),
            progress = progress,
            iconKind = iconKind,
        )
    }
    is GoalProgressComplicationData -> {
        val title = title.textAt(context, instant)
        val label = text.textAt(context, instant)
        val progress = if (targetValue > 0f) (value / targetValue).coerceIn(0f, 1f) else null
        VitalCardDisplay(
            primary = label ?: formatCount(value),
            secondary = title ?: "Goal ${formatCount(targetValue)}",
            tertiary = progress?.let { "${(it * 100).toInt()}% of goal" },
            progress = progress,
            iconKind = iconKind,
        )
    }
    is MonochromaticImageComplicationData -> VitalCardDisplay(
        primary = contentDescription.textAt(context, instant) ?: "—",
        secondary = null,
        tertiary = null,
        progress = null,
        iconKind = iconKind,
    )
    is SmallImageComplicationData -> VitalCardDisplay(
        primary = contentDescription.textAt(context, instant) ?: "—",
        secondary = null,
        tertiary = null,
        progress = null,
        iconKind = iconKind,
    )
    else -> null
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun ComplicationData.loadProviderIcon(context: Context, isAmbient: Boolean): Drawable? {
    val androidIcon = when (this) {
        is ShortTextComplicationData -> monochromaticImage?.let { pickIcon(it, isAmbient) }
        is LongTextComplicationData -> monochromaticImage?.let { pickIcon(it, isAmbient) }
        is RangedValueComplicationData -> monochromaticImage?.let { pickIcon(it, isAmbient) }
        is GoalProgressComplicationData -> monochromaticImage?.let { pickIcon(it, isAmbient) }
        is MonochromaticImageComplicationData -> pickIcon(monochromaticImage, isAmbient)
        is SmallImageComplicationData -> smallImage.let { if (isAmbient) it.ambientImage ?: it.image else it.image }
        else -> null
    }
    return androidIcon?.loadDrawable(context)?.mutate()
}

private fun pickIcon(
    image: androidx.wear.watchface.complications.data.MonochromaticImage,
    isAmbient: Boolean,
) = if (isAmbient) image.ambientImage ?: image.image else image.image

private val previewWeather = VitalCardDisplay(
    primary = "72°",
    secondary = "Mostly sunny",
    tertiary = "Feels 68° · H78 L61",
    progress = null,
    iconKind = VitalIconKind.Weather,
)
private val previewSteps = VitalCardDisplay(
    primary = "4,820",
    secondary = "Steps",
    tertiary = "72% of 6,700 goal",
    progress = 0.72f,
    iconKind = VitalIconKind.Steps,
)
private val previewHeart = VitalCardDisplay(
    primary = "68",
    secondary = "BPM · Resting",
    tertiary = "Range 62–74 today",
    progress = 0.45f,
    iconKind = VitalIconKind.Heart,
)

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun rememberVitalCard(
    flow: StateFlow<ComplicationData>?,
    iconKind: VitalIconKind,
    preview: VitalCardDisplay,
): VitalCardDisplay {
    val context = LocalContext.current
    val isAmbient by LocalIsAmbient.current
    if (flow == null) return preview
    val data by flow.collectAsState()
    return remember(data, isAmbient) {
        data.toVitalCard(context, Instant.now(), iconKind) ?: preview
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun rememberProviderIcon(flow: StateFlow<ComplicationData>?): Drawable? {
    val context = LocalContext.current
    val isAmbient by LocalIsAmbient.current
    if (flow == null) return null
    val data by flow.collectAsState()
    return remember(data, isAmbient) { data.loadProviderIcon(context, isAmbient) }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun VitalsTriadFace(
    complicationData: Map<Int, StateFlow<ComplicationData>> = emptyMap(),
) {
    val colors = LocalPlayfulColors.current
    val isAmbient by LocalIsAmbient.current
    val textMeasurer = rememberTextMeasurer()

    val weather = rememberVitalCard(
        complicationData[PlayfulComplicationSlots.WEATHER],
        VitalIconKind.Weather,
        previewWeather,
    )
    val steps = rememberVitalCard(
        complicationData[PlayfulComplicationSlots.STEPS],
        VitalIconKind.Steps,
        previewSteps,
    )
    val heart = rememberVitalCard(
        complicationData[PlayfulComplicationSlots.HEART_RATE],
        VitalIconKind.Heart,
        previewHeart,
    )

    val weatherIcon = rememberProviderIcon(complicationData[PlayfulComplicationSlots.WEATHER])
    val stepsIcon = rememberProviderIcon(complicationData[PlayfulComplicationSlots.STEPS])
    val heartIcon = rememberProviderIcon(complicationData[PlayfulComplicationSlots.HEART_RATE])

    OClockCanvas {
        val clusterCenterY = size.height * 0.48f
        drawWeatherBanner(
            card = weather,
            providerIcon = weatherIcon,
            colors = colors,
            isAmbient = isAmbient,
            textMeasurer = textMeasurer,
            centerY = clusterCenterY - size.height * 0.15f,
        )
        drawMetricTile(
            card = steps,
            providerIcon = stepsIcon,
            center = Offset(size.width * 0.27f, clusterCenterY + size.height * 0.14f),
            accent = colors.secondary,
            colors = colors,
            isAmbient = isAmbient,
            textMeasurer = textMeasurer,
        )
        drawMetricTile(
            card = heart,
            providerIcon = heartIcon,
            center = Offset(size.width * 0.73f, clusterCenterY + size.height * 0.14f),
            accent = colors.accent,
            colors = colors,
            isAmbient = isAmbient,
            textMeasurer = textMeasurer,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWeatherBanner(
    card: VitalCardDisplay,
    providerIcon: Drawable?,
    colors: PlayfulColorScheme,
    isAmbient: Boolean,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    centerY: Float,
) {
    val boxWidth = size.width * 0.80f
    val boxHeight = size.height * 0.21f
    val left = (size.width - boxWidth) / 2f
    val top = centerY - boxHeight / 2f
    val paddingH = 10.dp.toPx()
    val paddingTop = 8.dp.toPx()
    val paddingBottom = 8.dp.toPx()
    val inner = Rect(
        left + paddingH,
        top + paddingTop,
        left + boxWidth - paddingH,
        top + boxHeight - paddingBottom,
    )

    val boxPath = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(left, top, left + boxWidth, top + boxHeight),
                cornerRadius = CornerRadius(12.dp.toPx()),
            ),
        )
    }
    drawPath(boxPath, colors.primary.copy(alpha = if (isAmbient) 0.15f else 0.22f))
    drawPath(
        boxPath,
        colors.onBackground.copy(alpha = if (isAmbient) 0.25f else 0.4f),
        style = Stroke(width = 1.dp.toPx()),
    )

    val iconColumnWidth = inner.height * 0.98f
    val iconSize = inner.height * 0.86f
    val iconCenter = Offset(
        inner.left + iconColumnWidth / 2f,
        inner.top + inner.height / 2f,
    )
    val textLeft = inner.left + iconColumnWidth + 6.dp.toPx()
    val textMaxWidth = (inner.right - textLeft).coerceAtLeast(0f)

    val primaryStyle = TextStyle(
        color = colors.onBackground,
        fontSize = if (isAmbient) 11.sp else 12.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 13.sp,
    )
    val secondaryStyle = TextStyle(
        color = colors.onBackground.copy(alpha = 0.9f),
        fontSize = 8.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 10.sp,
    )
    val tertiaryStyle = TextStyle(
        color = colors.tertiary.copy(alpha = 0.85f),
        fontSize = 7.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 9.sp,
    )

    val primaryLayout = measureConstrained(textMeasurer, card.primary, primaryStyle, textMaxWidth, maxLines = 1)
    val secondaryLayout = card.secondary?.let {
        measureConstrained(textMeasurer, it, secondaryStyle, textMaxWidth, maxLines = 1)
    }
    val tertiaryLayout = card.tertiary?.let {
        measureConstrained(textMeasurer, it, tertiaryStyle, textMaxWidth, maxLines = 2, ellipsis = false)
    }

    var y = inner.top

    clipRect(left, top, left + boxWidth, top + boxHeight) {
        drawSlotIcon(providerIcon, card.iconKind, iconCenter, iconSize, colors.tertiary, isAmbient)
        drawText(primaryLayout, topLeft = Offset(textLeft, y))
        y += primaryLayout.size.height.toFloat() + 1.dp.toPx()
        secondaryLayout?.let {
            drawText(it, topLeft = Offset(textLeft, y))
            y += it.size.height.toFloat() + 1.dp.toPx()
        }
        tertiaryLayout?.let {
            drawText(it, topLeft = Offset(textLeft, y))
        }
    }
}

private fun measureConstrained(
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    text: String,
    style: TextStyle,
    maxWidth: Float,
    maxLines: Int,
    ellipsis: Boolean = true,
) = textMeasurer.measure(
    text = text,
    style = style,
    maxLines = maxLines,
    overflow = if (ellipsis) TextOverflow.Ellipsis else TextOverflow.Clip,
    constraints = Constraints(maxWidth = maxWidth.toInt().coerceAtLeast(1)),
)

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMetricTile(
    card: VitalCardDisplay,
    providerIcon: Drawable?,
    center: Offset,
    accent: Color,
    colors: PlayfulColorScheme,
    isAmbient: Boolean,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
    val tileWidth = size.width * 0.40f
    val tileHeight = size.height * 0.21f
    val left = center.x - tileWidth / 2f
    val top = center.y - tileHeight / 2f
    val paddingH = 8.dp.toPx()
    val paddingV = 8.dp.toPx()
    val inner = Rect(
        left + paddingH,
        top + paddingV,
        left + tileWidth - paddingH,
        top + tileHeight - paddingV,
    )

    val tilePath = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(left, top, left + tileWidth, top + tileHeight),
                cornerRadius = CornerRadius(12.dp.toPx()),
            ),
        )
    }
    drawPath(tilePath, colors.primary.copy(alpha = if (isAmbient) 0.12f else 0.18f))
    drawPath(
        tilePath,
        colors.onBackground.copy(alpha = if (isAmbient) 0.22f else 0.35f),
        style = Stroke(width = 1.dp.toPx()),
    )

    val iconColumnWidth = inner.height * 0.95f
    val iconSize = inner.height * 0.82f
    val iconCenter = Offset(
        inner.left + iconColumnWidth / 2f,
        inner.top + inner.height / 2f,
    )
    val textLeft = inner.left + iconColumnWidth + 5.dp.toPx()
    val textMaxWidth = inner.right - textLeft

    val primaryStyle = TextStyle(
        color = colors.onBackground,
        fontSize = if (isAmbient) 10.sp else 11.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 12.sp,
    )
    val secondaryStyle = TextStyle(
        color = colors.onBackground.copy(alpha = 0.88f),
        fontSize = 8.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 10.sp,
    )
    val tertiaryStyle = TextStyle(
        color = accent.copy(alpha = if (isAmbient) 0.75f else 0.9f),
        fontSize = 7.sp,
        lineHeight = 9.sp,
    )

    val primaryLayout = measureConstrained(textMeasurer, card.primary, primaryStyle, textMaxWidth, maxLines = 1)
    val secondaryLayout = card.secondary?.let {
        measureConstrained(textMeasurer, it, secondaryStyle, textMaxWidth, maxLines = 2, ellipsis = false)
    }
    val tertiaryLayout = card.tertiary?.let {
        measureConstrained(textMeasurer, it, tertiaryStyle, textMaxWidth, maxLines = 2, ellipsis = false)
    }

    var textY = inner.top

    clipRect(left, top, left + tileWidth, top + tileHeight) {
        drawSlotIcon(providerIcon, card.iconKind, iconCenter, iconSize, accent, isAmbient)
        drawText(primaryLayout, topLeft = Offset(textLeft, textY))
        textY += primaryLayout.size.height.toFloat() + 1.dp.toPx()
        secondaryLayout?.let {
            drawText(it, topLeft = Offset(textLeft, textY))
            textY += it.size.height.toFloat() + 1.dp.toPx()
        }
        tertiaryLayout?.let {
            drawText(it, topLeft = Offset(textLeft, textY))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSlotIcon(
    providerIcon: Drawable?,
    fallbackKind: VitalIconKind,
    center: Offset,
    sizePx: Float,
    tint: Color,
    isAmbient: Boolean,
) {
    if (providerIcon != null) {
        val half = (sizePx / 2f).toInt()
        val left = (center.x - half).toInt()
        val top = (center.y - half).toInt()
        providerIcon.setBounds(left, top, left + half * 2, top + half * 2)
        providerIcon.alpha = if (isAmbient) 180 else 255
        providerIcon.draw(drawContext.canvas.nativeCanvas)
    } else {
        drawVitalIcon(
            kind = fallbackKind,
            center = center,
            size = sizePx,
            color = tint.copy(alpha = if (isAmbient) 0.75f else 1f),
        )
    }
}

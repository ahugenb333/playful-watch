package com.playfulwatch.wear

import androidx.compose.runtime.Composable
import androidx.wear.watchface.complications.data.ComplicationData
import com.playfulwatch.foundation.PlayfulTheme
import android.graphics.RectF
import androidx.wear.watchface.complications.ComplicationSlotBounds
import androidx.wear.watchface.complications.DefaultComplicationDataSourcePolicy
import androidx.wear.watchface.complications.SystemDataSources
import androidx.wear.watchface.complications.data.ComplicationType
import com.playfulwatch.watchfaces.HourglassFace
import com.playfulwatch.watchfaces.LiquidVialsFace
import com.playfulwatch.watchfaces.PlayfulComplicationSlots
import com.playfulwatch.watchfaces.PlayfulFaceGallery
import com.playfulwatch.watchfaces.PlayfulWatchFaces
import com.playfulwatch.watchfaces.VitalsTriadFace
import kotlinx.coroutines.flow.StateFlow
import org.splitties.compose.oclock.ComposeWatchFaceService
import org.splitties.compose.oclock.InvalidationMode

class CollectionWatchFaceService : ComposeWatchFaceService(
    invalidationMode = InvalidationMode.WaitForInvalidation,
) {
    override fun watchFaceContent(): @Composable (Map<Int, StateFlow<ComplicationData>>) -> Unit = {
        PlayfulFaceGallery()
    }
}

class HourglassWatchFaceService : ComposeWatchFaceService(
    invalidationMode = InvalidationMode.WaitForInvalidation,
) {
    override fun watchFaceContent(): @Composable (Map<Int, StateFlow<ComplicationData>>) -> Unit = {
        PlayfulTheme(PlayfulWatchFaces.byId("hourglass")!!.defaultColors) {
            HourglassFace()
        }
    }
}

class LiquidVialsWatchFaceService : ComposeWatchFaceService(
    invalidationMode = InvalidationMode.WaitForInvalidation,
) {
    override fun watchFaceContent(): @Composable (Map<Int, StateFlow<ComplicationData>>) -> Unit = {
        PlayfulTheme(PlayfulWatchFaces.byId("liquid_vials")!!.defaultColors) {
            LiquidVialsFace()
        }
    }
}

class VitalsTriadWatchFaceService : ComposeWatchFaceService(
    complicationSlotIds = PlayfulComplicationSlots.vitalsTriad,
    invalidationMode = InvalidationMode.WaitForInvalidation,
) {
    override fun watchFaceContent(): @Composable (Map<Int, StateFlow<ComplicationData>>) -> Unit = { complicationData ->
        PlayfulTheme(PlayfulWatchFaces.byId("vitals_triad")!!.defaultColors) {
            VitalsTriadFace(complicationData = complicationData)
        }
    }

    override fun supportedComplicationTypes(slotId: Int): List<ComplicationType> = when (slotId) {
        PlayfulComplicationSlots.WEATHER -> listOf(
            ComplicationType.SHORT_TEXT,
            ComplicationType.LONG_TEXT,
            ComplicationType.SMALL_IMAGE,
            ComplicationType.MONOCHROMATIC_IMAGE,
        )
        PlayfulComplicationSlots.STEPS -> listOf(
            ComplicationType.RANGED_VALUE,
            ComplicationType.GOAL_PROGRESS,
            ComplicationType.SHORT_TEXT,
        )
        PlayfulComplicationSlots.HEART_RATE -> listOf(
            ComplicationType.RANGED_VALUE,
            ComplicationType.SHORT_TEXT,
        )
        else -> emptyList()
    }

    override fun complicationSlotBounds(slotId: Int): ComplicationSlotBounds = when (slotId) {
        PlayfulComplicationSlots.WEATHER -> ComplicationSlotBounds(RectF(0.10f, 0.24f, 0.90f, 0.46f))
        PlayfulComplicationSlots.STEPS -> ComplicationSlotBounds(RectF(0.02f, 0.52f, 0.48f, 0.74f))
        PlayfulComplicationSlots.HEART_RATE -> ComplicationSlotBounds(RectF(0.52f, 0.52f, 0.98f, 0.74f))
        else -> super.complicationSlotBounds(slotId)
    }

    override fun defaultComplicationDataSourcePolicy(slotId: Int): DefaultComplicationDataSourcePolicy =
        when (slotId) {
            PlayfulComplicationSlots.STEPS -> DefaultComplicationDataSourcePolicy(
                SystemDataSources.DATA_SOURCE_STEP_COUNT,
                ComplicationType.SHORT_TEXT,
            )
            else -> DefaultComplicationDataSourcePolicy()
        }
}

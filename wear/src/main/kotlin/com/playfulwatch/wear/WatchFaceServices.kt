package com.playfulwatch.wear

import androidx.compose.runtime.Composable
import androidx.wear.watchface.complications.data.ComplicationData
import com.playfulwatch.foundation.PlayfulTheme
import com.playfulwatch.watchfaces.HourglassFace
import com.playfulwatch.watchfaces.LiquidVialsFace
import com.playfulwatch.watchfaces.PlayfulFaceGallery
import com.playfulwatch.watchfaces.PlayfulWatchFaces
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

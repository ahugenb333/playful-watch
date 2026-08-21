package com.playfulwatch.watchfaces

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.playfulwatch.foundation.PlayfulTheme
import org.splitties.compose.oclock.OClockCanvas
import org.splitties.compose.oclock.TapEvent

@Composable
fun PlayfulFaceGallery(
    faces: List<PlayfulWatchFace> = PlayfulWatchFaces.all,
) {
    var index by remember { mutableIntStateOf(0) }
    val face = faces[index]

    PlayfulTheme(face.defaultColors) {
        OClockCanvas(
            onTap = handler@{ event ->
                val edge = px(48)
                val onLeft = event.position.x <= edge
                val onRight = !onLeft && event.position.x >= size.width - edge
                if (!onLeft && !onRight) return@handler false
                if (event is TapEvent.Up) {
                    val last = faces.lastIndex
                    index = when {
                        onLeft -> if (index == 0) last else index - 1
                        else -> if (index == last) 0 else index + 1
                    }
                }
                true
            },
        ) {}
        face.content()
    }
}

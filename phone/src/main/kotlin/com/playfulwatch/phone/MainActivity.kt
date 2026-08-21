package com.playfulwatch.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.playfulwatch.foundation.PlayfulTheme
import com.playfulwatch.foundation.rememberPlayfulColorEditor
import com.playfulwatch.watchfaces.PlayfulWatchFace
import com.playfulwatch.watchfaces.PlayfulWatchFaces
import org.splitties.compose.oclock.OClockRootCanvas

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                PhoneGalleryScreen()
            }
        }
    }
}

@Composable
private fun PhoneGalleryScreen() {
    val faces = remember { PlayfulWatchFaces.all }
    var selectedIndex by remember { mutableIntStateOf(0) }
    val selected = faces[selectedIndex]
    val editor = rememberPlayfulColorEditor(selected.defaultColors)
    // Reset editor when switching faces
    androidx.compose.runtime.LaunchedEffect(selected.id) {
        editor.updatePrimary(selected.defaultColors.primary)
        editor.updateSecondary(selected.defaultColors.secondary)
        editor.updateTertiary(selected.defaultColors.tertiary)
        editor.updateBackground(selected.defaultColors.background)
        editor.updateAccent(selected.defaultColors.accent)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Playful Watch Gallery", style = MaterialTheme.typography.headlineSmall)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(CircleShape)
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                PlayfulTheme(editor.colors) {
                    OClockRootCanvas {
                        selected.content()
                    }
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(faces) { face ->
                    FaceRow(
                        face = face,
                        selected = face.id == selected.id,
                        onClick = {
                            selectedIndex = faces.indexOf(face)
                        },
                    )
                }
            }
            ColorPickerRow("Primary", editor.colors.primary) { editor.updatePrimary(it) }
            ColorPickerRow("Secondary", editor.colors.secondary) { editor.updateSecondary(it) }
            ColorPickerRow("Accent", editor.colors.accent) { editor.updateAccent(it) }
        }
    }
}

@Composable
private fun FaceRow(face: PlayfulWatchFace, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(face.displayName, color = if (selected) Color.White else Color.Gray)
        Text(if (selected) "Selected" else "", color = Color(0xFF6C63FF))
    }
}

@Composable
private fun ColorPickerRow(label: String, color: Color, onPick: (Color) -> Unit) {
    val swatches = listOf(
        Color(0xFF6C63FF), Color(0xFF00C2A8), Color(0xFFFFB703),
        Color(0xFFE8C547), Color(0xFF48CAE4), Color(0xFFFF6B6B),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            swatches.forEach { swatch ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(swatch)
                        .clickable { onPick(swatch) },
                )
            }
        }
    }
}

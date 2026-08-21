package com.playfulwatch.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.playfulwatch.watchfaces.PlayfulFaceGallery
import com.playfulwatch.watchfaces.PlayfulWatchFaces

class GalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearGallery()
        }
    }
}

@Composable
private fun WearGallery() {
    PlayfulFaceGallery(faces = PlayfulWatchFaces.all)
}

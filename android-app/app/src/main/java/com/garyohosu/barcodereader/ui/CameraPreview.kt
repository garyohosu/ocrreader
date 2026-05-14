package com.garyohosu.barcodereader.ui

import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.garyohosu.barcodereader.camera.BarcodeScannerController

@Composable
fun CameraPreview(
    controller: BarcodeScannerController,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        onDispose { controller.stop() }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).also { previewView ->
                controller.start(lifecycleOwner, previewView.surfaceProvider)
            }
        },
        modifier = modifier
    )
}

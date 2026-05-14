package com.garyohosu.barcodereader.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class BarcodeAnalyzer(
    private val onDetected: (String?) -> Unit
) : ImageAnalysis.Analyzer {

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        // TODO: Task 5 — ML Kit barcode scanning
        imageProxy.close()
    }
}

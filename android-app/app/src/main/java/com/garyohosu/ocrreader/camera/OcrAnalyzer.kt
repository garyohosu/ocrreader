package com.garyohosu.ocrreader.camera

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions

class OcrAnalyzer(
    private val onDetected: (String?) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(image)
            .addOnSuccessListener { text ->
                val detectedText = text.text.trim().takeIf { it.isNotEmpty() }
                onDetected(detectedText)
            }
            .addOnFailureListener {
                onDetected(null)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}

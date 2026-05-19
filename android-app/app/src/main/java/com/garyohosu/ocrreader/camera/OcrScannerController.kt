package com.garyohosu.ocrreader.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

class OcrScannerController(
    private val context: Context,
    private val onDetected: (String?) -> Unit
) {
    private val analysisExecutor = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var surfaceProvider: Preview.SurfaceProvider? = null
    @Volatile
    private var readRequested = false

    fun start(lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) {
        this.lifecycleOwner = lifecycleOwner
        this.surfaceProvider = surfaceProvider
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            cameraProvider = future.get()
            bindCurrentMode()
        }, ContextCompat.getMainExecutor(context))
    }

    fun requestRead() {
        readRequested = true
        bindCurrentMode()
    }

    fun stop() {
        readRequested = false
        cameraProvider?.unbindAll()
    }

    private fun bindCurrentMode() {
        val provider = cameraProvider ?: return
        val owner = lifecycleOwner ?: return
        val surface = surfaceProvider ?: return

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(surface)
        }

        val useAnalysis = readRequested
        val imageAnalysis = if (useAnalysis) {
            ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        analysisExecutor,
                        OcrAnalyzer { value ->
                            readRequested = false
                            onDetected(value)
                            ContextCompat.getMainExecutor(context).execute {
                                bindCurrentMode()
                            }
                        }
                    )
                }
        } else {
            null
        }

        try {
            provider.unbindAll()
            if (imageAnalysis != null) {
                provider.bindToLifecycle(
                    owner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } else {
                provider.bindToLifecycle(
                    owner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview
                )
            }
        } catch (_: Exception) {
            // カメラが利用できない端末では無視
        }
    }
}

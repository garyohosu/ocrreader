package com.garyohosu.barcodereader

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garyohosu.barcodereader.audio.FeedbackSoundPlayer
import com.garyohosu.barcodereader.camera.BarcodeScannerController
import com.garyohosu.barcodereader.domain.ScanPhase
import com.garyohosu.barcodereader.domain.ScanResult
import com.garyohosu.barcodereader.ui.CameraPreview
import com.garyohosu.barcodereader.ui.ResultScreen
import com.garyohosu.barcodereader.ui.ScanScreen
import com.garyohosu.barcodereader.ui.StartScreen
import com.garyohosu.barcodereader.ui.theme.BarcodeReaderTheme
import com.garyohosu.barcodereader.viewmodel.ScanViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarcodeReaderTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val vm: ScanViewModel = viewModel()
                    val state by vm.state.collectAsStateWithLifecycle()

                    val controller = remember {
                        BarcodeScannerController(this@MainActivity) { value ->
                            vm.onBarcodeDetected(value)
                        }
                    }

                    val soundPlayer = remember { FeedbackSoundPlayer() }
                    DisposableEffect(Unit) {
                        onDispose { soundPlayer.release() }
                    }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { granted ->
                        if (granted) vm.onScanStart() else vm.onPermissionDenied()
                    }

                    LaunchedEffect(vm.soundEvent) {
                        vm.soundEvent.collect { event ->
                            soundPlayer.play(event)
                        }
                    }

                    when (state.phase) {
                        ScanPhase.IDLE -> StartScreen(
                            permissionDenied = state.permissionDenied,
                            onScanStart = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                        )
                        ScanPhase.WAITING_FOR_FIRST, ScanPhase.WAITING_FOR_SECOND -> ScanScreen(
                            phase = state.phase,
                            errorMessage = state.errorMessage,
                            onCancel = vm::onCancel,
                            cameraContent = { CameraPreview(controller = controller) }
                        )
                        ScanPhase.RESULT -> ResultScreen(
                            result = state.result ?: ScanResult.NG,
                            barcode1 = state.barcode1,
                            barcode2 = state.barcode2,
                            onRetry = vm::onRetry
                        )
                    }
                }
            }
        }
    }
}

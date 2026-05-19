package com.garyohosu.ocrreader

import android.Manifest
import android.content.Intent
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
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garyohosu.ocrreader.audio.FeedbackSoundPlayer
import com.garyohosu.ocrreader.camera.OcrScannerController
import com.garyohosu.ocrreader.data.CsvLogRepository
import com.garyohosu.ocrreader.data.SettingsRepository
import com.garyohosu.ocrreader.domain.ScanPhase
import com.garyohosu.ocrreader.domain.ScanResult
import com.garyohosu.ocrreader.ui.CameraPreview
import com.garyohosu.ocrreader.ui.ResultScreen
import com.garyohosu.ocrreader.ui.ScanScreen
import com.garyohosu.ocrreader.ui.StartScreen
import com.garyohosu.ocrreader.ui.theme.OcrReaderTheme
import com.garyohosu.ocrreader.viewmodel.ScanViewModel
import com.garyohosu.ocrreader.viewmodel.ScanViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OcrReaderTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val logRepo = remember { CsvLogRepository(this@MainActivity) }
                    val settingsRepo = remember { SettingsRepository(this@MainActivity) }
                    val vm: ScanViewModel = viewModel(
                        factory = ScanViewModelFactory(logRepo, settingsRepo)
                    )
                    val state by vm.state.collectAsStateWithLifecycle()
                    val logCount by vm.logCount.collectAsStateWithLifecycle()
                    val targetCount by vm.targetCount.collectAsStateWithLifecycle()
                    val ocrLength by vm.ocrLength.collectAsStateWithLifecycle()
                    val ocrHeader by vm.ocrHeader.collectAsStateWithLifecycle()

                    val controller = remember {
                        OcrScannerController(this@MainActivity) { value ->
                            vm.onDetected(value)
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
                            logCount = logCount,
                            targetCount = targetCount,
                            ocrLength = ocrLength,
                            ocrHeader = ocrHeader,
                            versionName = BuildConfig.VERSION_NAME,
                            onScanStart = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            onDownloadCsv = { shareCsv(logRepo) },
                            onClearLog = vm::onClearLog,
                            onSaveSettings = vm::onSaveSettings
                        )
                        ScanPhase.WAITING_FOR_FIRST,
                        ScanPhase.CONFIRMING_FIRST,
                        ScanPhase.WAITING_FOR_SECOND -> ScanScreen(
                            phase = state.phase,
                            ocr1 = state.ocr1,
                            errorMessage = state.errorMessage,
                            onCancel = vm::onCancel,
                            onConfirmFirst = {
                                vm.onConfirmFirst()
                                controller.requestRead()
                            },
                            onRead = { controller.requestRead() },
                            cameraContent = { CameraPreview(controller = controller) }
                        )
                        ScanPhase.RESULT -> ResultScreen(
                            result = state.result ?: ScanResult.NG,
                            ocr1 = state.ocr1,
                            ocr2 = state.ocr2,
                            scannedCount = logCount,
                            targetCount = targetCount,
                            onRetry = vm::onRetry,
                            onCancel = vm::onCancel
                        )
                    }
                }
            }
        }
    }

    private fun shareCsv(logRepo: CsvLogRepository) {
        val file = logRepo.getFile()
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "OCR読取ログ")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "CSVを共有"))
    }
}

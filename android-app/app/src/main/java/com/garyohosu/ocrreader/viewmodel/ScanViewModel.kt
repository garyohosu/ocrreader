package com.garyohosu.ocrreader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garyohosu.ocrreader.data.CsvLogRepository
import com.garyohosu.ocrreader.data.SettingsRepository
import com.garyohosu.ocrreader.domain.ScanLog
import com.garyohosu.ocrreader.domain.ScanPhase
import com.garyohosu.ocrreader.domain.ScanResult
import com.garyohosu.ocrreader.domain.ScanState
import com.garyohosu.ocrreader.domain.SoundEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ERROR_EMPTY = "OCR結果を取得できませんでした。もう一度カメラをかざしてください。"

class ScanViewModel(
    private val logRepo: CsvLogRepository? = null,
    private val settingsRepo: SettingsRepository? = null
) : ViewModel() {

    private val _state = MutableStateFlow(ScanState())
    val state: StateFlow<ScanState> = _state.asStateFlow()

    private val _soundEvent = MutableSharedFlow<SoundEvent>()
    val soundEvent: SharedFlow<SoundEvent> = _soundEvent.asSharedFlow()

    private val _logCount = MutableStateFlow(logRepo?.count() ?: 0)
    val logCount: StateFlow<Int> = _logCount.asStateFlow()

    private val _targetCount = MutableStateFlow(settingsRepo?.targetCount ?: 0)
    val targetCount: StateFlow<Int> = _targetCount.asStateFlow()

    private val _ocrLength = MutableStateFlow(settingsRepo?.ocrLength ?: 0)
    val ocrLength: StateFlow<Int> = _ocrLength.asStateFlow()

    private val _ocrHeader = MutableStateFlow(settingsRepo?.ocrHeader ?: "")
    val ocrHeader: StateFlow<String> = _ocrHeader.asStateFlow()

    fun onScanStart() {
        _state.value = ScanState(phase = ScanPhase.WAITING_FOR_FIRST)
    }

    fun onCameraReady() {
        val phase = _state.value.phase
        if (phase != ScanPhase.WAITING_FOR_FIRST && phase != ScanPhase.WAITING_FOR_SECOND) return
        _state.value = _state.value.copy(cameraReady = true)
    }

    fun onDetected(value: String?) {
        val phase = _state.value.phase
        if (phase != ScanPhase.WAITING_FOR_FIRST && phase != ScanPhase.WAITING_FOR_SECOND) return

        val normalized = normalize(value)
        if (normalized.isNullOrBlank()) {
            _state.value = _state.value.copy(errorMessage = ERROR_EMPTY)
            return
        }

        val validationError = validateOcr(normalized)
        if (validationError != null) {
            _state.value = _state.value.copy(errorMessage = validationError)
            return
        }

        when (phase) {
            ScanPhase.WAITING_FOR_FIRST -> {
                _state.value = _state.value.copy(
                    ocr1 = normalized,
                    phase = ScanPhase.CONFIRMING_FIRST,
                    errorMessage = null
                )
                emitSound(SoundEvent.BEEP)
            }
            ScanPhase.WAITING_FOR_SECOND -> {
                val ocr1 = _state.value.ocr1 ?: ""
                if (normalized != ocr1) {
                    _state.value = _state.value.copy(
                        ocr2 = normalized,
                        result = ScanResult.NG,
                        phase = ScanPhase.RESULT,
                        errorMessage = null
                    )
                    emitSound(SoundEvent.BEEP)
                    emitSound(SoundEvent.NG)
                } else {
                    val isDuplicate = logRepo?.isDuplicate(normalized) == true
                    val result = if (isDuplicate) ScanResult.DUPLICATE else ScanResult.OK
                    _state.value = _state.value.copy(
                        ocr2 = normalized,
                        result = result,
                        phase = ScanPhase.RESULT,
                        errorMessage = null
                    )
                    emitSound(SoundEvent.BEEP)
                    if (!isDuplicate) {
                        emitSound(SoundEvent.OK)
                        saveLog(ocr1, normalized)
                    } else {
                        emitSound(SoundEvent.NG)
                    }
                }
            }
            else -> Unit
        }
    }

    fun onConfirmFirst() {
        if (_state.value.phase != ScanPhase.CONFIRMING_FIRST) return
        _state.value = _state.value.copy(phase = ScanPhase.WAITING_FOR_SECOND, cameraReady = false)
    }

    fun onCancel() {
        _state.value = ScanState()
    }

    fun onRetry() {
        _state.value = ScanState(phase = ScanPhase.WAITING_FOR_FIRST)
    }

    fun onPermissionDenied() {
        _state.value = _state.value.copy(permissionDenied = true, phase = ScanPhase.IDLE, cameraReady = false)
    }

    fun onSaveSettings(targetCount: Int, ocrLength: Int, ocrHeader: String) {
        settingsRepo?.targetCount = targetCount
        settingsRepo?.ocrLength = ocrLength
        settingsRepo?.ocrHeader = ocrHeader
        _targetCount.value = targetCount
        _ocrLength.value = ocrLength
        _ocrHeader.value = ocrHeader
    }

    fun onClearLog() {
        logRepo?.clear()
        _logCount.value = 0
    }

    private fun validateOcr(value: String): String? {
        val length = _ocrLength.value
        if (length > 0 && value.length != length) {
            return "OCR文字数が違います（読み取った文字数 = ${value.length}）"
        }
        val header = _ocrHeader.value
        if (header.isNotEmpty() && !value.startsWith(header)) {
            return "先頭文字列が一致しません"
        }
        return null
    }

    private fun normalize(value: String?): String? {
        return value
            ?.replace('　', ' ')
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun saveLog(ocr1: String, ocr2: String) {
        logRepo ?: return
        val datetime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        logRepo.append(ScanLog(datetime, ocr1, ocr2, "OK"))
        _logCount.value = logRepo.count()
    }

    private fun emitSound(event: SoundEvent) {
        viewModelScope.launch { _soundEvent.emit(event) }
    }
}

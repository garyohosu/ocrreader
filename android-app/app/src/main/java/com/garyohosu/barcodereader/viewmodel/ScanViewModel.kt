package com.garyohosu.barcodereader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garyohosu.barcodereader.data.CsvLogRepository
import com.garyohosu.barcodereader.domain.ScanLog
import com.garyohosu.barcodereader.domain.ScanPhase
import com.garyohosu.barcodereader.domain.ScanResult
import com.garyohosu.barcodereader.domain.ScanState
import com.garyohosu.barcodereader.domain.SoundEvent
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

private const val ERROR_EMPTY = "読み取りに失敗しました。もう一度バーコードをかざしてください。"

class ScanViewModel(
    private val logRepo: CsvLogRepository? = null
) : ViewModel() {

    private val _state = MutableStateFlow(ScanState())
    val state: StateFlow<ScanState> = _state.asStateFlow()

    private val _soundEvent = MutableSharedFlow<SoundEvent>()
    val soundEvent: SharedFlow<SoundEvent> = _soundEvent.asSharedFlow()

    private val _logCount = MutableStateFlow(logRepo?.count() ?: 0)
    val logCount: StateFlow<Int> = _logCount.asStateFlow()

    fun onScanStart() {
        _state.value = ScanState(phase = ScanPhase.WAITING_FOR_FIRST)
    }

    fun onBarcodeDetected(value: String?) {
        val phase = _state.value.phase
        if (phase != ScanPhase.WAITING_FOR_FIRST && phase != ScanPhase.WAITING_FOR_SECOND) return

        if (value.isNullOrBlank()) {
            _state.value = _state.value.copy(errorMessage = ERROR_EMPTY)
            return
        }

        when (phase) {
            ScanPhase.WAITING_FOR_FIRST -> {
                _state.value = _state.value.copy(
                    barcode1 = value,
                    phase = ScanPhase.CONFIRMING_FIRST,
                    errorMessage = null
                )
                emitSound(SoundEvent.BEEP)
            }
            ScanPhase.WAITING_FOR_SECOND -> {
                val result = if (value == _state.value.barcode1) ScanResult.OK else ScanResult.NG
                _state.value = _state.value.copy(
                    barcode2 = value,
                    result = result,
                    phase = ScanPhase.RESULT,
                    errorMessage = null
                )
                emitSound(SoundEvent.BEEP)
                emitSound(if (result == ScanResult.OK) SoundEvent.OK else SoundEvent.NG)
                saveLog(_state.value.barcode1 ?: "", value, result)
            }
            else -> Unit
        }
    }

    fun onConfirmFirst() {
        if (_state.value.phase != ScanPhase.CONFIRMING_FIRST) return
        _state.value = _state.value.copy(phase = ScanPhase.WAITING_FOR_SECOND)
    }

    fun onCancel() {
        _state.value = ScanState()
    }

    fun onRetry() {
        _state.value = ScanState(phase = ScanPhase.WAITING_FOR_FIRST)
    }

    fun onPermissionDenied() {
        _state.value = _state.value.copy(permissionDenied = true, phase = ScanPhase.IDLE)
    }

    fun onClearLog() {
        logRepo?.clear()
        _logCount.value = 0
    }

    private fun saveLog(barcode1: String, barcode2: String, result: ScanResult) {
        logRepo ?: return
        val datetime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        logRepo.append(ScanLog(datetime, barcode1, barcode2, if (result == ScanResult.OK) "OK" else "NG"))
        _logCount.value = logRepo.count()
    }

    private fun emitSound(event: SoundEvent) {
        viewModelScope.launch { _soundEvent.emit(event) }
    }
}

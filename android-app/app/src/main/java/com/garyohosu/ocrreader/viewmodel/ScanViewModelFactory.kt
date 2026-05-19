package com.garyohosu.ocrreader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.garyohosu.ocrreader.data.CsvLogRepository
import com.garyohosu.ocrreader.data.SettingsRepository

class ScanViewModelFactory(
    private val logRepo: CsvLogRepository,
    private val settingsRepo: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ScanViewModel(logRepo, settingsRepo) as T
    }
}

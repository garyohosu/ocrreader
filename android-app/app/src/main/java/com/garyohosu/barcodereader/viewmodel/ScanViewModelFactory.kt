package com.garyohosu.barcodereader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.garyohosu.barcodereader.data.CsvLogRepository

class ScanViewModelFactory(
    private val logRepo: CsvLogRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ScanViewModel(logRepo) as T
    }
}

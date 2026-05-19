package com.garyohosu.ocrreader.data

import android.content.Context

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("ocr_settings", Context.MODE_PRIVATE)

    var targetCount: Int
        get() = prefs.getInt("target_count", 0)
        set(value) { prefs.edit().putInt("target_count", value).apply() }

    var ocrLength: Int
        get() = prefs.getInt("ocr_length", 0)
        set(value) { prefs.edit().putInt("ocr_length", value).apply() }

    var ocrHeader: String
        get() = prefs.getString("ocr_header", "") ?: ""
        set(value) { prefs.edit().putString("ocr_header", value).apply() }
}

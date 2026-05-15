package com.garyohosu.barcodereader.data

import android.content.Context

class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("barcode_settings", Context.MODE_PRIVATE)

    var targetCount: Int
        get() = prefs.getInt("target_count", 0)
        set(value) { prefs.edit().putInt("target_count", value).apply() }
}

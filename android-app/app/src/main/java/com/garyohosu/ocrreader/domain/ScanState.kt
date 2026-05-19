package com.garyohosu.ocrreader.domain

data class ScanState(
    val phase: ScanPhase = ScanPhase.IDLE,
    val ocr1: String? = null,
    val ocr2: String? = null,
    val result: ScanResult? = null,
    val errorMessage: String? = null,
    val permissionDenied: Boolean = false,
    val cameraReady: Boolean = false
)

package com.garyohosu.ocrreader.domain

data class ScanLog(
    val datetime: String,
    val ocr1: String,
    val ocr2: String,
    val result: String,
)

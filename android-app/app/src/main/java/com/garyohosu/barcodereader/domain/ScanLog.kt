package com.garyohosu.barcodereader.domain

data class ScanLog(
    val datetime: String,
    val barcode1: String,
    val barcode2: String,
    val result: String
)

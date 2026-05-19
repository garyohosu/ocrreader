package com.garyohosu.ocrreader.domain

enum class ScanPhase {
    IDLE,
    WAITING_FOR_FIRST,
    CONFIRMING_FIRST,
    WAITING_FOR_SECOND,
    RESULT
}

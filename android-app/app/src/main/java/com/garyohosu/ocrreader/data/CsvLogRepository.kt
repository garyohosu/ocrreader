package com.garyohosu.ocrreader.data

import android.content.Context
import com.garyohosu.ocrreader.domain.ScanLog
import java.io.File

class CsvLogRepository(private val context: Context) {
    private val csvFile = File(context.filesDir, "ocr_logs.csv")
    private val ocrSetFile = File(context.filesDir, "logged_ocrs.txt")
    private val loggedOcrs: MutableSet<String> = loadOcrs()

    fun append(log: ScanLog) {
        if (!csvFile.exists()) {
            csvFile.writeText("datetime,ocr1,ocr2,result\n")
        }
        csvFile.appendText("${log.datetime},${csv(log.ocr1)},${csv(log.ocr2)},${csv(log.result)}\n")
        ocrSetFile.appendText("${log.ocr1}\n")
        loggedOcrs.add(log.ocr1)
    }

    fun isDuplicate(ocr: String): Boolean = ocr in loggedOcrs

    fun count(): Int = loggedOcrs.size

    fun clear() {
        if (csvFile.exists()) csvFile.delete()
        if (ocrSetFile.exists()) ocrSetFile.delete()
        loggedOcrs.clear()
    }

    fun getFile(): File = csvFile

    private fun csv(value: String): String = "\"${value.replace("\"", "\"\"")}\""

    private fun loadOcrs(): MutableSet<String> {
        if (!ocrSetFile.exists()) return mutableSetOf()
        return ocrSetFile.readLines().filter { it.isNotBlank() }.toMutableSet()
    }
}

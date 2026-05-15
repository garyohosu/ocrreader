package com.garyohosu.barcodereader.data

import android.content.Context
import com.garyohosu.barcodereader.domain.ScanLog
import java.io.File

class CsvLogRepository(context: Context) {

    private val file = File(context.filesDir, "scanlogs.csv")
    private val barcodeSetFile = File(context.filesDir, "logged_barcodes.txt")

    private val loggedBarcodes: MutableSet<String> = loadBarcodes()

    fun append(log: ScanLog) {
        if (!file.exists()) {
            file.writeText("日時,1本目,2本目,結果\n")
        }
        file.appendText(
            "${csv(log.datetime)},${csv(log.barcode1)},${csv(log.barcode2)},${csv(log.result)}\n"
        )
        barcodeSetFile.appendText("${log.barcode1}\n")
        loggedBarcodes.add(log.barcode1)
    }

    fun isDuplicate(barcode: String): Boolean = barcode in loggedBarcodes

    fun getFile(): File = file

    fun count(): Int {
        if (!file.exists()) return 0
        return maxOf(0, file.readLines().size - 1)
    }

    fun clear() {
        file.delete()
        barcodeSetFile.delete()
        loggedBarcodes.clear()
    }

    private fun loadBarcodes(): MutableSet<String> {
        if (!barcodeSetFile.exists()) return mutableSetOf()
        return barcodeSetFile.readLines().filter { it.isNotBlank() }.toMutableSet()
    }

    private fun csv(value: String) = "\"${value.replace("\"", "\"\"")}\""
}

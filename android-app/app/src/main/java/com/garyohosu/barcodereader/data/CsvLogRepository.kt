package com.garyohosu.barcodereader.data

import android.content.Context
import com.garyohosu.barcodereader.domain.ScanLog
import java.io.File

class CsvLogRepository(context: Context) {

    private val file = File(context.filesDir, "scanlogs.csv")

    fun append(log: ScanLog) {
        if (!file.exists()) {
            file.writeText("日時,1本目,2本目,結果\n")
        }
        file.appendText(
            "${csv(log.datetime)},${csv(log.barcode1)},${csv(log.barcode2)},${csv(log.result)}\n"
        )
    }

    fun getFile(): File = file

    fun count(): Int {
        if (!file.exists()) return 0
        return maxOf(0, file.readLines().size - 1)
    }

    fun clear() {
        file.delete()
    }

    private fun csv(value: String) = "\"${value.replace("\"", "\"\"")}\""
}

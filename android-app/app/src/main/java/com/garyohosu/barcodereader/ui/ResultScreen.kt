package com.garyohosu.barcodereader.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garyohosu.barcodereader.domain.ScanResult

private val OK_COLOR = Color(0xFF1565C0)
private val NG_COLOR = Color(0xFFB71C1C)
private val DUPLICATE_COLOR = Color(0xFFE65100)

@Composable
fun ResultScreen(
    result: ScanResult,
    barcode1: String?,
    barcode2: String?,
    scannedCount: Int,
    targetCount: Int,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    val isComplete = targetCount > 0 && scannedCount >= targetCount

    BackHandler(onBack = onCancel)

    val bgColor = when (result) {
        ScanResult.OK -> OK_COLOR
        ScanResult.NG -> NG_COLOR
        ScanResult.DUPLICATE -> DUPLICATE_COLOR
    }
    val resultText = when (result) {
        ScanResult.OK -> "OK"
        ScanResult.NG -> "NG"
        ScanResult.DUPLICATE -> "重複"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = resultText,
                fontSize = 96.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "1本目: ${barcode1 ?: "—"}",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "2本目: ${barcode2 ?: "—"}",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {
                result == ScanResult.OK && isComplete ->
                    Text(
                        text = "読み込み終わりました。\n新しい読み込み数を設定してください",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                result == ScanResult.OK && targetCount > 0 ->
                    Text(
                        text = "$scannedCount / $targetCount 件完了",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                result == ScanResult.DUPLICATE ->
                    Text(
                        text = "既に読み込み済みです",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                else -> {}
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = if (result == ScanResult.OK && isComplete) onCancel else onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    text = if (result == ScanResult.OK && isComplete) "スタート画面へ" else "もう一度",
                    color = bgColor
                )
            }
        }
    }
}

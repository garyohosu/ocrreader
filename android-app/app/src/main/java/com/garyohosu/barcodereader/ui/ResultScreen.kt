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

@Composable
fun ResultScreen(
    result: ScanResult,
    barcode1: String?,
    barcode2: String?,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    // システムバックはスタート画面へ戻す（「もう一度」とは別動作）
    BackHandler(onBack = onCancel)

    val bgColor = if (result == ScanResult.OK) OK_COLOR else NG_COLOR
    val resultText = if (result == ScanResult.OK) "OK" else "NG"

    // 背景色をシステムバー裏まで全面に敷き、コンテンツだけ safe area に収める
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

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    text = "もう一度",
                    color = bgColor
                )
            }
        }
    }
}

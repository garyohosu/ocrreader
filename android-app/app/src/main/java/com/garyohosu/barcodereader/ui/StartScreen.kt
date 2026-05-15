package com.garyohosu.barcodereader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun StartScreen(
    permissionDenied: Boolean,
    logCount: Int,
    onScanStart: () -> Unit,
    onDownloadCsv: () -> Unit,
    onClearLog: () -> Unit
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
            text = "バーコード照合",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        if (permissionDenied) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "カメラの使用が許可されていません。\n設定アプリからカメラ権限を有効にしてください。",
                color = Color(0xFFB00020),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onScanStart,
            enabled = !permissionDenied
        ) {
            Text(text = "スタート")
        }

        Spacer(modifier = Modifier.height(48.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ログ",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (logCount == 0) "ログなし" else "${logCount}件",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDownloadCsv,
                enabled = logCount > 0
            ) {
                Text(text = "CSVをダウンロード")
            }
            OutlinedButton(
                onClick = onClearLog,
                enabled = logCount > 0,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFB00020)
                )
            ) {
                Text(text = "ログをクリア")
            }
        }
    }
}

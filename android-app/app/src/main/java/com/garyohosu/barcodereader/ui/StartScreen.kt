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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun StartScreen(
    permissionDenied: Boolean,
    logCount: Int,
    targetCount: Int,
    onScanStart: () -> Unit,
    onDownloadCsv: () -> Unit,
    onClearLog: () -> Unit,
    onSetTargetCount: (Int) -> Unit
) {
    val isComplete = targetCount > 0 && logCount >= targetCount
    val startEnabled = !permissionDenied && !isComplete

    var showTargetDialog by remember { mutableStateOf(false) }
    var targetInput by remember { mutableStateOf("") }

    if (showTargetDialog) {
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            title = { Text("読み込み数を設定") },
            text = {
                TextField(
                    value = targetInput,
                    onValueChange = { targetInput = it.filter { c -> c.isDigit() } },
                    label = { Text("目標件数（0で無制限）") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    onSetTargetCount(targetInput.toIntOrNull() ?: 0)
                    showTargetDialog = false
                }) { Text("設定") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showTargetDialog = false }) { Text("キャンセル") }
            }
        )
    }

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

        Spacer(modifier = Modifier.height(24.dp))

        if (targetCount > 0) {
            val progressText = if (isComplete) {
                "読み込み終わりました。\n新しい読み込み数を設定してください"
            } else {
                "$logCount / $targetCount 件"
            }
            Text(
                text = progressText,
                style = MaterialTheme.typography.titleMedium,
                color = if (isComplete) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onScanStart,
            enabled = startEnabled
        ) {
            Text(text = "スタート")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = {
            targetInput = if (targetCount > 0) targetCount.toString() else ""
            showTargetDialog = true
        }) {
            Text(text = if (targetCount > 0) "読み込み数: $targetCount 件（変更）" else "読み込み数を設定")
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
            text = if (logCount == 0) "ログなし" else "${logCount}件（OKのみ）",
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

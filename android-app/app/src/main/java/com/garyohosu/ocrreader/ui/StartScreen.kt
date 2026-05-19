package com.garyohosu.ocrreader.ui

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
    ocrLength: Int,
    ocrHeader: String,
    versionName: String,
    onScanStart: () -> Unit,
    onDownloadCsv: () -> Unit,
    onClearLog: () -> Unit,
    onSaveSettings: (targetCount: Int, ocrLength: Int, ocrHeader: String) -> Unit
) {
    val isComplete = targetCount > 0 && logCount >= targetCount
    val startEnabled = !permissionDenied && targetCount > 0 && !isComplete

    var showSettingsDialog by remember { mutableStateOf(false) }
    var targetInput by remember { mutableStateOf("") }
    var lengthInput by remember { mutableStateOf("") }
    var headerInput by remember { mutableStateOf("") }

    var showClearConfirmDialog by remember { mutableStateOf(false) }

    // 確認ダイアログ
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("ログをクリア") },
            text = { Text("ログをクリアします。\n重複チェックもリセットされます。よろしいですか？") },
            confirmButton = {
                Button(
                    onClick = { onClearLog(); showClearConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020))
                ) { Text("クリア") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirmDialog = false }) { Text("キャンセル") }
            }
        )
    }

    // 設定ダイアログ
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("設定") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = targetInput,
                        onValueChange = { targetInput = it.filter { c -> c.isDigit() } },
                        label = { Text("読み取り回数（1以上）") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = lengthInput,
                        onValueChange = { lengthInput = it.filter { c -> c.isDigit() } },
                        label = { Text("OCR長（0で任意）") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = headerInput,
                        onValueChange = { headerInput = it },
                        label = { Text("ヘッダー（空欄でチェックなし）") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val t = targetInput.toIntOrNull() ?: 0
                    val l = lengthInput.toIntOrNull() ?: 0
                    onSaveSettings(t, l, headerInput.trim())
                    showSettingsDialog = false
                }) { Text("保存") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSettingsDialog = false }) { Text("キャンセル") }
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
            text = "OCR読取",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        if (permissionDenied) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "カメラの使用が許可されていません。\n設定アプリからカメラ権限を有効にしてください。",
                color = Color(0xFFB00020),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 進捗 / 未設定メッセージ
        when {
            targetCount == 0 ->
                Text(
                    text = "読み取り回数を設定してください",
                    color = Color(0xFFB00020),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            isComplete ->
                Text(
                    text = "読み込み終わりました。\n新しい読み取り回数を設定してください",
                    color = Color(0xFF2E7D32),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            else ->
                Text(
                    text = "$logCount / $targetCount 件",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onScanStart,
            enabled = startEnabled
        ) {
            Text(text = "スタート")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = {
            targetInput = if (targetCount > 0) targetCount.toString() else ""
            lengthInput = if (ocrLength > 0) ocrLength.toString() else ""
            headerInput = ocrHeader
            showSettingsDialog = true
        }) {
            Text(text = "設定")
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

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onDownloadCsv,
                enabled = logCount > 0
            ) {
                Text(text = "CSVをダウンロード")
            }
            OutlinedButton(
                onClick = { showClearConfirmDialog = true },
                enabled = logCount > 0,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB00020))
            ) {
                Text(text = "ログをクリア")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "v$versionName",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

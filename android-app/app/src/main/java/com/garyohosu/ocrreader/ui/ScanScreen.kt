package com.garyohosu.ocrreader.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.garyohosu.ocrreader.domain.ScanPhase

@Composable
fun ScanScreen(
    phase: ScanPhase,
    ocr1: String?,
    errorMessage: String?,
    cameraReady: Boolean,
    onCancel: () -> Unit,
    onConfirmFirst: () -> Unit,
    onRead: () -> Unit,
    cameraContent: @Composable () -> Unit = {}
) {
    BackHandler(onBack = onCancel)

    val phaseMessage = when (phase) {
        ScanPhase.WAITING_FOR_FIRST -> "カメラが安定したら『読む』を押してください"
        ScanPhase.CONFIRMING_FIRST -> "1本目を確認してください"
        ScanPhase.WAITING_FOR_SECOND -> "2本目のOCRをかざしてください"
        else -> ""
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = phaseMessage,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color(0xFFB00020),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                cameraContent()
            }
        }

        if (phase == ScanPhase.CONFIRMING_FIRST) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = ocr1 ?: "",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onConfirmFirst) {
                Text(text = "次へ")
            }
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            if (!cameraReady) {
                Text(
                    text = "カメラを準備しています…",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(
                onClick = onRead
            ) {
                Text(text = "読む")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(
            text = "文字にピントを合わせてから『読む』を押してください",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF666666)),
            modifier = Modifier
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            Text(text = "中止")
        }
    }
}

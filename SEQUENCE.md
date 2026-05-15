# SEQUENCE.md — バーコード照合Androidアプリ シーケンス図

## 登場人物

| 略称 | 名称 | 役割 |
|------|------|------|
| User | 作業者 | アプリを操作する現場担当者 |
| UI | UI (Compose) | 画面描画・ユーザー操作受付 |
| VM | ScanViewModel | 状態管理・照合ロジック |
| Camera | CameraX | カメラプレビュー・フレーム供給 |
| BA | BarcodeAnalyzer (ML Kit) | バーコード検出 |
| Sound | FeedbackSoundPlayer | 音声フィードバック |
| Log | CsvLogRepository | CSV保存・重複チェック |
| OS | Android OS | 権限管理 |

---

## シーケンス 1: 正常フロー（OK・重複なし）

```mermaid
sequenceDiagram
    actor User as 作業者
    participant UI as UI (Compose)
    participant VM as ScanViewModel
    participant Camera as CameraX
    participant BA as BarcodeAnalyzer
    participant Sound as FeedbackSoundPlayer
    participant Log as CsvLogRepository

    User->>UI: アプリ起動
    UI->>VM: 初期化 (phase=IDLE)
    UI-->>User: スタート画面を表示（進捗 x/N 件・バージョン表示）

    User->>UI: スタートボタン押下
    UI->>Camera: カメラ起動
    Camera-->>UI: プレビュー開始
    UI->>VM: onScanStart()
    VM->>VM: phase = WAITING_FOR_FIRST
    UI-->>User: 読み取り画面「1本目のバーコードをかざしてください」

    loop 1つ目検出待ち
        Camera->>BA: フレーム供給
        BA-->>VM: onBarcodeDetected(value)
    end

    VM->>VM: barcode1 = value\nphase = CONFIRMING_FIRST
    VM->>Sound: playBeep()
    Sound-->>User: ピッ
    VM-->>UI: state 更新
    UI-->>User: 「1本目を確認してください」\nbarcode1 の値と「次へ」ボタンを表示

    User->>UI: 「次へ」ボタン押下
    UI->>VM: onConfirmFirst()
    VM->>VM: phase = WAITING_FOR_SECOND
    VM-->>UI: state 更新
    UI-->>User: 「2本目のバーコードをかざしてください」

    loop 2つ目検出待ち
        Camera->>BA: フレーム供給
        BA-->>VM: onBarcodeDetected(value)
    end

    VM->>VM: barcode1 == barcode2
    VM->>Log: isDuplicate(barcode1)
    Log-->>VM: false（未登録）
    VM->>VM: result = OK\nphase = RESULT
    VM->>Sound: playBeep()
    VM->>Sound: playOk()
    Sound-->>User: ピッ → OK音
    VM->>Log: append(datetime, barcode1, barcode2, "OK")
    VM->>VM: logCount++
    VM-->>UI: state 更新
    UI-->>User: 判定画面（青・「OK」・x/N件完了・「次のバーコードを読む」）
```

---

## シーケンス 2: 正常フロー（NG判定）

```mermaid
sequenceDiagram
    actor User as 作業者
    participant UI as UI (Compose)
    participant VM as ScanViewModel
    participant Sound as FeedbackSoundPlayer

    Note over User,Sound: 1つ目読み取り・確認（次へ）まではシーケンス1と同じ

    VM->>VM: barcode1 != barcode2
    VM->>VM: result = NG\nphase = RESULT
    VM->>Sound: playBeep()
    VM->>Sound: playNg()
    Sound-->>User: ピッ → NG音
    Note over VM: NGは保存しない
    VM-->>UI: state 更新（result=NG）
    UI-->>User: 判定画面（赤・「NG」・「もう一度」）
```

---

## シーケンス 3: 重複検出フロー

```mermaid
sequenceDiagram
    actor User as 作業者
    participant UI as UI (Compose)
    participant VM as ScanViewModel
    participant Sound as FeedbackSoundPlayer
    participant Log as CsvLogRepository

    Note over User,Log: 1つ目読み取り・確認・2つ目読み取りまではシーケンス1と同じ

    VM->>VM: barcode1 == barcode2
    VM->>Log: isDuplicate(barcode1)
    Log-->>VM: true（既に登録済み）
    VM->>VM: result = DUPLICATE\nphase = RESULT
    VM->>Sound: playBeep()
    VM->>Sound: playNg()
    Sound-->>User: ピッ → NG音
    Note over VM: 重複は保存しない・件数を増やさない
    VM-->>UI: state 更新（result=DUPLICATE）
    UI-->>User: 判定画面（橙・「重複」・「既に読み込み済みです」・「もう一度」）
```

---

## シーケンス 4: カメラ権限フロー

```mermaid
sequenceDiagram
    actor User as 作業者
    participant UI as UI (Compose)
    participant VM as ScanViewModel
    participant OS as Android OS
    participant Camera as CameraX

    User->>UI: スタートボタン押下
    UI->>OS: カメラ権限を確認

    alt 権限あり（PERMISSION_GRANTED）
        OS-->>UI: 許可済み
        UI->>Camera: カメラ起動
        Camera-->>UI: プレビュー開始
        UI-->>User: 読み取り画面へ遷移

    else 権限未決定（初回）
        OS-->>UI: 未決定
        UI->>OS: 権限ダイアログ要求
        OS-->>User: 権限ダイアログ表示

        alt 作業者が「許可」
            User->>OS: 許可
            OS-->>UI: PERMISSION_GRANTED
            UI->>Camera: カメラ起動
            Camera-->>UI: プレビュー開始
            UI-->>User: 読み取り画面へ遷移

        else 作業者が「拒否」
            User->>OS: 拒否
            OS-->>UI: PERMISSION_DENIED
            UI->>VM: onPermissionDenied()
            VM-->>UI: permissionDenied=true
            UI-->>User: スタート画面にエラーメッセージ表示
        end
    end
```

---

## シーケンス 5: 空文字・null 読み取り時の対応

```mermaid
sequenceDiagram
    actor User as 作業者
    participant UI as UI (Compose)
    participant VM as ScanViewModel
    participant Camera as CameraX
    participant BA as BarcodeAnalyzer

    Note over User,BA: 読み取り画面（1本目 or 2本目 待ち）

    Camera->>BA: フレーム供給
    BA-->>VM: onBarcodeDetected("") または null

    VM->>VM: 空文字/null を破棄\nphase 変更なし\nerrorMessage を設定
    VM-->>UI: state 更新
    UI-->>User: 「読み取りに失敗しました。もう一度バーコードをかざしてください。」

    Camera->>BA: フレーム供給
    BA-->>VM: onBarcodeDetected("123456")

    VM->>VM: errorMessage = null（クリア）\nバーコードを保存・次フェーズへ
    VM-->>UI: state 更新
    UI-->>User: エラーメッセージ消去・次フェーズへ進む
```

---

## シーケンス 6: 読み取り中止フロー

```mermaid
sequenceDiagram
    actor User as 作業者
    participant UI as UI (Compose)
    participant VM as ScanViewModel
    participant Camera as CameraX

    Note over User,Camera: 読み取り画面（1本目 or 2本目 待ち、または確認画面）

    alt 「中止」ボタン押下
        User->>UI: 中止ボタン押下
    else システムバック
        User->>UI: システムバック操作
    end

    UI->>VM: onCancel()
    VM->>VM: 全フィールドクリア\nphase = IDLE
    VM-->>UI: state 更新
    UI->>Camera: カメラ停止
    UI-->>User: スタート画面へ遷移
```

---

## シーケンス 7: 1本目確認フロー（CONFIRMING_FIRST）

```mermaid
sequenceDiagram
    actor User as 作業者
    participant UI as UI (Compose)
    participant VM as ScanViewModel
    participant Camera as CameraX
    participant BA as BarcodeAnalyzer

    Camera->>BA: フレーム供給
    BA-->>VM: onBarcodeDetected("123456")
    VM->>VM: barcode1 = "123456"\nphase = CONFIRMING_FIRST
    VM-->>UI: state 更新

    Note over UI,User: カメラ映像 + barcode1 の値 + 「次へ」ボタンを表示

    Note over Camera,BA: カメラフレームは引き続き供給されるが\nCONFIRMING_FIRST 中は VM が無視する

    User->>UI: 「次へ」ボタン押下
    UI->>VM: onConfirmFirst()
    VM->>VM: phase = WAITING_FOR_SECOND
    VM-->>UI: state 更新
    UI-->>User: 「2本目のバーコードをかざしてください」

    Camera->>BA: フレーム供給（2つ目のバーコード）
    BA-->>VM: onBarcodeDetected("789012")
    VM->>VM: barcode2 = "789012"\n照合へ進む
```

---

## シーケンス 8: 読み込み数完了フロー

```mermaid
sequenceDiagram
    actor User as 作業者
    participant UI as UI (Compose)
    participant VM as ScanViewModel
    participant Log as CsvLogRepository

    Note over User,Log: logCount = N-1、targetCount = N の状態で最後の1件を読む

    VM->>Log: append(...)
    VM->>VM: logCount = N（= targetCount）
    VM-->>UI: state 更新

    UI-->>User: 判定画面（青・「OK」\n「読み込み終わりました。新しい読み込み数を設定してください」\n「スタート画面へ」ボタン）

    User->>UI: 「スタート画面へ」押下
    UI->>VM: onCancel()
    VM->>VM: phase = IDLE
    VM-->>UI: state 更新
    UI-->>User: スタート画面（スタートボタン無効・完了メッセージ表示）
```

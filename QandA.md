# QandA.md — OCR読取Androidアプリ 実証実験

> 分類: **即修正** / **要判断** / **保留**  
> 重大度: 高 / 中 / 低  
> ステータス: ✅ 解決済 / ⏳ 保留

---

## 解決済み

### Q1. クールダウン時間が spec と plan で食い違っている【重大度: 高】✅

**回答（更新）:** クールダウン方式を廃止。「読む」ボタン押下で OCR を1回だけ実行する方式に変更。  
OCR は連続ループで動かさず、ボタン押下ごとに1フレームのみ解析して停止する。  
クールダウン設定は不要。

---

### Q2. パッケージ名が未決定【重大度: 高】✅

**回答:** `com.garyohosu.ocrreader` で確定。

---

### Q3. 最小 Android バージョン (minSdk) が未指定【重大度: 高】✅

**回答:** `minSdk = 23`（Android 6.0 以降）。  
targetSdk / compileSdk は Android Studio 標準テンプレートに合わせる。

---

### Q4. OK 表示の色が「青」——グリーンではない理由【重大度: 中】✅

**回答:** 青色のまま確定。仕様書・受け入れ条件の記載を優先する。  
現場確認で「緑の方が直感的」という意見が出た場合は次フェーズで変更可。

---

### Q5. 空文字・null 読み取り時の UI が未定義【重大度: 中】✅

**回答:** 画面内メッセージで表示する（トーストは使わない）。

- 現在の読み取りフェーズは維持する
- ocr1 / ocr2 には保存しない
- 音は鳴らさない
- 画面内に以下を表示する

```
読み取りに失敗しました。もう一度OCRをかざしてください。
```

---

### Q6. 「戻る」ボタンの実装有無【重大度: 中】✅

**回答:** 今回実装する。

- 読み取り画面に「中止」ボタンを配置する
- 押下時: 読み取り途中の値をクリア → カメラ終了 → スタート画面へ遷移

---

### Q7. 画面の向き（縦横）固定するか【重大度: 中】✅

**回答:** ポートレート固定とする。横向き対応は行わない。

---

### Q8. JANコード・DataMatrix 対応の扱い【重大度: 低】✅

**回答:** 実証実験版では対応しない。初期対応は以下の3形式で確定。

- QR_CODE
- CODE_39
- CODE_128

JANコード・DataMatrix は次フェーズで検討。

---

### Q9. ViewModel への ToneGenerator の渡し方【重大度: 低】✅

**回答:** ViewModel に ToneGenerator を直接持たせない。以下の構成で分離する。

- `FeedbackSoundPlayer` クラスを作成し、ToneGenerator はその内部で管理する
- ViewModel は「読み取り成功」「OK」「NG」のイベントを発火するのみ
- UI側または画面制御側で `FeedbackSoundPlayer` を呼び出す

---

### Q10. UIテストの要否【重大度: 低】✅

**回答:** 今回は必須にしない。ViewModel Unit テストを優先する。

今回必須のテストケース:

- 1回目読み取り後に2回目待ちへ進む
- 2回目読み取り後にOK/NG判定される
- クールダウン中の読み取りを無視する
- reset で初期状態に戻る
- 空文字/null を保存しない

---

### Q11. 長時間使用時のカメラ発熱・バッテリー対策【重大度: 低】✅

**回答:** 今回は実装しない。README の既知の制限事項に以下を記載する。

```
カメラを連続使用するため、長時間使用時は端末の発熱やバッテリー消費が増える可能性があります。
```

次フェーズで問題が出た場合は、一定時間操作なしでカメラを停止する省電力モードを検討する。

---

### Q12. Android のシステムバック操作の扱い【重大度: 中】✅

**回答:** システムバックも実装対象にする。画面上の戻る系操作と挙動を統一し、Activity は終了させない。

- 読み取り画面でシステムバックを押した場合:
  読み取り途中の値をクリアし、カメラ画面を終了してスタート画面へ戻る
- 判定画面でシステムバックを押した場合:
  `ocr1` / `ocr2` / 判定結果をクリアし、スタート画面へ戻る
- 実装は Jetpack Compose の `BackHandler` を使う

理由: 実証実験中に誤って戻る操作をしても、アプリが終了しない方が安全なため。

---

### Q13. 読み取り失敗メッセージのクリア条件【重大度: 中】✅

**回答:** 次に有効なOCRを読み取れた時点で即時クリアする。自動タイマーでは消さない。

- 空文字 / null 読み取り時に画面内メッセージを表示する
- 一定時間で自動的には消さない
- 次に有効なOCRを読み取った時点でクリアする
- 「中止」「もう一度」「スタート画面へ戻る」でもクリアする
- フェーズ切替時にもクリアする

理由: 自動で消すと作業者が見逃す可能性がある一方、成功後に残ると混乱するため。

---

### Q14. カメラ権限を拒否された後の再試行導線【重大度: 中】✅

**回答:** スタート画面で権限エラーを表示し、再度「スタート」を押したら権限要求を再実行する。

- カメラ権限が未許可の場合、スタート押下時に権限要求を出す
- 拒否された場合、スタート画面へ戻り、画面内にエラーメッセージを表示する
- 通常拒否の場合、再度スタートを押すと再び権限要求を出す
- 永久拒否、または権限ダイアログを出せない状態の場合、設定画面で許可する案内を表示する
- 実証実験版では、設定画面を開くボタンは必須にしない
- エラーメッセージには以下を表示する

```
カメラ権限が許可されていません。
再度スタートを押して権限を許可してください。
許可画面が出ない場合は、Androidの設定からカメラ権限を許可してください。
```

---

### F1. spec.md §4 と §20 の矛盾【重大度: 低】✅

**回答:** spec.md §4 の末尾を修正する。

変更前:
```
ただし、実装しやすい構成があれば、上記にこだわらず変更してよい。
```

変更後:
```
ただし、本実証実験版では §20「実装方針の追加指定」を優先し、
Kotlin / Jetpack Compose / CameraX / ML Kit Ocr Scanning / Gradle Kotlin DSL で実装する。
```

→ **spec.md を修正済み**

---

## シーケンス設計から生じた不明点（解決済み）

### Q15. 「二度と表示しない」選択後の権限エラー画面の挙動【重大度: 中】✅

**回答:** スタート画面に権限エラーメッセージを表示し、可能であれば「設定を開く」ボタンを表示する。

- 権限未許可時はスタート押下で権限要求を出す
- 通常拒否時はスタート画面へ戻り、エラーメッセージを表示する
- 再度スタートを押すと権限要求を再試行する
- 権限ダイアログを再表示できない場合（「二度と表示しない」相当）は設定画面での許可を案内する
- 可能であれば `Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)` でアプリ設定画面を開くボタンを用意する
- 最小実装では案内文のみでも可

表示メッセージ:

```
カメラ権限が許可されていません。
再度スタートを押して権限を許可してください。
許可画面が出ない場合は、Androidの設定からこのアプリのカメラ権限を許可してください。
```

---

### Q16. 「もう一度」後のカメラ挙動【重大度: 中】✅

**回答:** 判定画面ではカメラを停止する。「もう一度」ボタン押下でカメラを再起動して1つ目読み取り画面へ戻る。

- 2つ目読み取り完了後、判定画面へ遷移するタイミングで CameraX の解析を停止する
- 可能ならカメラプレビューも停止する
- 「もう一度」押下時に状態を初期化し（`phase = WAITING_FOR_FIRST`）、CameraX を再起動する

理由: 判定中の意図しないスキャン混入を防ぐことを誤スキャン防止として優先する。  
再起動待ち時間がわずかに生じるが、実証実験版では許容する。

---

### Q17. OcrAnalyzer のスレッドと ViewModel への通知方法【重大度: 低】✅

**回答:** 解析は background thread、ViewModel の状態更新は Main thread へ切り替える。実装詳細として扱う。

推奨方針:

- `ImageAnalysis.setAnalyzer` はカメラ解析用 Executor（background thread）で実行する
- OCR文字列確定後、ViewModel への通知は `viewModelScope.launch { ... }` または `withContext(Dispatchers.Main)` で Main thread に切り替える
- ViewModel の `StateFlow` 更新は ViewModel 内に閉じ込める
- Analyzer の background thread から直接 UI 状態を更新しない

---

## クラス設計から生じた不明点（解決済み）

### Q18. SoundEvent の伝搬方法【重大度: 中】✅

**回答:** A案で確定。`ScanViewModel` が `SharedFlow<SoundEvent>` を持ち、`MainActivity` が `collect` して `FeedbackSoundPlayer` を呼ぶ。

- 音は画面状態ではなく一度だけ発火するイベントであり、`ScanState` に入れると回転・再描画時に再生される恐れがある
- `SharedFlow` は「一回限りのイベント」として扱いやすく、ViewModel を Android 依存から切り離す方針とも一致する
- `SoundEvent.BEEP` → `playBeep()` / `SoundEvent.OK` → `playOk()` / `SoundEvent.NG` → `playNg()`

---

### Q19. OcrScannerController の要否【重大度: 中】✅

**回答:** A案で確定。`OcrScannerController` を独立クラスとして実装し、CameraX の起動・停止・Analyzer バインドを集約する。

- Composable に CameraX 処理を書くと重くなる
- 判定画面でのカメラ停止・「もう一度」での再起動という仕様と相性が良い
- `startCamera()` で Preview / ImageAnalysis を bind、`stopCamera()` で unbind する
- `CameraPreview` は表示専用 Composable に徹する

---

### Q20. FeedbackSoundPlayer のライフサイクル管理【重大度: 低】✅

**回答:** A案で確定。`MainActivity.onCreate()` で生成し、`onDestroy()` で `release()` する。

- 音は読み取り画面・判定画面どちらでも使うため、`ScanScreen` の `DisposableEffect` で管理すると画面遷移のたびに生成・破棄されて複雑になる
- `MainActivity` で `soundEvent.collect` する設計と一致し、シンプルに保てる

---

### Q21. OcrScannerController の生成・保持場所【重大度: 中】✅

**回答:** `OcrScannerController` は `MainActivity` が 1 インスタンスを生成・保持し、`ScanScreen` / `CameraPreview` に渡す。`ScanScreen` 内の `remember` では生成しない。

- recomposition のたびに CameraX 管理オブジェクトが揺れるのを避ける
- 判定画面で `stopCamera()` し、「もう一度」で同一インスタンスを使って `startCamera()` できる
- `MainActivity` が `FeedbackSoundPlayer` と同じく、アプリ実行中の外部リソース管理役になる
- `ScanScreen` / `CameraPreview` は表示と操作受付に集中できる
- Q19 の「Controller を独立クラスにして CameraX の起動・停止・Analyzer バインドを集約する」方針と一致する

---

## テストケース設計から生じた不明点（解決済み）

### Q22. クールダウンのテスト方法【重大度: 中】✅

**回答（更新）:** クールダウン方式を廃止したため、この問い自体が不要になった。  
OCR は「読む」ボタン押下ごとに1回実行して完了するため、`delay()` に依存したテストは不要。  
`MainDispatcherRule` + `runCurrent()` だけで全フェーズ遷移をテスト可能。

---

### Q23. OcrAnalyzer の形式フィルタリングのテスト方法【重大度: 低】✅

**回答:** `OcrAnalyzer.isValid()` の単体テストは採用しない。TC-BA は削除。

- null / blank の判定は `ScanViewModel.onOcrDetected(value: String?)` 側で行うと確定済み（Q5・CLASS.md）
- Analyzer に `isValid()` を持たせると null/blank 判定の責務が分散し設計が崩れる
- null / blank の期待動作は TC-VM-012〜TC-VM-016 で検証する
- 対応形式（QR_CODE / CODE_39 / CODE_128）のフィルタリングは `OcrScannerOptions` で設定し、実機手動確認とする

---

### Q24. `permissionDenied` フラグをいつクリアするか【重大度: 中】✅

**回答:** `permissionDenied` は、再度スタート操作を始めた時点、または権限許可後に読み取り開始する時点でクリアする。

- `onPermissionDenied()` で `permissionDenied=true`
- 再度スタートボタンを押したら、古い権限エラー表示はいったんクリアする
- 権限が再び拒否されたら、再度 `onPermissionDenied()` で `permissionDenied=true`
- 権限が許可されて `onScanStart()` したら、`permissionDenied=false`
- `onCancel()` でも `permissionDenied=false`
- `onRetry()` では原則影響なし。ただし全体リセット処理を共通化するなら `false` に戻してよい

---

## 確定した実装方針まとめ

| 項目 | 値 |
|------|-----|
| パッケージ名 | `com.garyohosu.ocrreader` |
| minSdk | 23（Android 6.0 以降） |
| 言語 | Kotlin |
| UI | Jetpack Compose（ポートレート固定） |
| カメラ | CameraX |
| OCR認識 | ML Kit Ocr Scanning |
| ビルド | Gradle Kotlin DSL |
| 対応形式 | QR_CODE / CODE_39 / CODE_128 |
| クールダウン | なし（「読む」ボタン押下で1回のみOCR実行） |
| OK表示色 | 青 |
| NG表示色 | 赤 |
| 音 | ToneGenerator（BEEP / ACK / NACK） |
| 空文字時UI | 画面内メッセージ（フェーズ維持） |
| 戻るボタン | 実装する（読み取り中止 → スタート画面） |
| システムバック | Activity は終了せず、画面上の戻る系操作と同じ挙動 |
| 失敗メッセージクリア | 次の有効読み取り / 中止 / 再開 / フェーズ切替でクリア |
| 権限拒否後の再試行 | 再スタートで再要求。再表示不可なら設定許可を案内（最小実装は案内文のみ） |
| 判定画面のカメラ | 停止する。「もう一度」で再起動 |
| Analyzerスレッド | background で解析、ViewModel 更新は Main thread |
| SoundEvent 伝搬 | ScanViewModel が SharedFlow~SoundEvent~ を持ち、MainActivity が collect して FeedbackSoundPlayer を呼ぶ |
| OcrScannerController | 独立クラスとして実装（CameraX の起動・停止・Analyzer バインドを集約） |
| FeedbackSoundPlayer 管理 | MainActivity で onCreate 生成・onDestroy release |
| onOcrDetected 引数型 | String?（null / blank は ViewModel 側で判定してフェーズ維持・エラーメッセージ表示） |
| OcrScannerController 保持 | MainActivity が 1 インスタンスを生成・保持し、ScanScreen / CameraPreview に渡す |
| permissionDenied クリア | onScanStart / onCancel で false。再拒否時は onPermissionDenied で true |
| UIテスト | 今回は対象外 |
| 発熱対策 | 今回は対象外（READMEに注意書きのみ） |

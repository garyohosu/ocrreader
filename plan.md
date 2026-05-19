# OCR読取Androidアプリ 実証実験 実装計画

> **For Hermes:** Use subagent-driven-development skill to implement this plan task-by-task.

**Goal:** Androidスマートフォンのカメラで2つのOCRを連続読み取りし、一致/不一致を即時表示する実証実験用アプリを作る。

**Architecture:** 単一のAndroidアプリとして構成し、Kotlin + Gradle Kotlin DSL のAndroid Studioプロジェクトで実装する。UIはJetpack Compose、カメラはCameraX、OCR認識は ML Kit Ocr Scanning を使う。画面状態はViewModelで管理し、1つ目の読み取り→2つ目の読み取り→判定表示の状態遷移を明確に分ける。音声フィードバックは Android 標準の ToneGenerator を使い、保存や通信は行わない。

**Tech Stack:** Kotlin, Android Studio, Gradle Kotlin DSL, Jetpack Compose, CameraX, ML Kit Ocr Scanning, ViewModel, Coroutines

---

## 実装方針

- 最初に最小のAndroid Studioプロジェクトを作る
- 画面遷移より先に、**OCR検出と2回照合の状態機械**を固める
- 読み取り成功音・OK音・NG音は ToneGenerator で実装する
- 端末内のオフライン完結を最優先する
- 保存やログインなどの機能は作らない

## 追加の固定条件

- 実装言語は Kotlin
- UI は Jetpack Compose
- 画面はポートレート固定
- カメラは CameraX
- OCR認識は ML Kit Ocr Scanning
- ビルドは Gradle Kotlin DSL
- 初期対応OCR形式は QR_CODE / CODE_39 / CODE_128
- APK は debug APK でよい
- 権限未許可なら要求を出し、拒否されたらスタート画面にエラーを出す
- 読み取り後 1 秒はスキャン結果を無視する

---

## Task 1: Androidプロジェクトを作成して起動確認する

**Objective:** 実証実験用アプリの土台となるAndroid Studioプロジェクトを作る。

**Files:**
- Create: `project/ocrreader/android-app/`（新規Android Studioプロジェクト一式）
- Modify: `project/ocrreader/README.md`

**Step 1: プロジェクトを作成する**

- Kotlin + Jetpack Compose の空プロジェクトを作る
- package名を決める
- 最小限のトップ画面を表示する
- Gradle Kotlin DSL を使う

**Step 2: 起動確認する**

Run:
```bash
cd project/ocrreader/android-app
./gradlew assembleDebug
```
Expected: ビルド成功
Verify:
- `app/build/outputs/apk/debug/app-debug.apk` が生成される
- `./gradlew assembleDebug` だけで debug APK が作れる

**Step 3: エミュレータまたは実機で起動する**

Run:
```bash
./gradlew installDebug
```
Expected: アプリが起動し、スタート画面が表示される

**Step 4: READMEを追加する**

- プロジェクト概要
- ビルド方法
- APK生成方法
- インストール方法

---

## Task 2: 画面状態モデルと2回読み取りの状態機械を作る

**Objective:** 1つ目読み取り→2つ目読み取り→判定表示の流れをアプリ内部で表現する。

**Files:**
- Create: `project/ocrreader/android-app/app/src/main/java/.../ScanState.kt`
- Create: `project/ocrreader/android-app/app/src/main/java/.../ScanViewModel.kt`
- Test: `project/ocrreader/android-app/app/src/test/java/.../ScanViewModelTest.kt`

**Step 1: 失敗するテストを書く**

```kotlin
@Test
fun firstScan_movesToSecondScan() {
    val vm = ScanViewModel()
    vm.onOcrDetected("123")
    assertEquals(ScanPhase.WAITING_FOR_SECOND, vm.state.value.phase)
}
```

**Step 2: テストを実行して失敗を確認する**

Run:
```bash
./gradlew testDebugUnitTest
```
Expected: FAIL

**Step 3: 最小実装を書く**

- `ScanPhase` を定義
- 1つ目と2つ目の値を保持する
- `errorMessage: String?` を状態に持たせる
- 2回目で比較結果を出す

**Step 4: テストを再実行する**

Run:
```bash
./gradlew testDebugUnitTest
```
Expected: PASS

---

## Task 3: スタート画面とカメラ画面のUIを作る

**Objective:** スタートボタンからカメラ画面へ遷移し、読み取り指示を表示する。

**Files:**
- Modify: `project/ocrreader/android-app/app/src/main/java/.../MainActivity.kt`
- Create: `project/ocrreader/android-app/app/src/main/java/.../ui/StartScreen.kt`
- Create: `project/ocrreader/android-app/app/src/main/java/.../ui/ScanScreen.kt`
- Create: `project/ocrreader/android-app/app/src/main/java/.../ui/ResultScreen.kt`

**Step 1: 画面ごとのComposableを作る**

- StartScreen: アプリ名と「スタート」ボタン
- ScanScreen: 「1つ目のOCRを読んでください」「2つ目のOCRを読んでください」
- ResultScreen: OK/NG表示と読み取った値

**Step 2: 画面遷移を確認する**

Run:
```bash
./gradlew assembleDebug
```
Expected: ビルド成功

**Step 3: スタート→読み取り画面→判定画面の遷移を確認する**

- ボタン操作だけで遷移できること
- 画面文言が仕様通りであること

---

## Task 4: CameraX を使ってカメラプレビューを出す

**Objective:** Android端末のカメラ映像をアプリ内で表示できるようにする。

**Files:**
- Create: `project/ocrreader/android-app/app/src/main/java/.../camera/CameraPreview.kt`
- Modify: `project/ocrreader/android-app/app/build.gradle.kts`
- Modify: `project/ocrreader/android-app/app/src/main/AndroidManifest.xml`

**Step 1: 依存関係を追加する**

- CameraXの依存を追加
- 必要な権限を追加
- カメラ権限がない場合は要求を出す
- 拒否された場合はスタート画面にエラーを出す
- AndroidManifest に `CAMERA` 権限を追加する
- MainActivity に `android:screenOrientation="portrait"` を設定する

**Step 2: カメラプレビューを表示する**

Run:
```bash
./gradlew assembleDebug
```
Expected: ビルド成功

**Step 3: 実機またはエミュレータで確認する**

Expected:
- カメラ映像が表示される
- カメラ権限ダイアログが適切に出る
- 横向きに切り替わらない
- 画面回転で Activity 再生成や CameraX 再バインドを避けられる

---

## Task 5: ML Kit でOCR検出を組み込む

**Objective:** カメラ映像からOCR文字列を取得できるようにする。初期対応形式は QR_CODE / CODE_39 / CODE_128 に絞る。

**Files:**
- Create: `project/ocrreader/android-app/app/src/main/java/.../camera/OcrAnalyzer.kt`
- Create: `project/ocrreader/android-app/app/src/main/java/.../camera/OcrScannerController.kt`
- Modify: `project/ocrreader/android-app/app/build.gradle.kts`

**Step 1: 失敗するテストまたは最小の検出データ変換テストを書く**

- OCR結果から文字列を安全に取り出す処理を単体テスト化する

**Step 2: 検出コールバックを実装する**

- 1件以上のOCRを受け取る
- 空文字やnullを除外する
- OCR候補は検出されたが値が空文字 / null の場合は、保存せず `errorMessage` に失敗文言を設定する
- 通常のカメラ待機状態で何も検出されていないフレームは失敗扱いにしない
- 空文字 / null の場合は現在の読み取りフェーズを維持する
- 空文字 / null の場合は読み取り成功音を鳴らさない
- 先頭1件だけではなく、必要なら優先順位を持たせる
- `Ocr.FORMAT_QR_CODE`
- `Ocr.FORMAT_CODE_39`
- `Ocr.FORMAT_CODE_128`

**Step 3: ビルドして確認する**

Run:
```bash
./gradlew testDebugUnitTest assembleDebug
```
Expected: PASS

---

## Task 6: 1つ目・2つ目の読み取りを連続処理する

**Objective:** 1回目と2回目の読み取りを切り替え、同じOCRの誤検出を抑える。

**Files:**
- Modify: `project/ocrreader/android-app/app/src/main/java/.../ScanViewModel.kt`
- Modify: `project/ocrreader/android-app/app/src/main/java/.../ui/ScanScreen.kt`
- Test: `project/ocrreader/android-app/app/src/test/java/.../ScanViewModelTest.kt`

**Step 1: 連続誤読を無視するテストを書く**

```kotlin
@Test
fun duplicateScanWithinCooldown_isIgnored() { ... }
```

- 空文字を渡しても `ocr1` / `ocr2` に保存されないこと
- null 相当の入力でもフェーズが進まないこと
- `errorMessage` が state に設定されること
- 空文字 / null 時に音イベントが発火しないこと
- 次に有効なOCRを読めたら `errorMessage` がクリアされること

**Step 2: クールダウンを実装する**

- 読み取り後1秒は次の検出を無効化する
- 1つ目→2つ目の同一連続誤読を防ぐ
- 1つ目と2つ目の同じ値そのものは禁止しない
- 有効なOCRを読み取った時点で `errorMessage` をクリアする
- `reset()`、中止、スタート画面へ戻る、フェーズ切替時にも `errorMessage` をクリアする

**Step 3: テストを実行する**

Run:
```bash
./gradlew testDebugUnitTest
```
Expected: PASS

---

## Task 7: 読み取り成功音、OK音、NG音を追加する

**Objective:** 操作結果を音で分かりやすく返す。音は ToneGenerator を使って実装する。

**Files:**
- Create: `project/ocrreader/android-app/app/src/main/java/.../audio/FeedbackSoundPlayer.kt`
- Modify: `project/ocrreader/android-app/app/src/main/java/.../ScanViewModel.kt`

**Step 1: 音の再生インターフェースを作る**

- 読み取り成功音
- OK判定音
- NG判定音
- 読み取り成功音は `TONE_PROP_BEEP`
- OK音は `TONE_PROP_ACK`
- NG音は `TONE_PROP_NACK`

**Step 2: 音の発火タイミングをつなぐ**

- 1つ目読み取り成功で「ピッ」
- 2つ目読み取り成功で「ピッ」
- 判定時にOK/NGの音

**Step 3: 実機で確認する**

Expected:
- 読み取り時に必ず音が鳴る
- 判定時にOK/NGが音でも分かる

---

## Task 8: 判定画面を仕上げる

**Objective:** OK/NGを色と文字で直感的に伝える。

**Files:**
- Modify: `project/ocrreader/android-app/app/src/main/java/.../ui/ResultScreen.kt`
- Modify: `project/ocrreader/android-app/app/src/main/java/.../ScanViewModel.kt`

**Step 1: OK/NG表示のテストを追加する**

- 一致時はOK
- 不一致時はNG

**Step 2: 色と文言を実装する**

- OK: 青系背景、`OK` 大表示
- NG: 赤系背景、`NG` 大表示

**Step 3: 値の見せ方を整える**

- 1つ目
- 2つ目
- 判定メッセージ

---

## Task 9: 「もう一度」ボタンと「戻る」動作を実装する

**Objective:** 失敗時や再確認時にすぐ再試行できるようにする。

**Files:**
- Modify: `project/ocrreader/android-app/app/src/main/java/.../ui/ResultScreen.kt`
- Modify: `project/ocrreader/android-app/app/src/main/java/.../ui/ScanScreen.kt`
- Modify: `project/ocrreader/android-app/app/src/main/java/.../MainActivity.kt`
- Modify: `project/ocrreader/android-app/app/src/main/java/.../ScanViewModel.kt`

**Step 1: リセットテストを書く**

- 判定結果、ocr1、ocr2がクリアされることを確認
- 読み取り画面の「中止」でスタート画面へ戻ることを確認
- 読み取り画面でシステムバックを押しても Activity を終了せず、スタート画面へ戻ることを確認
- 判定画面でシステムバックを押しても Activity を終了せず、スタート画面へ戻ることを確認

**Step 2: リセット処理を実装する**

- `reset()` で読み取り状態を初期化
- ScanScreen に「中止」ボタンを配置する
- 「中止」押下時に `reset()` を呼び出す
- 読み取り途中の値、判定結果、エラーメッセージをクリアする
- スタート画面へ戻る処理を用意する
- カメラ画面を終了し、スタート画面へ戻る
- Jetpack Compose の `BackHandler` を使い、システムバックを画面上の戻る系操作と同じ挙動に統一する
- 読み取り画面のシステムバックは「中止」と同じ動作にする
- 判定画面のシステムバックは「もう一度」ではなくスタート画面へ戻す

**Step 3: 再実行を確認する**

Expected:
- 連続で試せる
- 途中で戻れる
- 読み取り中でも安全に中止できる
- システムバックでアプリが終了せず、状態を初期化してスタート画面へ戻る

---

## Task 10: ビルド成果物とREADMEを整える

**Objective:** 実証実験で配布・確認しやすい形にまとめる。

**Files:**
- Create: `project/ocrreader/android-app/README.md`
- Modify: `project/ocrreader/README.md`

**Step 1: READMEを書く**

- 目的
- ビルド方法
- APK作成方法
- インストール方法
- 操作方法
- 制限事項

**Step 2: APK生成を確認する**

Run:
```bash
./gradlew assembleDebug
```
Expected: `app-debug.apk` が生成される

**Step 3: 最終確認する**

- 起動できる
- 2回読み取りできる
- OK/NGが分かる
- 音が鳴る

---

## 受け入れ条件

- AndroidスマートフォンにAPKでインストールできる
- スタートボタンを押すとカメラが起動する
- 1つ目のOCRを読み取れる
- 2つ目のOCRを読み取れる
- 読み取り画面で「中止」ボタンを押すとスタート画面へ戻れる
- 読み取り画面でシステムバックを押してもスタート画面へ戻れる
- 2つのOCR文字列を画面に表示できる
- 一致時に青色でOK表示できる
- 不一致時に赤色でNG表示できる
- 空文字 / null 読み取り時は画面内メッセージを表示し、フェーズを進めない
- 次に有効なOCRを読めたら失敗メッセージが消える
- 読み取り時に音が鳴る
- 判定時にOK音またはNG音が鳴る
- 「もう一度」ボタンで再実行できる
- 画面はポートレート固定で動作する

---

## 実装順の推奨

1. プロジェクト作成
2. 状態機械とテスト
3. UI
4. CameraX
5. ML Kit
6. 2回読み取り制御
7. 音
8. 判定画面
9. リセット
10. README / APK

---

## 備考

- 実証実験版なので、最初から完成度を上げすぎない
- まずは現場で本当に使えるかを早く確認する
- 問題が出たら、読み取り速度・音・表示の分かりやすさを優先して改善する

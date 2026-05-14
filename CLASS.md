# CLASS.md — バーコード照合Androidアプリ クラス図

## レイヤー構成

```
UI層           MainActivity / StartScreen / ScanScreen / ResultScreen / CameraPreview
ViewModel層    ScanViewModel
Domain層       ScanState / ScanPhase / ScanResult / SoundEvent
Camera層       BarcodeScannerController / BarcodeAnalyzer
Audio層        FeedbackSoundPlayer
```

---

## クラス図（全体）

```mermaid
classDiagram
    namespace domain {
        class ScanPhase {
            <<enumeration>>
            IDLE
            WAITING_FOR_FIRST
            WAITING_FOR_SECOND
            RESULT
        }

        class ScanResult {
            <<enumeration>>
            OK
            NG
        }

        class SoundEvent {
            <<enumeration>>
            BEEP
            OK
            NG
        }

        class ScanState {
            <<data class>>
            +phase : ScanPhase
            +barcode1 : String?
            +barcode2 : String?
            +result : ScanResult?
            +errorMessage : String?
            +permissionDenied : Boolean
        }
    }

    namespace viewmodel {
        class ScanViewModel {
            <<ViewModel>>
            -_state : MutableStateFlow~ScanState~
            +state : StateFlow~ScanState~
            -_soundEvent : MutableSharedFlow~SoundEvent~
            +soundEvent : SharedFlow~SoundEvent~
            -cooldownActive : Boolean
            +onScanStart()
            +onBarcodeDetected(value : String)
            +onCancel()
            +onRetry()
            +onPermissionDenied()
            -compare() ScanResult
            -startCooldown()
            -reset()
        }
    }

    namespace camera {
        class BarcodeAnalyzer {
            <<ImageAnalysis.Analyzer>>
            -onDetected : (String) -> Unit
            +analyze(image : ImageProxy)
            -isValid(value : String) Boolean
        }

        class BarcodeScannerController {
            -analyzer : BarcodeAnalyzer
            -cameraProvider : ProcessCameraProvider?
            +startCamera(lifecycleOwner : LifecycleOwner, previewView : PreviewView)
            +stopCamera()
        }
    }

    namespace audio {
        class FeedbackSoundPlayer {
            -toneGenerator : ToneGenerator
            +playBeep()
            +playOk()
            +playNg()
            +release()
        }
    }

    namespace ui {
        class MainActivity {
            <<ComponentActivity>>
            -viewModel : ScanViewModel
            -soundPlayer : FeedbackSoundPlayer
            +onCreate()
            +onDestroy()
        }

        class StartScreen {
            <<Composable>>
            +state : ScanState
            +onStartClick : () -> Unit
        }

        class ScanScreen {
            <<Composable>>
            +state : ScanState
            +controller : BarcodeScannerController
            +onCancel : () -> Unit
        }

        class ResultScreen {
            <<Composable>>
            +state : ScanState
            +onRetry : () -> Unit
            +onBack : () -> Unit
        }

        class CameraPreview {
            <<Composable>>
            +controller : BarcodeScannerController
        }
    }

    %% Domain 内の関連
    ScanState --> ScanPhase : phase
    ScanState --> ScanResult : result (nullable)

    %% ViewModel ↔ Domain
    ScanViewModel ..> ScanState : emits via StateFlow
    ScanViewModel ..> SoundEvent : emits via SharedFlow

    %% Camera層
    BarcodeScannerController *-- BarcodeAnalyzer : owns
    BarcodeAnalyzer ..> ScanViewModel : onBarcodeDetected()

    %% Audio層
    MainActivity *-- FeedbackSoundPlayer : creates / releases

    %% MainActivity ↔ ViewModel
    MainActivity o-- ScanViewModel : observes state
    MainActivity ..> FeedbackSoundPlayer : calls on SoundEvent

    %% UI ↔ ViewModel / Controller
    StartScreen ..> ScanState : observes
    ScanScreen ..> ScanState : observes
    ScanScreen *-- CameraPreview : contains
    ScanScreen o-- BarcodeScannerController : uses
    CameraPreview o-- BarcodeScannerController : uses
    ResultScreen ..> ScanState : observes

    %% MainActivity → 画面
    MainActivity ..> StartScreen : renders
    MainActivity ..> ScanScreen : renders
    MainActivity ..> ResultScreen : renders
```

---

## クラス図（Domain + ViewModel 詳細）

状態遷移の核となる部分を抜き出した詳細図。

```mermaid
classDiagram
    class ScanPhase {
        <<enumeration>>
        IDLE
        WAITING_FOR_FIRST
        WAITING_FOR_SECOND
        RESULT
    }

    class ScanResult {
        <<enumeration>>
        OK
        NG
    }

    class SoundEvent {
        <<enumeration>>
        BEEP
        OK
        NG
    }

    class ScanState {
        <<data class>>
        +phase : ScanPhase
        +barcode1 : String?
        +barcode2 : String?
        +result : ScanResult?
        +errorMessage : String?
        +permissionDenied : Boolean
    }

    class ScanViewModel {
        <<ViewModel>>
        -_state : MutableStateFlow~ScanState~
        +state : StateFlow~ScanState~
        -_soundEvent : MutableSharedFlow~SoundEvent~
        +soundEvent : SharedFlow~SoundEvent~
        -cooldownActive : Boolean
        +onScanStart()
        +onBarcodeDetected(value : String)
        +onCancel()
        +onRetry()
        +onPermissionDenied()
        -compare() ScanResult
        -startCooldown()
        -reset()
    }

    ScanState --> ScanPhase : phase
    ScanState --> ScanResult : result
    ScanViewModel ..> ScanState : 管理・更新
    ScanViewModel ..> SoundEvent : 発火
```

---

## クラス図（Camera + Audio 詳細）

```mermaid
classDiagram
    class BarcodeAnalyzer {
        <<ImageAnalysis.Analyzer>>
        -onDetected : (String) -> Unit
        +analyze(image : ImageProxy)
        -isValid(value : String) Boolean
    }

    class BarcodeScannerController {
        -analyzer : BarcodeAnalyzer
        -cameraProvider : ProcessCameraProvider?
        +startCamera(lifecycleOwner : LifecycleOwner, previewView : PreviewView)
        +stopCamera()
    }

    class FeedbackSoundPlayer {
        -toneGenerator : ToneGenerator
        +playBeep()
        +playOk()
        +playNg()
        +release()
    }

    class ScanViewModel {
        <<ViewModel>>
        +soundEvent : SharedFlow~SoundEvent~
        +onBarcodeDetected(value : String)
    }

    class MainActivity {
        <<ComponentActivity>>
        -soundPlayer : FeedbackSoundPlayer
    }

    BarcodeScannerController *-- BarcodeAnalyzer : owns
    BarcodeAnalyzer ..> ScanViewModel : onBarcodeDetected() (Main thread)
    MainActivity *-- FeedbackSoundPlayer : creates / releases
    MainActivity ..> ScanViewModel : soundEvent を observe
    MainActivity ..> FeedbackSoundPlayer : playBeep / playOk / playNg
```

---

## クラス一覧

### Domain層

| クラス | 種別 | 役割 |
|--------|------|------|
| `ScanPhase` | enum | 読み取りフェーズ（IDLE / WAITING_FOR_FIRST / WAITING_FOR_SECOND / RESULT） |
| `ScanResult` | enum | 照合結果（OK / NG） |
| `SoundEvent` | enum | 音イベント（BEEP / OK / NG）。ViewModel が発火し MainActivity が受け取る |
| `ScanState` | data class | 画面全体の状態スナップショット。StateFlow で UI に流す |

### ViewModel層

| クラス | 種別 | 役割 |
|--------|------|------|
| `ScanViewModel` | ViewModel | 状態管理・照合ロジック・クールダウン制御。Android 依存を持たない |

### Camera層

| クラス | 種別 | 役割 |
|--------|------|------|
| `BarcodeAnalyzer` | ImageAnalysis.Analyzer | ML Kit でバーコードを検出し、コールバックで ViewModel に通知する |
| `BarcodeScannerController` | 通常クラス | CameraX の起動・停止と BarcodeAnalyzer のバインドを担う |

### Audio層

| クラス | 種別 | 役割 |
|--------|------|------|
| `FeedbackSoundPlayer` | 通常クラス | ToneGenerator を内部管理し、3種の音を再生する |

### UI層

| クラス | 種別 | 役割 |
|--------|------|------|
| `MainActivity` | ComponentActivity | ViewModel・FeedbackSoundPlayer を保持し、SoundEvent を観察して音を鳴らす |
| `StartScreen` | Composable | スタート画面。ボタン押下で onStartClick を呼ぶ |
| `ScanScreen` | Composable | 読み取り画面。CameraPreview を内包し、フェーズ文言を表示する |
| `ResultScreen` | Composable | 判定画面。OK/NG 表示と「もう一度」「戻る」ボタンを持つ |
| `CameraPreview` | Composable | CameraX のプレビューを AndroidView でラップして表示する |

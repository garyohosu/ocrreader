# CLAUDE.md — プロジェクト共通手順

## Android アプリのビルド手順（標準）

**このプロジェクトでは PC に Android SDK / JDK をインストールせず、GitHub Actions でビルドする。**  
ローカルの `./gradlew assembleDebug` は使わない。

---

### APK のビルド方法

#### 方法 A：手動実行（開発中の動作確認）

```bash
gh workflow run build-apk.yml
```

またはブラウザで Actions → "Build APK" → "Run workflow"。  
ビルド完了後、Artifacts から `ocrreader-vX.X.X-debug.apk` をダウンロードできる。

#### 方法 B：タグを push してリリース（バージョンリリース時）

```bash
# versionCode / versionName を app/build.gradle.kts で上げてからコミット済みの前提
git tag v0.4.0
git push origin v0.4.0
```

タグ push で自動的に：
1. APK をビルド
2. GitHub Release を作成して APK を添付

#### ローカルへのダウンロード

```bash
# run ID は gh run list で確認
gh run download <run-id> --dir dist
```

---

### スマートフォンへの配布手順

1. GitHub Release ページ（`https://github.com/garyohosu/ocrreader/releases`）をスマートフォンで開く
2. `ocrreader-vX.X.X-debug.apk` をダウンロード
3. 初回のみ「提供元不明のアプリ」を許可してインストール

既存リリースに APK を追加・更新したい場合：

```bash
gh release upload v0.4.0 dist/ocrreader-v0.4.0-debug.apk --clobber
```

---

### ワークフローファイル

`.github/workflows/build-apk.yml`

| トリガー | 動作 |
|---------|------|
| `v*` タグ push | ビルド → GitHub Release 作成 |
| `workflow_dispatch`（手動）| ビルド → Artifacts 保存（30日間）、`create_release=true` でリリースも作成 |

---

## 変更履歴（Claude による作業ログ）

### 2026-05-18：GitHub Actions ビルド環境を整備

- **追加**：`.github/workflows/build-apk.yml`
  - タグ push または手動実行で debug APK をビルド
  - Artifacts に 30 日間保存
  - タグ push 時は GitHub Release も自動作成
- **背景**：PC に Android SDK / JDK がないため、ローカルビルドの代替としてCI ビルドを標準化
- **動作確認**：手動実行で `ocrreader-v0.3.0-debug.apk`（33 MB）のビルドと配布を確認済み

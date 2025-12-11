# Vetro - Glassmorphism Clock

**Vetro**（ヴェトロ: イタリア語で「ガラス」）は、Android デバイスを「高級なデジタルインテリア」へと変貌させる、グラスモーフィズムデザインの時計アプリです。

システム壁紙を動的に取得し、Android 12の `RenderEffect` を活用したすりガラスのようなブラー効果と光の反射をリアルタイムで合成することで、**「あなたの壁紙に溶け込む時計」** を実現しました。

## Key Features

- **Real-time Glassmorphism**: `RenderEffect` (Android 12+) を活用し、背景画像を動的にぼかして透過させる高度なUI表現。
- **System Wallpaper Integration**: ユーザーが設定している壁紙を自動取得し、アプリの背景として継承。
- **Daydream (Screensaver)**: 充電中に自動起動するスクリーンセーバーに対応。Composeの描画能力をフルに活かした滑らかな表示を実現。
- **Burn-in Protection**: 有機ELディスプレイ保護のため、表示位置を定期的にピクセル単位でランダムシフト。
- **AppWidget (Experimental)**: Bitmap生成技術を用いたホーム画面ウィジェット（※後述の技術的判断により、メイン機能はDaydreamへ移行）。

## 🛠 Tech Stack

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose (Material3)
- **Graphics**: RenderEffect, Canvas, Custom Shader
- **Architecture**: MVVM (Model-View-ViewModel)
- **Async**: Coroutines, Flow
- **System Service**: Daydream Service, WallpaperManager
- **Build System**: Gradle (Version Catalogs / libs.versions.toml)

## 💡 Technical Challenges & Solutions

開発において直面した技術的なトレードオフと、品質担保のためのアーキテクチャ選定について解説します。

### 1. RemoteViewsの制約と「デザイン品質」の両立

**課題:**
Androidのホーム画面ウィジェット（AppWidget）は `RemoteViews` アーキテクチャに基づいており、Jetpack Composeのような高度なグラフィックス処理（リアルタイムブラーや複雑な合成）をネイティブでサポートしていません。
また、システムフォント以外の「極細のカスタムフォント」を綺麗に表示することも困難でした。

**解決策:**
ウィジェット向けには **「バックグラウンドBitmap生成方式」** を実装しました。
`Canvas` APIを使用して、裏側で「壁紙の切り抜き」「ブラー処理」「テキスト描画」「光沢エフェクト」を1枚のBitmap画像として合成・生成し、それを `RemoteViews` に渡すことで、制約の多いウィジェット上でもアプリ本体と遜色ないリッチなデザインを再現しました。

### 2. OSの省電力仕様と「UX」のトレードオフによるピボット

**課題:**
当初はウィジェットをメイン機能として開発していましたが、Android OSの省電力仕様（DozeモードやAppWidgetの更新頻度制限）と、Vetroが目指す「リッチな描画」の相性が悪いという問題に直面しました。
高解像度のBitmap合成を頻繁に行うことはバッテリー負荷が高く、更新間隔を広げると「時計としての正確性」が損なわれます。`AlarmManager` 等での強制更新も検討しましたが、ユーザー体験（バッテリー持ち）を犠牲にする実装は避けるべきと判断しました。

**技術的判断 (Pivot):**
Vetroのコアバリューである「ガラスの質感（RenderEffect）」と「インテリアとしての美しさ」を最大限に発揮できる環境は、バッテリー制約の厳しいホーム画面ではなく、**電源接続時の「スクリーンセーバー（Daydream）」** であると再定義しました。

Daydreamとして実装することで、以下のメリットを享受し、理想的なUXを実現しました。
* **Composeのフル活用:** RemoteViewsの制約を受けず、Composeの高度な描画機能を直接利用可能。
* **常時起動の許容:** 電源接続中であるため、リッチな描画を行ってもバッテリー消費が問題にならない。
* **正確な時刻更新:** システムのスリープに干渉されず、ラグのない正確な時刻表示が可能。

### 3. Android 14 以降の権限管理とユーザビリティ

**課題:**
壁紙に合わせてUIを変化させるため画像へのアクセス権が必要ですが、Android 14から導入された "Selected Photos Access" により、権限フローが複雑化しました。

**解決策:**
最新の権限モデル `READ_MEDIA_VISUAL_USER_SELECTED` に対応しつつ、アプリのコア機能（自動での壁紙取得）には広範なアクセスが必要であることをUI上で丁寧に説明するフローを設計しました。また、権限が得られない場合でもデフォルトの背景で動作するフォールバック機能を実装し、アプリがクラッシュしない堅牢性を確保しました。

---

_Vetro - Transform your Android device into a premium digital interior_
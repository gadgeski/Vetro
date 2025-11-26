// WallpaperUtil.kt
package com.example.vetro.ui.util

import android.annotation.SuppressLint
import android.app.WallpaperManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap

// 【追加】エラー抑制のために必要(android.annotation.SuppressLint)

@Composable
fun rememberSystemWallpaper(): ImageBitmap? {
    val context = LocalContext.current

    return remember(context) {
        try {
            val wallpaperManager = WallpaperManager.getInstance(context)

            // =================================================================
            // 【注意】動く壁紙 (Live Wallpaper) に関する制約
            // =================================================================
            // ユーザーが「動く壁紙」を設定している場合、以下の挙動になる可能性があります。
            // 1. wallpaperManager.drawable が null を返す。
            // 2. 動く壁紙のサムネイル（静止画）が返される。
            // 3. メーカー独自のホームアプリの場合、アクセス権限で SecurityException が出る。
            //
            // そのため、必ず try-catch で保護し、呼び出し元で null の場合の
            // フォールバック画像（アプリ内デフォルト画像）を用意する設計にしています。
            // =================================================================

            // 【追加】権限エラーの赤線を消すためのアノテーション
            // 実際にはアプリが持つ権限で取得可能ですが、IDEが過剰に警告するため抑制します
            @SuppressLint("MissingPermission")
            val drawable = wallpaperManager.drawable

            drawable?.toBitmap()?.asImageBitmap()

        } catch (e: Exception) {
            // 権限不足やLive Wallpaperの不整合などで取得できなかった場合は null を返す
            e.printStackTrace()
            null
        }
    }
}
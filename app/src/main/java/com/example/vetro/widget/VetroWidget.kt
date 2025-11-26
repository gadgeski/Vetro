// VetroWidget.kt
package com.example.vetro.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp // 【修正】 dpのエラー解消用
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.example.vetro.MainActivity

class VetroWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    companion object {
        val KEY_LAST_UPDATE = longPreferencesKey("last_update_time")
        val KEY_WALLPAPER_INDEX = intPreferencesKey("wallpaper_index")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val size = LocalSize.current

            val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
            val lastUpdate = prefs[KEY_LAST_UPDATE] ?: 0L
            val wallpaperIndex = prefs[KEY_WALLPAPER_INDEX] ?: 0

            // 画像生成処理
            val bitmap = rememberWidgetBitmap(context, size, lastUpdate, wallpaperIndex)

            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // 1. 時計画像
                if (bitmap != null) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = "Clock",
                        contentScale = ContentScale.FillBounds,
                        modifier = GlanceModifier.fillMaxSize()
                            .clickable(actionStartActivity<MainActivity>())
                    )
                }

                // 2. 壁紙切り替えボタン
                Box(
                    modifier = GlanceModifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Image(
                        provider = ImageProvider(android.R.drawable.ic_menu_gallery),
                        contentDescription = "Change Wallpaper",
                        // 【修正】ColorProviderのエラーを回避するため、色指定(tint)を削除しました。
                        // アイコンは標準のグレーっぽい色で表示されますが、機能に影響はありません。
                        modifier = GlanceModifier
                            .size(32.dp)
                            .clickable(actionRunCallback<ChangeWallpaperAction>())
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberWidgetBitmap(
    context: Context,
    size: DpSize,
    triggerKey: Long,
    wallpaperIndex: Int
): Bitmap? {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val density = context.resources.displayMetrics.density
    val widthPx = size.width.value * density
    val heightPx = size.height.value * density

    LaunchedEffect(size, triggerKey, wallpaperIndex) {
        bitmap = VetroWidgetRenderer.renderWidgetBitmap(context, widthPx, heightPx, wallpaperIndex)
    }

    return bitmap
}
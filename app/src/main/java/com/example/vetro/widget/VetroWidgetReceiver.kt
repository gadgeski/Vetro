// VetroWidgetReceiver.kt
package com.example.vetro.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VetroWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VetroWidget()

    companion object {
        const val ACTION_UPDATE_TICK = "com.example.vetro.ACTION_UPDATE_TICK"
        private const val REQUEST_CODE = 1001
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // ウィジェットが配置されたらタイマー開始
        scheduleNextUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // ウィジェットがなくなったらタイマー停止
        cancelUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // 自作の更新アクションを受け取った場合
        if (intent.action == ACTION_UPDATE_TICK) {
            // 1. ウィジェットの画面更新を実行
            updateWidget(context)
            // 2. 次の1分後のアラームをセット（ループさせる）
            scheduleNextUpdate(context)
        }
    }

    /**
     * ウィジェットの表示を更新する処理
     */
    private fun updateWidget(context: Context) {
        // コルーチンを使って非同期で更新
        CoroutineScope(Dispatchers.Default).launch {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(VetroWidget::class.java)

            glanceIds.forEach { glanceId ->
                // ステートを更新して再描画をトリガー
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    // 【修正】toMutablePreferences().apply { ... } を使うことで、
                    // 更新後の Preferences オブジェクト自体を返却するようにします。
                    // (以前は代入式の結果である Unit を返していたためエラーでした)
                    prefs.toMutablePreferences().apply {
                        this[VetroWidget.KEY_LAST_UPDATE] = System.currentTimeMillis()
                    }
                }
                VetroWidget().update(context, glanceId)
            }
        }
    }

    /**
     * 次の「00秒」にアラームをセットする
     */
    private fun scheduleNextUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, VetroWidgetReceiver::class.java).apply {
            action = ACTION_UPDATE_TICK
        }
        // 更新用インテントの準備
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 現在時刻から「次の分の00秒」を計算
        val now = System.currentTimeMillis()
        val nextMinute = now + 60000 - (now % 60000)

        // 正確な時間に発火させる (setExact)
        try {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP, // スリープ中でも起こす
                nextMinute,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Android 12以上で権限がない場合の例外対策（通常はmanifestの権限でOK）
            e.printStackTrace()
        }
    }

    private fun cancelUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, VetroWidgetReceiver::class.java).apply {
            action = ACTION_UPDATE_TICK
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
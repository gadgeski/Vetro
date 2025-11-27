// VetroWidgetReceiver.kt
package com.example.vetro.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
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
        private const val TAG = "VetroWidgetReceiver"
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleNextUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_UPDATE_TICK) {
            Log.d(TAG, "Alarm received. Starting update...")

            // 【重要】 goAsync() を呼んで、処理が終わるまでReceiverを生かしておく
            // これがないと、非同期処理(launch)が終わる前にプロセスがキルされることがある
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.Default).launch {
                try {
                    // 1. 更新処理
                    updateWidget(context)
                    Log.d(TAG, "Update finished.")
                } catch (e: Exception) {
                    Log.e(TAG, "Update failed", e)
                } finally {
                    // 2. 処理が終わったら必ず finish() を呼んでOSに報告する
                    pendingResult.finish()
                }
            }

            // 3. 次のアラームをセット（ループ）
            scheduleNextUpdate(context)
        }
    }

    /**
     * ウィジェットの表示を更新する処理
     */
    private suspend fun updateWidget(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(VetroWidget::class.java)

        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[VetroWidget.KEY_LAST_UPDATE] = System.currentTimeMillis()
                }
            }
            VetroWidget().update(context, glanceId)
        }
    }

    private fun scheduleNextUpdate(context: Context) {
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

        val now = System.currentTimeMillis()
        // 次の00秒 + わずかなバッファ(50ms)
        val nextMinute = now + 60000 - (now % 60000) + 50

        try {
            // setExactAndAllowWhileIdle: 省電力モード(Doze)でも強制的に起こす最強のメソッド
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextMinute,
                pendingIntent
            )
        } catch (e: SecurityException) {
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
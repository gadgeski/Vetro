// VetroWidgetReceiver.kt
package com.example.vetro.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class VetroWidgetReceiver : GlanceAppWidgetReceiver() {
    // どのウィジェットを表示するかを指定
    override val glanceAppWidget: GlanceAppWidget = VetroWidget()
}
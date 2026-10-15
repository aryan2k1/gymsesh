package com.gymsesh.app.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Broadcast receiver that binds [TodayWidget] to the home screen. Registered in
 * the manifest with the standard `APPWIDGET_UPDATE` intent filter and the
 * `today_widget_info` provider metadata.
 */
class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()
}

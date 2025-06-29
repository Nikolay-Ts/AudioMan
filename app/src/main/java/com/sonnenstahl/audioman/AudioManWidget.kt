package com.sonnenstahl.audioman

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class AudioManWidget : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HomeWidget()
}

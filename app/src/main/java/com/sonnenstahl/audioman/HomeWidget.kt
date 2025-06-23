package com.sonnenstahl.audioman

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.datastore.preferences.core.*
import androidx.glance.state.*


class HomeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long running
        // operations.

        val TITLE_KEY = stringPreferencesKey("title")
        val DESCRIPTION_KEY = stringPreferencesKey("description")
        val IS_PLAYING_KEY = booleanPreferencesKey("isPlaying")
        val COVER_URI_KEY = stringPreferencesKey("coverUri")

        provideContent {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment   = Alignment.Vertical.CenterVertically
            ) {
                Column {
                    Text("Title")
                    Text("Description")
                }
            }
        }
    }
}

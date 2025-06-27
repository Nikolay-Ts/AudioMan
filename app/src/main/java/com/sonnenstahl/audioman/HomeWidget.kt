package com.sonnenstahl.audioman

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
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
import androidx.glance.GlanceNode
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.size
import com.sonnenstahl.audioman.utils.UpdateAudioPlayer

val TITLE_KEY = stringPreferencesKey("title")
val DESCRIPTION_KEY = stringPreferencesKey("description")
val IS_PLAYING_KEY = booleanPreferencesKey("isPlaying")
val COVER_URI_KEY = stringPreferencesKey("coverUri")

class HomeWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val preferences = currentState<Preferences>()
            val title = preferences[TITLE_KEY] ?: "title"
            val isPlaying = preferences[IS_PLAYING_KEY]
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionStartActivity<MainActivity>())
            ) {

                Row(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.default_black),
                        contentDescription = "Cover Image",
                        modifier = GlanceModifier
                            .padding(end = 8.dp)
                            .size(50.dp)
                    )

                    Column(
                        modifier = GlanceModifier.defaultWeight(),
                        horizontalAlignment = Alignment.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title)
                        Text("Description")
                    }

                    val playPauseIcon = if (isPlaying == true) R.drawable.pause else R.drawable.play
                    Image(
                        provider = ImageProvider(playPauseIcon),
                        contentDescription = "Play/Pause",
                        modifier = GlanceModifier
                            .padding(start = 8.dp)
                            .size(50.dp)
                            .clickable(actionRunCallback<UpdateAudioPlayer>())
                    )
                }
            }
        }
    }
}

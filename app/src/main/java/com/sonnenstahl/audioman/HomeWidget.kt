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
import androidx.glance.layout.size
import androidx.glance.state.*
import coil.compose.rememberAsyncImagePainter
import com.sonnenstahl.audioman.utils.AudioPlayer
import com.sonnenstahl.audioman.utils.UpdateAudioPlayer
import java.io.File

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
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                Row(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment   = Alignment.Vertical.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.default_black),
                        contentDescription = "Play/Pause",
                        modifier = GlanceModifier
                            .padding(8.dp)
                            .size(50 "".dp)
                    )
                    Column {
                        Text(title)
                        Text("Description")
                    }
                    if (isPlaying == true) {
                        Image(
                            provider = ImageProvider(R.drawable.pause),
                            contentDescription = "Play/Pause",
                            modifier = GlanceModifier
                                .padding(8.dp)
                                .clickable(actionRunCallback<UpdateAudioPlayer>())
                        )
                    } else {
                        Image(
                            provider = ImageProvider(R.drawable.play),
                            contentDescription = "Play/Pause",
                            modifier = GlanceModifier
                                .padding(8.dp)
                                .clickable(actionRunCallback<UpdateAudioPlayer>())
                        )
                    }
                }
            }
        }
    }
}

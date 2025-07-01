package com.sonnenstahl.audioman

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.*
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import com.sonnenstahl.audioman.utils.UpdateAudioPlayer
import java.io.File // Required for loading images from file paths

val TITLE_KEY = stringPreferencesKey("title")
val DESCRIPTION_KEY = stringPreferencesKey("description")
val IS_PLAYING_KEY = booleanPreferencesKey("isPlaying")
val COVER_URI_KEY = stringPreferencesKey("coverUri")

class HomeWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            val preferences = currentState<Preferences>()
            val title = preferences[TITLE_KEY] ?: "No Sound Selected" // Default title
            val description = preferences[DESCRIPTION_KEY] ?: "Tap to open app" // Default description
            val isPlaying = preferences[IS_PLAYING_KEY] ?: false // Default to not playing
            val coverUriString = preferences[COVER_URI_KEY] // Get the cover URI string

            val playPauseIcon = if (isPlaying) R.drawable.pause else R.drawable.play

            Box(
                modifier =
                    GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionStartActivity<MainActivity>()), // Click widget to open app
            ) {
                Row(
                    modifier =
                        GlanceModifier
                            .fillMaxSize()
                            .background(Color.White) // Adjust widget background color as needed
                            .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start,
                ) {
                    Image(
                        provider = ImageProvider(playPauseIcon), // Use the determined image provider
                        contentDescription = "Cover Image",
                        modifier =
                            GlanceModifier
                                .padding(end = 8.dp)
                                .size(50.dp),
                    )

                    Column(
                        modifier = GlanceModifier.defaultWeight(), // Makes this column take available space
                        horizontalAlignment = Alignment.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(title)
                        Text(description) // Display the description
                    }

                    Image(
                        provider = ImageProvider(playPauseIcon),
                        contentDescription = "Play/Pause",
                        modifier =
                            GlanceModifier
                                .padding(start = 8.dp)
                                .size(50.dp)
                                .clickable(actionRunCallback<UpdateAudioPlayer>()), // Play/Pause button
                    )
                }
            }
        }
    }
}
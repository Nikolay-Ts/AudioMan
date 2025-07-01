package com.sonnenstahl.audioman.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.sonnenstahl.audioman.COVER_URI_KEY
import com.sonnenstahl.audioman.DESCRIPTION_KEY
import com.sonnenstahl.audioman.HomeWidget
import com.sonnenstahl.audioman.IS_PLAYING_KEY
import com.sonnenstahl.audioman.TITLE_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateAudioPlayer : ActionCallback {
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val currentIsPlaying = AudioPlayer.isPlaying()
        val newIsPlaying = currentIsPlaying // The state after this tap
        val currentSound = AudioPlayer.getSound() // Get the currently loaded sound

        if (newIsPlaying) {
            if (currentSound.id == "fallback" || currentSound.audioPath.isEmpty()) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "No sound selected!", android.widget.Toast.LENGTH_SHORT).show()
                }
                updateWidgetPreferences(context, glanceId, currentSound.title, currentSound.description, false, currentSound.imagePath)
                HomeWidget().update(context, glanceId)
                return // Exit without trying to play if no sound
            }
            AudioPlayer.play()
        } else {
            AudioPlayer.pause()
        }

        // 3. Update the widget's preferences with the *current* sound info and *new* playing state
        updateWidgetPreferences(
            context,
            glanceId,
            currentSound.title,         // Use sound's name for title
            currentSound.description,  // Use sound's description
            newIsPlaying,              // The playback state *after* the tap
            currentSound.imagePath      // Use sound's cover URI
        )
        HomeWidget().update(context, glanceId)
    }

    private suspend fun updateWidgetPreferences(
        context: Context,
        glanceId: GlanceId,
        title: String,
        description: String,
        isPlaying: Boolean,
        coverUri: String?
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[TITLE_KEY] = title
            prefs[DESCRIPTION_KEY] = description
            prefs[IS_PLAYING_KEY] = isPlaying
            prefs[COVER_URI_KEY] = coverUri ?: "" // Ensure non-null string for preference
        }
    }
}

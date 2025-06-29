package com.sonnenstahl.audioman.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.sonnenstahl.audioman.HomeWidget
import com.sonnenstahl.audioman.IS_PLAYING_KEY

class UpdateAudioPlayer : ActionCallback {
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        AudioPlayer.initialize(context)
        if (AudioPlayer.isPlaying()) {
            AudioPlayer.pause()
        } else {
            AudioPlayer.play()
        }
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { preferences ->
            preferences.toMutablePreferences().apply {
                this[IS_PLAYING_KEY] = AudioPlayer.isPlaying()
            }
        }
        HomeWidget().update(context, glanceId)
    }
}

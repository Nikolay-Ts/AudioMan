package com.sonnenstahl.audioman.utils

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.sonnenstahl.audioman.HomeWidget
import com.sonnenstahl.audioman.IS_PLAYING_KEY
import com.sonnenstahl.audioman.TITLE_KEY

class UpdateAudioPlayer : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {


        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { preferences ->
            preferences.toMutablePreferences().apply {
                this[IS_PLAYING_KEY]?.let { this[IS_PLAYING_KEY] = ! it }
            }
        }
        HomeWidget().update(context, glanceId)
    }
}
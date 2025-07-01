package com.sonnenstahl.audioman.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow

class AudioService : MediaSessionService() {
    private lateinit var player: ExoPlayer
    private var session: MediaSession? = null
    private lateinit var mediaSessionCompat: MediaSessionCompat

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this).build()

        player = ExoPlayer.Builder(this).build()
        session = MediaSession.Builder(this, player).setId("AudioManSession").build()
        mediaSessionCompat = MediaSessionCompat(this, "AudioManCompatSession")
        mediaSessionCompat.isActive = true

        instance = this
        startForeground(101, buildPlaceholderNotification())

        Log.d("AudioService", "MediaSessionService initialized")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
        super.onDestroy()
        session?.release()
        player.release()
        instance = null
    }

    fun getPlayer(): ExoPlayer = player

    companion object {
        var instance: AudioService? = null
            private set
    }


    private fun buildPlaceholderNotification(): Notification {
        val channelId = "audioman_playback_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    channelId,
                    "Audio Playback",
                    NotificationManager.IMPORTANCE_LOW,
                )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat
            .Builder(this, channelId)
            .setContentTitle("AudioMan Ready")
            .setContentText("Waiting for playback...")
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSessionCompat.sessionToken),
            ).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }
}

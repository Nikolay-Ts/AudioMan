package com.sonnenstahl.audioman.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player

object AudioPlayer {
    @RequiresApi(Build.VERSION_CODES.O)
    fun initialize(context: Context) {
        // Start the MediaSessionService
        val intent = Intent(context, AudioService::class.java)
        context.startForegroundService(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun playAsset(
        context: Context,
        sound: Noise,
    ) {
        initialize(context)

        val mediaItem =
            if (!sound.audioPath.contains("/") && !sound.audioPath.startsWith("content:")) {
                // asset sound
                val assetUri = "asset:///${sound.audioPath}".toUri()
                MediaItem.Builder().setUri(assetUri).build()
            } else {
                // external file (file:// or content://)
                MediaItem.fromUri(sound.audioPath)
            }

        getPlayer()?.apply {
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0.5f
            prepare()
            play()
        }

        this.sound = sound
    }

    fun pause() {
        getPlayer()?.pause()
    }

    fun play() {
        getPlayer()?.play()
    }

    fun isPlaying(): Boolean = getPlayer()?.isPlaying == true

    fun getSound(): Noise = sound ?: fallBackSound

    fun setSound(noise: Noise?) {
        sound = noise
    }

    fun getVolume() = getPlayer()?.volume

    fun setVolume(newVolume: Float) {
        getPlayer()?.volume = newVolume
    }

    fun clearSound() {
        sound = null
    }

    private var sound: Noise? = null

    private fun getPlayer(): Player? = AudioService.instance?.getPlayer()
}

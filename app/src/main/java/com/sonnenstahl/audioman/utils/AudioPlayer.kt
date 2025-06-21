package com.sonnenstahl.audioman.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.core.net.toUri

object AudioPlayer {
    private var sound: Noise? = null

    private fun getPlayer(): Player? {
        return AudioService.instance?.getPlayer()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initialize(context: Context) {
        // Start the MediaSessionService
        val intent = Intent(context, AudioService::class.java)
        context.startForegroundService(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun playAsset(context: Context, sound: Noise) {
        initialize(context)

        val mediaItem = if (sound.audioPath.startsWith("audio_") || sound.audioPath.endsWith(".m4a")) {
            // asset sound
            val assetUri = "asset:///${sound.audioPath}".toUri()
            MediaItem.Builder().setUri(assetUri).build()
        } else {
            // external file
            MediaItem.fromUri(sound.audioPath)
        }

        getPlayer()?.apply {
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ONE
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

    fun isPlaying(): Boolean {
        return getPlayer()?.isPlaying ?: false
    }

    fun getSound(): Noise = sound ?: fallBackSound

    fun setSound(noise: Noise?) {
        sound = noise
    }

    fun clearSound() {
        sound = null
    }
}
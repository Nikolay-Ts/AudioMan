package com.sonnenstahl.audioman.utils

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import android.content.Context
import androidx.media3.common.Player


/**
 * This is a singleton that allows to play the sounds from assets
 * This allows also the import of custom sounds from the user themselves
 * the player uses M3's exoplayer
 */
object AudioPlayer {
    private var exoPlayer: ExoPlayer? = null
    private var sound: Sounds? = null

    fun initialize(context: Context) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
    }

    fun playAsset(context: Context, sound: Sounds) {
        initialize(context)
        val assetUri = "asset:///${sound.audiPath}" // for use with AssetDataSource

        val mediaItem = MediaItem.fromUri(assetUri)
        exoPlayer?.apply {
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
            play()
        }
        this.sound = sound
    }

    fun getSound() = sound ?: fallBackSound
    fun pause() = exoPlayer?.pause()
    fun play() = exoPlayer?.play()
    fun isPlaying() = exoPlayer?.isPlaying ?: false
}
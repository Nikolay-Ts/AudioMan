package com.sonnenstahl.audioman.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.util.MutableFloat
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.sonnenstahl.audioman.HomeWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import androidx.glance.appwidget.updateAll
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.glance.appwidget.updateAll



object AudioPlayer {
    @RequiresApi(Build.VERSION_CODES.O)
    fun initialize(context: Context) {
        // Start the MediaSessionService
        val intent = Intent(context, AudioService::class.java)
        context.startForegroundService(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun playAsset(
        context: Context,
        sound: Noise,
    ) {
        mutex.withLock {
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
            this.soundFlow.value = sound
            this.isPlaying.value = true

            HomeWidget().updateAll(context)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun countDown(
    ) {
        sleepTimerJob?.cancel()
        sleepTimerJob =  timerCoroutine.launch {
            Log.d("TIME-LEFT", "$sleepTimeMilli before loop")
            while (sleepTimeMilli.value != 0L && isActive.value) {
                delay(1000)
                sleepTimeMilli.value -= 1000L

                if (sleepTimeMilli.value < 0) {
                    sleepTimeMilli.value = 0L
                }

                Log.d("TIME-LEFT", "${sleepTimeMilli.value / 1000L}")
            }

            if (sleepTimeMilli.value == 0L) {
                withContext(Dispatchers.Main) {
                    pause()
                }
                mutex.withLock {
                    isActive.value = false
                }
            }
        }
    }

    suspend fun pause() {
        mutex.withLock {
            getPlayer()?.pause()
            isPlaying.value = false
        }
    }

    suspend fun play() {
        mutex.withLock {
            getPlayer()?.play()
            isPlaying.value = true
        }
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
        soundFlow.value = fallBackSound
    }

    suspend fun turnOnTimer(timeMilli: Long) {
        mutex.withLock {
            sleepTimeMilli.value = timeMilli
            isActive.value = true
        }
    }

    fun pauseTimer() {
        isActive.value = false
    }


    private var sound: Noise? = null
    var sleepTimeMilli = MutableStateFlow(-1L)
    var isActive = MutableStateFlow(false)
    var soundFlow = MutableStateFlow(fallBackSound)
    var isPlaying = MutableStateFlow(false)
    private var sleepTimerJob: Job? = null
    private val timerCoroutine = CoroutineScope(Dispatchers.Default + Job())
    private var mutex: Mutex = Mutex()

    private fun getPlayer(): Player? = AudioService.instance?.getPlayer()
}

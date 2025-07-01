package com.sonnenstahl.audioman.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
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
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun countDown(
    ) {
        sleepTimerJob?.cancel()

        Log.d("TIME-LEFT", "$sleepTimeMilli before loop")

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
            Log.d("TIME-LEFT", "jobs done")
            mutex.withLock {
                withContext(Dispatchers.Main) {
                    getPlayer()?.pause()
                }
                isActive.value = false
            }
        }
    }

    suspend fun pause() {
        mutex.withLock {
            getPlayer()?.pause()
        }
    }

    suspend fun play() {
        mutex.withLock {
            getPlayer()?.play()
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
    private var sleepTimeMilli = MutableStateFlow(0L)
    var isActive = MutableStateFlow(false)
    private var sleepTimerJob: Job? = null
    private val timerCoroutine = CoroutineScope(Dispatchers.Default + Job())
    private var mutex: Mutex = Mutex()

    private fun getPlayer(): Player? = AudioService.instance?.getPlayer()
}

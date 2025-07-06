package com.sonnenstahl.audioman.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.sonnenstahl.audioman.HomeWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import androidx.glance.appwidget.updateAll
import androidx.media3.common.MediaItem
import androidx.media3.common.Player

/**
 * @brief a singleton that is the audioPlayer interface and is also the central
 * storage of what is currently being played, the sleep timer and how many miliseconds
 * until it goes off
 */
object AudioPlayer {

    /**
     * @brief initialise the player. Must be called before play, pause, playAsset
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun initialize(context: Context) {
        // Start the MediaSessionService
        val intent = Intent(context, AudioService::class.java)
        context.startForegroundService(intent)
    }

    /**
     * @brief plays the asset. Must be called the first time as it loads the asset in the Media
     * of the M3 player
     */
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

    /**
     * @brief begins the countdown timer by cancelling any older timers and launches a
     * new timer in the Main thread so it persists across all screens
     */
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

    /**
     * @brief pause the current audio when possible
     */
    suspend fun pause() {
        mutex.withLock {
            getPlayer()?.pause()
            isPlaying.value = false
        }
    }

    /**
    * @brief play the current audio when possible
    */
    suspend fun play() {
        mutex.withLock {
            getPlayer()?.play()
            isPlaying.value = true
        }
    }

    /**
     * @brief A but deprecated now because of the isPlaying stateFlow
     * @returns true if the player is play, false otherwise
     */
    fun isPlaying(): Boolean = getPlayer()?.isPlaying == true

    /**
     * @brief gets the current sound or the fallback if nothing is on
     * Again, this is a bit deprecated and might be removed as there is the soundFlow var
     *
     * @return Sound
     */
    fun getSound(): Noise = sound ?: fallBackSound


    /**
     * @brief gets the current volume which is a float 0F..1F
     *
     * @return Float
     */
    fun getVolume() = getPlayer()?.volume

    /**
     * @brief sets the volume of the AudioPlayer
     *
     * @param newVolume Float which should be in 0F..1F
     */
    fun setVolume(newVolume: Float) {
        getPlayer()?.volume = newVolume
    }

    /**
     * @brief sets the current to null and the soundFlow to the fallbackSound
     */
    fun clearSound() {
        sound = null
        soundFlow.value = fallBackSound
    }

    /**
     * @brief begins the timer and sets the timer length
     *
     * @param timeMilli Long the amount of time after which the AudioPlayer is paused
     */
    suspend fun turnOnTimer(timeMilli: Long) {
        mutex.withLock {
            sleepTimeMilli.value = timeMilli
            isActive.value = true
        }
    }

    /**
     * @brief pauses the timer but not the audio
     */
    fun pauseTimer() {
        isActive.value = false
    }

    var sleepTimeMilli = MutableStateFlow(-1L)
    var isActive = MutableStateFlow(false)
    var soundFlow = MutableStateFlow(fallBackSound)
    var isPlaying = MutableStateFlow(false)



    private var sound: Noise? = null
    private var sleepTimerJob: Job? = null
    private val timerCoroutine = CoroutineScope(Dispatchers.Default + Job())
    private var mutex: Mutex = Mutex()
    private fun getPlayer(): Player? = AudioService.instance?.getPlayer()
}

package com.sonnenstahl.audioman.utils

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * @brief the data class that models the noises and tracks
 *
 * @property id a unique id with -1, -2 being reserved for the fallbacksound and customsound
 * @property title of the Noise (must be also unique)
 * @property description optional description
 * @property audioPath to the audio in disk
 * @property imagePath of the image in disk (optional)
 *
 */
@Serializable
data class Noise(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var description: String,
    var audioPath: String = DEFAULT_AUDIO_URI,
    var imagePath: String = DEFAULT_LIGHT_IMAGE,
)

/**
 * @brief placeholder for when you want nothing to be played
 */
val fallBackSound = Noise("-1", "Nothing Selected", "Pick a Sound from the library!", "")

/**
 * a list of of the default sounds that the user can chose from
 */
val defaultSounds =
    listOf(
        Noise("0", "Coffee Shop", "people talking in a coffee shop", "coffee_shop.m4a"),
        Noise("1", "Rain", "rain outside of your window", "rain.m4a"),
        Noise("2", "Forest", "wild forest", "forest.m4a"),
        Noise("3", "Campfire", "sitting by the campfire", "fire.m4a"),
        Noise("4", "City Traffic", "cars honking at each other", "city.m4a"),
    )

/**
 * @brief This data class is to tell exactly which fields are valid and which are not
 *
 * @property title true if valid
 * @property description true if valid
 * @property description true if valid
 * @property audioPath true if valid
 * @property imagePath true if valid
 */
data class ValidNoise(
    var title: Boolean = true,
    var description: Boolean = true,
    var audioPath: Boolean = true,
    var imagePath: Boolean = true,
)

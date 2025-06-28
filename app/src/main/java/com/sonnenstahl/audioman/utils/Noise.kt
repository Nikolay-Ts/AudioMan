package com.sonnenstahl.audioman.utils

import kotlinx.serialization.Serializable
import java.util.UUID

const val DEFAULT_AUDIO_URI: String   = "default.m4a"
const val DEFAULT_IMAGE_URI: String   = "default.png"
const val DEFAULT_LIGHT_IMAGE: String = "default_white.png"

@Serializable
data class Noise(
    val id:          String = UUID.randomUUID().toString(),
    var title:       String,
    var description: String,
    var audioPath:   String = DEFAULT_AUDIO_URI,
    var imagePath:   String = DEFAULT_LIGHT_IMAGE
)

val fallBackSound = Noise("-1", "Nothing Selected", "Pick a Sound from the library!", "")

val defaultSounds = listOf(
    Noise("0", "Coffee Shop" , "people talking in a coffee shop" , "coffee_shop.m4a"),
    Noise("1", "Rain"        , "rain outside of your window"     , "rain.m4a"       ),
    Noise("2", "Forest"      , "wild forest"                     , "forest.m4a"     ),
    Noise("3", "Campfire"    , "sitting by the campfire"         , "fire.m4a"       ),
    Noise("4", "City Traffic", "cars honking at each other"      , "city.m4a"       )
)

/**
 * This data class is to tell exactly
 * which fields are valid and which are not
 */
data class ValidNoise(
    var title:       Boolean = true,
    var description: Boolean = true,
    var audioPath:   Boolean = true,
    var imagePath:   Boolean = true
)
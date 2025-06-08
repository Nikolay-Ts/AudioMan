package com.sonnenstahl.audioman.utils

import kotlinx.serialization.Serializable

@Serializable
data class Sounds(
    var title: String,
    var description: String,
    var filePath: String,
    var imagePath: String = "default.svg"
)

val defaultSounds = listOf(
    Sounds("Coffee Shop", "people talking in a coffee shop" , "coffee_shop"),
    Sounds("Rain"       , "rain outside of your window"     , "rain"),
    Sounds("Forest"     ,  "wild forest"                    , "forest"),
    Sounds("Campfire"   , "sitting by the campfire"         , "fire")
)
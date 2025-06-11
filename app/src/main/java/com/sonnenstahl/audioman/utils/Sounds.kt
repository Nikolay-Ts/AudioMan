package com.sonnenstahl.audioman.utils

import kotlinx.serialization.Serializable

@Serializable
data class Sounds(
    var title: String,
    var description: String,
    var audiPath: String,
    var imagePath: String = "default.svg"
)

val fallBackSound = Sounds("Nothing", "Nothing is player", "")

val defaultSounds = listOf(
    Sounds("Coffee Shop" , "people talking in a coffee shop" , "coffee_shop.m4a"),
    Sounds("Rain"        , "rain outside of your window"     , "rain.m4a"),
    Sounds("Forest"      , "wild forest"                     , "forest.m4a"),
    Sounds("Campfire"    , "sitting by the campfire"         , "fire.m4a"),
    Sounds("City Traffic", "cars honking at each other"      , "city.m4a")
)

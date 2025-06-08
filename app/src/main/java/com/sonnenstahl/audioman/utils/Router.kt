package com.sonnenstahl.audioman.utils

/**
 * This is used to navigate between different screens.
 * If more screens are needed, they should first be added
 */
sealed class Router(val route: String) {
    data object Home        : Router("home")
    data object Sounds      : Router("sounds")
    data object AddSound    : Router("addSounds")
    data object CustomNoise : Router("customNoise")
}

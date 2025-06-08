package com.sonnenstahl.audioman.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryAddCheck
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * This is used to navigate between different screens.
 * If more screens are needed, they should first be added
 */
sealed class Router(val route: String, val label: String, val icon: ImageVector = Icons.AutoMirrored.Filled.LibraryBooks) {
    data object Home        : Router("home", "home", Icons.Outlined.Home)
    data object Sounds      : Router("sounds", "sounds", Icons.Outlined.LibraryAddCheck)
    data object AddSound    : Router("addSounds", "addSounds")
    data object CustomNoise : Router("customNoise", "white noise", Icons.Outlined.Waves)
}

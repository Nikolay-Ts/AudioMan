package com.sonnenstahl.audioman.utils

import kotlinx.serialization.Serializable

@Serializable
data class CustomNoise(
    val noiseType: String,
    val amplitude: Float,
    val spectrum: Float
)
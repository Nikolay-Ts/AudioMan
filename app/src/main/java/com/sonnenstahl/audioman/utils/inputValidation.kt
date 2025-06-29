package com.sonnenstahl.audioman.utils

fun validateNoise(noise: Noise): ValidNoise =
    ValidNoise(
        noise.title != "",
        noise.description != "",
        noise.audioPath != DEFAULT_AUDIO_URI,
        noise.imagePath != DEFAULT_IMAGE_URI,
    )

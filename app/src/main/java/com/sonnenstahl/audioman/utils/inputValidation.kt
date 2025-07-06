package com.sonnenstahl.audioman.utils

/**
 * @brief ensures that the input noises is valid but more checks are perfomed
 *
 * @param noise the current noise inputed by the user
 *
 * @return ValidNoise a data class with bools for each field
 */
fun validateNoise(noise: Noise): ValidNoise =
    ValidNoise(
        noise.title != "",
        noise.description != "",
        noise.audioPath != DEFAULT_AUDIO_URI,
        noise.imagePath != DEFAULT_IMAGE_URI,
    )

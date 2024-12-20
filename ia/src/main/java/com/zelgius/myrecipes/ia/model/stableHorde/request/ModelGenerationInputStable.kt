package com.zelgius.myrecipes.ia.model.stableHorde.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModelGenerationInputStable(
    @SerialName("steps")
    val steps: Int = 50, // Number of steps for the diffusion process

    @SerialName("cfg_scale")
    val cfgScale: Float = 7.0f, // Classifier-free guidance scale

    @SerialName("seed")
    val seed: String? = null, // Seed for randomness, null for random

    @SerialName("width")
    val width: Int = 512, // Width of the output image

    @SerialName("height")
    val height: Int = 512, // Height of the output image

    @SerialName("sampler")
    val sampler: String = "k_lms", // Sampling method

    @SerialName("transparent")
    val transparent: Boolean, // If true, the image will be transparent

    @SerialName("post_processing")
    val postProcessing: List<String> = listOf(),

)
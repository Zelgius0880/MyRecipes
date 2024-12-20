package com.zelgius.myrecipes.ia.model.stableHorde.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ExtraSourceImage(
    @SerialName("image")
    val image: String, // Base64-encoded image

    @SerialName("mask")
    val mask: String? = null, // Optional Base64-encoded mask

    @SerialName("processing")
    val processing: String = "inpainting" // Processing method (e.g., "inpainting", "outpainting")
)

package com.zelgius.myrecipes.ia.model.stableHorde.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestStatusStable(
    @SerialName("finished")
    val finished: Int = 0, // The amount of finished jobs in this request.

    @SerialName("processing")
    val processing: Int = 0, // The amount of still processing jobs in this request.

    @SerialName("restarted")
    val restarted: Int = 0, // The amount of jobs that timed out and were restarted or reported as failed by a worker.

    @SerialName("waiting")
    val waiting: Int = 0, // The amount of jobs waiting to be picked up by a worker.

    @SerialName("done")
    val done: Boolean = false, // True when all jobs in this request are completed.

    @SerialName("faulted")
    val faulted: Boolean = false, // True when this request caused an internal server error.

    @SerialName("wait_time")
    val waitTime: Int = 0, // The expected wait time (in seconds) to generate all jobs in this request.

    @SerialName("queue_position")
    val queuePosition: Int = 0, // The position in the request queue.

    @SerialName("kudos")
    val kudos: Float = 0f, // The total Kudos consumed by this request so far.

    @SerialName("is_possible")
    val isPossible: Boolean = true, // If False, the request cannot be completed with the current worker pool.

    @SerialName("generations")
    val generations: List<GenerationStable>, // List of completed generations for this request.

    @SerialName("shared")
    val shared: Boolean = false // If True, images have been shared with LAION.
)

@Serializable
data class GenerationStable(
    @SerialName("img")
    val img: String, // The generated image as a Base64-encoded .webp file.

    @SerialName("seed")
    val seed: String = "", // The seed used to generate this image.

    @SerialName("id")
    val id: String = "", // The unique ID for this image.

    @SerialName("censored")
    val censored: Boolean = false, // Indicates if the image was censored by a safety filter.

    @SerialName("gen_metadata")
    val genMetadata: List<GenerationMetadataStable> = listOf()// Metadata about the generation.
)

@Serializable
data class GenerationMetadataStable(
    @SerialName("type")
    val type: String = "", // The type of metadata (e.g., "lora", "source_image").

    @SerialName("value")
    val value: String = "", // The value associated with the metadata.

    @SerialName("ref")
    val ref: String? =null // Optional reference for the metadata (e.g., a LoRA ID).
)

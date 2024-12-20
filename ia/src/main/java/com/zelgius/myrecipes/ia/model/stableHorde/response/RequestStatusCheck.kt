package org.example.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestStatusCheck(
    @SerialName("finished")
    val finished: Int, // The amount of finished jobs in this request.

    @SerialName("processing")
    val processing: Int, // The amount of still processing jobs in this request.

    @SerialName("restarted")
    val restarted: Int, // The amount of jobs that timed out and were restarted or reported as failed by a worker.

    @SerialName("waiting")
    val waiting: Int, // The amount of jobs waiting to be picked up by a worker.

    @SerialName("done")
    val done: Boolean, // True when all jobs in this request are completed.

    @SerialName("faulted")
    val faulted: Boolean = false, // True when this request caused an internal server error.

    @SerialName("wait_time")
    val waitTime: Int, // The expected wait time (in seconds) to generate all jobs in this request.

    @SerialName("queue_position")
    val queuePosition: Int, // The position in the request queue.

    @SerialName("kudos")
    val kudos: Float, // The total Kudos consumed by this request so far.

    @SerialName("is_possible")
    val isPossible: Boolean = true // If False, the request cannot be completed with the current worker pool.
)

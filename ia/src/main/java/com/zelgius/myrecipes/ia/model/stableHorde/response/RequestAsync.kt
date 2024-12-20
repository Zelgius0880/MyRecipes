package org.example.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class RequestAsync (
    @SerialName("id")
    val id: String,

    @SerialName("kudos")
    val kudos: Double, // Use Double for number types to handle potential decimals

    @SerialName("message")
    val message: String? = null, // Optional string

    @SerialName("warnings")
    val warnings: List<RequestSingleWarning>? = null // Optional list of warnings
)

@Serializable
data class RequestSingleWarning(
    @SerialName("code")
    val code: WarningCode,

    @SerialName("message")
    val message: String
)

@Serializable
enum class WarningCode {
    @SerialName("NoAvailableWorker")
    NoAvailableWorker,

    @SerialName("ClipSkipMismatch")
    ClipSkipMismatch,

    @SerialName("StepsTooFew")
    StepsTooFew,

    @SerialName("StepsTooMany")
    StepsTooMany,

    @SerialName("CfgScaleMismatch")
    CfgScaleMismatch,

    @SerialName("CfgScaleTooSmall")
    CfgScaleTooSmall,

    @SerialName("CfgScaleTooLarge")
    CfgScaleTooLarge,

    @SerialName("SamplerMismatch")
    SamplerMismatch,

    @SerialName("SchedulerMismatch")
    SchedulerMismatch
}
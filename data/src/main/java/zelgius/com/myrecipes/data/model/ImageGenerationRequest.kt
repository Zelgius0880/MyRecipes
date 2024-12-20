package zelgius.com.myrecipes.data.model

import zelgius.com.myrecipes.data.entities.ImageGenerationRequestEntity

data class ImageGenerationRequest(
    val requestId: String,
    val remainingTime: Long,
    val status: Status,
    val type: Type,
    val fileName: String,
    val entityId: Long,
) {
    enum class Status {
        Waiting,
        Done,
        Processing
    }

    enum class Type {
        Ingredient, Recipe
    }
}


fun ImageGenerationRequest.Status.toEntity(): ImageGenerationRequestEntity.Status =
    when (this) {
        ImageGenerationRequest.Status.Waiting -> ImageGenerationRequestEntity.Status.Waiting
        ImageGenerationRequest.Status.Done -> ImageGenerationRequestEntity.Status.Done
        ImageGenerationRequest.Status.Processing -> ImageGenerationRequestEntity.Status.Processing
    }

fun ImageGenerationRequest.toEntity() = ImageGenerationRequestEntity(
    requestId = requestId,
    remainingTime = remainingTime,
    status = status.toEntity(),
    type = when (type) {
        ImageGenerationRequest.Type.Ingredient -> ImageGenerationRequestEntity.Type.Ingredient
        ImageGenerationRequest.Type.Recipe -> ImageGenerationRequestEntity.Type.Recipe
    },
    fileName = fileName,
    entityId = entityId
)
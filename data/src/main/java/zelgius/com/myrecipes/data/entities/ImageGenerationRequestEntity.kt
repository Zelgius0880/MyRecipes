package zelgius.com.myrecipes.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import zelgius.com.myrecipes.data.model.ImageGenerationRequest

@Entity(tableName = "ImageGenerationRequest", primaryKeys = ["request_id"])
data class ImageGenerationRequestEntity(
    @ColumnInfo(name = "request_id") val requestId: String,
    @ColumnInfo(name = "remaining_time") val remainingTime: Long,
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "entity_id") val entityId: Long,
    val type: Type,
    val status: Status,
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

fun ImageGenerationRequestEntity.Status.toModel(): ImageGenerationRequest.Status =
    when (this) {
        ImageGenerationRequestEntity.Status.Waiting -> ImageGenerationRequest.Status.Waiting
        ImageGenerationRequestEntity.Status.Done -> ImageGenerationRequest.Status.Done
        ImageGenerationRequestEntity.Status.Processing -> ImageGenerationRequest.Status.Processing
    }

fun ImageGenerationRequestEntity.toModel() = ImageGenerationRequest(
    requestId = requestId,
    remainingTime = remainingTime,
    status = status.toModel(),
    type = when (type) {
        ImageGenerationRequestEntity.Type.Ingredient -> ImageGenerationRequest.Type.Ingredient
        ImageGenerationRequestEntity.Type.Recipe -> ImageGenerationRequest.Type.Recipe
    },
    fileName = fileName,
    entityId = entityId,
)

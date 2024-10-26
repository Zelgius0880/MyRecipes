package zelgius.com.myrecipes.data.entities

import androidx.room.ColumnInfo
import zelgius.com.myrecipes.data.model.SimpleIngredient

data class SimpleIngredientEntity(
    val id: Long,
    val name: String,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    val removable: Boolean,
)

fun SimpleIngredientEntity.asModel() = SimpleIngredient(
    id = id,
    name = name,
    imageUrl = imageUrl,
    removable = removable
)
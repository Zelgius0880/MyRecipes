package zelgius.com.myrecipes.data.entities

import androidx.room.ColumnInfo
import zelgius.com.myrecipes.data.model.SimpleIngredient

data class SimpleIngredientEntity(
    val id: Long,
    val name: String,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    val removable: Boolean,
    val seed: Int? = null,
    val prompt: String? = null,
    @ColumnInfo(name = "generation_enabled") val generationEnabled: Boolean = true,
)

fun SimpleIngredientEntity.asModel() = SimpleIngredient(
    id = id,
    name = name,
    imageUrl = imageUrl,
    removable = removable,
    seed = seed,
    prompt = prompt,
    generationEnabled = generationEnabled
)
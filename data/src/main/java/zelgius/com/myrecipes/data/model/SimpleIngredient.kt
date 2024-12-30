package zelgius.com.myrecipes.data.model

import zelgius.com.myrecipes.data.entities.IngredientEntity

data class SimpleIngredient(
    val id: Long,
    val name: String,
    val imageUrl: String?,
    val removable: Boolean,
    val seed: Int? = null,
    val prompt: String? = null,
    val generationEnabled: Boolean = true,
)


fun SimpleIngredient.asIngredient() = Ingredient(
    id = null,
    idIngredient = id,
    name = name,
    quantity = 0.0,
    unit = Ingredient.Unit.Unit,
    imageUrl = imageUrl,
    optional = false,
    sortOrder = 0,
    recipe = null,
    step = null,

    )

fun SimpleIngredient.asEntity() = IngredientEntity(
    id = id,
    name = name,
    imageURL = imageUrl,
    seed = seed,
    prompt = prompt,
    generationEnabled = generationEnabled,
)

enum class PlayRecipeStepPosition {
    Last, First,
}
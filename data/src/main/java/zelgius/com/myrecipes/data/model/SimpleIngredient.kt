package zelgius.com.myrecipes.data.model

data class SimpleIngredient(
    val id: Long,
    val name: String,
    val imageUrl: String?,
    val removable: Boolean,
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
    step = null
)
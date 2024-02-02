package zelgius.com.myrecipes.data.model

import zelgius.com.myrecipes.data.entities.StepEntity

data class Step(
    val id: Long? = null,
    val text: String,
    val order: Int = Int.MAX_VALUE,
    val optional: Boolean = false,
    val recipe: Recipe
)

fun Step.toEntity() = StepEntity(
    id = id,
    text = text,
    order = order,
    optional = optional,
    refRecipe = recipe.id,
)



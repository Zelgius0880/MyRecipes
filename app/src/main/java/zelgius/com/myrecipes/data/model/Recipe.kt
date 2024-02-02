package zelgius.com.myrecipes.data.model

import zelgius.com.myrecipes.data.entities.RecipeEntity

data class Recipe(
    val id: Long? = null,
    val name: String,
    val imageUrl: String? = null,
    val type: Type
) {
    enum class Type {
        Dessert,
        Meal,
        Other
    }
}

fun Recipe.toEntity() = RecipeEntity(
    id = id,
    name = name,
    imageURL = imageUrl,
    type = when (type) {
        Recipe.Type.Dessert -> RecipeEntity.Type.DESSERT
        Recipe.Type.Meal -> RecipeEntity.Type.MEAL
        Recipe.Type.Other -> RecipeEntity.Type.OTHER
    }
)
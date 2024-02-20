package zelgius.com.myrecipes.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import zelgius.com.myrecipes.data.entities.RecipeEntity

@Parcelize
data class Recipe(
    val id: Long? = null,
    val name: String,
    val imageUrl: String? = null,
    val type: Type,
    val steps: List<Step> = mutableListOf(),
    val ingredients: List<Ingredient> = mutableListOf()
) : Parcelable{
    enum class Type {
        Dessert,
        Meal,
        Other
    }
}

fun Recipe.asEntity() = RecipeEntity(
    id = id,
    name = name,
    imageURL = imageUrl,
    type = when (type) {
        Recipe.Type.Dessert -> RecipeEntity.Type.DESSERT
        Recipe.Type.Meal -> RecipeEntity.Type.MEAL
        Recipe.Type.Other -> RecipeEntity.Type.OTHER
    }
)
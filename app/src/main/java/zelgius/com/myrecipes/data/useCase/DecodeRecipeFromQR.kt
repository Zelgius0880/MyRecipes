package zelgius.com.myrecipes.data.useCase

import android.util.Base64
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.entities.asModel
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.utils.unzip
import zelgius.com.protobuff.RecipeProto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DecodeRecipeFromQR  @Inject constructor() {
    fun execute(base64: String): Recipe {
        val bytes = Base64.decode(base64, Base64.NO_PADDING).unzip()
        val proto = RecipeProto.Recipe.parseFrom(bytes)

        val recipe = RecipeEntity(proto)

        recipe.ingredients.forEach {
            it.step = recipe.steps.find { s -> s == it.step }
        }

        val steps = recipe.steps.toList()
        recipe.steps.clear()
        recipe.steps.addAll(steps.map {
            it.copy(
                refRecipe = recipe.id,
            )
        })

        val ingredients = recipe.ingredients.toList()
        recipe.ingredients.clear()

        recipe.ingredients.addAll(ingredients)

        return Recipe(
            name = recipe.name,
            imageUrl = recipe.imageURL,
            type = recipe.type.asModel(),
            steps = steps.map { it.asModel() },
            ingredients = ingredients.map { it.asModel(step = it.step?.asModel()) }
        )
    }
}
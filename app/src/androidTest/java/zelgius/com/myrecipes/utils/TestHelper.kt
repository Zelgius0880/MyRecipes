package zelgius.com.myrecipes.utils

import android.util.Base64
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.entities.asModel
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.protobuff.RecipeProto

object TestHelper {
    fun getFromQr(base64: String): Recipe {
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

        return recipe.asModel()
    }
}
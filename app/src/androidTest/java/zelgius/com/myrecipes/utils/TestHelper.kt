package zelgius.com.myrecipes.utils

import android.util.Base64
import org.junit.Assert
import org.junit.Assert.assertArrayEquals
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
const val DEFAULT_BASE_64 =
    "UEsDBBQACAgIABlxalEAAAAAAAAAAAAAAAAAAAAAjY/PSsNAEIfTpik1osR4UHrQIV40IE3Bqngx\n" +
            "f9CbIAgelY3drkvjJuxu0MfyGXwiH8HdNJD2kODedmZ+33xj21FRZBgStMSHhndg7z/QL5DvGCgj\n" +
            "HM8pZlI4Pe/EPk5KWTWQDgjVl7n6UwZCcloIp+959lGsOIAkTC8D+PmGBBY5h4vgfBbAB2WO6Udb\n" +
            "hn7ha+ha91le8rEz5+gTpRm+mUwWugKG3+YRDK8N/3GFMJzQHVX2p+Jsg1IZKsp/pANTEf2a+BK6\n" +
            "w7iUEvPx3hovrUpgVNujZtZ6KgnaPEDoCvS6DwhrxEituyNE6++uMTAh0G8naOGgJvzeKmG0VANg\n" +
            "ticGKjFrEtsxfUM8zRmSGAbdsasmtvOMGM0yBKsbrU7DP1BLBwg/FjLQIQEAAFgCAABQSwECFAAU\n" +
            "AAgICAAZcWpRPxYy0CEBAABYAgAAAAAAAAAAAAAAAAAAAAAAAAAAUEsFBgAAAAABAAEALgAAAE8B\n" +
            "AAAAAA"

fun Recipe.assertEquals(other: Recipe) {
    // Removing ids to compare with original
    val savedSteps = other.steps.map { step -> step.copy(id = null) }.toTypedArray()
    assertArrayEquals(steps.map { it.copy(id = null) }.toTypedArray(), savedSteps)

    // Removing ids to compare with original
    val savedIngredients =
        other.ingredients.map { i -> i.copy(id = null, step = i.step?.copy(id = null), idIngredient = null) }
            .toTypedArray()
    assertArrayEquals(ingredients.map { i ->
        i.copy(id = null, step = i.step?.copy(id = null), idIngredient = null)
    }.toTypedArray(), savedIngredients)

    Assert.assertEquals(name, other.name)
    Assert.assertEquals(type, other.type)
    Assert.assertEquals(imageUrl, other.imageUrl)
}
package com.zelgius.myrecipes.ia.usecase

import com.google.firebase.Firebase
import com.google.firebase.functions.functions
import com.zelgius.myrecipes.ia.BuildConfig
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONObject
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.data.repository.IngredientRepository
import java.net.URL
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

open class DataExtractionUseCase(
    private val ingredientRepository: IngredientRepository,
) {

    @Serializable
    data class Response(
        val result: Result? = null,
        val error: Error? = null
    )

    @Serializable
    data class Error(
        val message: String,
        val status: Int
    )


    @Serializable
    data class Result(
        val recipe: RecipeResponse,
        val steps: List<StepResponse>,
        val ingredients: List<IngredientResponse>
    ) {

        @Serializable
        data class RecipeResponse(
            val name: String,
            @SerialName("image_url") val imageUrl: String?,
        )

        @Serializable
        data class IngredientResponse(val name: String, val quantity: Double?, val unit: String?)

        @Serializable
        data class StepResponse(val description: String, val ingredients: List<IngredientResponse>)
    }

    protected open val callable = Firebase.functions
        .getHttpsCallableFromUrl(URL(BuildConfig.CLOUD_FUNCTION_URL))

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun execute(bytes: ByteArray, locale: String): kotlin.Result<Recipe> {
        val pdfBase64 = Base64.encode(bytes)

        val response = callable.call(
            mapOf(
                "apiKey" to BuildConfig.CLOUD_FUNCTION_KEY,
                "pdfData" to pdfBase64,
                "name" to "Recipe",
                "locale" to locale
            ),
        )
            .continueWith { task ->
                @Suppress("UNCHECKED_CAST")
                val result = (task.result?.getData() as Map<String, Any>)
                // convert data to json string
                val jsonString = JSONObject(result).toString()

                Json.decodeFromString<Response>(jsonString)
            }.await()

        val result = response.result
        if (result == null) return kotlin.Result.failure(
            DataExtractionException(
                response.error?.status ?: -1, response.error?.message ?: "Unknown"
            )
        )

        val steps = result.steps.mapIndexed { index, step ->
            Step(
                id = null,
                text = step.description,
                order = index + 1,
                recipe = null
            )
        }

        val ingredients = (result.ingredients + result.steps.flatMap { it.ingredients })
            .distinct()
            .mapIndexed { index, i ->
                val step = result.steps.firstOrNull { step ->
                    (step.ingredients.firstOrNull { ingredient ->
                        ingredient.name.lowercase() == i.name.lowercase()
                                && ingredient.quantity == i.quantity
                                && ingredient.unit == i.unit }
                        ?: step.ingredients.firstOrNull { ingredient -> ingredient.name.lowercase() == i.name.lowercase() }) != null
                }?.let {
                    steps.find { step -> it.description == step.text }
                }

                ingredientRepository.getIngredientByName(i.name)?.copy(
                    quantity = i.quantity ?: 1.0,
                    unit = Ingredient.Unit.valueOfOrElse(i.unit ?: "Unit"),
                    sortOrder = index + 1,
                    step = step
                ) ?: Ingredient(
                    id = null,
                    idIngredient = null,
                    name = i.name,
                    quantity = i.quantity ?: 1.0,
                    unit = Ingredient.Unit.valueOfOrElse(i.unit ?: "Unit"),
                    imageUrl = null,
                    optional = false,
                    sortOrder = index + 1,
                    recipe = null,
                    step = step
                )
            }


        return kotlin.Result.success(
            Recipe(
                name = result.recipe.name,
                type = Recipe.Type.Meal,
                imageUrl = result.recipe.imageUrl,
                steps = steps,
                ingredients = ingredients
            )
        )

    }
}

class DataExtractionException(
    val status: Int,
    override val message: String
) : Exception()
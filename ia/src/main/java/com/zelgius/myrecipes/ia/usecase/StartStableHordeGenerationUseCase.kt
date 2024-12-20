package com.zelgius.myrecipes.ia.usecase

import android.content.Context
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import com.google.firebase.functions.dagger.assisted.AssistedFactory
import com.zelgius.myrecipes.ia.R
import com.zelgius.myrecipes.ia.model.stableHorde.request.GenerationInputStable
import com.zelgius.myrecipes.ia.model.stableHorde.request.ModelGenerationInputStable
import com.zelgius.myrecipes.ia.repository.StableHordeRepository
import com.zelgius.myrecipes.ia.utils.save
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import org.example.model.response.RequestAsync
import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.model.ImageGenerationRequest
import zelgius.com.myrecipes.data.repository.RemoteConfigRepository
import zelgius.com.myrecipes.data.repository.dao.IngredientDao
import zelgius.com.myrecipes.data.repository.dao.RecipeDao
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random

class StartStableHordeGenerationUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val stableHordeRepository: StableHordeRepository,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val stableHordeGenerationStatusUseCase: StableHordeStatusUseCase
) {
    suspend fun execute(): ImageGenerationRequest? = coroutineScope {
        val recipes = recipeDao.getAllWithoutImages().filter { it.generationEnabled }
        val ingredients = ingredientDao.getAllWithoutImages().filter { it.generationEnabled }

        val recipe = recipes.firstOrNull()
        val ingredient = ingredients.firstOrNull()

        var fileName = ""
        var entityId = 0L
        val request = if (recipe != null) {
            val translatedName = FrenchToEnglishUseCase().execute(recipe.prompt?: recipe.name)
            fileName = "R_${recipe.id}"
            entityId = recipe.id ?: 0L
            generate(RecipeInfo(context, recipe, translatedName))
        } else if (ingredient != null) {
            val translatedName = FrenchToEnglishUseCase().execute(ingredient.prompt?: ingredient.name)
            fileName = "I_${ingredient.id}"
            entityId =ingredient.id ?: 0L
            generate(IngredientInfo(context, ingredient, translatedName))
        } else {
            null
        }

        request?.let {
            stableHordeGenerationStatusUseCase.execute(
                ImageGenerationRequest(
                    requestId = it.id,
                    type = if (recipe != null) ImageGenerationRequest.Type.Recipe
                    else ImageGenerationRequest.Type.Ingredient,
                    status = ImageGenerationRequest.Status.Waiting,
                    fileName = fileName,
                    remainingTime = Long.MAX_VALUE,
                    entityId = entityId
                )
            )
        }
    }

    private suspend fun generate(info: GenerationInfo): RequestAsync {
        return stableHordeRepository.postGenerateAsync(
            apiKey = remoteConfigRepository.stableHordeKey.first(),
            generationInput = GenerationInputStable(
                prompt = info.prompt,
                style = info.style?.id,
                params = ModelGenerationInputStable(
                    transparent = info.transparent,
                    seed = "${info.seed ?: abs(Random.nextInt())}",
                    postProcessing = listOf("strip_background")
                )
            )
        )
    }

    @AssistedFactory
    interface Factory {
        fun create(notificationBuilder: NotificationCompat.Builder): StartStableHordeGenerationUseCase
    }


    sealed class GenerationInfo(
        val name: String,
        val prompt: String,
        val seed: Int? = null,
        val style: StableHordeStyle?,
        val transparent: Boolean = false
    ) {
        abstract suspend fun save(bitmap: Bitmap)
    }

    inner class RecipeInfo(
        private val context: Context,
        private val recipe: RecipeEntity,
        translatedName: String
    ) :
        GenerationInfo(
            name = recipe.name,
            prompt = context.getString(
                R.string.recipe_prompt_generation,
                translatedName.lowercase()
            ),
            seed = recipe.seed,
            style = null,
            transparent = false
        ) {
        override suspend fun save(bitmap: Bitmap) {
            val fileName = context.save(bitmap, "R_${recipe.id}")
            recipeDao.update(recipe.copy(imageURL = fileName, seed = seed))
        }
    }

    inner class IngredientInfo(
        private val context: Context,
        private val ingredient: IngredientEntity,
        translatedName: String,
    ) : GenerationInfo(
        name = ingredient.name,
        prompt = context.getString(
            R.string.ingredient_prompt_generation,
            translatedName.lowercase()
        ),
        seed = ingredient.seed,
        style = null,
        transparent = true
    ) {
        override suspend fun save(bitmap: Bitmap) {
            val fileName = context.save(bitmap, "I_${ingredient.id}")
            ingredientDao.update(ingredient.copy(imageURL = fileName))
        }
    }

    companion object {
        const val NOTIFICATION_ID = 42
    }

}
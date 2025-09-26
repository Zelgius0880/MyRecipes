package com.zelgius.myrecipes.ia.usecase

import android.content.Context
import com.zelgius.myrecipes.ia.repository.StableHordeRepository
import com.zelgius.myrecipes.ia.utils.save
import dagger.hilt.android.qualifiers.ApplicationContext
import zelgius.com.myrecipes.data.model.ImageGenerationRequest
import zelgius.com.myrecipes.data.repository.IngredientRepository
import zelgius.com.myrecipes.data.repository.RecipeRepository
import javax.inject.Inject

class StableHordeDownloadUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val stableHordeRepository: StableHordeRepository,
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
) {
    suspend fun execute(imageGenerationRequest: ImageGenerationRequest) {
        val status = stableHordeRepository.getRequestStatus(imageGenerationRequest.requestId)
        val bitmap = status.generations.firstOrNull()?.img?.let {
            stableHordeRepository.downloadImage(it)
        }

        bitmap?.let {
            val url = context.save(it, imageGenerationRequest.fileName)
            when (imageGenerationRequest.type) {
                ImageGenerationRequest.Type.Recipe -> recipeRepository.updateUrlImage(
                    imageGenerationRequest.entityId,
                    url
                )

                ImageGenerationRequest.Type.Ingredient -> ingredientRepository.updateUrlImage(
                    imageGenerationRequest.entityId,
                    url
                )
            }
        }
    }

}
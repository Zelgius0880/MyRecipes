package zelgius.com.myrecipes.data.useCase

import androidx.room.withTransaction
import zelgius.com.myrecipes.data.AppDatabase
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.repository.IngredientRepository
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.repository.StepRepository
import javax.inject.Inject

class DeleteRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val stepRepository: StepRepository,
    private val ingredientRepository: IngredientRepository,
    private val database: AppDatabase,
) {
    suspend fun execute(recipe: Recipe) = database.withTransaction {

        ingredientRepository.delete(recipe)

        stepRepository.delete(recipe)

        recipeRepository.delete(recipe)
    }
}
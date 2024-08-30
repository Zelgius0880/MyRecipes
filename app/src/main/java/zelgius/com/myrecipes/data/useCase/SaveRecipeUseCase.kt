package zelgius.com.myrecipes.data.useCase

import androidx.room.withTransaction
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.repository.AppDatabase
import zelgius.com.myrecipes.data.repository.IngredientRepository
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.repository.StepRepository
import javax.inject.Inject

class SaveRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val stepRepository: StepRepository,
    private val ingredientRepository: IngredientRepository,
    private val database: AppDatabase,
) {
    suspend fun execute(recipe: Recipe): Long = database.withTransaction {
        val id = if (recipe.id == null)
            recipeRepository.insert(recipe)
        else {
            recipeRepository.update(recipe)
            recipe.id!!
        }

        recipe.steps.forEach {
            val step = it.copy(recipe = recipe)

            if (it.id == null)
                stepRepository.insert(step)
            else stepRepository.update(step)
        }

        recipe.ingredients.forEach {
            val ingredient = it.copy(
                recipe = recipe,
            )

            if (it.id == null)
                ingredientRepository.insert(ingredient, recipe)
            else
                ingredientRepository.update(ingredient)
        }

        ingredientRepository.deleteAllButThem(recipe, recipe.ingredients)
        stepRepository.deleteAllButThem(recipe, recipe.steps)

        id
    }
}
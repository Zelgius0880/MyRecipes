package zelgius.com.myrecipes.data.useCase

import androidx.room.withTransaction
import zelgius.com.myrecipes.data.AppDatabase
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.repository.IngredientRepository
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.repository.StepRepository
import zelgius.com.myrecipes.worker.WorkerRepository
import javax.inject.Inject

class SaveRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val stepRepository: StepRepository,
    private val ingredientRepository: IngredientRepository,
    private val workRepository: WorkerRepository,
    private val database: AppDatabase,
) {
    suspend fun execute(toSave: Recipe): Long = database.withTransaction {
        var recipe = if (toSave.id == null)
            toSave.copy(id = recipeRepository.insert(toSave))
        else {
            recipeRepository.update(toSave)
            toSave
        }.copy(ingredients = toSave.ingredients.mapIndexed { index, i -> i.copy(sortOrder = index + 1) })

        val recipeId = recipe.id?: return@withTransaction -1L

        val ingredients = mutableListOf<Ingredient>()

        recipe = recipe.copy(steps = recipe.steps.mapIndexed {index, s ->
            val stepIngredients = recipe.ingredients.filter { i -> i.step == s }

            val step = s.copy(recipe = recipe, order = index + 1)

            val inserted = if (s.id == null)
                step.copy(id = stepRepository.insert(step))
            else {
                stepRepository.update(step)
                step
            }

            ingredients.addAll(
                stepIngredients.map { i ->
                    insertIngredient(i.copy(step = inserted, recipe = recipe), recipe)
                }
            )

            inserted
        })

        ingredients.addAll(recipe.ingredients.filter { it.step == null }.map {
            insertIngredient(it, recipe)
        })

        ingredientRepository.deleteAllButThem(ingredients, recipeId)
        stepRepository.deleteAllButThem(recipe, recipe.steps)

        workRepository.startIaGenerationWorker(true)

        recipeId
    }

    private suspend fun insertIngredient(
        ingredient: Ingredient,
        recipe: Recipe
    ): Ingredient {
        ingredient.copy(recipe = recipe).let {
            return if (it.idIngredient == null)
                ingredientRepository.insert(it, recipe)
            else {
                ingredientRepository.update(it)
            }
        }
    }
}
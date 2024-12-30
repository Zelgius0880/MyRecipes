package zelgius.com.myrecipes.data.useCase

import kotlinx.coroutines.flow.first
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.PlayRecipeStepPosition
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import zelgius.com.myrecipes.data.repository.RecipeRepository
import javax.inject.Inject

class GetInstructionsUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val dataStoreRepository: DataStoreRepository
) {
    suspend fun execute(id: Long): List<InstructionItem> {
        val recipe = recipeRepository.getFull(id) ?: return emptyList()

        return recipe.instructions(dataStoreRepository.playRecipeStepPosition.first())
    }


}

fun Recipe.instructions(stepPosition: PlayRecipeStepPosition): List<InstructionItem> = buildList {
    val remainingIngredients = ingredients.toMutableList()

    steps.forEach { step ->
        val stepIngredients = remainingIngredients.filter { it.step == step }

        if (stepPosition == PlayRecipeStepPosition.Last) {
            addAll(stepIngredients.map { IngredientInstruction(it) })
            add(StepInstruction(step))
        } else {
            add(StepInstruction(step))
            addAll(stepIngredients.map { IngredientInstruction(it) })
        }

        remainingIngredients.removeAll(stepIngredients)
    }

    if (remainingIngredients.size == ingredients.size) {
        addAll(0, remainingIngredients.map { IngredientInstruction(it) })
    } else if (remainingIngredients.isNotEmpty() && isNotEmpty()) {
        addAll(this.lastIndex - 1, remainingIngredients.map { IngredientInstruction(it) })
    }
}

sealed interface InstructionItem
data class IngredientInstruction(val ingredient: Ingredient) : InstructionItem
data class StepInstruction(val step: Step) : InstructionItem
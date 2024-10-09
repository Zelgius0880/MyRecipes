package zelgius.com.myrecipes.data.useCase

import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.data.repository.RecipeRepository

class GetInstructionsUseCase(private val recipeRepository: RecipeRepository) {
    suspend fun execute(id: Long): List<InstructionItem> {
        val recipe = recipeRepository.getFull(id) ?: return emptyList()

        return recipe.instructions
    }


}

val Recipe.instructions: List<InstructionItem>
    get() = buildList {
        val remainingIngredients = ingredients.toMutableList()

        steps.forEach { step ->
            val stepIngredients = remainingIngredients.filter { it.step == step }
            addAll(stepIngredients.map { IngredientInstruction(it) })
            add(StepInstruction(step))

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
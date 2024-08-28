package zelgius.com.myrecipes.ui.edit.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.data.RecipeRepository
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step

@HiltViewModel(assistedFactory = EditRecipeViewModel.Factory::class)
class EditRecipeViewModel @AssistedInject constructor(
    @Assisted recipe: Recipe,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _recipeFlow = MutableStateFlow(recipe)

    val recipeFlow
        get() = _recipeFlow.asStateFlow()

    val itemsFlow
        get() = _recipeFlow
            .map {
                it.ingredients.mapIndexed { index, item ->
                    IngredientItem(
                        item,
                        isFirst = index == 0,
                        isLast = index == it.ingredients.lastIndex
                    )
                } + AddIngredient + it.steps.map { s ->
                    StepItem(
                        s,
                        it.ingredients.filter { i -> i.step == s })
                } + AddStep
            }

    init {
        viewModelScope.launch {
            val id = recipe.id ?: return@launch
            recipeRepository.getFull(id)?.let {
                _recipeFlow.value = it
            }
        }
    }

    fun changeName(name: String) {
        _recipeFlow.value = _recipeFlow.value.copy(name = name)
    }

    fun changeImageUrl(imageUrl: String) {
        _recipeFlow.value = _recipeFlow.value.copy(imageUrl = imageUrl)
    }

    fun addStep(step: Step) {
        val recipe = _recipeFlow.value
        _recipeFlow.value =
            recipe.copy(steps = recipe.steps + step.copy(order = recipe.steps.size + 1))
    }

    fun updateStep(old: StepItem, newStep: StepItem) {
        val recipe = _recipeFlow.value

        val steps = recipe.steps.toMutableList()
        val index = steps.indexOf(old.step)
        if (index >= 0) {
            steps[index] = newStep.step.copy(order = index + 1)

            val ingredients = recipe.ingredients.toMutableList()
            ingredients.updateIngredients(old.ingredients, null)
            ingredients.updateIngredients(newStep.ingredients, newStep.step)
            _recipeFlow.value = recipe.copy(ingredients = ingredients, steps = steps)
        }
    }

    fun deleteStep(step: Step) {
        val recipe = _recipeFlow.value
        val steps = recipe.steps.toMutableList()
        steps.remove(step)
        _recipeFlow.value = recipe.copy(steps = steps)
    }


    fun addIngredient(ingredient: Ingredient) {
        val recipe = _recipeFlow.value
        _recipeFlow.value = recipe.copy(ingredients = recipe.ingredients + ingredient)
    }

    fun updateIngredient(old: Ingredient, new: Ingredient) {
        val recipe = _recipeFlow.value

        val ingredients = recipe.ingredients.toMutableList()
        val index = ingredients.indexOf(old)
        if (index >= 0) {
            ingredients[index] = new
            _recipeFlow.value = recipe.copy(ingredients = ingredients)
        }
    }

    fun deleteIngredient(ingredient: Ingredient) {
        val recipe = _recipeFlow.value
        val ingredients = recipe.ingredients.toMutableList()
        ingredients.remove(ingredient)
        _recipeFlow.value = recipe.copy(
            ingredients = ingredients,
            steps = recipe.steps
        )
    }


    private fun MutableList<Ingredient>.updateIngredients(ingredients: List<Ingredient>, step: Step?) {
        forEachIndexed { index, ingredient ->
            val i = ingredients.find { it.id == ingredient.id }
            if (i != null) {
                this[index] = i.copy(step = step)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(recipe: Recipe): EditRecipeViewModel
    }
}

sealed interface ListItem
data class StepItem(val step: Step, val ingredients: List<Ingredient> = emptyList()) : ListItem
data class IngredientItem(
    val ingredient: Ingredient,
    val isFirst: Boolean,
    val isLast: Boolean
) : ListItem

data object AddIngredient : ListItem
data object AddStep : ListItem

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
        get() = _recipeFlow.asStateFlow()
            .map {
                it.ingredients.mapIndexed { index, item ->
                    IngredientItem(
                        item,
                        isFirst = index == 0,
                        isLast = index == it.ingredients.lastIndex
                    )
                } + AddIngredient + it.steps.map { s -> StepItem(s) } + AddStep
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


    @AssistedFactory
    interface Factory {
        fun create(recipe: Recipe): EditRecipeViewModel
    }
}

sealed interface ListItem
data class StepItem(val step: Step) : ListItem
data class IngredientItem(
    val ingredient: Ingredient,
    val isFirst: Boolean,
    val isLast: Boolean
) : ListItem

data object AddIngredient : ListItem
data object AddStep : ListItem

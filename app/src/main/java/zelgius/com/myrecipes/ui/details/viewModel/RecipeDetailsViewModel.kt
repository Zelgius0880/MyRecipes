package zelgius.com.myrecipes.ui.details.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.data.RecipeRepository
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step

@HiltViewModel(assistedFactory = RecipeDetailsViewModel.Factory::class)
class RecipeDetailsViewModel @AssistedInject constructor(
    @Assisted recipe: Recipe,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _recipeFlow = MutableStateFlow(recipe)

    val recipeFlow
        get() = _recipeFlow.asStateFlow()

    val itemsFlow
        get() = _recipeFlow.asStateFlow()
            .map {
                listOf(Step(text= "", ingredients = it.ingredients, recipe = it)) + it.steps
            }

    init {
        viewModelScope.launch {
            val id = recipe.id ?: return@launch
            recipeRepository.getFull(id)?.let {
                _recipeFlow.value = it
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(recipe: Recipe): RecipeDetailsViewModel
    }
}

sealed interface ListItem
data class StepItem(val step: Step) : ListItem
data class AllIngredientsItem(val ingredients: List<Ingredient>) : ListItem
package zelgius.com.myrecipes.ui.details.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.ui.edit.viewModel.StepItem

@HiltViewModel(assistedFactory = RecipeDetailsViewModel.Factory::class)
class RecipeDetailsViewModel @AssistedInject constructor(
    @Assisted private val recipe: Recipe,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    val recipeFlow
        get() = recipeRepository.getFullFlow(recipe.id!!).filterNotNull()

    val itemsFlow
        get() = recipeFlow
            .map {
                (listOf(Step(text = "", recipe = it)) + it.steps)
                    .mapIndexed { index, item ->
                        if (index == 0) StepItem(item, ingredients = it.ingredients)
                        else StepItem(item, it.ingredients.filter { i -> i.step ==  item})
                    }
            }

    @AssistedFactory
    interface Factory {
        fun create(recipe: Recipe): RecipeDetailsViewModel
    }
}

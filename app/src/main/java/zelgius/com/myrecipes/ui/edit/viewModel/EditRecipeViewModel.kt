package zelgius.com.myrecipes.ui.edit.viewModel

import android.content.Context
import android.webkit.URLUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.useCase.SaveRecipeUseCase
import zelgius.com.myrecipes.worker.DownloadImageWorker

@HiltViewModel(assistedFactory = EditRecipeViewModel.Factory::class)
class EditRecipeViewModel @AssistedInject constructor(
    @Assisted recipe: Recipe,
    private val recipeRepository: RecipeRepository,
    private val saveRecipeUseCase: SaveRecipeUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _recipeFlow = MutableStateFlow(recipe)
    val addFromWeb = recipe.name.isEmpty()

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
                } + AddIngredient(it.ingredients.isEmpty()) + it.steps.map { s ->
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

    fun load(recipe: Recipe) {
        _recipeFlow.value = recipe
    }

    fun changeName(name: String) {
        _recipeFlow.value = _recipeFlow.value.copy(name = name)
    }

    fun changeImageUrl(imageUrl: String) {
        _recipeFlow.value = _recipeFlow.value.copy(imageUrl = imageUrl)
    }

    fun changeType(type: Recipe.Type) {
        _recipeFlow.value = _recipeFlow.value.copy(type = type)
    }

    fun addStep(newStep: StepItem) {
        _recipeFlow.value.let {
            val step = newStep.step.copy(order = it.steps.size + 1)
            val recipe = it.copy(steps = (it.steps + step).mapIndexed { i, s ->
                s.copy(order = i + 1)
            })

            val ingredients = recipe.ingredients.toMutableList()
            ingredients.updateIngredients(newStep.ingredients, step)

            _recipeFlow.value =
                recipe.copy(ingredients = ingredients)
        }
    }

    fun updateStep(old: StepItem, newStep: StepItem) {
        val recipe = _recipeFlow.value

        val steps = recipe.steps.toMutableList()
        val index = steps.indexOf(old.step)
        if (index >= 0) {
            steps[index] = newStep.step.copy(order = index + 1)

            val ingredients = recipe.ingredients.toMutableList()
            ingredients.updateIngredients(old.ingredients, null)
            ingredients.updateIngredients(
                newStep.ingredients,
                newStep.step
            )
            _recipeFlow.value =
                recipe.copy(ingredients = ingredients, steps = steps.mapIndexed { i, step ->
                    step.copy(order = i + 1)
                })
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
        _recipeFlow.value =
            recipe.copy(ingredients = (recipe.ingredients + ingredient).mapIndexed { index, i ->
                i.copy(sortOrder = index + 1)
            })
    }

    fun updateIngredient(old: Ingredient, new: Ingredient) {
        val recipe = _recipeFlow.value

        val ingredients = recipe.ingredients.toMutableList()
        val index = ingredients.indexOf(old)
        if (index >= 0) {
            ingredients[index] = new
            _recipeFlow.value = recipe.copy(ingredients = ingredients.mapIndexed { i, ingredient ->
                ingredient.copy(sortOrder = i + 1)
            })
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

    fun save() {
        viewModelScope.launch {
            val recipe = recipeFlow.value
            saveRecipeUseCase.execute(toSave = recipe)

            recipe.imageUrl?.takeIf { URLUtil.isHttpUrl(it) || URLUtil.isHttpsUrl(it) }?.let {
                val worker = OneTimeWorkRequestBuilder<DownloadImageWorker>()
                    .setInputData(
                        Data.Builder()
                            .putString("URL", recipe.imageUrl)
                            .putLong("ID", recipe.id ?: 0L)
                            .build()
                    )
                    .setConstraints(Constraints.NONE)
                    .build()

                WorkManager
                    .getInstance(context)
                    .enqueue(worker)
            }

        }
    }

    private fun MutableList<Ingredient>.updateIngredients(
        ingredients: List<Ingredient>,
        step: Step?
    ) {
        map { it.copy(step = null) }
            .forEachIndexed { index, ingredient ->
                val i = ingredients
                    .map { it.copy(step = null) }
                    .find { it == ingredient }
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

data class AddIngredient(val isFirst: Boolean) : ListItem
data object AddStep : ListItem

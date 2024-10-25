package zelgius.com.myrecipes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.useCase.DecodeRecipeFromQR
import zelgius.com.myrecipes.data.useCase.DeleteRecipeUseCase
import zelgius.com.myrecipes.data.useCase.SaveRecipeUseCase
import javax.inject.Inject

@HiltViewModel
open class HomeViewModel @Inject constructor(
    private val saveRecipeUseCase: SaveRecipeUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase,
    private val decodeRecipeFromQR: DecodeRecipeFromQR,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    suspend fun addRecipeFromQr(base64: String) {
        viewModelScope.launch {
            val recipe = decodeRecipeFromQR.execute(base64)
            saveRecipeUseCase.execute(recipe)
        }
    }

    fun removeRecipe(recipe: Recipe) = flow {
        val backup = recipeRepository.getFull(recipe.id ?: return@flow) ?: return@flow
        deleteRecipeUseCase.execute(recipe)
        emit(backup)
    }

    fun restoreRecipe(recipe: Recipe) {
        viewModelScope.launch {
            saveRecipeUseCase.execute(
                with(recipe.copy(id = null)) {
                    copy(
                        ingredients = recipe.ingredients.map {
                            it.copy(
                                id = null,
                                step = it.step?.copy(id = null)
                            )
                        },
                        steps = recipe.steps.map {
                            it.copy(
                                id = null,
                            )
                        }
                    )
                },
            )
        }
    }

    private val pageConfig = PagingConfig(
        pageSize = 10, // how many to load in each page
        prefetchDistance = 3, // how far from the end before we should load more; defaults to page size
        initialLoadSize = 10, // how many items should we initially load; defaults to 3x page size
    )


    val mealsPage = Pager(config = pageConfig) { recipeRepository.pagedMeal() }.flow
    val dessertPage = Pager(config = pageConfig) { recipeRepository.pagedDessert() }.flow
    val otherPage = Pager(config = pageConfig) { recipeRepository.pagedOther() }.flow


}
package zelgius.com.myrecipes.ui.home

import android.content.Context
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.entities.asModel
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.useCase.DeleteRecipeUseCase
import zelgius.com.myrecipes.data.useCase.SaveRecipeUseCase
import zelgius.com.myrecipes.utils.unzip
import zelgius.com.myrecipes.worker.InsertDefaultDataWorker
import zelgius.com.protobuff.RecipeProto
import javax.inject.Inject

@HiltViewModel
open class HomeViewModel @Inject constructor(
    private val saveRecipeUseCase: SaveRecipeUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase,
    private val recipeRepository: RecipeRepository,
    @ApplicationContext context: Context
) : ViewModel() {
    init {
        viewModelScope.launch {
            val status = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkFlow("insert_create")
                .first()

            if (status.isEmpty() || status.first().state == WorkInfo.State.FAILED) {
                val worker = OneTimeWorkRequestBuilder<InsertDefaultDataWorker>()
                    .setConstraints(Constraints.NONE)
                    .build()

                WorkManager
                    .getInstance(context)
                    .enqueueUniqueWork("insert_create", ExistingWorkPolicy.REPLACE, worker)
            }
        }

    }

    fun addRecipeFromQr(base64: String) {
        viewModelScope.launch {
            val bytes = Base64.decode(base64, Base64.NO_PADDING).unzip()
            val proto = RecipeProto.Recipe.parseFrom(bytes)

            val recipe = RecipeEntity(proto)

            recipe.ingredients.forEach {
                it.step = recipe.steps.find { s -> s == it.step }
            }

            val steps = recipe.steps.toList()
            recipe.steps.clear()
            recipe.steps.addAll(steps.map {
                it.copy(
                    refRecipe = recipe.id,
                )
            })

            val ingredients = recipe.ingredients.toList()
            recipe.ingredients.clear()

            recipe.ingredients.addAll(ingredients)

            saveRecipeUseCase.execute(recipe.asModel())
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
                                recipe = this
                            )
                        },
                        steps = recipe.steps.map {
                            it.copy(
                                id = null,
                                recipe = this
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
package zelgius.com.myrecipes.ui.home

import android.content.Context
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
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.worker.InsertDefaultDataWorker
import javax.inject.Inject

@HiltViewModel
open class HomeViewModel @Inject constructor(
    @ApplicationContext context: Context,
    recipeRepository: RecipeRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            val status = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkFlow("insert_create")
                .first()

            if(status.isEmpty() || status.first().state == WorkInfo.State.FAILED) {
                val worker = OneTimeWorkRequestBuilder<InsertDefaultDataWorker>()
                    .setConstraints(Constraints.NONE)
                    .build()

                WorkManager
                    .getInstance(context)
                    .enqueueUniqueWork("insert_create", ExistingWorkPolicy.REPLACE, worker)
            }
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
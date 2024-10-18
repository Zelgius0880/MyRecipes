package zelgius.com.myrecipes.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStoreRepository: DataStoreRepository
) {


    suspend fun startIaGenerationWorker(resetStatus: Boolean = false) {
        val isIaGenerationEnabled = dataStoreRepository.isIAGenerationChecked.first()
        val stillNeedToGenerate = if(resetStatus) {
            dataStoreRepository.setStillNeedToGenerate(true)
            true
        } else dataStoreRepository.stillNeedToGenerate.first()

        if (!isIaGenerationEnabled || !stillNeedToGenerate || !ImageGenerationWorker.modelExists) return

        val worker = OneTimeWorkRequestBuilder<ImageGenerationWorker>()
            .setInputData(
                Data.Builder().build()
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresCharging(true)
                    .setRequiresDeviceIdle(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).apply {
            cancelAllWork()
            enqueue(worker)
        }
    }
}
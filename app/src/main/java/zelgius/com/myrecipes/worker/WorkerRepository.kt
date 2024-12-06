package zelgius.com.myrecipes.worker

import android.content.Context
import androidx.work.Constraints
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

        if (!isIaGenerationEnabled || !stillNeedToGenerate || !com.zelgius.myrecipes.ia.worker.ImageGenerationWorker.Companion.modelExists) return

        val worker = OneTimeWorkRequestBuilder<com.zelgius.myrecipes.ia.worker.ImageGenerationWorker>()
            .addTag(com.zelgius.myrecipes.ia.worker.ImageGenerationWorker.Companion.TAG)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresCharging(true)
                    .setRequiresDeviceIdle(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).apply {
            cancelAllWorkByTag(com.zelgius.myrecipes.ia.worker.ImageGenerationWorker.Companion.TAG)
            enqueue(worker)
        }
    }

    fun startIaGenerationImmediately() {
        val worker = OneTimeWorkRequestBuilder<com.zelgius.myrecipes.ia.worker.ImageGenerationWorker>()
            .addTag(com.zelgius.myrecipes.ia.worker.ImageGenerationWorker.Companion.TAG)
            .build()

        WorkManager.getInstance(context).apply {
            cancelAllWorkByTag(com.zelgius.myrecipes.ia.worker.ImageGenerationWorker.Companion.TAG)
            enqueue(worker)
        }
    }
}
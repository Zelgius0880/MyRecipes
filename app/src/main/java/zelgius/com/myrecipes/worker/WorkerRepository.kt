package zelgius.com.myrecipes.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import zelgius.com.myrecipes.data.model.ImageGenerationRequest
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import zelgius.com.myrecipes.data.repository.ImageGenerationRequestRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStoreRepository: DataStoreRepository,
    private val imageGenerationProgressRepository: ImageGenerationRequestRepository,
    private val checkMissingImageUseCase: CheckMissingImageUseCase,
) {

    suspend fun startOrScheduleIaGenerationWorker(
        pendingRequest: ImageGenerationRequest? = null,
        resetStatus: Boolean = false
    ) {
        val isIaGenerationEnabled = dataStoreRepository.isIAGenerationChecked.first()
        val stillNeedToGenerate = if (resetStatus) {
            dataStoreRepository.setStillNeedToGenerate(true)
            true
        } else dataStoreRepository.stillNeedToGenerate.first()

        if (!isIaGenerationEnabled || !stillNeedToGenerate) return

        if (pendingRequest == null
            || pendingRequest.status == ImageGenerationRequest.Status.Waiting
            || pendingRequest.status == ImageGenerationRequest.Status.Processing
        ) {
            val worker = OneTimeWorkRequestBuilder<ImageGenerationWorker>()
                .addTag(ImageGenerationWorker.TAG)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                ).let {
                    if (pendingRequest != null) {
                        it.setInitialDelay(
                            (pendingRequest.remainingTime).coerceAtLeast(60),
                            TimeUnit.SECONDS
                        )
                    } else it
                }
                .build()

            pendingRequest?.let {
                imageGenerationProgressRepository.insert(it)
            }

            WorkManager.getInstance(context).
                enqueueUniqueWork(ImageGenerationWorker.TAG, ExistingWorkPolicy.REPLACE, worker)


        }
    }

    suspend fun startIaGenerationImmediately() {
        checkMissingImageUseCase.execute()

        val worker = OneTimeWorkRequestBuilder<ImageGenerationWorker>()
            .addTag(ImageGenerationWorker.TAG)
            .build()

        WorkManager.getInstance(context).apply {
            enqueueUniqueWork(ImageGenerationWorker.TAG, ExistingWorkPolicy.REPLACE, worker)
        }
    }
}
package zelgius.com.myrecipes.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.zelgius.myrecipes.ia.usecase.StableHordeDownloadUseCase
import com.zelgius.myrecipes.ia.usecase.StableHordeStatusUseCase
import com.zelgius.myrecipes.ia.usecase.StartStableHordeGenerationUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import zelgius.com.myrecipes.data.logger.Logger
import zelgius.com.myrecipes.data.model.ImageGenerationRequest
import zelgius.com.myrecipes.data.repository.ImageGenerationRequestRepository

@HiltWorker
open class ImageGenerationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val startStableHordeGenerationUseCase: StartStableHordeGenerationUseCase,
    private val stableHordeStatusUseCase: StableHordeStatusUseCase,
    private val stableHordeDownloadUseCase: StableHordeDownloadUseCase,
    private val imageGenerationRequestRepository: ImageGenerationRequestRepository,
    val workRepository: WorkerRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val currentRequest = imageGenerationRequestRepository.get()
        try {

            val progress = if (currentRequest == null) {
                startStableHordeGenerationUseCase.execute()
            } else {
                stableHordeStatusUseCase.execute(currentRequest)
            } ?: return Result.success()

            when (progress.status) {
                ImageGenerationRequest.Status.Done -> {
                    stableHordeDownloadUseCase.execute(progress)
                    imageGenerationRequestRepository.delete(progress)
                    imageGenerationRequestRepository.delete()
                    workRepository.startOrScheduleIaGenerationWorker()
                }

                ImageGenerationRequest.Status.Waiting,
                ImageGenerationRequest.Status.Processing -> {
                    val newProgress = stableHordeStatusUseCase.execute(progress)
                    workRepository.startOrScheduleIaGenerationWorker(newProgress)
                }
            }

            return Result.success()
        } catch (e: Exception) {
            // If it failed here, it means the API is in error. The best way to handle that is to restart generation
            imageGenerationRequestRepository.delete()
            Logger.e("Generation Error", e)
            Firebase.crashlytics.recordException(e)
            return Result.retry()
        }
    }

    companion object {
        const val TAG = "ImageGenerationWorker"
    }

}
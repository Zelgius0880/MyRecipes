package zelgius.com.myrecipes.mediapipe.usecase

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import zelgius.com.myrecipes.repository.Gesture
import zelgius.com.myrecipes.repository.GestureRecognizerRepository
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LiveGestureRecognitionUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gestureRecognizerRepository: GestureRecognizerRepository
) {
    private var backgroundExecutor: ExecutorService? = null

    private val _gestureFlow = MutableSharedFlow<Gesture?>(replay = 1)
    val gestureFlow = _gestureFlow.filterNotNull()

    private var cameraProvider: ProcessCameraProvider? = null

    suspend fun execute(
        targetRotation: Int,
        previewView: PreviewView?,
        lifecycleOwner: LifecycleOwner
    )  {
        val cameraProvider = setUpCamera()
        this.cameraProvider = cameraProvider

        gestureRecognizerRepository.onResultListener = {
            _gestureFlow.tryEmit(it)
        }
        gestureRecognizerRepository.onErrorListener = {
            _gestureFlow.tryEmit(null)
        }

        val backgroundExecutor = Executors.newSingleThreadExecutor()
        val dispatcher = backgroundExecutor.asCoroutineDispatcher()

        withContext(dispatcher) {
            if (gestureRecognizerRepository.isClosed) {
                gestureRecognizerRepository.setup()
            }
        }

        val imageAnalyzer =
            ImageAnalysis.Builder().setResolutionSelector(
                ResolutionSelector.Builder()
                    .setAspectRatioStrategy(RATIO_4_3_FALLBACK_AUTO_STRATEGY).build()
            )
                .setTargetRotation(targetRotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(backgroundExecutor) { image ->
                        gestureRecognizerRepository.recognizeLiveStream(image)
                    }
                }


        val preview = Preview.Builder()
            .setTargetRotation(targetRotation)
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setAspectRatioStrategy(RATIO_4_3_FALLBACK_AUTO_STRATEGY).build()
            ).build()
        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        // A variable number of use-cases can be passed here -
        // camera provides access to CameraControl & CameraInfo
        cameraProvider.bindToLifecycle(
            lifecycleOwner, cameraSelector, imageAnalyzer,
            preview
        )

        preview.setSurfaceProvider(previewView?.surfaceProvider)

    }

    fun clear() {
        backgroundExecutor?.shutdownNow()
        backgroundExecutor?.awaitTermination(
            Long.MAX_VALUE, TimeUnit.NANOSECONDS
        )
        gestureRecognizerRepository.clear()
        cameraProvider?.unbindAll()
        backgroundExecutor = null
    }

    private suspend fun setUpCamera() =
        suspendCancellableCoroutine { continuation ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener(
                {
                    continuation.resumeWith(
                        Result.success(
                            cameraProviderFuture.get()
                        )
                    )
                }, ContextCompat.getMainExecutor(context)
            )
        }
}
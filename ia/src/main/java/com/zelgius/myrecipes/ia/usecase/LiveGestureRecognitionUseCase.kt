package com.zelgius.myrecipes.ia.usecase

import android.content.Context
import android.graphics.Rect
import android.graphics.RectF
import android.view.OrientationEventListener
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.zelgius.myrecipes.ia.repository.Gesture
import com.zelgius.myrecipes.ia.repository.GestureRecognizerRepository
import com.zelgius.myrecipes.ia.repository.Landmark
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LiveGestureRecognitionUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gestureRecognizerRepository: GestureRecognizerRepository,
    val dataStoreRepository: DataStoreRepository,
) {
    private var backgroundExecutor: ExecutorService? = null

    private val _gestureDetectionFlow =
        MutableSharedFlow<Pair<Gesture?, List<List<Landmark>>>?>(replay = 1)
    val gestureFlow = _gestureDetectionFlow.filterNotNull().map { (gesture, _) -> gesture }
    val landMarksFlow = _gestureDetectionFlow.filterNotNull().map { (_, landmarks) -> landmarks }

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var lastRecognitionTime = 0L

    var recognitionAreaPercent = 0f
    val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        coroutineScope.launch {
            dataStoreRepository.gestureDetectionAreaPercent.collectLatest {
                recognitionAreaPercent = it
            }
        }
    }

    suspend fun execute(
        targetRotation: Int,
        recognitionWidth: Double,
        recognitionHeight: Double,
        previewView: androidx.camera.view.PreviewView?,
        lifecycleOwner: LifecycleOwner,
        cooldown: Long = DELAY_BETWEEN_RECOGNITIONS
    ) {
        val cameraProvider = setUpCamera()
        this.cameraProvider = cameraProvider


        val recognitionArea = recognitionWidth * recognitionHeight


        gestureRecognizerRepository.onResultListener = { gesture, landmarks ->
            if (gesture != null && landmarks.isNotEmpty()) {
                val rect = with(landmarks[0].rectF) {
                    Rect(
                        (left * recognitionWidth).toInt(),
                        (top * recognitionHeight).toInt(),
                        (right * recognitionWidth).toInt(),
                        (bottom * recognitionHeight).toInt()
                    )
                }

                val area = rect.width() * rect.height()

                if (System.currentTimeMillis() - lastRecognitionTime > cooldown && area / recognitionArea > recognitionAreaPercent) {
                    lastRecognitionTime = System.currentTimeMillis()
                    _gestureDetectionFlow.tryEmit(gesture to landmarks)
                } else _gestureDetectionFlow.tryEmit(null to emptyList())
            } else _gestureDetectionFlow.tryEmit(gesture to emptyList())

        }
        gestureRecognizerRepository.onErrorListener = {
            _gestureDetectionFlow.tryEmit(null)
        }

        val backgroundExecutor = Executors.newSingleThreadExecutor()
        val dispatcher = backgroundExecutor.asCoroutineDispatcher()

        withContext(dispatcher) {
            if (gestureRecognizerRepository.isClosed) {
                gestureRecognizerRepository.setup()
            }
        }

        imageAnalyzer =
            ImageAnalysis.Builder().setResolutionSelector(
                ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                    .build()
            )
                .setTargetRotation(targetRotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(backgroundExecutor) { image ->
                        gestureRecognizerRepository.recognizeLiveStream(
                            image
                        )
                    }
                }

        val preview = Preview.Builder()
            .setTargetRotation(targetRotation)
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                    .build()
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

        preview.surfaceProvider = previewView?.surfaceProvider

        orientationEventListener.enable()
    }

    fun clear() {
        backgroundExecutor?.shutdownNow()
        backgroundExecutor?.awaitTermination(
            Long.MAX_VALUE, TimeUnit.NANOSECONDS
        )
        gestureRecognizerRepository.clear()
        cameraProvider?.unbindAll()
        backgroundExecutor = null
        orientationEventListener.disable()
    }

    private suspend fun setUpCamera() =
        suspendCancellableCoroutine { continuation ->
            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(context)
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

    private val orientationEventListener by lazy {
        object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return
                }

                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageAnalyzer?.targetRotation = rotation
            }
        }
    }

    companion object {
        const val DELAY_BETWEEN_RECOGNITIONS = 2000L
    }
}

val List<Landmark>.rectF
    get() = RectF(
        minOf { it.x },
        minOf { it.y },
        maxOf { it.x },
        maxOf { it.y },
    )
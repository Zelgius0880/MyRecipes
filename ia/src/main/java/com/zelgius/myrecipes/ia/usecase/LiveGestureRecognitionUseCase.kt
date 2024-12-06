package com.zelgius.myrecipes.ia.usecase

import android.content.Context
import android.view.OrientationEventListener
import android.view.Surface
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.zelgius.myrecipes.ia.repository.Gesture
import com.zelgius.myrecipes.ia.repository.GestureRecognizerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
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

    private var cameraProvider: androidx.camera.lifecycle.ProcessCameraProvider? = null
    private var imageAnalyzer: androidx.camera.core.ImageAnalysis? = null

    suspend fun execute(
        targetRotation: Int,
        previewView: androidx.camera.view.PreviewView?,
        lifecycleOwner: LifecycleOwner
    ) {
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

        imageAnalyzer =
            androidx.camera.core.ImageAnalysis.Builder().setResolutionSelector(
                androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
                    .setAspectRatioStrategy(androidx.camera.core.resolutionselector.AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY).build()
            )
                .setTargetRotation(targetRotation)
                .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(backgroundExecutor) { image ->
                        gestureRecognizerRepository.recognizeLiveStream(
                            image
                        )
                    }
                }

        val preview = androidx.camera.core.Preview.Builder()
            .setTargetRotation(targetRotation)
            .setResolutionSelector(
                androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
                    .setAspectRatioStrategy(androidx.camera.core.resolutionselector.AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY).build()
            ).build()
        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA

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
                androidx.camera.lifecycle.ProcessCameraProvider.Companion.getInstance(context)
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

}
package com.zelgius.myrecipes.ia.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.core.graphics.createBitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.ErrorListener
import com.google.mediapipe.tasks.core.OutputHandler
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GestureRecognizerRepository @Inject constructor(
    @param:ApplicationContext val context: Context,
) : OutputHandler.ResultListener<GestureRecognizerResult, MPImage>, ErrorListener {
    companion object {
        private const val MP_RECOGNIZER_TASK = "gesture_recognizer.task"

        const val DEFAULT_HAND_DETECTION_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_PRESENCE_CONFIDENCE = 0.5F

    }

    var onErrorListener: (error: Throwable) -> Unit = {}
    var onResultListener: (result: Gesture?, landmarks: List<List<Landmark>>) -> Unit = { _, _ -> }

    private var gestureRecognizer: GestureRecognizer? = null

    fun setup() {
        val baseOptionBuilder = BaseOptions.builder()
        baseOptionBuilder.setModelAssetPath(MP_RECOGNIZER_TASK)
        val baseOptions = baseOptionBuilder/*.setDelegate(Delegate.GPU)*/.build()
        val optionsBuilder =
            GestureRecognizer.GestureRecognizerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinHandDetectionConfidence(DEFAULT_HAND_DETECTION_CONFIDENCE)
                .setMinTrackingConfidence(DEFAULT_HAND_TRACKING_CONFIDENCE)
                .setMinHandPresenceConfidence(DEFAULT_HAND_PRESENCE_CONFIDENCE)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener(this)
                .setErrorListener(this)
        val options = optionsBuilder.build()

        gestureRecognizer = GestureRecognizer.createFromOptions(context, options)

    }

    override fun onError(e: RuntimeException) {
        onErrorListener(e)
    }

    override fun run(result: GestureRecognizerResult, input: MPImage) {
        val category = result.gestures().flatMap { it }.maxByOrNull { it.score() }
        if (category == null) {
            onResultListener(null, emptyList())
            return
        }

        val gesture = when (category.categoryName().uppercase()) {
            "THUMB_UP" -> Gesture.ThumbUp
            "THUMB_DOWN" -> Gesture.ThumbDown
            "POINTING_UP" -> Gesture.PointingUp
            "CLOSED_FIST" -> Gesture.PointingUp
            else -> null
        }

        onResultListener(
            gesture,
            result.landmarks().map { it.map { Landmark(it.x(), it.y(), it.z()) } })
    }


    fun clear() {
        gestureRecognizer?.close()
        gestureRecognizer = null
    }

    // Convert the ImageProxy to MP Image and feed it to GestureRecognizer.
    fun recognizeLiveStream(
        imageProxy: androidx.camera.core.ImageProxy,
    ) {
        val frameTime = SystemClock.uptimeMillis()

        // Copy out RGB bits from the frame to a bitmap buffer
        val bitmapBuffer = createBitmap(imageProxy.width, imageProxy.height)
        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
        imageProxy.close()

        val matrix = Matrix().apply {
            // Rotate the frame received from the camera to be in the same direction as it'll be shown
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

            // flip image since we only support front camera
            postScale(
                -1f, 1f, imageProxy.width.toFloat(), imageProxy.height.toFloat()
            )
        }

        // Rotate bitmap to match what our model expects
        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer,
            0,
            0,
            bitmapBuffer.width,
            bitmapBuffer.height,
            matrix,
            true
        )

        // Convert the input Bitmap object to an MPImage object to run inference
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        recognizeAsync(mpImage, frameTime)
    }

    // Run hand gesture recognition using MediaPipe Gesture Recognition API
    private fun recognizeAsync(mpImage: MPImage, frameTime: Long) {
        // As we're using running mode LIVE_STREAM, the recognition result will
        // be returned in returnLivestreamResult function
        try {
            gestureRecognizer?.recognizeAsync(mpImage, frameTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val isClosed: Boolean get() = gestureRecognizer == null
}

enum class Gesture {
    ThumbUp, ThumbDown, PointingUp, ClosedFist
}

data class Landmark(
    val x: Float,
    val y: Float,
    val z: Float
) {
    class Connection(val start: Int, val end: Int)
}

object LandmarkConnections {
    val hand = HandLandmarker.HAND_CONNECTIONS.map {
        Landmark.Connection(it.start(), it.end())
    }
}
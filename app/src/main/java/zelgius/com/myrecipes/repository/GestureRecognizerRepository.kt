package zelgius.com.myrecipes.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.core.ErrorListener
import com.google.mediapipe.tasks.core.OutputHandler
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import dagger.hilt.android.qualifiers.ApplicationContext
import zelgius.com.myrecipes.utils.Logger
import java.lang.RuntimeException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GestureRecognizerRepository @Inject constructor(
    @ApplicationContext val context: Context
) : OutputHandler.ResultListener<GestureRecognizerResult, MPImage>, ErrorListener {
    companion object {
        private const val MP_RECOGNIZER_TASK = "gesture_recognizer.task"

        const val DEFAULT_HAND_DETECTION_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_PRESENCE_CONFIDENCE = 0.5F

        const val DELAY_BETWEEN_RECOGNITIONS = 2000L
    }

    var onErrorListener: (error: Throwable) -> Unit = {}
    var onResultListener: (result: Gesture) -> Unit = {}

    private var gestureRecognizer: GestureRecognizer? = null
    private var lastRecognitionTime = 0L

    fun setup() {
        val baseOptionBuilder = BaseOptions.builder()
        baseOptionBuilder.setModelAssetPath(MP_RECOGNIZER_TASK)
        val baseOptions = baseOptionBuilder.setDelegate(Delegate.GPU).build()
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
        if(category == null) return

        Logger.i( "Category: ${category.categoryName()}")

        val gesture = when (category.categoryName().uppercase()) {
            "THUMB_UP" -> Gesture.ThumbUp
            "THUMB_DOWN" -> Gesture.ThumbDown
            "POINTING_UP" -> Gesture.PointingUp
            "CLOSED_FIST" -> Gesture.PointingUp
            else -> null
        }

        if (gesture != null && System.currentTimeMillis() - lastRecognitionTime > DELAY_BETWEEN_RECOGNITIONS) {
            lastRecognitionTime = System.currentTimeMillis()
            onResultListener(gesture)
        }
    }


    fun clear() {
        gestureRecognizer?.close()
        gestureRecognizer = null
    }

    // Convert the ImageProxy to MP Image and feed it to GestureRecognizer.
    fun recognizeLiveStream(
        imageProxy: ImageProxy,
    ) {
        val frameTime = SystemClock.uptimeMillis()

        // Copy out RGB bits from the frame to a bitmap buffer
        val bitmapBuffer = Bitmap.createBitmap(
            imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
        )
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
    fun recognizeAsync(mpImage: MPImage, frameTime: Long) {
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
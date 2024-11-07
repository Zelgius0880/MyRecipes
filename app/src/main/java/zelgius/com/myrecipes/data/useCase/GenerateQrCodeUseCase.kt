package zelgius.com.myrecipes.data.useCase

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Base64
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.toBitmap
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColors
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorShapes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GenerateQrCodeUseCase {

    companion object {
        const val FINDER_PATTERN_SIZE = 7
        const val CIRCLE_SCALE_DOWN_FACTOR = 21f / 30f
    }

    suspend fun execute(
        bytes: ByteArray,
        width: Int = 400,
        height: Int = 400,
        @ColorInt dotColor: Int
    ): Bitmap? =
        withContext(Dispatchers.IO) {
            val options = QrVectorOptions.Builder()
                .setPadding(0.05f)
                .setColors(
                    QrVectorColors(
                        dark = QrVectorColor.Solid(dotColor),
                        ball = QrVectorColor.Solid(dotColor),
                        frame = QrVectorColor.Solid(dotColor),
                    )
                )
                .setShapes(
                    QrVectorShapes(
                        darkPixel = QrVectorPixelShape.Circle(),
                        lightPixel = QrVectorPixelShape.Circle(),
                        ball = QrVectorBallShape.RoundCorners(.5f),
                        frame = QrVectorFrameShape.RoundCorners(.5f),
                    )
                )
                .build()

            val drawable: Drawable = QrCodeDrawable(
                QrData.Text(Base64.encodeToString(bytes, Base64.NO_PADDING)),
                options
            )

           drawable.toBitmap( width, height)
        }
}
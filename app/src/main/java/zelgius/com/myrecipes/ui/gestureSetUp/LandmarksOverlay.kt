package zelgius.com.myrecipes.ui.gestureSetUp

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import com.zelgius.myrecipes.ia.repository.Landmark
import com.zelgius.myrecipes.ia.repository.LandmarkConnections
import com.zelgius.myrecipes.ia.usecase.rectF
import java.util.Locale

private const val LANDMARK_STROKE_WIDTH = 16F

@Composable
fun LandmarksOverlay(
    landmarks: List<List<Landmark>>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val pointColor = MaterialTheme.colorScheme.secondary
    val lineColor = color
    val rectColor =  MaterialTheme.colorScheme.tertiary

    var height by remember {
        mutableFloatStateOf(0f)
    }

    var width by remember {
        mutableFloatStateOf(0f)
    }

    val area by remember {
        derivedStateOf {
            height * width
        }
    }

    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier = modifier.onGloballyPositioned { coordinates ->
        height = coordinates.size.height.toFloat()
        width = coordinates.size.width.toFloat()
    }) {

        if(landmarks.isNotEmpty()) {
            val rect = with(landmarks[0].rectF) {
                Rect(
                    Offset(
                        this.left * width,
                        this.top * height
                    ),
                    Offset(
                        this.right * width,
                        this.bottom * height
                    )
                )
            }
            drawRect(
                brush = SolidColor(rectColor),
                topLeft = rect.topLeft,
                size = rect.size,
                style = Stroke(width = LANDMARK_STROKE_WIDTH)
            )
            LandmarkConnections.hand.forEach {
                drawLine(
                    brush = SolidColor(lineColor),
                    Offset(
                        landmarks[0][it.start].x * width, landmarks[0][it.start].y * height
                    ),
                    Offset(
                        landmarks[0][it.end].x * width, landmarks[0][it.end].y * height
                    ),
                    strokeWidth = LANDMARK_STROKE_WIDTH,
                )
            }

            for (landmark in landmarks) {
                landmark.forEachIndexed { index, normalizedLandmark ->
                    if (normalizedLandmark.x * width > 0 || normalizedLandmark.y * height > 0) {

                        drawCircle(
                            brush = SolidColor(pointColor),
                            radius = LANDMARK_STROKE_WIDTH,
                            center = Offset(
                                normalizedLandmark.x * width,
                                normalizedLandmark.y * height
                            ),
                        )
                    }
                }

                drawText(
                    textMeasurer,
                    "(${
                        String.format(
                            Locale.getDefault(),
                            "%.2f%%",
                            (rect.width * rect.height / area) * 100
                        )
                    })",
                    rect.topLeft
                )

            }
        }
    }
}

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import zelgius.com.myrecipes.ui.AppTheme
import kotlin.math.roundToInt
import kotlin.math.sqrt


/**
 * A modifier that clips the composable content using a circular reveal animation. The circle will
 * expand or shrink whenever [isVisible] changes.
 *
 * For more control over the transition, consider using this method's variant which allows passing
 * a [State] object to control the progress of the reveal animation.
 *
 * By default, the circle is centered in the content. However, custom positions can be specified using
 * [revealFrom]. The specified offsets should range from 0 (left/top) to 1 (right/bottom).
 *
 * @param isVisible Determines whether content is visible or not. If true, circle expands; if false, it shrinks.
 * @param revealFrom Custom position from which to start the circular reveal. Default is center of content.
 * @param durationMillis Duration of animation in milliseconds. Default is 250ms.
 * @param easing Easing function used for animation. Default is EaseInOutSine.
 * @param size Size state of component being revealed. Default size is (0, 0).
 */
fun Modifier.circularReveal(
    isVisible: Boolean,
    revealFrom: Offset = Offset(0.5f, 0.5f),
    durationMillis: Int = 250,
    easing: Easing = EaseInOutSine,
    size: MutableState<IntSize> = mutableStateOf(IntSize(0, 0))
): Modifier =
    onGloballyPositioned {
        size.value = it.size
    }.composed(
        factory = {

            val animationProgress: State<Float> = animateFloatAsState(
                targetValue = if (isVisible) 1f else 0f,
                animationSpec = tween(durationMillis = durationMillis, easing = easing),
                label = ""
            )

            circularReveal(animationProgress, revealFrom / size.value.toSize())
        },
        inspectorInfo = debugInspectorInfo {
            name = "circularReveal"
            properties["visible"] = isVisible
            properties["revealFrom"] = revealFrom
            properties["durationMillis"] = durationMillis
        }
    )

/**A modifier that clips the composable content using a circular shape. The radius of the circle
 * will be determined by the [transitionProgress].
 *
 * The values of the progress should be between 0 and 1.
 *
 * By default, the circle is centered in the content, but custom positions may be specified using
 *  [revealFrom]. Specified offsets should be between 0 (left/top) and 1 (right/bottom).
 *  */
fun Modifier.circularReveal(
    transitionProgress: State<Float>,
    revealFrom: Offset = Offset(0.5f, 0.5f)
): Modifier {
    return drawWithCache {
        val path = Path()

        val center = revealFrom.mapTo(size)
        val radius = calculateRadius(revealFrom, size)

        path.addOval(Rect(center, radius * transitionProgress.value))

        onDrawWithContent {
            clipPath(path) { this@onDrawWithContent.drawContent() }
        }
    }
}

private fun Offset.mapTo(size: Size): Offset {
    return Offset(x * size.width, y * size.height)
}

private fun calculateRadius(normalizedOrigin: Offset, size: Size) = with(normalizedOrigin) {
    val x = (if (x > 0.5f) x else 1 - x) * size.width
    val y = (if (y > 0.5f) y else 1 - y) * size.height

    sqrt(x * x + y * y)
}

operator fun Offset.times(size: Size): Offset = Offset(x * size.width, y * size.height)

operator fun Offset.div(size: Size): Offset {
    val dx = if (size.width == 0f) x else x / size.width
    val dy = if (size.height == 0f) y else y / size.height
    return Offset(dx, dy)
}

@Composable
fun DraggableCircle(
    modifier: Modifier = Modifier,
    onTap: (Offset) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val position = remember { mutableStateOf(Offset(0f, 0f)) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .onGloballyPositioned {
                position.value = it.positionInRoot()
            }
            .pointerInput(Unit) {
                detectTapGestures { onTap(it + position.value) }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .clip(RoundedCornerShape(25.dp))
            .then(modifier)
    )
}

@Composable
private fun RevealTest() {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
    ) {

        val isVisible = remember { mutableStateOf(false) }
        val revealFrom = remember { mutableStateOf(Offset(0f, 0f)) }

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.error)
                .fillMaxWidth()
                .height(150.dp)
        )

        Box(
            modifier = Modifier
                .circularReveal(
                    isVisible = isVisible.value,
                    revealFrom = revealFrom.value
                )
                .background(MaterialTheme.colorScheme.primary)
                .fillMaxWidth()
                .height(300.dp)
        ) {

        }

        DraggableCircle(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.tertiary)
                .size(50.dp)
                .align(Alignment.CenterEnd)
        ) {
            revealFrom.value = it
            isVisible.value = !isVisible.value
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RevealTestPreview() {
    AppTheme {
        RevealTest()
    }
}
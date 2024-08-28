@file:OptIn(ExperimentalFoundationApi::class)

package zelgius.com.myrecipes.ui.common

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class DragAnchors {
    Start,
    Center,
    End,
}


@Composable
fun AppSwipeRevealItem(
    modifier: Modifier = Modifier,
    startAction: @Composable (RowScope.() -> Unit)? = null,
    endAction: @Composable (RowScope.() -> Unit)? = null,
    startActionSize: Dp = 0.dp,
    endActionSize: Dp = 0.dp,
    contentElevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {

    val state = rememberAnchoredDraggableState(startActionSize, endActionSize)
    Box(modifier = modifier) {

        endAction?.let {
            val endActionSizePx = with(LocalDensity.current) { endActionSize.toPx() }
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(endActionSize)
                    .align(Alignment.CenterEnd)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .offset {
                        IntOffset(
                            (-state
                                .requireOffset() + endActionSizePx)
                                .roundToInt(), 0
                        )
                    }
            )
            {
                endAction()
            }
        }

        startAction?.let {
            val startActionSizePx = with(LocalDensity.current) { startActionSize.toPx() }
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(endActionSize)
                    .align(Alignment.CenterStart)
                    .offset {
                        IntOffset(
                            (-state
                                .requireOffset() - startActionSizePx)
                                .roundToInt(), 0
                        )
                    }
            )
            {
                startAction()
            }
        }
        Surface (
            shadowElevation = contentElevation,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart)
                .offset {
                    IntOffset(
                        x = -state
                            .requireOffset()
                            .roundToInt(),
                        y = 0,
                    )
                }
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal,
                    reverseDirection = true
                ),
            content = content
        )
    }
}

@Composable
private fun rememberAnchoredDraggableState(
    startActionSize: Dp = 0.dp,
    endActionSize: Dp = 0.dp,
): AnchoredDraggableState<DragAnchors> {
    val density = LocalDensity.current
    val endActionSizePx = with(density) { (endActionSize).toPx() }
    val startActionSizePx = with(density) { startActionSize.toPx() }

    val anchoredDraggableState = remember {
        AnchoredDraggableState(
            initialValue = DragAnchors.Center,
            anchors = DraggableAnchors {
                DragAnchors.Start at -startActionSizePx
                DragAnchors.Center at 0f
                DragAnchors.End at endActionSizePx
            },
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            snapAnimationSpec = tween(),
            decayAnimationSpec = exponentialDecay(),
        )
    }

    return anchoredDraggableState
}

@Preview
@Composable
fun AppSwipeRevealItemPreview() {
    AppSwipeRevealItem(
        startAction = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(100.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
        },
        startActionSize = 64.dp,
        endAction = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .width(100.dp),
            )
        },
        endActionSize = 100.dp,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ){
                Text(text = "Hello", modifier = Modifier.align(Alignment.Center))
            }
        })
}
package zelgius.com.myrecipes.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

val RemovableItemEndActionSize = 64.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RemovableItem(
    modifier: Modifier = Modifier,
    state: AnchoredDraggableState<DragAnchors> = rememberAnchoredDraggableState(
        0.dp,
        RemovableItemEndActionSize
    ),
    onRemove: () -> Unit,
    content: @Composable () -> Unit,
    ) {

    val coroutineScope = rememberCoroutineScope()
    AppSwipeRevealItem(
        contentElevation = 2.dp,
        modifier = modifier,
        state = state,
        endActionSize = RemovableItemEndActionSize,
        endAction = {
            Button(
                modifier = Modifier
                    .fillMaxSize(),
                onClick = {
                    onRemove()
                    coroutineScope.launch {
                        state.snapTo(DragAnchors.Start)
                    }
                },
                shape = RectangleShape,
                contentPadding = PaddingValues(horizontal = 0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    Icons.TwoTone.Delete,
                    contentDescription = "",
                )
            }
        }) {
        Surface(
            color = CardDefaults.cardColors().containerColor,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            content()
        }
    }
}
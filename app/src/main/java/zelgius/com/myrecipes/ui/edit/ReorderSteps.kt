@file:OptIn(ExperimentalMaterial3Api::class)

package zelgius.com.myrecipes.ui.edit

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.ui.common.dragHandle
import zelgius.com.myrecipes.ui.common.draggableItems
import zelgius.com.myrecipes.ui.common.rememberDraggableListState
import zelgius.com.myrecipes.ui.edit.viewModel.StepItem
import zelgius.com.myrecipes.ui.common.recipe.Step as StepCard

@Composable
fun ReorderStepsBottomSheet(
    steps: List<StepItem>,
    modalBottomSheetState: SheetState,
    onDismiss: () -> Unit = {},
    onStepReordered: (List<StepItem>) -> Unit = {},
) {

    if (steps.isNotEmpty()) {
        ModalBottomSheet(
            sheetState = modalBottomSheetState,
            onDismissRequest = onDismiss,
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 48.dp)
        ) {
            ReorderSteps(steps, onStepReordered = {
                onStepReordered(it)
                onDismiss()
            })
        }
    }
}

@Composable
private fun ReorderSteps(
    steps: List<StepItem>,
    onStepReordered: (List<StepItem>) -> Unit = {},
) {
    var draggableItems by remember {
        mutableStateOf(steps)
    }

    val draggableState = rememberDraggableListState(
        onMove = { fromIndex, toIndex ->
            draggableItems =
                draggableItems.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        }
    )

    Column {
        LazyColumn(
            state = draggableState.listState,
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            draggableItems(
                items = draggableItems,
                state = draggableState,
                key = { it.hashCode() }
            ) { item, isDragging ->

                var itemSize: Size? by remember {
                    mutableStateOf(null)
                }

                val targetCornerRadius =
                    if (isDragging) MaterialTheme.shapes.medium.topStart.toPx(
                        itemSize ?: Size(1f, 1f),
                        LocalDensity.current
                    )
                    else MaterialTheme.shapes.small.topStart.toPx(
                        itemSize ?: Size(1f, 1f),
                        LocalDensity.current
                    )

                val animatedCornerRadius by animateFloatAsState(
                    targetValue = targetCornerRadius,
                    animationSpec = spring()
                )

                val color by animateColorAsState(if (isDragging) MaterialTheme.colorScheme.secondaryContainer else CardDefaults.cardColors().containerColor)

                Card(
                    modifier = Modifier
                        .dragHandle(
                            draggableState,
                            key = item.hashCode(),
                            onlyAfterLongPress = true
                        )
                        .onGloballyPositioned {
                            itemSize = Size(it.size.width.toFloat(), it.size.height.toFloat())
                        },
                    shape = if (itemSize == null) MaterialTheme.shapes.large else RoundedCornerShape(
                        animatedCornerRadius.dp
                    ),
                    colors = CardDefaults.cardColors(containerColor = color)
                ) {
                    StepCard(
                        item.step,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp).fillMaxWidth()
                    )
                }
            }
        }
        Button(
            onClick = { onStepReordered(draggableItems) },
            modifier = Modifier
                .align(Alignment.End)
                .padding(vertical = 8.dp)
        ) {
            Text(stringResource(id = R.string.save))
        }
    }
}

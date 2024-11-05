package zelgius.com.myrecipes.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private const val CORNER_RADIUS = 28f

@Composable
fun <H, I> ExpandableList(
    sections: List<H>,
    modifier: Modifier = Modifier,
    children: (header: H) -> List<I>,
    header: (@Composable () -> Unit)? = null,
    section: @Composable (isExpanded: Boolean, header: H) -> Unit,
    child: @Composable (header: H, item: I) -> Unit,
    reversed: Boolean = false,
    initiallyExpanded: Map<Int, Boolean> = mapOf(),
    radius: Float = CORNER_RADIUS
) {
    val isExpandedMap = rememberSavableSnapshotStateMap {
        List(sections.size) { index: Int -> index to (initiallyExpanded[index] == true) }
            .toMutableStateMap()
    }

    LazyColumn(
        modifier = modifier,
        content = {
            if (header != null) {
                item {
                    header()
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }


            sections.forEachIndexed { index, s ->
                val isExpanded = isExpandedMap[index] ?: (initiallyExpanded[index] == true)
                val onExpand = { isExpandedMap[index] = !isExpanded }

                val items = children(s)

                if (!reversed) {
                    item {
                        Header(
                            isReversed = false,
                            isExpanded = isExpanded,
                            radius = radius,
                            item = s,
                            onExpand = if (items.isNotEmpty()) onExpand else null,
                            header = section,
                        )
                    }


                    if (items.isNotEmpty())
                        itemsIndexed(items) { index, item ->
                            Child(
                                isReversed = false,
                                section = s,
                                radius = radius,
                                child = item,
                                childContent = child,
                                isExpanded = isExpanded,
                                showRadius = index == items.lastIndex
                            )
                        }
                } else {
                    if (items.isNotEmpty())
                        itemsIndexed(items) { index, item ->
                            Child(
                                isReversed = true,
                                radius = radius,
                                section = s,
                                child = item,
                                childContent = child,
                                isExpanded = isExpanded,
                                showRadius = index == 0,
                                onExpand = if (index == 0) onExpand else null,
                            )
                        }

                    item {
                        Header(
                            isReversed = true,
                            radius = radius,
                            isExpanded = isExpanded,
                            item = s,
                            onExpand = if (items.isNotEmpty()) onExpand else null,
                            header = section,
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    )
}

@Composable
private fun <H> Header(
    isExpanded: Boolean,
    isReversed: Boolean,
    item: H,
    onExpand: (() -> Unit)? = null,
    radius: Float = CORNER_RADIUS,
    header: @Composable (isExpanded: Boolean, header: H) -> Unit,
) {
    val animatedRadius: Float by animateFloatAsState(
        if (!isExpanded) radius else 0f,
        label = "header"
    )
    val shape = RoundedCornerShape(
        topStart = if (!isReversed || !isExpanded || onExpand == null) CornerSize(radius.dp) else CornerSize(
            animatedRadius
        ),
        topEnd = if (!isReversed || !isExpanded || onExpand == null) CornerSize(radius.dp) else CornerSize(
            animatedRadius.dp
        ),
        bottomStart = if ((!isReversed || !isExpanded) && onExpand != null) CornerSize(
            animatedRadius.dp
        ) else CornerSize(
            radius.dp
        ),
        bottomEnd = if ((!isReversed || !isExpanded) && onExpand != null) CornerSize(animatedRadius.dp) else CornerSize(
            radius.dp
        )
    )
    Card(
        modifier = Modifier
            .clip(shape)
            .clickable(enabled = onExpand != null) {
                onExpand?.invoke()
            },
        shape = shape
    ) {
        header(isExpanded, item)
    }
}

@Composable
private fun <H, C> Child(
    isReversed: Boolean,
    section: H,
    child: C,
    isExpanded: Boolean,
    showRadius: Boolean,
    radius: Float = CORNER_RADIUS,
    onExpand: (() -> Unit)? = null,
    childContent: @Composable (header: H, child: C) -> Unit
) {
    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Card(
            modifier = Modifier.clickable(enabled = onExpand != null) {
                onExpand?.invoke()
            },
            shape = if (showRadius) RoundedCornerShape(
                bottomStart = if (!isReversed) CornerSize(radius.dp) else ZeroCornerSize,
                bottomEnd = if (!isReversed) CornerSize(radius.dp) else ZeroCornerSize,
                topStart = if (!isReversed) ZeroCornerSize else CornerSize(radius.dp),
                topEnd = if (!isReversed) ZeroCornerSize else CornerSize(radius.dp)
            ) else RectangleShape
        ) {
            childContent(section, child)
        }
    }
}

@Preview
@Composable
private fun ExpandableListPreview() {
    ExpandableList(
        sections = listOf("Header 1", "Header 2", "Header 3"),
        children = { listOf("Item 1", "Item 2", "Item 3") },
        initiallyExpanded = mapOf(0 to true, 2 to true),
        child = { index, item ->
            Text(
                text = "$index - $item",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )
        },
        section = { _, header ->
            Text(
                text = header,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )
        },

        )
}

@Preview
@Composable
private fun ReversedExpandableListPreview() {
    ExpandableList(
        sections = listOf("Header 1", "Header 2", "Header 3"),
        initiallyExpanded = mapOf(0 to true, 2 to true),
        children = { listOf("Item 1", "Item 2", "Item 3") },
        child = { index, item ->
            Text(
                text = "$index - $item",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )
        },
        section = { _, header ->
            Text(
                text = header,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )
        },
        reversed = true,
    )
}

@Preview
@Composable
private fun NoChildExpandableListPreview() {
    ExpandableList(
        sections = listOf("Header 1", "Header 2", "Header 3"),
        initiallyExpanded = mapOf(0 to true, 2 to true),
        children = { emptyList<Any>() },
        child = { index, item ->
            Text(
                text = "$index - $item",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )
        },
        section = { _, header ->
            Text(
                text = header,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )
        },
    )
}


@Preview
@Composable
private fun ReversedNoChildExpandableListPreview() {
    ExpandableList(
        sections = listOf("Header 1", "Header 2", "Header 3"),
        children = { emptyList<Any>() },
        reversed = true,
        initiallyExpanded = mapOf(0 to true, 2 to true),
        child = { index, item ->
            Text(
                text = "$index - $item",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )
        },
        section = { _, header ->
            Text(
                text = header,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )
        },
    )
}

fun <K, V> snapshotStateMapSaver() = Saver<SnapshotStateMap<K, V>, Any>(
    save = { state -> state.toList() },
    restore = { value ->
        @Suppress("UNCHECKED_CAST")
        (value as? List<Pair<K, V>>)?.toMutableStateMap() ?: mutableStateMapOf()
    }

)

@Composable
fun <K, V> rememberSavableSnapshotStateMap(init: () -> SnapshotStateMap<K, V>): SnapshotStateMap<K, V> =
    rememberSaveable(saver = snapshotStateMapSaver(), init = init)
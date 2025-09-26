@file:OptIn(ExperimentalSharedTransitionApi::class)

package zelgius.com.myrecipes.ui.common.recipe

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import zelgius.com.myrecipes.data.entities.asModel
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.text
import zelgius.com.myrecipes.ui.common.AppImage
import zelgius.com.myrecipes.ui.common.DragAnchors
import zelgius.com.myrecipes.ui.common.RemovableItem
import zelgius.com.myrecipes.ui.common.RemovableItemEndActionSize
import zelgius.com.myrecipes.ui.common.rememberAnchoredDraggableState
import zelgius.com.myrecipes.ui.preview.SharedElementPreview
import zelgius.com.myrecipes.ui.preview.createDummySample

@Composable
fun RecipeList(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    list: Flow<PagingData<Recipe>>,
    modifier: Modifier = Modifier,
    onClick: (Recipe) -> Unit = {},
    onRemove: (Recipe) -> Unit = {}
) {
    val items = list.collectAsLazyPagingItems()

    LazyColumn(modifier = modifier) {
        items(count = items.itemCount, key = { it.hashCode() }) { index ->
            val recipe = items[index]

            recipe?.let {
                RecipeListItem(
                    animatedVisibilityScope = animatedVisibilityScope,
                    sharedTransitionScope = sharedTransitionScope,
                    recipe = recipe,
                    onClick = onClick,
                    onRemove = onRemove,
                    modifier = Modifier
                        .animateItem()
                        .fillParentMaxWidth()
                        .padding(vertical = 2.dp, horizontal = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecipeListItem(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    recipe: Recipe,
    modifier: Modifier = Modifier,
    onClick: (Recipe) -> Unit = {},
    onRemove: (Recipe) -> Unit = {}
) = with(sharedTransitionScope) {
    Card(
        shape = RoundedCornerShape(4.dp),
        modifier = modifier then sharedElement(animatedVisibilityScope, "${recipe.id}_recipe_card")
            .height(IntrinsicSize.Max)
    ) {
        val state = rememberAnchoredDraggableState(
            0.dp,
            RemovableItemEndActionSize
        )

        RemovableItem(
            modifier = Modifier
                .clickable { onClick(recipe) }
                .fillMaxWidth(),
            onRemove = {
                onRemove(recipe)
                state.progress(DragAnchors.End, DragAnchors.Start)
            },
            state = state
        ) {
            Row {
                AppImage(
                    imageUrl = recipe.imageUrl,
                    modifier = sharedElement(animatedVisibilityScope, "${recipe.id}_recipe_image")
                        .size(64.dp)
                )

                Column(modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)) {
                    Text(
                        recipe.type.text(LocalContext.current),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = sharedElement(animatedVisibilityScope, "${recipe.id}_recipe_type")
                    )
                    Text(
                        recipe.name,
                        modifier = sharedElement(animatedVisibilityScope, "${recipe.id}_recipe_name")
                            .padding(top = 8.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun SharedTransitionScope?.sharedElement(
    animatedVisibilityScope: AnimatedVisibilityScope?,
    key: String,
) = if (this != null && animatedVisibilityScope != null)
    Modifier.sharedElement(
        animatedVisibilityScope = animatedVisibilityScope,
        sharedContentState = rememberSharedContentState(
            key = key
        )
    )
else Modifier

@Preview
@Composable
fun RecipeListPreview() {
    val list = (1..6).map {
        createDummySample(suffix = " $it", id = it.toLong())
    }

    SharedElementPreview { animatedVisibilityScope, sharedTransitionScope ->
        RecipeList(
            animatedVisibilityScope = animatedVisibilityScope,
            sharedTransitionScope = sharedTransitionScope,
            list = flowOf(PagingData.from(list.map { it.asModel() }))
        )
    }
}

@Composable
@Preview
fun RecipeListItemPreview() {
    SharedElementPreview { animatedVisibilityScope, sharedTransitionScope ->
        RecipeListItem(
            animatedVisibilityScope = animatedVisibilityScope,
            sharedTransitionScope = sharedTransitionScope, recipe = createDummySample().asModel()
        )
    }
}

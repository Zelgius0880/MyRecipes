@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)

package zelgius.com.myrecipes.ui.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.twotone.Edit
import androidx.compose.material.icons.twotone.PlayArrow
import androidx.compose.material.icons.twotone.Share
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.ui.common.ExpandableList
import zelgius.com.myrecipes.ui.common.recipe.Ingredient
import zelgius.com.myrecipes.ui.common.recipe.IngredientChip
import zelgius.com.myrecipes.ui.common.recipe.Step
import zelgius.com.myrecipes.ui.details.viewModel.RecipeDetailsViewModel
import zelgius.com.myrecipes.ui.edit.viewModel.StepItem
import zelgius.com.myrecipes.ui.preview.SharedElementPreview
import zelgius.com.myrecipes.ui.preview.createDummyModel
import zelgius.com.myrecipes.ui.share.ShareDialog
import zelgius.com.myrecipes.utils.ifNotNull


@Composable
fun RecipeDetails(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    viewModel: RecipeDetailsViewModel,
    isTwoPanes: Boolean = false,
    navigateBack: () -> Unit = {},
    playRecipe: (recipe: Recipe) -> Unit = {},
    onEdit: (Recipe) -> Unit = {}
) {
    val recipe by viewModel.recipeFlow.collectAsState(null)
    val items by viewModel.itemsFlow.collectAsState(emptyList())

    recipe?.let {
        RecipeDetailsView(
            sharedTransitionScope,
            animatedVisibilityScope,
            isTwoPanes,
            navigateBack,
            onEdit,
            it,
            items,
            playRecipe
        )
    }
}

@Composable
private fun RecipeDetailsView(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    isTwoPanes: Boolean = false,
    navigateBack: () -> Unit = {},
    onEdit: (Recipe) -> Unit = {},
    recipe: Recipe,
    items: List<StepItem>,
    playRecipe: (recipe: Recipe) -> Unit = {},
) = with(sharedTransitionScope) {
    var showShareDialog: Boolean by remember {
        mutableStateOf(false)
    }

    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            title = {},
            navigationIcon = {
                if (!isTwoPanes)
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.TwoTone.ArrowBack,
                            contentDescription = ""
                        )
                    }
            },
            actions = {
                IconButton(onClick = { showShareDialog = true }) {
                    Icon(
                        imageVector = Icons.TwoTone.Share,
                        contentDescription = ""
                    )
                }

                IconButton(onClick = { onEdit(recipe) }) {
                    Icon(
                        imageVector = Icons.TwoTone.Edit,
                        contentDescription = ""
                    )
                }
                IconButton(onClick = { playRecipe(recipe) }) {
                    Icon(
                        imageVector = Icons.TwoTone.PlayArrow,
                        contentDescription = ""
                    )
                }
            }
        )
    }, content = { padding ->
        ExpandableList(
            reversed = true,
            initiallyExpanded = mapOf(0 to true),
            radius = 28f,
            header = {
                RecipeDetailsHeader(
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    recipe = recipe,
                    onEdit = onEdit,
                    onPlay = playRecipe,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 8.dp),
            sections = items,
            section = { isExpanded, item ->
                if (item == items.first()) if (isExpanded) Box(
                    Modifier
                        .height(24.dp)
                        .fillMaxWidth()
                )
                else Text(
                    stringResource(
                        R.string.ingredients
                    ), modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                else Row {
                    Step(
                        step = item.step,
                        Modifier
                            .weight(1f)
                            .padding(8.dp)
                    )
                    val count = item.ingredients.size
                    AnimatedVisibility(count > 0 && !isExpanded) { IngredientChip(count) }

                }
            },
            child = { _, item ->
                Ingredient(
                    item, modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            },
            children = {
                it.ingredients
            },
        )
    })

    if(showShareDialog) ShareDialog(recipe) {
        showShareDialog = false
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RecipeDetailsHeader(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    modifier: Modifier = Modifier,
    recipe: Recipe,
    onEdit: (Recipe) -> Unit = {},
    onPlay: (Recipe) -> Unit = {},
) = with(sharedTransitionScope) {
    Card(
        modifier = modifier
            .ifNotNull(animatedVisibilityScope) {
                sharedElement(
                    animatedVisibilityScope = it,
                    state = rememberSharedContentState(
                        key = "${recipe.id}_recipe_container"
                    )
                )
            }
            .padding(top = 8.dp), shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box {
                Image(
                    painter = rememberAsyncImagePainter(
                        recipe.imageUrl, error = painterResource(R.drawable.ic_dish)
                    ), contentDescription = null, modifier = Modifier
                        .size(128.dp)
                        .ifNotNull(animatedVisibilityScope) {
                            sharedElement(
                                animatedVisibilityScope = it,
                                state = rememberSharedContentState(key = "${recipe.id}_recipe_image")
                            )
                        }
                        .clip(
                            shape = MaterialTheme.shapes.extraLarge
                        ), contentScale = ContentScale.Crop
                )
            }

            Text(
                recipe.name, modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
                    .ifNotNull(animatedVisibilityScope) {
                        sharedElement(
                            animatedVisibilityScope = it,
                            state = rememberSharedContentState(
                                key = "${recipe.id}_recipe_name"
                            )
                        )
                    }, style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}


@Preview
@Composable
fun RecipeDetailsPreview() {
    SharedElementPreview { animatedVisibilityScope, sharedTransitionScope ->
        val recipe = createDummyModel(suffix = "")
        RecipeDetailsView(
            animatedVisibilityScope = animatedVisibilityScope,
            sharedTransitionScope = sharedTransitionScope,
            recipe = recipe,
            items = (listOf(
                Step(
                    text = "", recipe = recipe
                )
            ) + recipe.steps).mapIndexed { index, item ->
                if (index == 0) StepItem(item, ingredients = recipe.ingredients)
                else StepItem(item, emptyList())
            }
        )
    }
}


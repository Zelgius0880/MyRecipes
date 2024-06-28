@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)

package zelgius.com.myrecipes.ui.details

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.preview.SharedElementPreview
import zelgius.com.myrecipes.preview.createDummyModel
import zelgius.com.myrecipes.ui.common.ExpandableList
import zelgius.com.myrecipes.ui.details.viewModel.RecipeDetailsViewModel
import zelgius.com.myrecipes.ui.home.string
import zelgius.com.myrecipes.ui.recipe.Ingredient
import zelgius.com.myrecipes.ui.recipe.Step


@Composable
fun RecipeDetails(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: RecipeDetailsViewModel,
    navigateBack: () -> Unit = {}
) {
    val recipe by viewModel.recipeFlow.collectAsState()
    val items by viewModel.itemsFlow.collectAsState(emptyList())

    RecipeDetailsView(sharedTransitionScope, animatedVisibilityScope, navigateBack, recipe, items)
}

@Composable
private fun RecipeDetailsView(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    navigateBack: () -> Unit = {},
    recipe: Recipe,
    items: List<Step>,
) = with(sharedTransitionScope) {
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                recipe.type.string(), modifier = Modifier.sharedElement(
                    animatedVisibilityScope = animatedVisibilityScope,
                    state = rememberSharedContentState(
                        key = "${recipe.id}_recipe_type"
                    )
                )
            )
        }, navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = ""
                )
            }
        })
    }, content = { padding ->
        ExpandableList(
            reversed = true,
            initiallyExpanded = mapOf(0 to true),
            radius = 64f,
            header = {
                RecipeDetailsHeader(
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    recipe = recipe,
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
                        .height(16.dp)
                        .fillMaxWidth()
                )
                else Text(
                    stringResource(
                        R.string.ingredients
                    ), modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                else Step(
                    step = item,
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
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
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RecipeDetailsHeader(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    recipe: Recipe,
    modifier: Modifier = Modifier
) = with(sharedTransitionScope) {
    Card(
        modifier = modifier.sharedElement(
                animatedVisibilityScope = animatedVisibilityScope,
                state = rememberSharedContentState(
                    key = "${recipe.id}_recipe_container"
                )
            ), shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = rememberAsyncImagePainter(
                    recipe.imageUrl, error = painterResource(R.drawable.ic_dish)
                ), contentDescription = null, modifier = Modifier
                    .size(128.dp)
                    .clip(
                        shape = MaterialTheme.shapes.extraLarge
                    ), contentScale = ContentScale.Crop
            )

            Text(
                recipe.name, modifier = Modifier
                    .padding(16.dp)
                    .sharedElement(
                        animatedVisibilityScope = animatedVisibilityScope,
                        state = rememberSharedContentState(
                            key = "${recipe.id}_recipe_name"
                        )
                    ), style = MaterialTheme.typography.headlineLarge
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
            items = listOf(
                Step(
                    text = "", ingredients = recipe.ingredients, recipe = recipe
                )
            ) + recipe.steps
        )
    }
}


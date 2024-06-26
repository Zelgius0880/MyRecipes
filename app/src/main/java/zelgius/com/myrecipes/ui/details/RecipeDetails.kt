@file:OptIn(ExperimentalSharedTransitionApi::class)

package zelgius.com.myrecipes.ui.details

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.text
import zelgius.com.myrecipes.preview.SharedElementPreview
import zelgius.com.myrecipes.preview.createDummyModel


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RecipeDetails(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    recipe: Recipe
) {
    LazyColumn {
        item {
            RecipeDetailsHeader(sharedTransitionScope, animatedVisibilityScope, recipe)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RecipeDetailsHeader(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    recipe: Recipe
) = with(sharedTransitionScope) {
    Card(
        shape = RoundedCornerShape(4.dp), modifier = Modifier.sharedElement(
            animatedVisibilityScope = animatedVisibilityScope,
            state = rememberSharedContentState(
                key = "recipe_container"
            )
        )
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.imageUrl)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_dish),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(128.dp)
                    .sharedElement(
                        animatedVisibilityScope = animatedVisibilityScope,
                        state = rememberSharedContentState(
                            key = "recipe_image"
                        )
                    )
            )

            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    recipe.type.text(LocalContext.current),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.sharedElement(
                        animatedVisibilityScope = animatedVisibilityScope,
                        state = rememberSharedContentState(
                            key = "recipe_type"
                        )
                    )
                )
                Text(
                    recipe.name, modifier = Modifier
                        .padding(top = 8.dp)
                        .sharedElement(
                            animatedVisibilityScope = animatedVisibilityScope,
                            state = rememberSharedContentState(
                                key = "recipe_name"
                            )
                        ), style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}

@Preview
@Composable
fun RecipeDetailsPreview() {
    SharedElementPreview { animatedVisibilityScope, sharedTransitionScope ->
        RecipeDetails(
            animatedVisibilityScope = animatedVisibilityScope,
            sharedTransitionScope = sharedTransitionScope,
            recipe = createDummyModel()
        )
    }
}


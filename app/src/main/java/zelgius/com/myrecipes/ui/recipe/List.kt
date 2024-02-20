package zelgius.com.myrecipes.ui.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.entities.asModel
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.text
import zelgius.com.myrecipes.preview.createDummySample

@Composable
fun RecipeList(list: Flow<PagingData<Recipe>>) {
    val items = list.collectAsLazyPagingItems()

    LazyColumn{
        items(count = items.itemCount) { index ->
            val recipe = items[index]

            recipe?.let {
                RecipeListItem(recipe = recipe, Modifier.fillParentMaxWidth().padding(vertical = 2.dp, horizontal = 8.dp))
            }
        }
    }
}

@Composable
fun RecipeListItem(recipe: Recipe, modifier: Modifier= Modifier, onClick: (Recipe) -> Unit = {}) {
    Card(shape = RoundedCornerShape(4.dp), modifier =  modifier.clickable { onClick(recipe) }) {
        Row(modifier =  Modifier.padding(8.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.imageUrl)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_dish),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp)
            )

            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(recipe.type.text(LocalContext.current), style = MaterialTheme.typography.labelMedium)
                Text(recipe.name, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Preview
@Composable
fun RecipeListPreview() {
    val list = (1..6).map {
        createDummySample(" $it")
    }

    RecipeList(list = flowOf(PagingData.from(list.map { it.asModel() })))
}

@Composable
@Preview
fun RecipeListItemPreview() {
    RecipeListItem(recipe = createDummySample().asModel())
}

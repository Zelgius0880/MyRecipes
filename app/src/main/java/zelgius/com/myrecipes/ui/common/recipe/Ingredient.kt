package zelgius.com.myrecipes.ui.common.recipe


import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import zelgius.com.myrecipes.data.DefaultIngredients
import zelgius.com.myrecipes.data.drawable
import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.IngredientForRecipe
import zelgius.com.myrecipes.data.entities.asModel
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.text
import zelgius.com.myrecipes.ui.common.StepCard
import zelgius.com.myrecipes.ui.md_blue_grey_700
import kotlin.random.Random

@Composable
fun Ingredient(
    ingredient: Ingredient,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    val drawable = ingredient.drawable
    val text = ingredient.text(LocalContext.current)

    if (drawable != null) StepCard(
        image = drawable,
        text = text,
        avatarColor = md_blue_grey_700,
        shape = shape,
        modifier = modifier
    )
    else StepCard(
        "${ingredient.name.uppercase().first()}",
        shape = shape,
        text = text,
        avatarColor = md_blue_grey_700,
        modifier = modifier
    )
}

@Composable
fun Ingredient(
    ingredient: Ingredient,
    modifier: Modifier = Modifier,
    text: String = ingredient.text(LocalContext.current),
    shape: Shape = MaterialTheme.shapes.medium,
) {
    val drawable = ingredient.drawable

    if (drawable != null) StepCard(
        image = drawable,
        text = text,
        avatarColor = md_blue_grey_700,
        shape = shape,
        modifier = modifier
    )
    else StepCard(
        "${ingredient.name.uppercase().first()}",
        shape = shape,
        text = text,
        avatarColor = md_blue_grey_700,
        modifier = modifier
    )
}


@Preview
@Composable
fun IngredientPreviewDefaultIngredient() {
    val ingredient = DefaultIngredients.entries.random().let {
        IngredientForRecipe(
            name = it.name,
            imageUrl = it.url,
            sortOrder = 0,
            unit = IngredientEntity.Unit.entries.random(),
            quantity = Random.nextInt(50, 101).toDouble(),
            optional = false,
            id = null,
            refRecipe = null,
            refStep = null,
        )
    }

    Ingredient(ingredient = ingredient.asModel())
}

@Preview
@Composable
fun IngredientPreviewLetterIngredient() {
    val ingredient = DefaultIngredients.entries.random().let {
        IngredientForRecipe(
            name = it.name,
            imageUrl = null,
            sortOrder = 0,
            unit = IngredientEntity.Unit.entries.random(),
            quantity = Random.nextInt(50, 101).toDouble(),
            optional = false,
            id = null,
            refRecipe = null,
            refStep = null,
        )
    }

    Ingredient(ingredient = ingredient.asModel())
}
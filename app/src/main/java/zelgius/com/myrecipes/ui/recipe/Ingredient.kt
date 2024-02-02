package zelgius.com.myrecipes.ui.recipe

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import zelgius.com.myrecipes.data.entities.DefaultIngredients
import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.IngredientForRecipe
import zelgius.com.myrecipes.data.entities.text
import zelgius.com.myrecipes.ui.common.StepCard
import zelgius.com.myrecipes.ui.md_blue_grey_700
import kotlin.random.Random

@Composable
fun Ingredient(
    ingredient: IngredientForRecipe,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    val drawable = ingredient.drawable
    val text = ingredient.text(LocalContext.current)

    if (drawable != null) StepCard(
        image = drawable,
        text = text,
        avatarColor = md_blue_grey_700,
        shape = shape
    )
    else StepCard(
        "${ingredient.name.uppercase().first()}",
        shape = shape,
        text = text,
        avatarColor = md_blue_grey_700
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

    Ingredient(ingredient = ingredient)
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

    Ingredient(ingredient = ingredient)
}
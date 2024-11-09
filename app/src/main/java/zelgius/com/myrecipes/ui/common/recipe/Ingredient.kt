package zelgius.com.myrecipes.ui.common.recipe


import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import zelgius.com.myrecipes.data.DefaultIngredients
import zelgius.com.myrecipes.data.drawable
import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.IngredientForRecipe
import zelgius.com.myrecipes.data.entities.asModel
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.text
import zelgius.com.myrecipes.ui.common.LocalStepCardValues
import zelgius.com.myrecipes.ui.common.StepCard
import zelgius.com.myrecipes.ui.md_blue_grey_700
import zelgius.com.myrecipes.utils.conditional
import kotlin.random.Random

@Composable
fun Ingredient(
    ingredient: Ingredient,
    modifier: Modifier = Modifier,
) {
    val drawable = ingredient.drawable
    val text = ingredient.text(LocalContext.current)

    CompositionLocalProvider(
        LocalStepCardValues provides LocalStepCardValues.current.copy(
            avatarColor = md_blue_grey_700
        )
    ) {
        if (drawable != null) StepCard(
            image = drawable,
            text = text,
            modifier = modifier
        )
        else StepCard(
            "${ingredient.name.uppercase().first()}",
            text = text,
            modifier = modifier.conditional(ingredient.optional == true){alpha(0.6f)},
            imageUrl = ingredient.imageUrl
        )
    }
}

@Composable
fun Ingredient(
    ingredient: Ingredient,
    modifier: Modifier = Modifier,
    text: String = ingredient.text(LocalContext.current),
) {
    val drawable = ingredient.drawable

    CompositionLocalProvider(
        LocalStepCardValues provides LocalStepCardValues.current.copy(
            avatarColor = md_blue_grey_700
        )
    ) {
        if (drawable != null) StepCard(
            image = drawable,
            text = text,
            modifier = modifier
        )
        else StepCard(
            "${ingredient.name.uppercase().first()}",
            text = text,
            modifier = modifier,
            imageUrl = ingredient.imageUrl
        )
    }
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
            refIngredient = null,
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
            unit = IngredientEntity.Unit.CUP,
            quantity = 1.3333,
            optional = false,
            id = null,
            refIngredient = null,
            refRecipe = null,
            refStep = null,
        )
    }

    Ingredient(ingredient = ingredient.asModel())
}
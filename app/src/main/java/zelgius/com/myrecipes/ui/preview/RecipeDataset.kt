package zelgius.com.myrecipes.ui.preview

import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.IngredientForRecipe
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.entities.StepEntity
import zelgius.com.myrecipes.data.entities.asModel

fun createDummySample(id: Long = 0, suffix: String = ""): RecipeEntity {

    val ingredients = listOf(
        IngredientForRecipe(
            1,
            2.0,
            IngredientEntity.Unit.UNIT,
            "Eggs",
            "drawable://egg",
            1,
            id,
            3
        ),
        IngredientForRecipe(
            2,
            500.0,
            IngredientEntity.Unit.GRAMME,
            "Flour",
            "drawable://flour",
            2,
            id,
            3
        ),
        IngredientForRecipe(
            3,
            200.0,
            IngredientEntity.Unit.MILLILITER,
            "Water",
            "drawable://water",
            3,
            id,
            3
        ),
        IngredientForRecipe(
            4,
            2.33,
            IngredientEntity.Unit.CUP,
            "Butter",
            "drawable://butter",
            4,
            id,
            null
        )
    )

    val currentRecipe = RecipeEntity(
        id = id,
        name = "Recipe For Testing$suffix", imageURL =
        "https://img.huffingtonpost.com/asset/5c92b00222000033001b332d.jpeg?ops=scalefit_630_noupscale",
        ingredients = ingredients.toMutableList()
    ).apply {

        steps.add(StepEntity(id = 1, text = "Step 1", optional = true, refRecipe = null, order = 1))
        steps.add(
            StepEntity(
                id = 2,
                text = "Step 2",
                optional = false,
                refRecipe = null,
                order = 2
            )
        )
        steps.add(
            StepEntity(
                id = 3,
                text = "Step 3",
                optional = false,
                refRecipe = null,
                order = 3
            ).apply {
                this.ingredients.add(ingredients[0])
                this.ingredients.add(ingredients[1])
                this.ingredients.add(ingredients[2])
            })
    }

    return currentRecipe
}

fun createDummyModel(id: Long = 0, suffix: String = "") =
    createDummySample(id, suffix = suffix).asModel()
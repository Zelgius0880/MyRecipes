package zelgius.com.myrecipes.preview

import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.IngredientForRecipe
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.entities.StepEntity

fun createDummySample(suffix: String = ""): RecipeEntity {

    val currentRecipe = RecipeEntity(
        name = "Recipe For Testing$suffix", imageURL =
        "https://img.huffingtonpost.com/asset/5c92b00222000033001b332d.jpeg?ops=scalefit_630_noupscale").apply {

        ingredients.add(
            IngredientForRecipe(
                null,
                2.0,
                IngredientEntity.Unit.UNIT,
                "Eggs",
                "drawable://egg",
                1,
                null,
                null
            )
        )
        ingredients.add(
            IngredientForRecipe(
                null,
                500.0,
                IngredientEntity.Unit.GRAMME,
                "Flour",
                "drawable://flour",
                2,
                null,
                null
            )
        )
        ingredients.add(
            IngredientForRecipe(
                null,
                200.0,
                IngredientEntity.Unit.MILLILITER,
                "Water",
                "drawable://water",
                3,
                null,
                null
            )
        )
        ingredients.add(
            IngredientForRecipe(
                null,
                2.33,
                IngredientEntity.Unit.CUP,
                "Butter",
                "drawable://butter",
                4,
                null,
                null
            )
        )

        steps.add(StepEntity(id =null, text ="Step 1", optional =  true, refRecipe =  null, order = 1 ))
        steps.add(StepEntity(id = null, text = "Step 2", optional = false, refRecipe =  null, order = 2 ))
        steps.add(StepEntity(id = null, text = "Step 3", optional = false, refRecipe = null, order = 3).apply {
            ingredients.add(
                IngredientForRecipe(
                    null,
                    1.0,
                    IngredientEntity.Unit.TEASPOON,
                    "Salt",
                    "drawable://salt",
                    4,
                    null,
                    null
                ).also {
                    it.step = this
                }
            )

            ingredients.add(
                IngredientForRecipe(
                    null,
                    1000.0,
                    IngredientEntity.Unit.TABLESPOON,
                    "Sugar",
                    "drawable://sugar",
                    4,
                    null,
                    null
                ).also {
                    it.step = this
                }
            )

            ingredients.add(
                IngredientForRecipe(
                    null,
                    1000.0,
                    IngredientEntity.Unit.LITER,
                    "Milk",
                    "drawable://milk",
                    4,
                    null,
                    null
                ).also {
                    it.step = this
                }
            )
        })
    }

    return currentRecipe
}
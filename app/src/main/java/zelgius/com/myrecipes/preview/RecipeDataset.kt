package zelgius.com.myrecipes.preview

import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.IngredientForRecipe
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.entities.StepEntity
import zelgius.com.myrecipes.data.entities.asModel

fun createDummySample(id: Long = 0, suffix: String = ""): RecipeEntity {

    val currentRecipe = RecipeEntity(
        name = "Recipe For Testing$suffix", imageURL =
        "https://img.huffingtonpost.com/asset/5c92b00222000033001b332d.jpeg?ops=scalefit_630_noupscale").apply {

        ingredients.add(
            IngredientForRecipe(
                id,
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
                2,
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
                3,
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
                4,
                2.33,
                IngredientEntity.Unit.CUP,
                "Butter",
                "drawable://butter",
                4,
                null,
                null
            )
        )

        steps.add(StepEntity(id =1, text ="Step 1", optional =  true, refRecipe =  null, order = 1 ))
        steps.add(StepEntity(id = 2, text = "Step 2", optional = false, refRecipe =  null, order = 2 ))
        steps.add(StepEntity(id = 3, text = "Step 3", optional = false, refRecipe = null, order = 3).apply {
            ingredients.add(
                IngredientForRecipe(
                    1,
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
                    2,
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
                    3,
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

fun createDummyModel(id: Long = 0, suffix: String = "") = createDummySample(suffix = suffix).asModel()
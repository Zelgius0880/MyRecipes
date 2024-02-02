package zelgius.com.myrecipes.data.model

import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.IngredientForRecipe

data class Ingredient(
    val id: Long?,
    val quantity: Double,
    val unit: Unit,
    val name: String,
    val imageUrl: String?,
    val optional: Boolean?,
    val sortOrder: Int,
    val recipe: Recipe,
    val step: Step?
) {
    enum class Unit {
        Milliliter,
        Liter,
        Unit,
        TeaSpoon,
        TableSpoon,
        Gramme,
        Kilogramme,
        Cup,
        Pinch
    }
}

fun Ingredient.toEntity() = IngredientForRecipe(
    id = id,
    quantity = quantity,
    unit = when(unit) {
        Ingredient.Unit.Milliliter -> IngredientEntity.Unit.MILLILITER
        Ingredient.Unit.Liter -> IngredientEntity.Unit.LITER
        Ingredient.Unit.Unit -> IngredientEntity.Unit.UNIT
        Ingredient.Unit.TeaSpoon -> IngredientEntity.Unit.TEASPOON
        Ingredient.Unit.TableSpoon -> IngredientEntity.Unit.TABLESPOON
        Ingredient.Unit.Gramme -> IngredientEntity.Unit.GRAMME
        Ingredient.Unit.Kilogramme -> IngredientEntity.Unit.KILOGRAMME
        Ingredient.Unit.Cup -> IngredientEntity.Unit.CUP
        Ingredient.Unit.Pinch -> IngredientEntity.Unit.PINCH
    },
    name = name,
    imageUrl = imageUrl,
    optional = optional,
    sortOrder = sortOrder,
    refRecipe = recipe.id ,
    refStep = step?.id,
)
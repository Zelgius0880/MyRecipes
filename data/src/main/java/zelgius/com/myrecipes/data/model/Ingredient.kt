package zelgius.com.myrecipes.data.model

import android.content.Context
import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.IgnoredOnParcel
import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.IngredientForRecipe
import kotlinx.parcelize.Parcelize
import java.text.DecimalFormat

@Parcelize
data class Ingredient(
    val id: Long?,
    val idIngredient: Long?,
    val quantity: Double,
    val unit: Unit,
    val name: String,
    val imageUrl: String?,
    val optional: Boolean?,
    val sortOrder: Int,
    val recipe: Recipe?,
    val step: Step?,
) : Parcelable {

    enum class Unit {
        Gramme,
        Kilogramme,
        Milliliter,
        Liter,
        Unit,
        TeaSpoon,
        TableSpoon,
        Cup,
        Pinch
    }

    companion object {
        val Empty = Ingredient(
            id = null,
            idIngredient = null,
            name = "",
            quantity = 0.0,
            unit = Unit.Unit,
            imageUrl = null,
            optional = false,
            sortOrder = 0,
            recipe = null,
            step = null
        )
    }
}

fun Ingredient.Unit.asEntity() = when (this) {
    Ingredient.Unit.Milliliter -> IngredientEntity.Unit.MILLILITER
    Ingredient.Unit.Liter -> IngredientEntity.Unit.LITER
    Ingredient.Unit.Unit -> IngredientEntity.Unit.UNIT
    Ingredient.Unit.TeaSpoon -> IngredientEntity.Unit.TEASPOON
    Ingredient.Unit.TableSpoon -> IngredientEntity.Unit.TABLESPOON
    Ingredient.Unit.Gramme -> IngredientEntity.Unit.GRAMME
    Ingredient.Unit.Kilogramme -> IngredientEntity.Unit.KILOGRAMME
    Ingredient.Unit.Cup -> IngredientEntity.Unit.CUP
    Ingredient.Unit.Pinch -> IngredientEntity.Unit.PINCH
}

fun Ingredient.asEntity() = IngredientForRecipe(
    id = id,
    refIngredient = idIngredient,
    quantity = quantity,
    unit = unit.asEntity(),
    name = name,
    imageUrl = imageUrl,
    optional = optional,
    sortOrder = sortOrder,
    refRecipe = recipe?.id,
    refStep = step?.id,
)
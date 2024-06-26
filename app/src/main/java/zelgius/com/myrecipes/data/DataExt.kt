package zelgius.com.myrecipes.data

import android.content.Context
import androidx.annotation.DrawableRes
import zelgius.com.myrecipes.data.model.Ingredient
import java.text.DecimalFormat
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.utils.context
import zelgius.com.myrecipes.utils.round


fun Ingredient.text(context: Context): String {
    val abrv = when (unit) {
        Ingredient.Unit.Milliliter -> context.getString(R.string.milliliter_abrv)
        Ingredient.Unit.Liter -> context.getString(R.string.liter_abrv)
        Ingredient.Unit.Unit -> context.getString(R.string.unit_abrv)
        Ingredient.Unit.TeaSpoon -> context.getString(R.string.teaspoon_abrv)
        Ingredient.Unit.TableSpoon -> context.getString(R.string.tablespoon_abrv)
        Ingredient.Unit.Gramme -> context.getString(R.string.gramme_abrv)
        Ingredient.Unit.Kilogramme -> context.getString(R.string.kilogramme_abrv)
        Ingredient.Unit.Cup -> context.getString(R.string.cup_abrv)
        Ingredient.Unit.Pinch -> context.getString(R.string.pinch_abrv)
    }

    return if (unit != Ingredient.Unit.Cup) {
        String.format(
            "%s %s %s",
            DecimalFormat("#0.##").format(quantity),
            abrv,
            name
        )
    } else {
        val part1 = if (quantity.toInt() == 0) "" else "${quantity.toInt()} "

        val part2 = when ("${(quantity - quantity.toInt()).round(2)}".trim()) {
            "0.0", "0" -> ""
            "0.33", "0.34" -> "1/3 "
            "0.66", "0.67" -> "2/3 "
            "0.25" -> "1/4 "
            "0.5" -> "1/2 "
            "0.75" -> "3/4 "
            else -> "${DecimalFormat("#0.##").format(quantity - quantity.toInt())} "
        }

        return String.format("%s%s%s %s", part1, part2, abrv, name)
    }
}

val Ingredient.drawable: Int?
    @DrawableRes
    get() = DefaultIngredients.entries.firstOrNull { it.url == imageUrl }?.drawable


fun Recipe.Type.text(context: Context) = when (this) {
    Recipe.Type.Meal -> context.getString(R.string.meal)
    Recipe.Type.Dessert -> context.getString(R.string.dessert)
    Recipe.Type.Other -> context.getString(R.string.other)
}
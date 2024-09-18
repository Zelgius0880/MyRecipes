package zelgius.com.myrecipes.utils

import TextDrawable
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.DefaultIngredients
import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.model.Ingredient
import java.util.*
import kotlin.math.roundToInt


object UiUtils {

    /**
     * Get a drawable with a circular background and the corresponding drawable if item.imageURL is known as a drawable from thee resources(see KNOWN_ICONS)
     * or if not, return a drawable with a circular background and the first letter of the name of the ingredient
     * @param context Context
     * @param item Ingredient
     * @param color Int?        if not null, set the color of the letter of the ingredient, else the color will be white
     * @return (Drawable?)
     */
    fun getDrawableForImageView(
        context: Context,
        item: Ingredient,
        padding: Float = 8f,
        @ColorInt color: Int? = null
    ): Drawable =
        getDrawableForImageView(
            context,
            IngredientEntity(name = item.name, imageURL = item.imageUrl, id = item.id),
            padding = padding,
            color = color
        )

    /**
     * Get a drawable with a circular background and the corresponding drawable if item.imageURL is known as a drawable from thee resources(see KNOWN_ICONS)
     * or if not, return a drawable with a circular background and the first letter of the name of the ingredient
     * @param context Context
     * @param item Ingredient
     * @param color Int?        if not null, set the color of the letter of the ingredient, else the color will be white
     * @return (Drawable?)
     */
    private fun getDrawableForImageView(
        context: Context,
        item: IngredientEntity,
        padding: Float = 8f,
        @ColorInt color: Int? = null
    ): Drawable =
        DefaultIngredients.entries.find { it.url == item.imageURL }.let {
            if (it != null) {
                LayerDrawable(
                    arrayOf(
                        ContextCompat.getDrawable(context, R.drawable.background_circle),
                        ContextCompat.getDrawable(context, it.drawable)
                    )
                ).apply {
                    val dp = context.dpToPx(padding).roundToInt()
                    setLayerInset(1, dp, dp, dp, dp)
                }
            } else {
                TextDrawable(
                    context.resources,
                    "${item.name.uppercase(Locale.getDefault())[0]}"
                ).apply {
                    color?.let { c ->
                        colorFilter = PorterDuffColorFilter(
                            c,
                            PorterDuff.Mode.SRC_ATOP
                        )
                    }
                }
            }
        }

}
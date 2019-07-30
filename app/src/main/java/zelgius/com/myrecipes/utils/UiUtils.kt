package zelgius.com.myrecipes.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.amulyakhare.textdrawable.TextDrawable
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.adapter_ingredient.view.*
import kotlinx.android.synthetic.main.layout_header_edit.*
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.IngredientForRecipe
import zelgius.com.myrecipes.entities.Recipe
import java.lang.IllegalStateException
import java.util.*
import kotlin.math.roundToInt


object UiUtils {
    val KNOWN_ICONS = arrayOf(
        "drawable://egg",
        "drawable://flour",
        "drawable://sugar",
        "drawable://water",
        "drawable://milk",
        "drawable://butter",
        "drawable://salt"
    )

    fun getIngredientDrawable(imageView: ImageView, item: IngredientForRecipe) =
        getIngredientDrawable(imageView, Ingredient(name = item.name, imageURL = item.imageUrl, id = item.id))


    fun getIngredientDrawable(imageView: ImageView, item: Ingredient) {
        if (KNOWN_ICONS.contains(item.imageURL)) {
            val image = ContextCompat.getDrawable(
                imageView.context, when (item.imageURL) {
                    "drawable://egg" -> R.drawable.ic_eggs
                    "drawable://flour" -> R.drawable.ic_flour
                    "drawable://sugar" -> R.drawable.ic_suggar
                    "drawable://water" -> R.drawable.ic_drop
                    "drawable://milk" -> R.drawable.ic_milk
                    "drawable://butter" -> R.drawable.ic_butter
                    "drawable://salt" -> R.drawable.ic_salt
                    else -> throw IllegalStateException("It's impossible to be there")
                }
            )

            imageView.setImageDrawable(
                LayerDrawable(
                    arrayOf(ColorDrawable(ContextCompat.getColor(imageView.context, R.color.md_blue_grey_700)), image)
                ).apply {
                    val dp = imageView.context.dpToPx(4f).roundToInt()
                    setLayerInset(1, dp, dp, dp, dp)
                }
            )
        } else
            imageView.setImageDrawable(
                TextDrawable.builder()
                    .beginConfig()
                    .fontSize(imageView.context.dpToPx(20f).toInt())
                    .width(imageView.context.dpToPx(36f).toInt())
                    .height(imageView.context.dpToPx(36f).toInt())
                    .bold()
                    .endConfig()
                    .buildRound(
                    "${item.name.toUpperCase(Locale.getDefault())[0]}",
                    ContextCompat.getColor(imageView.context, R.color.md_blue_grey_700)
                )
            )
    }


    fun generateCircleBitmap(context: Context, circleColor: Int, diameterDP: Float, text: String?): Bitmap {
        val textColor = -0x1

        val metrics = Resources.getSystem().displayMetrics
        val diameterPixels = diameterDP * (metrics.densityDpi / 160f)
        val radiusPixels = diameterPixels / 2

        // Create the bitmap
        val output = Bitmap.createBitmap(
            diameterPixels.toInt(), diameterPixels.toInt(),
            Bitmap.Config.ARGB_8888
        )

        // Create the canvas to draw on
        val canvas = Canvas(output)
        canvas.drawARGB(0, 0, 0, 0)

        // Draw the circle
        val paintC = Paint()
        paintC.isAntiAlias = true
        paintC.color = circleColor
        canvas.drawCircle(radiusPixels, radiusPixels, radiusPixels, paintC)

        // Draw the text
        if (text != null && text.isNotEmpty()) {
            val paintT = Paint()
            paintT.color = textColor
            paintT.isAntiAlias = true
            paintT.textSize = radiusPixels * 2
            val typeFace = Typeface.createFromAsset(context.assets, "fonts/Roboto-Thin.ttf")
            paintT.typeface = typeFace
            val textBounds = Rect()
            paintT.getTextBounds(text, 0, text.length, textBounds)
            canvas.drawText(
                text,
                radiusPixels - textBounds.exactCenterX(),
                radiusPixels - textBounds.exactCenterY(),
                paintT
            )
        }

        return output
    }


    fun bindHeader(recipe: Recipe, viewHolder: HeaderViewHolder) {
        val context = viewHolder.root.context

        viewHolder.root.transitionName = "cardView${recipe.id}"
        viewHolder.imageView.transitionName = "imageView${recipe.id}"
        viewHolder.name.transitionName = "name${recipe.id}"
        viewHolder.category.transitionName = "category${recipe.id}"


        val category = when (recipe.type) {
            Recipe.Type.MEAL -> context.getString(R.string.meal)
            Recipe.Type.DESSERT -> context.getString(R.string.dessert)
            Recipe.Type.OTHER -> context.getString(R.string.other)
        }

        with(viewHolder.category){
            when(this){
                is Spinner -> {
                    val typeStringArray = context.resources.getStringArray(R.array.category_array)
                    val spinnerArrayAdapter = ArrayAdapter(
                        context, R.layout.adapter_text_category,
                        typeStringArray
                    ) //selected item will look like a spinner set from XML
                    spinnerArrayAdapter.setDropDownViewResource(
                        android.R.layout
                            .simple_spinner_dropdown_item
                    )
                    adapter = spinnerArrayAdapter
                    setSelection(typeStringArray.indexOf(category))
                }
                is TextView -> this.text = category
            }
        }

        with(viewHolder.name){
            when(this){
                is TextInputLayout -> editText?.setText(recipe.name)
                is TextView -> text = recipe.name
                else -> error("should not be there")
            }
        }

        if (!recipe.imageURL.isNullOrEmpty()) {
            Picasso.get()
                .load(recipe.imageURL)
                .resize(2048, 2048)
                .centerCrop()
                .into(viewHolder.imageView)

            viewHolder.imageView.setPadding(0, 0, 0, 0)
        } else {
            viewHolder.imageView.setImageResource(R.drawable.ic_dish)

            context.let {
                viewHolder.imageView.setPadding(
                    it.dpToPx(8f).toInt(),
                    it.dpToPx(8f).toInt(),
                    it.dpToPx(8f).toInt(),
                    it.dpToPx(8f).toInt()
                )

            }


        }
    }

    class HeaderViewHolder(val root: View, val imageView: ImageView, val name: View, val category: View)
}
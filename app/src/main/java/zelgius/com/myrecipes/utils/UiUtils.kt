package zelgius.com.myrecipes.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.amulyakhare.textdrawable.TextDrawable
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import zelgius.com.myrecipes.ActionBroadcastReceiver
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.IngredientForRecipe
import zelgius.com.myrecipes.entities.Recipe
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
        getIngredientDrawable(
            imageView,
            Ingredient(name = item.name, imageURL = item.imageUrl, id = item.id)
        )


    fun getIngredientDrawable(
        imageView: ImageView,
        item: Ingredient, @ColorInt color: Int? = null
    ) {
        imageView.setImageDrawable(getDrawableForImageView(imageView.context, item, color = color))
    }

    /**
     * Get a drawable with a circular background and the corresponding drawable if item.imageURL is known as a drawable from thee resources(see KNOWN_ICONS)
     * or if not, return a drawable with a circular background and the first letter of the name of the ingredient
     * @param context Context
     * @param item Ingredient
     * @param color Int?        if not null, set the color of the letter of the ingredient, else the color will be white
     * @return (Drawable?)
     */
    fun getDrawableForImageView(context: Context, item: IngredientForRecipe, padding: Float = 8f, @ColorInt color: Int? = null) =
        getDrawableForImageView(context, Ingredient(name = item.name, imageURL = item.imageUrl, id = item.id), padding = padding, color = color)

    /**
     * Get a drawable with a circular background and the corresponding drawable if item.imageURL is known as a drawable from thee resources(see KNOWN_ICONS)
     * or if not, return a drawable with a circular background and the first letter of the name of the ingredient
     * @param context Context
     * @param item Ingredient
     * @param color Int?        if not null, set the color of the letter of the ingredient, else the color will be white
     * @return (Drawable?)
     */
    fun getDrawableForImageView(context: Context, item: Ingredient, padding: Float = 8f, @ColorInt color: Int? = null) =
        if (KNOWN_ICONS.contains(item.imageURL)) {
            LayerDrawable(
                arrayOf(
                    context.getDrawable(R.drawable.background_circle)
                    , getDrawableFromName(context, item.imageURL!!)
                )
            ).apply {
                val dp = context.dpToPx(padding).roundToInt()
                setLayerInset(1, dp, dp, dp, dp)
            }
        } else {
            TextDrawable.builder()
                .beginConfig()
                .fontSize(context.dpToPx(20f).toInt())
                .width(context.dpToPx(36f).toInt())
                .height(context.dpToPx(36f).toInt())
                .bold().apply {
                    if (color != null)
                        textColor(color)
                }
                .endConfig()
                .buildRound(
                    "${item.name.toUpperCase(Locale.getDefault())[0]}",
                    ContextCompat.getColor(context, R.color.md_blue_grey_700)
                )
        }


    fun getDrawable(
        context: Context,
        drawableName: String, @ColorInt color: Int? = null
    ): Drawable? {
        if (KNOWN_ICONS.contains(drawableName)) {
            return getDrawableFromName(context, drawableName)
        } else
            return TextDrawable.builder()
                .beginConfig()
                .fontSize(context.dpToPx(20f).toInt())
                .width(context.dpToPx(36f).toInt())
                .height(context.dpToPx(36f).toInt())
                .bold().apply {
                    if (color != null)
                        textColor(color)
                }
                .endConfig()
                .buildRound(
                    "${drawableName.toUpperCase(Locale.getDefault())[0]}",
                    ContextCompat.getColor(context, R.color.md_blue_grey_700)
                )
    }

    private fun getDrawableFromName(
        context: Context,
        drawableName: String
    ): Drawable? {
        return ContextCompat.getDrawable(
            context, when (drawableName) {
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
    }

    fun getCircleDrawable(
        imageView: ImageView, @DrawableRes drawable: Int, @ColorRes color: Int,
        dp: Float = 4f
    ) {
        imageView.setImageDrawable(
            LayerDrawable(
                arrayOf(
                    ColorDrawable(
                        ContextCompat.getColor(
                            imageView.context,
                            color
                        )
                    ), imageView.context.getDrawable(drawable)
                )
            ).apply {
                val px = imageView.context.dpToPx(dp).roundToInt()
                setLayerInset(1, px, px, px, px)
            }
        )
    }


    fun generateCircleBitmap(
        context: Context,
        circleColor: Int,
        diameterDP: Float,
        text: String?
    ): Bitmap {
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

        // Draw the background_circle
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

        with(viewHolder.category) {
            when (this) {
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

        with(viewHolder.name) {
            when (this) {
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

    class HeaderViewHolder(
        val root: View,
        val imageView: ImageView,
        val name: View,
        val category: View
    )
}
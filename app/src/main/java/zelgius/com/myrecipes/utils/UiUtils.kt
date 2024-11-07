package zelgius.com.myrecipes.utils

import TextDrawable
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.DefaultIngredients
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
    ): Drawable {
        val defaultIngredient = DefaultIngredients.entries.find { it.url == item.imageUrl }
        return LayerDrawable(
            arrayOf(
                ContextCompat.getDrawable(context, R.drawable.background_circle),
                when {
                    defaultIngredient != null ->
                        ContextCompat.getDrawable(context, defaultIngredient.drawable)

                    item.imageUrl != null -> {
                        getFromUrl(context, item)
                    }

                    else -> getTextDrawable(context, item)
                }
            )
        ).apply {
            val dp = context.dpToPx(padding).roundToInt()
            if (defaultIngredient != null) {
                setLayerInset(1, dp, dp, dp, dp)
            } else if (this.getDrawable(1) is TextDrawable) {
                setLayerInset(1, 0, context.dpToPx(12f).roundToInt(), 0, 0)
            }
        }
    }

    private fun getFromUrl(context: Context, ingredient: Ingredient): Drawable {
        val contentResolver = context.contentResolver
        try {
            val hardwareBitmap = ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(
                    contentResolver,
                    ingredient.imageUrl!!.toUri()
                )
            )

            val bitmap = hardwareBitmap.copy(Bitmap.Config.ARGB_8888, true)
            hardwareBitmap.recycle()

            val output =
                Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)

            paint.isAntiAlias = true
            paint.isFilterBitmap = true
            paint.isDither = true
            canvas.drawARGB(0, 0, 0, 0)
            canvas.drawCircle(
                bitmap.width / 2 + 0.7f,
                bitmap.height / 2 + 0.7f,
                bitmap.width / 2 + 0.1f,
                paint
            )
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)

            return BitmapDrawable(
                context.resources,
                output
            ).also {
                bitmap.recycle()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return getTextDrawable(context, ingredient)
        }
    }

    private fun getTextDrawable(context: Context, item: Ingredient): Drawable {
        return TextDrawable(
            context.resources,
            item.name.first().toString().uppercase(Locale.getDefault())
        )
    }
}


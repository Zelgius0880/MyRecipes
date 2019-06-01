package zelgius.com.myrecipes.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.util.DisplayMetrics
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.amulyakhare.textdrawable.TextDrawable
import kotlinx.android.synthetic.main.adapter_ingredient.view.*
import kotlinx.android.synthetic.main.layout_header.*
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.IngredientForRecipe
import java.lang.IllegalStateException
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
                    "${item.name.toUpperCase()[0]}",
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
}
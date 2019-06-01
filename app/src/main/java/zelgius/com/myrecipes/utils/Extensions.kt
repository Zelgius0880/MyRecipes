package zelgius.com.myrecipes.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.round
import kotlin.reflect.full.declaredMemberProperties

fun Any.asMap(): Map<String, Any?> =
    this::class.declaredMemberProperties.map {
        it.name to it.getter.call(this)
    }.toMap()


fun TextInputLayout.toDouble()
        = editText!!.text.toString().replace(',', '.').toDouble()

fun Context.resourcePath() =
    Uri.parse("android.resource://$packageName")

fun Context.asstesPath() =
    Uri.parse("file:///android_asset/")

fun Context.getCompatColor(@ColorRes color: Int) = ContextCompat.getColor(this, color)


fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

/**
 *
 * Get the value of dp to Pixel according to density of the screen
 *
 * @receiver Context
 * @param dp Float      the value in dp
 * @return the value of dp to Pixel according to density of the screen
 */
fun Context.dpToPx(dp: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

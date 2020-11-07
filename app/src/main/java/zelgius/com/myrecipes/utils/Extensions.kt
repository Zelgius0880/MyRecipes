package zelgius.com.myrecipes.utils

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.viewbinding.ViewBinding
import com.google.android.material.textfield.TextInputLayout
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.math.round
import kotlin.math.roundToInt

val ViewBinding.context: Context
    get() = root.context

fun TextInputLayout.toDouble() = editText!!.text.toString().replace(',', '.').toDouble()

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

@ColorInt
fun Context.getColor(@ColorRes color: Int, alpha: Float) =
    getColor(color).let{
        Color.argb(
            (Color.alpha(color) * alpha).roundToInt(),
            Color.red(it),
            Color.green(it),
            Color.blue(it))
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


fun ByteArray.unzip(): ByteArray {
    val zis = ZipInputStream(ByteArrayInputStream(this))
    val data: ZipEntry =  zis.nextEntry
    println(data.name)

    val os = ByteArrayOutputStream()
    val buffer = ByteArray(1024)

    var byte = zis.read(buffer)
    while (byte > 0) {
        os.write(buffer, 0, byte)
        byte = zis.read(buffer)
    }

    val result = os.toByteArray()
    os.close()
    zis.closeEntry()

    return result
}
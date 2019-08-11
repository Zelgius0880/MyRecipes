package zelgius.com.myrecipes.utils

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputLayout
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.math.round
import kotlin.math.roundToInt
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
    var data: ZipEntry =  zis.nextEntry
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

fun <T>LiveData<T>.observe(lifecycleOwner: LifecycleOwner, work: (T) -> Unit) {
    observe(lifecycleOwner, Observer {
        work(it)
    })
}
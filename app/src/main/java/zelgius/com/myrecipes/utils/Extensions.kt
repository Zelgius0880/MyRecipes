package zelgius.com.myrecipes.utils

import android.content.Context
import android.util.Log
import android.util.TypedValue
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowWidthSizeClass
import zelgius.com.myrecipes.BuildConfig
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.math.round

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


fun Modifier.conditional(condition : Boolean, modifier : Modifier.() -> Modifier) : Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}


fun<T> Modifier.ifNotNull (item: T?, modifier : Modifier.(T) -> Modifier) : Modifier {
    return if (item != null)  {
        then(modifier(item))
    } else {
        this
    }
}

@Composable
fun isTwoPanes(): Boolean =
    currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT

object Logger {
    val TAG = Logger::class.simpleName?:"Logger"
    fun i(tag:String, message: String) {
        if(BuildConfig.DEBUG) Log.i(tag, message)
    }

    fun i(message: String) {
        i(TAG, message)
    }

    fun w(tag:String, message: String) {
        if(BuildConfig.DEBUG) Log.w(tag, message)
    }

    fun w(message: String) {
        w(TAG, message)
    }
}
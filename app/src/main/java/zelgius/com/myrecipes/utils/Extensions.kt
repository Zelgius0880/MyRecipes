package zelgius.com.myrecipes.utils

import android.content.Context
import android.util.TypedValue
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

@Composable
fun<T> Modifier.ifNotNull (item: T?, modifier : @Composable Modifier.(T) -> Modifier) : Modifier {
    return if (item != null)  {
        then(modifier(item))
    } else {
        this
    }
}

@Composable
fun hasNavigationRail(): Boolean =
    NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo()) == NavigationSuiteType.NavigationRail

@Composable
fun hasNavigationBar(): Boolean =
    NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo()) == NavigationSuiteType.NavigationBar

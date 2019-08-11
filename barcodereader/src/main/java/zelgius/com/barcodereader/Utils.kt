package zelgius.com.barcodereader

/**
 * Created by Ezequiel Adrian on 24/02/2017.
 */

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.graphics.PointF
import android.view.WindowManager
import com.google.android.gms.common.images.Size
import kotlin.math.sqrt

object Utils {

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun getScreenHeight(c: Context): Int {
        val wm = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.y
    }

    fun getScreenWidth(c: Context): Int {
        val wm = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.x
    }

    fun getScreenRatio(c: Context): Float {
        val metrics = c.resources.displayMetrics
        return metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
    }

    fun getScreenRotation(c: Context): Int {
        val wm = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return wm.defaultDisplay.rotation
    }

    fun distancePointsF(p1: PointF, p2: PointF): Int {
        return sqrt(((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)).toDouble())
            .toInt()
    }

    fun middlePoint(p1: PointF?, p2: PointF?): PointF? {
        return if (p1 == null || p2 == null) null else PointF(
            (p1.x + p2.x) / 2,
            (p1.y + p2.y) / 2
        )
    }

    fun sizeToSize(sizes: List<Size>): Array<Size> {
        val size = arrayOfNulls<Size>(sizes.size)
        for (i in sizes.indices) {
            size[i] = Size(sizes[i].width, sizes[i].height)
        }
        return size.map { it!! }.toTypedArray()
    }
}

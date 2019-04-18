package zelgius.com.myrecipes.utils

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import zelgius.com.myrecipes.R


fun getAttributeColor(context: Context, @AttrRes resource: Int): Int {
    val typedValue = TypedValue()

    val a = context.obtainStyledAttributes(typedValue.data, intArrayOf(resource))
    val color = a.getColor(0, 0)

    a.recycle()

    return color
}

val Context.colorPrimary: Int
    get() = getAttributeColor(this, R.attr.colorPrimary)

val Context.colorPrimaryDark: Int
    get() = getAttributeColor(this, R.attr.colorPrimaryDark)

val Context.colorPrimaryVariant: Int
    get() = getAttributeColor(this, R.attr.colorPrimaryVariant)

val Context.colorSecondary: Int
    get() = getAttributeColor(this, R.attr.colorSecondary)

val Context.colorSecondaryVariant: Int
    get() = getAttributeColor(this, R.attr.colorSecondaryVariant)
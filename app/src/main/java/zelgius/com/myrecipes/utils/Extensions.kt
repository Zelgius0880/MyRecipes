package zelgius.com.myrecipes.utils

import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.doOnPreDraw
import kotlin.reflect.full.declaredMemberProperties

fun Any.asMap(): Map<String, Any?> =
    this::class.declaredMemberProperties.map {
        it.name to it.getter.call(this)
    }.toMap()

package zelgius.com.myrecipes.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.view.ViewAnimationUtils
import androidx.core.view.doOnLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import android.view.animation.AccelerateInterpolator
import android.content.Intent
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import kotlin.math.roundToInt


object AnimationUtils {
    const val EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y"
    const val EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X"
    const val EXTRA_CIRCULAR_REVEAL_SETTINGS = "EXTRA_CIRCULAR_REVEAL_SETTINGS"

    private fun getMediumDuration(context: Context) =
        context.resources.getInteger(android.R.integer.config_mediumAnimTime)

    private fun getLongDuration(context: Context) =
        context.resources.getInteger(android.R.integer.config_longAnimTime)

    fun enterCircularRevealAnimation(
        context: Context,
        view: View,
        revealSettings: RevealAnimationSetting,
        startColor: Int,
        endColor: Int,
        listener: () -> Unit = {}
    ) {
        view.doOnLayout {
            val cx = revealSettings.centerX
            val cy = revealSettings.centerY
            val width = revealSettings.width
            val height = revealSettings.height

            //Simply use the diagonal of the view
            val finalRadius = Math.sqrt((width * width + height * height).toDouble()).toFloat()
            val anim = ViewAnimationUtils.createCircularReveal(it, cx, cy, 0f, finalRadius)
            anim.duration = getMediumDuration(context).toLong()
            anim.interpolator = FastOutSlowInInterpolator()
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    listener()
                }
            })
            anim.start()
            startBackgroundColorAnimation(
                view,
                startColor,
                endColor,
                getMediumDuration(context)
            )
        }
    }

    fun exitCircularRevealAnimation(
        context: Context,
        view: View,
        revealSettings: RevealAnimationSetting,
        startColor: Int,
        endColor: Int,
        listener: () -> Unit = {}
    ) {
        val cx = revealSettings.centerX
        val cy = revealSettings.centerY
        val width = revealSettings.width
        val height = revealSettings.height

        val initRadius = Math.sqrt((width * width + height * height).toDouble()).toFloat()
        val anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initRadius, 0f)
        anim.duration = getMediumDuration(context).toLong()
        anim.interpolator = FastOutSlowInInterpolator()
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                //Important: This will prevent the view's flashing (visible between the finished animation and the Fragment remove)
                view.visibility = View.GONE
                listener()
            }
        })
        anim.start()
        startBackgroundColorAnimation(
            view,
            startColor,
            endColor,
            getMediumDuration(context)
        )
    }

    private fun startBackgroundColorAnimation(view: View, startColor: Int, endColor: Int, duration: Int) {
        val anim = ValueAnimator()
        anim.setIntValues(startColor, endColor)
        anim.setEvaluator(ArgbEvaluator())
        anim.duration = duration.toLong()
        anim.addUpdateListener { valueAnimator -> view.setBackgroundColor(valueAnimator.animatedValue as Int) }
        anim.start()
    }

    class RevealAnimationSetting(
        val centerX: Int,
        val centerY: Int,
        val width: Int,
        val height: Int
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(centerX)
            parcel.writeInt(centerY)
            parcel.writeInt(width)
            parcel.writeInt(height)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<RevealAnimationSetting> {
            override fun createFromParcel(parcel: Parcel): RevealAnimationSetting {
                return RevealAnimationSetting(parcel)
            }

            override fun newArray(size: Int): Array<RevealAnimationSetting?> {
                return arrayOfNulls(size)
            }
        }
    }


}
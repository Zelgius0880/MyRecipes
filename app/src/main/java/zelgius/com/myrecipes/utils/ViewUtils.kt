/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package zelgius.com.myrecipes.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment


object ViewUtils {
    fun hitTest(v: View, x: Int, y: Int): Boolean {
        val tx = (v.translationX + 0.5f).toInt()
        val ty = (v.translationY + 0.5f).toInt()
        val left = v.left + tx
        val right = v.right + tx
        val top = v.top + ty
        val bottom = v.bottom + ty

        Log.e(ViewUtils::class.java.name, "${x in left..right && y >= top && y <= bottom}")
        return x in left..right && y >= top && y <= bottom
    }

}


fun View.enterReveal() {

    // get the center for the clipping circle
    val cx = measuredWidth / 2;
    val cy = measuredHeight / 2;

    // get the final radius for the clipping circle
    val finalRadius = this.width.coerceAtLeast(this.height) / 2f

    // create the animator for this view (the start radius is zero)
    val anim =
        ViewAnimationUtils.createCircularReveal(this, cx, cy, 0f, finalRadius);

    // make the view visible and start the animation
    this.visibility = View.VISIBLE;
    anim.start();
}

fun View.exitReveal(endVisibility: Int = View.INVISIBLE) {

    // get the center for the clipping circle
    val cx = measuredWidth / 2
    val cy = measuredHeight / 2

    // get the initial radius for the clipping circle
    val initialRadius = width / 2

    // create the animation (the final radius is zero)
    val anim = ViewAnimationUtils.createCircularReveal(this, cx, cy, initialRadius.toFloat(), 0f)

    // make the view invisible when the animation is done
    anim.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            visibility = endVisibility
        }
    })

    // start the animation
    anim.start()
}

fun Fragment.hideKeyboard() {
    // Check if no view has focus:
    val view = this.requireActivity().currentFocus
    view?.let { v ->
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(v.windowToken, 0)
    }
}

package zelgius.com.myrecipes.views

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat


/**
 * TODO: document your custom view class.
 */
class FloatingActionMenu : FloatingActionButton {

    var menuLayouts: Array<ViewGroup> = arrayOf()
    val isOpen: Boolean
        get() = tag == true

    var animation: Pair<AnimatedVectorDrawableCompat, AnimatedVectorDrawableCompat>? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        setOnClickListener {
            val isOpen = tag
            if (isOpen is Boolean && isOpen) {
                closeMenu()
            } else {
                showMenu()
            }
        }
    }

    private fun showMenu() {
        tag = true

        if (animation == null)
            animate().rotationBy(90f)
        else {
            setImageDrawable(animation?.first)
            animation?.first?.start()
        }

        val baseDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72f, resources.displayMetrics)
        val buttonDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics) //or 45dp?
        var translation = baseDp

        menuLayouts.forEach {
            it.animate().translationY(-translation)
            translation += buttonDp
            it.visibility = View.VISIBLE
        }
    }


    private fun closeMenu() {
        tag = false

        if (animation == null)
            animate().rotationBy(-90f)
        else {
            setImageDrawable(animation?.second)
            animation?.second?.start()
        }

        var lastAnimator: ViewPropertyAnimator? = null
        val dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics)

        menuLayouts.forEach {
            lastAnimator = it.animate().translationY(-dp)
        }


        lastAnimator?.setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                val isOpen = tag
                if (isOpen is Boolean && !isOpen) {
                    menuLayouts.forEach {
                        it.visibility = View.GONE
                    }
                }
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }

        })
    }


}

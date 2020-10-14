package zelgius.com.myrecipes.views


import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat.animate
import com.google.android.material.snackbar.Snackbar

/**
 * Created by Ajay on 07-03-2017.
 */

//Use this class only if you want toslide up the FAB whenever the seekbar appear, if u don't want this funcionality delete this file
class MoveUpwardBehavior : CoordinatorLayout.Behavior<View>() {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency is Snackbar.SnackbarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val translationY = 0f.coerceAtMost(dependency.translationY - dependency.height)
        child.translationY = translationY
        return true
    }

    //you need this when you swipe the snackbar(thanx to ubuntudroid's comment)
    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: View, dependency: View) {
        animate(child).translationY(0f).start()
    }
}
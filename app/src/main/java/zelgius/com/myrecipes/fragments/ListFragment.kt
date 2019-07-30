package zelgius.com.myrecipes.fragments

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.adapters.RecipePagedAdapter
import zelgius.com.myrecipes.entities.Recipe

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 */
class ListFragment : AbstractRecipeListFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_list, container, false).also {
        recyclerView = it as RecyclerView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewTouchActionGuardManager = RecyclerViewTouchActionGuardManager()
        recyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true)
        recyclerViewTouchActionGuardManager.isEnabled = true

        recyclerViewSwipeManager = RecyclerViewSwipeManager()

        val animator = SwipeDismissItemAnimator()

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.supportsChangeAnimations = false

        when (Recipe.Type.valueOf(
            arguments?.getString("type") ?: error("Arguments are null")
        )) {
            Recipe.Type.MEAL -> viewModel.mealList
            Recipe.Type.DESSERT -> viewModel.dessertList
            Recipe.Type.OTHER -> viewModel.otherList
        }.observe(this@ListFragment, Observer {
            adapter.submitList(it)
        })

    }


    companion object {
        @JvmStatic
        fun newInstance(list: Recipe.Type) =
            ListFragment().apply {
                arguments = bundleOf("type" to list.name)
            }
    }


}



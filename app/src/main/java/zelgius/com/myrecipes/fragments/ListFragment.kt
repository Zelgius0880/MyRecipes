package zelgius.com.myrecipes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.entities.RecipeEntity

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

        when (RecipeEntity.Type.valueOf(
            arguments?.getString("type") ?: error("Arguments are null")
        )) {
            RecipeEntity.Type.MEAL -> viewModel.mealList
            RecipeEntity.Type.DESSERT -> viewModel.dessertList
            RecipeEntity.Type.OTHER -> viewModel.otherList
        }.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

    }


    companion object {
        @JvmStatic
        fun newInstance(list: RecipeEntity.Type) =
            ListFragment().apply {
                arguments = bundleOf("type" to list.name)
            }
    }


}



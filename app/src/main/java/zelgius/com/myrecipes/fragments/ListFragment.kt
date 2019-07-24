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
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.adapters.RecipePagedAdapter
import zelgius.com.myrecipes.entities.Recipe

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ListFragment.OnRecipeClickedListener] interface.
 */
class ListFragment : Fragment() {

    private lateinit var recyclerViewSwipeManager: RecyclerViewSwipeManager
    private lateinit var wrappedAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private lateinit var recyclerViewTouchActionGuardManager: RecyclerViewTouchActionGuardManager

    val viewModel by lazy { ViewModelProviders.of(activity!!).get(RecipeViewModel::class.java) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_list, container, false)


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

        if (view is RecyclerView) {
            val adapter = RecipePagedAdapter()

            wrappedAdapter =
                recyclerViewSwipeManager.createWrappedAdapter(adapter)      // wrap for swiping


            view.layoutManager = LinearLayoutManager(context)
            view.adapter = adapter

            adapter.deleteListener = { r ->
                viewModel.delete(r).observe(this, Observer {
                    undoSnackBar(r, getString(R.string.recipe_removed))
                })
            }

            adapter.editListener = { r, extras ->
                findNavController(view).navigate(
                    R.id.action_tabFragment_to_recipeFragment
                    , bundleOf("ID" to r.id), null, extras // ID is used to bind the transition name
                )

                viewModel.loadRecipe(r.id!!)
            }

            adapter.clickListener = { r, extras ->
                findNavController(view).navigate(
                    R.id.action_tabFragment_to_recipeFragment
                    , bundleOf("ID" to r.id), null, extras
                )

                viewModel.loadRecipe(r.id!!)
            }

            when (Recipe.Type.valueOf(
                arguments?.getString("type") ?: error("Arguments are null")
            )) {
                Recipe.Type.MEAL -> viewModel.mealList
                Recipe.Type.DESSERT -> viewModel.dessertList
                Recipe.Type.OTHER -> viewModel.otherList
            }.observe(this@ListFragment, Observer {
                adapter.submitList(it)
            })

            view.adapter = wrappedAdapter  // requires *wrapped* adapter
            view.itemAnimator = animator

            // NOTE:
            // The initialization order is very important! This order determines the priority of touch event handling.
            //
            // priority: TouchActionGuard > Swipe > DragAndDrop
            recyclerViewTouchActionGuardManager.attachRecyclerView(view)
            recyclerViewSwipeManager.attachRecyclerView(view)
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(list: Recipe.Type) =
            ListFragment().apply {
                arguments = bundleOf("type" to list.name)
            }
    }

    private fun undoSnackBar(recipe: Recipe, text: String) {
        Snackbar.make(parentFragment!!.view!!, text, Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) {
                recipe.id = null
                //recipe.ingredients.forEach { it.id = null }
                recipe.steps.forEach { it.id = null }
                viewModel.saveRecipe(recipe)
            }.show()
    }
}



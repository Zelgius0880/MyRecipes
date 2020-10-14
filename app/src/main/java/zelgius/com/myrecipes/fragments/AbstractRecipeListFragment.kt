package zelgius.com.myrecipes.fragments

import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
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
import zelgius.com.myrecipes.utils.observe
import java.io.File

abstract class AbstractRecipeListFragment : Fragment() {
    protected lateinit var adapter: RecipePagedAdapter
    lateinit var recyclerView: RecyclerView
    protected val ctx by lazy { activity as AppCompatActivity }

    protected lateinit var recyclerViewSwipeManager: RecyclerViewSwipeManager
    private var wrappedAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null
    protected lateinit var recyclerViewTouchActionGuardManager: RecyclerViewTouchActionGuardManager

    val viewModel by lazy {  ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(RecipeViewModel::class.java) }

    override fun onDestroyView() {
        if (wrappedAdapter != null)
            WrapperAdapterUtils.releaseAll(wrappedAdapter)
        recyclerViewSwipeManager.release()

        super.onDestroyView()
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

        adapter = RecipePagedAdapter()

        wrappedAdapter =
            recyclerViewSwipeManager.createWrappedAdapter(adapter)      // wrap for swiping


        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        adapter.deleteListener = { r ->
            viewModel.loadRecipe(r.id!!).observe(this) { recipe ->
                if (recipe != null)
                    viewModel.delete(recipe).observe(this) {
                        undoSnackBar(recipe, getString(R.string.recipe_removed))
                    }
            }
        }

        adapter.editListener = { r, extras ->
            Navigation.findNavController(view).navigate(
                R.id.action_tabFragment_to_editRecipeFragment
                , bundleOf("RECIPE" to r), null, extras // ID is used to bind the transition name
            )

            viewModel.loadRecipe(r.id!!)
        }

        adapter.clickListener = { r, extras ->
            Navigation.findNavController(view).navigate(
                R.id.action_tabFragment_to_recipeFragment
                , bundleOf("RECIPE" to r), null, extras
            )

            viewModel.loadRecipe(r.id!!)
        }

        recyclerView.adapter = wrappedAdapter  // requires *wrapped* adapter
        recyclerView.itemAnimator = animator

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        recyclerViewTouchActionGuardManager.attachRecyclerView(recyclerView)
        recyclerViewSwipeManager.attachRecyclerView(recyclerView)
    }


    private fun undoSnackBar(recipe: Recipe, text: String) {
        Snackbar.make(parentFragment!!.view!!, text, Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) {
                recipe.id = null
                //recipe.ingredients.forEach { it.id = null }
                recipe.steps.forEach { it.id = null }
                viewModel.saveRecipe(recipe).observe(this) {
                    File(ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${recipe.id}")
                        .renameTo(
                            File(
                                ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                                "${it.id}"
                            )
                        )
                }
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event == DISMISS_EVENT_CONSECUTIVE || event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_TIMEOUT) {
                        viewModel.removeImage(recipe)
                    }
                }
            })
            .show()
    }

}
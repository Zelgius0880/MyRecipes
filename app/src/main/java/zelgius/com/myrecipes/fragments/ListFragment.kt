package zelgius.com.myrecipes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.adapters.RecipePagedAdapter
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.repository.RecipeRepository

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ListFragment.OnRecipeClickedListener] interface.
 */
class ListFragment : Fragment() {

    private var listener: MyRecipeRecyclerViewAdapter.OnRecipeClickedListener? = null

    val viewModel by lazy { ViewModelProviders.of(activity!!).get(RecipeViewModel::class.java) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                val adapter = RecipePagedAdapter()
                this.adapter = adapter

                when (Recipe.Type.valueOf(arguments?.getString("type") ?: error("Arguments are null"))) {
                    Recipe.Type.MEAL -> viewModel.mealList
                    Recipe.Type.DESSERT -> viewModel.dessertList
                    Recipe.Type.OTHER -> viewModel.otherList
                }.observe(this@ListFragment, Observer {
                    adapter.submitList(it)
                })
            }
        }
        return view
    }


    companion object {
        @JvmStatic
        fun newInstance(list: Recipe.Type) =
            ListFragment().apply {
                arguments = bundleOf("type" to list.name)
            }
    }
}

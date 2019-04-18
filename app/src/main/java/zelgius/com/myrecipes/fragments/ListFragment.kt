package zelgius.com.myrecipes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.adapters.RecipeFirebaseAdapter
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.repository.RecipeRepository

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ListFragment.OnRecipeClickedListener] interface.
 */
class ListFragment : Fragment() {

    private var listener: MyRecipeRecyclerViewAdapter.OnRecipeClickedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)

                view.adapter = RecipeFirebaseAdapter.newInstance(
                    when (Recipe.Type.valueOf(arguments?.getString("type") ?: error("Arguments are null"))) {
                        Recipe.Type.MEAL -> RecipeRepository().getMealsQuery()
                        Recipe.Type.DESSERT -> RecipeRepository().getDessertsQuery()
                        Recipe.Type.OTHER -> RecipeRepository().getOthersQuery()
                    }
                )
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

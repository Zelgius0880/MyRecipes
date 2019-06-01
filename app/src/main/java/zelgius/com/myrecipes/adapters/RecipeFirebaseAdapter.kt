/*
package zelgius.com.myrecipes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.extensions.LayoutContainer
import zelgius.com.myrecipes.entities.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import zelgius.com.myrecipes.R




class RecipeFirebaseAdapter(options: FirestoreRecyclerOptions<Recipe>) : FirestoreRecyclerAdapter<Recipe, RecipeFirebaseAdapter.ViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_fragment_recipe, parent, false))

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int, item: Recipe) {
    }

    companion object {
        @JvmStatic
        fun newInstance(query: Query) =
                RecipeFirebaseAdapter(
                    FirestoreRecyclerOptions.Builder<Recipe>()
                        .setQuery(query, Recipe::class.java)
                        .build()
                )
    }
    inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer
}*/

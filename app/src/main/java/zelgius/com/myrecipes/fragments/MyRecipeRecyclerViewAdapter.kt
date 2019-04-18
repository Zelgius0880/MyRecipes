package zelgius.com.myrecipes.fragments


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.adapter_fragment_recipe.view.*
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.fragments.MyRecipeRecyclerViewAdapter.OnRecipeClickedListener


/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnRecipeClickedListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyRecipeRecyclerViewAdapter(
    private val mValues: List<Recipe>,
    private val mListener: OnRecipeClickedListener?

) : RecyclerView.Adapter<MyRecipeRecyclerViewAdapter.ViewHolder>() {
    interface OnRecipeClickedListener {
        fun onRecipeClicked(item: Recipe?)
    }

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Recipe
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onRecipeClicked(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_fragment_recipe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]


        with(holder.itemView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        val mIdView: TextView = itemView.item_number
        val mContentView: TextView = itemView.content

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}

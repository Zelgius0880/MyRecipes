package zelgius.com.myrecipes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.adapter_fragment_recipe.view.*
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.utils.dpToPx


class RecipePagedAdapter :
    PagedListAdapter<Recipe, RecipePagedAdapter.ViewHolder>(DIFF_CALLBACK) {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = getItem(position)

        if (recipe != null) {
            val itemView = holder.itemView

            itemView.name.text = recipe.name
            itemView.category.text = when (recipe.type) {
                Recipe.Type.MEAL -> itemView.context.getString(R.string.meal)
                Recipe.Type.DESSERT -> itemView.context.getString(R.string.dessert)
                Recipe.Type.OTHER -> itemView.context.getString(R.string.other)
            }

            if (!recipe.imageURL.isNullOrEmpty()) {
                Picasso.get().apply {
                    //setIndicatorsEnabled(true)
                    //isLoggingEnabled = true
                }
                    .load(recipe.imageURL)
                    .resize(2048, 2048)
                    .centerCrop()
                    .into(itemView.imageView, object : Callback {
                        override fun onSuccess() {
                        }

                        override fun onError(e: Exception?) {
                            e?.printStackTrace()
                        }

                    })

                itemView.imageView.setPadding(0, 0, 0, 0)
            } else {
                itemView.imageView.setImageResource(R.drawable.ic_dish)

                itemView.context.let {
                    itemView.imageView.setPadding(
                        it.dpToPx(8f).toInt(),
                        it.dpToPx(8f).toInt(),
                        it.dpToPx(8f).toInt(),
                        it.dpToPx(8f).toInt()
                    )

                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.adapter_fragment_recipe,
                parent,
                false
            )
        )


    companion object {
        @JvmStatic
        fun newInstance(list: PagedList<Recipe>) =
            RecipePagedAdapter()

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Recipe>() {
            override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean =
                oldItem.id == newItem.id


            override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean =
                oldItem == newItem

        }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer

}
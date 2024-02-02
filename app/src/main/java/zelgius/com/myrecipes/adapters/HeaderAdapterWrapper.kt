package zelgius.com.myrecipes.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.headerfooter.AbstractHeaderFooterWrapperAdapter
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.databinding.LayoutHeaderBinding
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.utils.context


class HeaderAdapterWrapper(
    val context: Context,
    val viewModel: RecipeViewModel,
    private val bindListener: (() -> Unit)? = null
) :
    AbstractHeaderFooterWrapperAdapter<HeaderAdapterWrapper.HeaderViewHolder, RecyclerView.ViewHolder>() {

    var recipe: RecipeEntity = viewModel.currentRecipe
    var viewHolder: HeaderViewHolder? = null

    override fun onCreateHeaderItemViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder =
        HeaderViewHolder(
            LayoutHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onCreateFooterItemViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder = throw IllegalStateException("No footer available")

    override fun onBindHeaderItemViewHolder(viewHolder: HeaderViewHolder, localPosition: Int) {
/*
        viewHolder.binding.imageView.transitionName = "imageView${recipe.id?:""}"
        viewHolder.binding.editName.transitionName = "name${recipe.id?:""}"
        viewHolder.binding.editCategory.transitionName = "category${recipe.id?:""}"
*/

        this.viewHolder = viewHolder
        val binding = viewHolder.binding
        val category = when (recipe.type) {
            RecipeEntity.Type.MEAL -> binding.context.getString(R.string.meal)
            RecipeEntity.Type.DESSERT -> binding.context.getString(R.string.dessert)
            RecipeEntity.Type.OTHER -> binding.context.getString(R.string.other)
        }



        binding.name.text = recipe.name
        binding.category.text = category

        if (context is LifecycleOwner) {
            viewModel.selectedImageUrl.observe(context, {
                if (it != null && it.toString().isNotEmpty() && it.toString() != "null") {
                    binding.imageView.setPadding(0, 0, 0, 0)
                    Picasso.get().apply {
                        //setIndicatorsEnabled(true)
                        //isLoggingEnabled = true
                    }
                        .load(it)
                        .resize(2048, 2048)
                        .centerCrop()
                        .into(binding.imageView, object : Callback {
                            override fun onSuccess() {
                            }

                            override fun onError(e: Exception?) {
                                e?.printStackTrace()
                            }

                        })
                }
            })
        }


        bindListener?.invoke()
    }

    override fun onBindFooterItemViewHolder(holder: RecyclerView.ViewHolder, localPosition: Int) {
    }

    override fun getHeaderItemCount(): Int = 1

    override fun getFooterItemCount(): Int = 0

    inner class HeaderViewHolder(val binding: LayoutHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)
}
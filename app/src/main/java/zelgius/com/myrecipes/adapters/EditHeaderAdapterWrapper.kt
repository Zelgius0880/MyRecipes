package zelgius.com.myrecipes.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.headerfooter.AbstractHeaderFooterWrapperAdapter
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.layout_header_edit.view.*
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.dialogs.ImageDialogFragment
import zelgius.com.myrecipes.entities.Recipe


class EditHeaderAdapterWrapper(
    val context: Context,
    val viewModel: RecipeViewModel,
    private val bindListener: (() -> Unit)? = null
) :
    AbstractHeaderFooterWrapperAdapter<EditHeaderAdapterWrapper.HeaderViewHolder, RecyclerView.ViewHolder>() {

    var recipe: Recipe = viewModel.currentRecipe
    private val typeStringArray: Array<String> by lazy {
        context.resources.getStringArray(R.array.category_array)
    }

    var viewHolder: HeaderViewHolder? = null


    override fun onCreateHeaderItemViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder =
        HeaderViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_header_edit,
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
        viewHolder.itemView.imageView.transitionName = "imageView${recipe.id?:""}"
        viewHolder.itemView.editName.transitionName = "name${recipe.id?:""}"
        viewHolder.itemView.editCategory.transitionName = "category${recipe.id?:""}"
*/

        this.viewHolder = viewHolder
        val itemView = viewHolder.itemView
        val category = when (recipe.type) {
            Recipe.Type.MEAL -> itemView.context.getString(R.string.meal)
            Recipe.Type.DESSERT -> itemView.context.getString(R.string.dessert)
            Recipe.Type.OTHER -> itemView.context.getString(R.string.other)
        }

        itemView.editName.editText?.setText(recipe.name)

        val spinnerArrayAdapter = ArrayAdapter(
            context, R.layout.adapter_text_category,
            typeStringArray
        ) //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(
            android.R.layout
                .simple_spinner_dropdown_item
        )
        itemView.editCategory.adapter = spinnerArrayAdapter
        itemView.editCategory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                }
            }
        itemView.editCategory.setSelection(typeStringArray.indexOf(category))

        itemView.editImage.setOnClickListener { _ ->
            ImageDialogFragment().let {
                if (context is AppCompatActivity)
                    it.show(context.supportFragmentManager, "image_dialog")

            }
        }

        if (context is LifecycleOwner) {
            viewModel.selectedImageUrl.observe(context, {
                if (it != null && it.toString().isNotEmpty() && it.toString() != "null") {
                    itemView.imageView.setPadding(0, 0, 0, 0)
                    Picasso.get().apply {
                        //setIndicatorsEnabled(true)
                        //isLoggingEnabled = true
                    }
                        .load(it)
                        .resize(2048, 2048)
                        .centerCrop()
                        .into(itemView.imageView, object : Callback {
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

    fun complete(recipe: Recipe) {
        recipe.name = viewHolder?.itemView?.editName?.editText?.text?.toString() ?: ""
        recipe.type = when (viewHolder?.itemView?.editCategory?.selectedItem as String) {
            context.getString(R.string.meal) -> Recipe.Type.MEAL
            context.getString(R.string.dessert) -> Recipe.Type.DESSERT
            else -> Recipe.Type.OTHER
        }
    }

    inner class HeaderViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}
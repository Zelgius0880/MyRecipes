package zelgius.com.myrecipes.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.headerfooter.AbstractHeaderFooterWrapperAdapter
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.layout_header.view.*
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.dialogs.ImageDialogFragment
import zelgius.com.myrecipes.entities.Recipe
import java.lang.Exception
import java.lang.IllegalStateException


class HeaderAdapterWrapper(val context: Context, val viewModel: RecipeViewModel) :
    AbstractHeaderFooterWrapperAdapter<HeaderAdapterWrapper.HeaderViewHolder, RecyclerView.ViewHolder>() {

    var recipe: Recipe = viewModel.currentRecipe
    private val typeStringArray: Array<String> by lazy {
        context.resources.getStringArray(R.array.category_array)
    }

    override fun onCreateHeaderItemViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder =
        HeaderViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_header,
                parent,
                false
            )
        )

    override fun onCreateFooterItemViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder = throw IllegalStateException("No footer available")

    override fun onBindHeaderItemViewHolder(viewHolder: HeaderViewHolder, localPosition: Int) {
        val itemView = viewHolder.itemView
        val category = when (recipe.type) {
            Recipe.Type.MEAL -> itemView.context.getString(R.string.meal)
            Recipe.Type.DESSERT -> itemView.context.getString(R.string.dessert)
            Recipe.Type.OTHER -> itemView.context.getString(R.string.other)
        }

        itemView.editName.editText?.setText(recipe.name)
        itemView.editCategory.setSelection(typeStringArray.indexOf(category))

        itemView.editImage.setOnClickListener { _ ->
            ImageDialogFragment().let {
                if (context is AppCompatActivity)
                    it.show(context.supportFragmentManager, "image_dialog")
            }
        }

        if (context is LifecycleOwner)
            viewModel.selectedImageUrl.observe(context, Observer {
                Picasso.get().apply {
                    //setIndicatorsEnabled(true)
                    //isLoggingEnabled = true
                }
                    .load(it)
                    .resize(2048, 2048)
                    .centerCrop()
                    .into(itemView.imageView, object :  Callback{
                        override fun onSuccess() {
                        }

                        override fun onError(e: Exception?) {
                            e?.printStackTrace()
                        }

                    })
            })


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
    }

    override fun onBindFooterItemViewHolder(holder: RecyclerView.ViewHolder, localPosition: Int) {
    }

    override fun getHeaderItemCount(): Int = 1

    override fun getFooterItemCount(): Int = 0

    inner class HeaderViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}
package zelgius.com.myrecipes.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.adapter_ingredient.view.*
import kotlinx.android.synthetic.main.adapter_step.view.*
import kotlinx.android.synthetic.main.layout_header.*
import kotlinx.android.synthetic.main.layout_header.view.*
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.dialogs.ImageDialogFragment
import zelgius.com.myrecipes.entities.Recipe


abstract class AbstractViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {
    abstract fun bind(position: Int)
}


class RecipeDetailsAdapter(val context: Context, val viewModel: RecipeViewModel) :
    RecyclerView.Adapter<AbstractViewHolder>() {
    var edit = false
    var recipe: Recipe = Recipe(Recipe.Type.MEAL)

    val typeStringArray by lazy {
        context.resources.getStringArray(R.array.category_array)
    }

    override fun getItemViewType(position: Int): Int =
        when (position) {
            0 -> R.layout.layout_header
            in 1..recipe.ingredients.size -> R.layout.adapter_ingredient
            else -> R.layout.adapter_step
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder =
        when (viewType) {
            R.layout.layout_header -> HeaderViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    viewType,
                    parent,
                    false
                )
            )
            R.layout.adapter_ingredient -> IngredientViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    viewType,
                    parent,
                    false
                )
            )
            R.layout.adapter_step -> StepViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    viewType,
                    parent,
                    false
                )
            )
            else -> error("Unknown layout")
        }

    override fun getItemCount(): Int = 1 + recipe.ingredients.size + recipe.steps.size

    override fun onBindViewHolder(holder: AbstractViewHolder, position: Int) {
        when (position) {
            0 -> holder.bind(position)
            in 1..recipe.ingredients.size -> holder.bind(position - 1)
            else -> holder.bind(position - 1 - recipe.ingredients.size)
        }
    }


    inner class HeaderViewHolder(view: View) : AbstractViewHolder(view) {
        override fun bind(position: Int) {

            val category = when (recipe.type) {
                Recipe.Type.MEAL -> itemView.context.getString(R.string.meal)
                Recipe.Type.DESSERT -> itemView.context.getString(R.string.dessert)
                Recipe.Type.OTHER -> itemView.context.getString(R.string.other)
            }

            itemView.name.text = recipe.name
            itemView.category.text = category

            itemView.editName.editText?.setText(recipe.name)
            itemView.editCategory.setSelection(typeStringArray.indexOf(category))

            itemView.headerEditGroup.visibility = if (edit) View.VISIBLE else View.GONE
            itemView.headerShowGroup.visibility = if (edit) View.GONE else View.VISIBLE

            if (edit) {
                editImage.setOnClickListener {_ ->
                    ImageDialogFragment().let {
                        if(context is AppCompatActivity)
                            it.show(context.supportFragmentManager, "image_dialog")
                    }
                }

                if(context is LifecycleOwner)
                    viewModel.selectedImageUrl.observe(context, Observer {
                        Picasso.get()
                            .load(it)
                            .resize(2048, 2048)
                            .centerCrop()
                            .into(itemView.imageView)
                    })


                val spinnerArrayAdapter = ArrayAdapter<String>(
                    context, R.layout.adapter_text_category,
                    typeStringArray
                ) //selected item will look like a spinner set from XML
                spinnerArrayAdapter.setDropDownViewResource(
                    android.R.layout
                        .simple_spinner_dropdown_item
                )
                editCategory.adapter = spinnerArrayAdapter
                editCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    }

                }
            }
        }
    }

    inner class IngredientViewHolder(view: View) : AbstractViewHolder(view) {
        override fun bind(position: Int) {
            itemView.deleteIngredient.visibility = if (edit) View.VISIBLE else View.GONE
            itemView.text1.text = recipe.ingredients[position].name
        }
    }

    inner class StepViewHolder(view: View) : AbstractViewHolder(view) {
        override fun bind(position: Int) {
            itemView.deleteStep.visibility = if (edit) View.VISIBLE else View.GONE
            itemView.step.text = recipe.steps[position].text
        }
    }
}
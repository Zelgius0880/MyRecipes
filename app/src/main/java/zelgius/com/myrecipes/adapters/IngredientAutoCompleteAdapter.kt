package zelgius.com.myrecipes.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import gr.escsoft.michaelprimez.searchablespinner.interfaces.ISpinnerSelectedView
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.utils.UiUtils

class IngredientAutoCompleteAdapter(context: Context, val ingredients: List<Ingredient>) :
    ArrayAdapter<Ingredient>(context, R.layout.adapter_autocomplete_ingredient), Filterable, ISpinnerSelectedView {
    private var resultList: List<Ingredient> = ingredients.subList(0, Math.min(5, ingredients.size))

    override fun getCount(): Int {
        return resultList.size
    }

    override fun getItem(index: Int): Ingredient? {
        return resultList[index]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =

        (convertView ?: View.inflate(context, R.layout.adapter_autocomplete_ingredient, null).also {
            it.tag = ViewHolder().apply {
                name = it.findViewById(android.R.id.text1)
                image = it.findViewById(R.id.image)
            }
        }).apply {
            val viewHolder = tag as ViewHolder?
            if (viewHolder != null) {
                viewHolder.name.text = resultList[position].name
                UiUtils.getIngredientDrawable(viewHolder.image, resultList[position])
            }
        }


    override fun getSelectedView(position: Int): View =
        View.inflate(context, R.layout.adapter_autocomplete_ingredient, null).apply {
            UiUtils.getIngredientDrawable(findViewById(R.id.image), resultList[position])
            findViewById<TextView>(android.R.id.text1).text = resultList[position].name
        }


    override fun getNoSelectionView(): View {
        return View.inflate(context, R.layout.adapter_autocomplete_ingredient, null).also { view ->
            view.findViewById<TextView>(android.R.id.text1).text = context.getText(R.string.no_selection)
            view.findViewById<ImageView>(R.id.image).visibility = View.GONE
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val filterResults = FilterResults()
                if (constraint.isEmpty())
                    resultList = ingredients.subList(0, Math.min(5, ingredients.size))
                else {
                    // Retrieve the autocomplete results.
                    resultList = ingredients.filterIndexed { _, ingredient ->
                        ingredient.name.startsWith(constraint.toString(), true)
                    }

                    // Assign the data to the FilterResults
                    filterResults.values = resultList
                    filterResults.count = resultList.size
                }

                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                if (results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    private inner class ViewHolder {
        lateinit var image: ImageView
        lateinit var name: TextView
    }

    enum class ItemViewType {
        ITEM, NO_SELECTION_ITEM
    }
}
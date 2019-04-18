package zelgius.com.myrecipes.adapters

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.entities.Ingredient


class IngredientAutoCompleteAdapter(context: Context, val ingredients: List<Ingredient>) :
    ArrayAdapter<Ingredient>(context, R.layout.adapter_ingredient), Filterable {
    private var resultList: List<Ingredient> = listOf()

    override fun getCount(): Int {
        return resultList.size
    }

    override fun getItem(index: Int): Ingredient? {
        return resultList[index]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val filterResults = FilterResults()
                // Retrieve the autocomplete results.
                resultList = ingredients.filterIndexed { _, ingredient ->
                    ingredient.name.startsWith(constraint.toString(), true)
                }

                // Assign the data to the FilterResults
                filterResults.values = resultList
                filterResults.count = resultList.size

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
}
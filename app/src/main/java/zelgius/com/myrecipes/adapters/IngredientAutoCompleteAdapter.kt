package zelgius.com.myrecipes.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.utils.UiUtils

class IngredientAutoCompleteAdapter(context: Context, val ingredients: List<IngredientEntity>) :
    ArrayAdapter<IngredientEntity>(context, R.layout.adapter_autocomplete_ingredient), Filterable {

    private var resultList: List<IngredientEntity> = ingredients.subList(
        0,
        5.coerceAtMost(ingredients.size)
    )

    override fun getCount(): Int {
        return resultList.size
    }

    override fun getItem(index: Int): IngredientEntity? {
        return resultList.getOrNull(index)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =

        (convertView ?: View.inflate(context, R.layout.adapter_autocomplete_ingredient, null).also {
            it.tag = ViewHolder().apply {
                name = it.findViewById(android.R.id.text1)
                image = it.findViewById(R.id.image)
            }
        }).apply {
            (tag as ViewHolder).let {

                if (position >= 0) {
                    it.name.text = resultList[position].name
                    UiUtils.getIngredientDrawable(it.image, resultList[position])
                    it.image.visibility = View.VISIBLE
                } else {
                    it.name.text = context.getText(R.string.no_selection)
                    it.image.visibility = View.GONE
                }
            }
        }


/*    override fun getSelectedView(position: Int): View =
        View.inflate(context, R.layout.adapter_autocomplete_ingredient, null).apply {
            UiUtils.getIngredientDrawable(findViewById(R.id.image), resultList[position])
            findViewById<TextView>(android.R.id.text1).text = resultList[position].name
        }


    override fun getNoSelectionView(): View {
        return View.inflate(context, R.layout.adapter_autocomplete_ingredient, null).also { view ->
            view.findViewById<TextView>(android.R.id.text1).text =
                context.getText(R.string.no_selection)
            view.findViewById<ImageView>(R.id.image).visibility = View.GONE
        }
    }*/

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint.isNullOrBlank())
                    resultList = ingredients.subList(0, 5.coerceAtMost(ingredients.size))
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

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
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
}
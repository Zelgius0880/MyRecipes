package zelgius.com.myrecipes.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import gr.escsoft.michaelprimez.searchablespinner.interfaces.OnItemSelectedListener
import kotlinx.android.synthetic.main.dialog_fragment_ingredient.view.*
import zelgius.com.myrecipes.NoticeDialogListener
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.adapters.IngredientAutoCompleteAdapter
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.IngredientForRecipe
import zelgius.com.myrecipes.utils.UiUtils
import zelgius.com.myrecipes.utils.toDouble
import java.text.DecimalFormat


/**
 *
 * [AlertDialog] for choosing or creating an ingredient
 *
 * @property ingredient IngredientForRecipe?             the selected ingredient. If new ingredient, id will be null, else it is the id in the database
 * @property new Boolean                        TRUE if it is a new ingredient, false otherwise
 * @property listener NoticeDialogListener?     the [NoticeDialogListener] listener called when valid or cancel the dialog.
 * If the Activity implements [NoticeDialogListener], the listener is not used and the method of the Activity will be called instead. Can be null
 */
class IngredientDialogFragment : DialogFragment() {

    private val dialogView by lazy {
        View.inflate(
            activity,
            R.layout.dialog_fragment_ingredient,
            null
        )
    }
    private val viewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(RecipeViewModel::class.java)
    }

    private val context by lazy { activity!! }
    var ingredient: IngredientForRecipe =
        IngredientForRecipe(null, -1.0, Ingredient.Unit.UNIT, "", null, 0, null, null)
            .apply { new = true }
    private var new = false
    var listener: NoticeDialogListener? = null
    private val units by lazy { context.resources.getStringArray(R.array.select_unit_array) }

    companion object {
        fun newInstance(listener: NoticeDialogListener? = null) = IngredientDialogFragment().apply {
            this.listener = listener
        }

        fun newInstance(ingredient: IngredientForRecipe, listener: NoticeDialogListener? = null) =
            IngredientDialogFragment().apply {
                this.listener = listener

                this.ingredient = ingredient
            }

        var lastSelectedUnit = Ingredient.Unit.GRAMME
    }

    private val addAnimation by lazy {
        AnimatedVectorDrawableCompat.create(
            context,
            R.drawable.avd_add_to_close
        )!!
    }

    private val closeAnimation by lazy {
        AnimatedVectorDrawableCompat.create(
            context,
            R.drawable.avd_close_to_add
        )!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            viewModel.ingredients.observe(this, { list ->
                dialogView.ingredients.setAdapter(IngredientAutoCompleteAdapter(context, list))
                dialogView.ingredients.setOnItemSelectedListener(object : OnItemSelectedListener {
                    override fun onNothingSelected() {

                    }

                    override fun onItemSelected(view: View?, position: Int, id: Long) {
                        dialogView.ingredients.selectedItem.let { i ->
                            if(i is Ingredient) {
                                ingredient.id = i.id
                                ingredient.name = i.name
                                ingredient.imageUrl = i.imageURL

                                dialogView.spinner.setSelection(units.indexOfFirst { s ->
                                    s == context.getSharedPreferences(
                                        "UNITS",
                                        Context.MODE_PRIVATE
                                    ).getString(i.name, null)?: lastSelectedUnit
                                })
                            }
                        }
                    }

                })
            })


            dialogView.quantity.hint = getString(R.string.quantity)

            dialogView.button.setOnClickListener {
                dialogView.error.visibility = View.GONE
                removeErrors()
                if (!new) {
                    setNewIngredient(View.VISIBLE)
                    dialogView.ingredients.visibility = View.GONE
                    dialogView.button.setImageResource(R.drawable.ic_close_black_24dp)
                    dialogView.button.setImageDrawable(addAnimation)
                    UiUtils.getCircleDrawable(dialogView.image, R.drawable.ic_carrot_solid, R.color.md_blue_grey_700, 8f)
                    addAnimation.start()
                    ingredient.name = ""
                    new = true
                } else {
                    setNewIngredient(View.GONE)
                    dialogView.ingredients.visibility = View.VISIBLE
                    dialogView.button.setImageDrawable(closeAnimation)

                    (dialogView.ingredients.selectedItem as Ingredient?)?.let { i ->
                        ingredient.id = i.id
                        ingredient.name = i.name
                        ingredient.imageUrl = i.imageURL
                    }
                    if(ingredient.name.isNotEmpty())UiUtils.getIngredientDrawable(dialogView.image, ingredient)
                    else UiUtils.getCircleDrawable(dialogView.image, R.drawable.ic_carrot_solid, R.color.md_blue_grey_700, 8f)


                    closeAnimation.start()
                    ingredient.id = null
                    ingredient.name = ""
                    new = false
                }
            }

            val spinnerArrayAdapter = ArrayAdapter(
                it, android.R.layout.simple_spinner_item, units

            ) //selected item will look like a spinner set from XML
            spinnerArrayAdapter.setDropDownViewResource(
                android.R.layout
                    .simple_spinner_dropdown_item
            )
            dialogView.spinner.adapter = spinnerArrayAdapter
            dialogView.spinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(adapterView: AdapterView<*>?) {}

                    override fun onItemSelected(
                        adapterView: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        when (dialogView.spinner.selectedItem.toString()) {
                            getString(R.string.gramme_select) -> ingredient.unit =
                                Ingredient.Unit.GRAMME
                            getString(R.string.kilogramme_select) -> ingredient.unit =
                                Ingredient.Unit.KILOGRAMME
                            getString(R.string.milliliter_select) -> ingredient.unit =
                                Ingredient.Unit.MILLILITER
                            getString(R.string.liter_select) -> ingredient.unit =
                                Ingredient.Unit.LITER
                            getString(R.string.unit_select) -> ingredient.unit =
                                Ingredient.Unit.UNIT
                            getString(R.string.tablespoon_select) -> ingredient.unit =
                                Ingredient.Unit.TABLESPOON
                            getString(R.string.teaspoon_select) -> ingredient.unit =
                                Ingredient.Unit.TEASPOON
                            getString(R.string.cup_select) -> ingredient.unit = Ingredient.Unit.CUP
                            getString(R.string.pinch_select) -> ingredient.unit = Ingredient.Unit.PINCH
                        }

                        lastSelectedUnit = ingredient.unit
                    }

                }

            if (!ingredient.new) {
                dialogView.quantity.editText?.setText(DecimalFormat("#0.##").format(ingredient.quantity))
                dialogView.ingredientName.apply {
                    text = ingredient.name
                    visibility = View.VISIBLE
                }

                dialogView.image.apply {
                    visibility = View.VISIBLE
                    UiUtils.getIngredientDrawable(this, ingredient)
                }

                setEditIngredient(View.GONE)

            } else {
                setEditIngredient(View.VISIBLE)
                setNewIngredient(View.GONE)

                ingredient.unit = lastSelectedUnit
            }

            dialogView.optional.isChecked = ingredient.optional?:false

            dialogView.spinner.setSelection(units.indexOfFirst { s ->
                when (s) {
                    getString(R.string.gramme_select) -> ingredient.unit == Ingredient.Unit.GRAMME
                    getString(R.string.kilogramme_select) -> ingredient.unit == Ingredient.Unit.KILOGRAMME
                    getString(R.string.milliliter_select) -> ingredient.unit == Ingredient.Unit.MILLILITER
                    getString(R.string.liter_select) -> ingredient.unit == Ingredient.Unit.LITER
                    getString(R.string.unit_select) -> ingredient.unit == Ingredient.Unit.UNIT
                    getString(R.string.tablespoon_select) -> ingredient.unit == Ingredient.Unit.TABLESPOON
                    getString(R.string.teaspoon_select) -> ingredient.unit == Ingredient.Unit.TEASPOON
                    getString(R.string.cup_select) -> ingredient.unit == Ingredient.Unit.CUP
                    getString(R.string.pinch_select) -> ingredient.unit == Ingredient.Unit.PINCH
                    else -> false
                }
            })

            return AlertDialog.Builder(it)
                .setView(dialogView)
                .setTitle(R.string.select_an_ingredient)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel) { _, _ ->
                    it.let { d ->
                        if (d is NoticeDialogListener) d.onDialogNegativeClick(this)
                        else listener?.onDialogNegativeClick(this)
                    }
                }
                .create().apply {
                    setOnShowListener {
                        getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener { _ ->
                            removeErrors()

                            if (dialogView.quantity.editText!!.text.isBlank()) {
                                dialogView.quantity.error = getString(R.string.field_required)
                            } else {

                                try {
                                    ingredient.quantity = dialogView.quantity.toDouble()
                                } catch (e: NumberFormatException) {
                                    e.printStackTrace()
                                    dialogView.quantity.error =
                                        getString(R.string.error_not_a_number)
                                }

                                if (new) {
                                    if (dialogView.name.editText?.text?.isEmpty() != false) {
                                        dialogView.name.error = getString(R.string.field_required)
                                    } else {
                                        //ingredient = IngredientForRecipe(null, dialogView.quantity.toDouble(), Ingredient.Unit.valueOf() dialogView.name.editText!!.text.toString(), "")

                                        ingredient.name = dialogView.name.editText!!.text.toString()
                                        ingredient.imageUrl = null
                                        ingredient.optional = dialogView.optional.isChecked
                                        dismiss()

                                        context.getSharedPreferences(
                                            "UNITS",
                                            Context.MODE_PRIVATE
                                        ).edit{
                                            putString(ingredient.name, dialogView.spinner.selectedItem as String)
                                        }
                                        if (it is NoticeDialogListener) it.onDialogPositiveClick(
                                            this@IngredientDialogFragment
                                        )
                                        else listener?.onDialogPositiveClick(this@IngredientDialogFragment)

                                    }
                                } else {
                                    if (ingredient.id == null && ingredient.name.isBlank()) {
                                        dialogView.error.visibility = View.VISIBLE
                                        dialogView.error.text =
                                            getString(R.string.select_an_ingredient)
                                    } else if (ingredient.id == null && (viewModel.ingredients.value
                                            ?: listOf())
                                            .findLast { i ->
                                                i.name.equals(
                                                    ingredient.name,
                                                    true
                                                )
                                            } != null
                                    ) {
                                        dialogView.error.visibility = View.VISIBLE
                                        dialogView.error.text =
                                            getString(R.string.select_an_ingredient)
                                    } else {
                                        ingredient.optional = dialogView.optional.isChecked
                                        dismiss()

                                        context.getSharedPreferences(
                                            "UNITS",
                                            Context.MODE_PRIVATE
                                        ).edit{
                                            putString(ingredient.name, dialogView.spinner.selectedItem as String)
                                        }
                                        if (it is NoticeDialogListener) it.onDialogPositiveClick(
                                            this@IngredientDialogFragment
                                        )
                                        else listener?.onDialogPositiveClick(this@IngredientDialogFragment)
                                    }
                                }
                            }
                        }
                    }
                }
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setNewIngredient(visibility: Int) {
        dialogView.name.visibility = visibility
        dialogView.image.visibility = visibility
        dialogView.error.visibility = View.GONE
    }

    private fun setEditIngredient(visibility: Int) {
        dialogView.name.visibility = visibility
        dialogView.ingredients.visibility = visibility
        dialogView.button.visibility = visibility
        dialogView.error.visibility = View.GONE
    }

    private fun removeErrors() {
        dialogView.name.isErrorEnabled = false
        dialogView.quantity.isErrorEnabled = false
        dialogView.error.visibility = View.GONE
    }
}
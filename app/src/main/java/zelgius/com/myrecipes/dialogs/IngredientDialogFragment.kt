package zelgius.com.myrecipes.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.dialog_fragment_ingredient.*
import kotlinx.android.synthetic.main.dialog_fragment_ingredient.view.*
import zelgius.com.myrecipes.NoticeDialogListener
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.adapters.IngredientAutoCompleteAdapter
import zelgius.com.myrecipes.entities.Ingredient


class IngredientDialogFragment : DialogFragment() {

    private val dialogView by lazy { View.inflate(activity, R.layout.dialog_fragment_ingredient, null) }
    private val viewModel by lazy { ViewModelProviders.of(activity!!).get(RecipeViewModel::class.java) }

    var ingredient: Ingredient? = null
    private var new = false
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            viewModel.ingredients.observe(activity!!, Observer { list ->
                dialogView.ingredients.setAdapter(IngredientAutoCompleteAdapter(activity!!, list))
            })

            dialogView.quantity.hint = getString(R.string.quantity)

            dialogView.button.setOnClickListener {
                dialogView.error.visibility = View.GONE
                if (!new) {
                    dialogView.newIngredient.visibility = View.VISIBLE
                    dialogView.ingredients.visibility = View.GONE
                    dialogView.button.setText(R.string.cancel)
                    new = true
                } else {
                    dialogView.newIngredient.visibility = View.GONE
                    dialogView.ingredients.visibility = View.VISIBLE
                    dialogView.button.setText(R.string.new_ingredient)
                    new = false
                }
            }


            return AlertDialog.Builder(it)
                .setView(dialogView)
                .setTitle(R.string.select_an_ingredient)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel) { _, _ ->
                    activity.let { d ->
                        if (d is NoticeDialogListener) d.onDialogNegativeClick(this)
                    }
                }
                .create().apply {
                    setOnShowListener {
                        getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                            if (new) {
                                if (dialogView.name.editText?.text?.isEmpty() != false) {
                                    dialogView.name.error = getString(R.string.field_required)
                                } else {
                                    ingredient = Ingredient(null, dialogView.name.editText!!.text.toString(), "")
                                    dismiss()
                                    activity.let { d ->
                                        if (d is NoticeDialogListener) d.onDialogNegativeClick(this@IngredientDialogFragment)
                                    }
                                }
                            } else {
                                if (ingredient == null) {
                                    dialogView.error.visibility = View.VISIBLE
                                    dialogView.error.text = getString(R.string.select_an_ingredient)
                                } else {
                                    dismiss()
                                    activity.let { d ->
                                        if (d is NoticeDialogListener) d.onDialogNegativeClick(this@IngredientDialogFragment)
                                    }
                                }
                            }
                        }
                    }
                }
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
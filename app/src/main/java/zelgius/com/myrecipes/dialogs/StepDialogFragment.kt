package zelgius.com.myrecipes.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.dialog_fragment_step.view.*
import zelgius.com.myrecipes.NoticeDialogListener
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.entities.IngredientForRecipe
import zelgius.com.myrecipes.entities.Step


/**
 *
 * [AlertDialog] for choosing or creating an ingredient
 *
 * @property step Step?             the selected step. If new step, id will be null, else it is the id in the database
 * @property new Boolean                        TRUE if it is a new ingredient, false otherwise
 * @property listener NoticeDialogListener?     the [NoticeDialogListener] listener called when valid or cancel the dialog.
 * If the [Activity] implements [NoticeDialogListener], the listener is not used and the method of the [Activity] will be called instead. Can be null
 */
class StepDialogFragment : DialogFragment() {

    private val dialogView by lazy { View.inflate(activity, R.layout.dialog_fragment_step, null) }
    private val viewModel by lazy { ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(RecipeViewModel::class.java) }

    var step = Step(null, "", Int.MAX_VALUE, null)
        .apply { new = true }
    var listener: NoticeDialogListener? = null

    companion object {
        fun newInstance(listener: NoticeDialogListener? = null) = StepDialogFragment().apply {
            this.listener = listener
        }

        fun newInstance(step: Step, listener: NoticeDialogListener? = null) =
            StepDialogFragment().apply {
                this.listener = listener

                this.step = step
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            if(!step.new){
                dialogView.text.editText?.setText(step.text)
            }

            return AlertDialog.Builder(it)
                .setView(dialogView)
                .setTitle(R.string.enter_a_step)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel) { _, _ ->
                    activity.let { d ->
                        if (d is NoticeDialogListener) d.onDialogNegativeClick(this)
                        else listener?.onDialogNegativeClick(this)
                    }
                }
                .create().apply {
                    dialogView.text.editText?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    setOnShowListener {
                        getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {

                            if (dialogView.text.editText?.text?.isEmpty() != false) {
                                dialogView.text.error = getString(R.string.field_required)
                            } else {
                                //ingredient = IngredientForRecipe(null, dialogView.quantity.toDouble(), Ingredient.Unit.valueOf() dialogView.name.editText!!.text.toString(), "")

                                step.text = dialogView.text.editText!!.text.toString()
                                dismiss()
                                activity.let { d ->
                                    if (d is NoticeDialogListener) d.onDialogPositiveClick(this@StepDialogFragment)
                                    else listener?.onDialogPositiveClick(this@StepDialogFragment)

                                }
                            }
                        }
                    }
                }
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
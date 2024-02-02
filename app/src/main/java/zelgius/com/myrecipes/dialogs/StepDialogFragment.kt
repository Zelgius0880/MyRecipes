package zelgius.com.myrecipes.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import zelgius.com.myrecipes.NoticeDialogListener
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.databinding.DialogFragmentStepBinding
import zelgius.com.myrecipes.data.entities.StepEntity


/**
 *
 * [AlertDialog] for choosing or creating an ingredient
 *
 * @property step Step?             the selected step. If new step, id will be null, else it is the id in the database
 * @property listener NoticeDialogListener?     the [NoticeDialogListener] listener called when valid or cancel the dialog.
 * If the Activity implements [NoticeDialogListener], the listener is not used and the method of the Activity will be called instead. Can be null
 */
class StepDialogFragment : DialogFragment() {
    private var _binding: DialogFragmentStepBinding? = null
    private val binding: DialogFragmentStepBinding
        get() = _binding!!

    var step = StepEntity(null, "", Int.MAX_VALUE, false, null)
        .apply { new = true }
    var listener: NoticeDialogListener? = null

    companion object {
        fun newInstance(listener: NoticeDialogListener? = null) = StepDialogFragment().apply {
            this.listener = listener
        }

        fun newInstance(step: StepEntity, listener: NoticeDialogListener? = null) =
            StepDialogFragment().apply {
                this.listener = listener

                this.step = step
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogFragmentStepBinding.inflate(LayoutInflater.from(requireContext()))
        return activity?.let {
            if(!step.new) {
                binding.text.editText?.setText(step.text)
                binding.optional.isChecked = step.optional
            }

            return AlertDialog.Builder(it)
                .setView(binding.root)
                .setTitle(R.string.enter_a_step)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel) { _, _ ->
                    activity.let { d ->
                        if (d is NoticeDialogListener) d.onDialogNegativeClick(this)
                        else listener?.onDialogNegativeClick(this)
                    }
                }
                .create().apply {
                    binding.text.editText?.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_AUTO_CORRECT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    setOnShowListener {
                        getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {

                            if (binding.text.editText?.text?.isEmpty() != false) {
                                binding.text.error = getString(R.string.field_required)
                            } else {
                                //ingredient = IngredientForRecipe(null, binding.quantity.toDouble(), Ingredient.Unit.valueOf() binding.name.editText!!.text.toString(), "")

                                step.text = binding.text.editText!!.text.toString()
                                step.optional = binding.optional.isChecked
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

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}
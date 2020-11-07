package zelgius.com.myrecipes.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import zelgius.com.myrecipes.NoticeDialogListener
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.adapters.IngredientAutoCompleteAdapter
import zelgius.com.myrecipes.databinding.DialogFragmentIngredientBinding
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.IngredientForRecipe
import zelgius.com.myrecipes.utils.UiUtils
import zelgius.com.myrecipes.utils.enterReveal
import zelgius.com.myrecipes.utils.hideKeyboard
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

    private var _binding: DialogFragmentIngredientBinding? = null
    private val binding: DialogFragmentIngredientBinding
        get() = _binding!!

    private val viewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(RecipeViewModel::class.java)
    }

    private val ctx by lazy { requireContext() }
    var ingredient: IngredientForRecipe =
        IngredientForRecipe(null, -1.0, Ingredient.Unit.UNIT, "", null, 0, null, null)
            .apply { new = true }
    private var new = false
    var listener: NoticeDialogListener? = null
    private val units by lazy { ctx.resources.getStringArray(R.array.select_unit_array) }


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
            ctx,
            R.drawable.avd_add_to_close
        )!!
    }

    private val closeAnimation by lazy {
        AnimatedVectorDrawableCompat.create(
            ctx,
            R.drawable.avd_close_to_add
        )!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogFragmentIngredientBinding.inflate(LayoutInflater.from(requireContext()))
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            viewModel.ingredients.observe(this, { list ->

                if (new) binding.ingredients.visibility = View.GONE
                else binding.holder.visibility = View.GONE

                (binding.ingredients.editText as? AutoCompleteTextView)?.let { textView ->
                    binding.holder.setOnClickListener {
                        binding.ingredients.enterReveal()
                        textView.setText("")
                        textView.requestFocus()

                        binding.holder.visibility = View.GONE
                    }

                    val adapter = IngredientAutoCompleteAdapter(ctx, list)
                    textView.setAdapter(adapter)
                    textView.setOnItemClickListener { _, _, position, _ ->
                        adapter.getItem(position)?.let { i ->
                            //binding.ingredients.exitReveal(View.GONE)
                            binding.ingredients.visibility = View.GONE
                            binding.holder.enterReveal()
                            binding.error.visibility = View.GONE

                            UiUtils.getIngredientDrawable(binding.imageHolder, i)
                            binding.ingredientNameHolder.text = i.name

                            ingredient.id = i.id
                            ingredient.name = i.name
                            ingredient.imageUrl = i.imageURL

                            binding.spinner.setSelection(units.indexOfFirst { s ->
                                s == ctx.getSharedPreferences(
                                    "UNITS",
                                    Context.MODE_PRIVATE
                                ).getString(i.name, null) ?: lastSelectedUnit
                            })

                            hideKeyboard()
                        }
                    }
                }
            })


            binding.quantity.hint = getString(R.string.quantity)

            binding.button.setOnClickListener {
                binding.error.visibility = View.GONE
                removeErrors()
                if (!new) {
                    setNewIngredient(View.VISIBLE)
                    binding.ingredients.visibility = View.GONE
                    binding.button.setIconResource(R.drawable.ic_close_black_24dp)
                    binding.button.icon = addAnimation
                    UiUtils.getCircleDrawable(
                        binding.image,
                        R.drawable.ic_carrot_solid,
                        R.color.md_blue_grey_700,
                        8f
                    )
                    addAnimation.start()
                    ingredient.name = ""
                    new = true
                } else {
                    setNewIngredient(View.GONE)
                    binding.ingredients.visibility = View.VISIBLE
                    binding.button.icon = closeAnimation

                    if (ingredient.name.isNotEmpty()) UiUtils.getIngredientDrawable(
                        binding.image,
                        ingredient
                    )
                    else UiUtils.getCircleDrawable(
                        binding.image,
                        R.drawable.ic_carrot_solid,
                        R.color.md_blue_grey_700,
                        8f
                    )


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
            binding.spinner.adapter = spinnerArrayAdapter
            binding.spinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(adapterView: AdapterView<*>?) {}

                    override fun onItemSelected(
                        adapterView: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        when (binding.spinner.selectedItem.toString()) {
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
                            getString(R.string.pinch_select) -> ingredient.unit =
                                Ingredient.Unit.PINCH
                        }

                        lastSelectedUnit = ingredient.unit
                    }

                }

            if (!ingredient.new) {
                binding.quantity.editText?.setText(DecimalFormat("#0.##").format(ingredient.quantity))
                binding.ingredientName.apply {
                    text = ingredient.name
                    visibility = View.VISIBLE
                }

                binding.image.apply {
                    visibility = View.VISIBLE
                    UiUtils.getIngredientDrawable(this, ingredient)
                }

                setEditIngredient(View.GONE)

            } else {
                setEditIngredient(View.VISIBLE)
                setNewIngredient(View.GONE)

                ingredient.unit = lastSelectedUnit
            }

            binding.optional.isChecked = ingredient.optional ?: false

            binding.spinner.setSelection(units.indexOfFirst { s ->
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
                .setView(binding.root)
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

                            if (binding.quantity.editText!!.text.isBlank()) {
                                binding.quantity.error = getString(R.string.field_required)
                            } else {

                                try {
                                    ingredient.quantity = binding.quantity.toDouble()
                                } catch (e: NumberFormatException) {
                                    e.printStackTrace()
                                    binding.quantity.error =
                                        getString(R.string.error_not_a_number)
                                }

                                if (new) {
                                    if (binding.name.editText?.text?.isEmpty() != false) {
                                        binding.name.error = getString(R.string.field_required)
                                    } else {
                                        //ingredient = IngredientForRecipe(null, dialogView.quantity.toDouble(), Ingredient.Unit.valueOf() dialogView.name.editText!!.text.toString(), "")

                                        ingredient.name = binding.name.editText!!.text.toString()
                                        ingredient.imageUrl = null
                                        ingredient.optional = binding.optional.isChecked
                                        dismiss()

                                        context.getSharedPreferences(
                                            "UNITS",
                                            Context.MODE_PRIVATE
                                        ).edit {
                                            putString(
                                                ingredient.name,
                                                binding.spinner.selectedItem as String
                                            )
                                        }
                                        if (it is NoticeDialogListener) it.onDialogPositiveClick(
                                            this@IngredientDialogFragment
                                        )
                                        else listener?.onDialogPositiveClick(this@IngredientDialogFragment)

                                    }
                                } else {
                                    if (ingredient.id == null && ingredient.name.isBlank()) {
                                        binding.error.visibility = View.VISIBLE
                                        binding.error.text =
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
                                        binding.error.visibility = View.VISIBLE
                                        binding.error.text =
                                            getString(R.string.select_an_ingredient)
                                    } else {
                                        ingredient.optional = binding.optional.isChecked
                                        dismiss()

                                        context.getSharedPreferences(
                                            "UNITS",
                                            Context.MODE_PRIVATE
                                        ).edit {
                                            putString(
                                                ingredient.name,
                                                binding.spinner.selectedItem as String
                                            )
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

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setNewIngredient(visibility: Int) {
        binding.name.visibility = visibility
        binding.image.visibility = visibility
        binding.error.visibility = View.GONE
    }

    private fun setEditIngredient(visibility: Int) {
        binding.name.visibility = visibility
        binding.ingredients.visibility = visibility
        binding.button.visibility = visibility
        binding.error.visibility = View.GONE
    }

    private fun removeErrors() {
        binding.name.isErrorEnabled = false
        binding.quantity.isErrorEnabled = false
        binding.error.visibility = View.GONE
    }
}

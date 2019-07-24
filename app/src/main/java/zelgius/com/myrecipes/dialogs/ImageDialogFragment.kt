package zelgius.com.myrecipes.dialogs

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.webkit.URLUtil
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_fragment_image.view.*
import net.alhazmy13.mediapicker.Image.ImagePicker
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel


class ImageDialogFragment : DialogFragment() {

    private val dialogView by lazy { View.inflate(activity, R.layout.dialog_fragment_image, null) }
    private val viewModel by lazy { ViewModelProviders.of(activity!!).get(RecipeViewModel::class.java) }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null && URLUtil.isValidUrl(s.toString())) {
                Picasso.get()
                    .load(s.toString())
                    .resize(2048, 2048)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_24dp)
                    .into(dialogView.imageView)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            dialogView.button.setOnClickListener {
                ImagePicker.Builder(activity)
                    .mode(ImagePicker.Mode.CAMERA_AND_GALLERY)
                    .compressLevel(ImagePicker.ComperesLevel.HARD)
                    .directory(ImagePicker.Directory.DEFAULT)
                    .extension(ImagePicker.Extension.PNG)
                    .scale(600, 600)
                    .allowMultipleImages(false)
                    .enableDebuggingMode(true)
                    .build()

                dismiss()
            }

            return AlertDialog.Builder(it)
                .setView(dialogView)
                .setTitle(R.string.select_image)
                .setPositiveButton(R.string.save) { _, _ ->
                    if (dialogView.imageUrl.editText?.text?.isNotEmpty() == true) {
                        viewModel.selectedImageUrl.value = Uri.parse(dialogView.imageUrl.editText!!.text.toString())
                    }
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onStart() {
        super.onStart()

        dialogView.imageUrl.editText?.addTextChangedListener(textWatcher)
    }

    override fun onStop() {
        super.onStop()

        dialogView.imageUrl.editText?.removeTextChangedListener(textWatcher)
    }
}
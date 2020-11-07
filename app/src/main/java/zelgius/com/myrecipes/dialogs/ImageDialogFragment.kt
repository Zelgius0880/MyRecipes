package zelgius.com.myrecipes.dialogs

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.webkit.URLUtil
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import zelgius.com.myrecipes.BuildConfig
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.databinding.DialogFragmentImageBinding
import java.io.File

class ImageDialogFragment : DialogFragment() {

    private val viewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(RecipeViewModel::class.java)
    }

    private val imageUri: Uri by lazy {
        FileProvider.getUriForFile(
            requireContext(),
            BuildConfig.APPLICATION_ID + ".fileprovider",
            File(
                requireContext()
                    .getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "temp_image"
            )
        )
    }
    private val galleryRequest by lazy {
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it == null) return@registerForActivityResult

            val from = requireContext().contentResolver.openInputStream(it)
            val to = requireContext().contentResolver.openOutputStream(imageUri)
            if (from != null && to != null)
                viewModel.copy(from, to).observe(this) {
                    refreshImage()
                }
        }
    }

    private val cameraRequest by lazy {
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (!it) return@registerForActivityResult
            refreshImage()
        }
    }

    private fun refreshImage() {
        Picasso.get()
            .load(imageUri)
            .resize(2048, 2048)
            .centerCrop()
            .placeholder(R.drawable.ic_image_24dp)
            .into(binding.imageView)
    }

    private val cameraPermissionRequest by lazy {
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) return@registerForActivityResult
            else cameraRequest.launch(imageUri)
        }
    }

    private var _binding: DialogFragmentImageBinding? = null
    private val binding: DialogFragmentImageBinding
        get() = _binding!!


    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null && URLUtil.isValidUrl(s.toString())) {
                Picasso.get()
                    .load(s.toString())
                    .resize(2048, 2048)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_24dp)
                    .into(binding.imageView)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        galleryRequest
        cameraPermissionRequest
        cameraRequest
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            binding.camera.setOnClickListener {
                cameraPermissionRequest.launch(android.Manifest.permission.CAMERA)
            }

            binding.gallery.setOnClickListener {
                galleryRequest.launch("image/*")
            }

            _binding = DialogFragmentImageBinding.inflate(LayoutInflater.from(requireContext()))

            return AlertDialog.Builder(it)
                .setView(binding.root)
                .setTitle(R.string.select_image)
                .setPositiveButton(R.string.save) { _, _ ->
                    if (binding.imageUrl.editText?.text?.isNotEmpty() == true) {
                        viewModel.selectedImageUrl.value =
                            Uri.parse(binding.imageUrl.editText!!.text.toString())
                    } else {
                        viewModel.selectedImageUrl.value = imageUri
                    }
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStart() {
        super.onStart()

        binding.imageUrl.editText?.addTextChangedListener(textWatcher)
    }

    override fun onStop() {
        super.onStop()

        binding.imageUrl.editText?.removeTextChangedListener(textWatcher)
    }
}

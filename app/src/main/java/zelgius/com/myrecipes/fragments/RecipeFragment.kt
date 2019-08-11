package zelgius.com.myrecipes.fragments


import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.transition.TransitionInflater
import android.view.*
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import zelgius.com.myrecipes.filepicker.model.DialogConfigs
import zelgius.com.myrecipes.filepicker.model.DialogProperties
import zelgius.com.myrecipes.filepicker.view.FilePickerDialog
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.fragment_recipe.header
import kotlinx.android.synthetic.main.fragment_recipe.list
import kotlinx.android.synthetic.main.fragment_tab.view.*
import kotlinx.android.synthetic.main.layout_header.*
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.adapters.GroupDividerDecoration
import zelgius.com.myrecipes.adapters.HeaderAdapterWrapper
import zelgius.com.myrecipes.adapters.RecipeExpandableAdapter
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.utils.UiUtils
import java.io.File


/**
 * A simple [Fragment] subclass.
 *
 */
class RecipeFragment : Fragment(), OnBackPressedListener,
    RecyclerViewExpandableItemManager.OnGroupExpandListener,
    RecyclerViewExpandableItemManager.OnGroupCollapseListener {

    companion object {
        const val REQUEST_CODE = 543
        const val SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager"

    }

    override fun onBackPressed() {
        //endActivity()
    }

    override fun onDestroy() {
        super.onDestroy()

        expandableItemManager?.release()
        touchActionGuardManager.release()
    }

    private var itemAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null

    private val context by lazy { activity as AppCompatActivity }
    private val viewModel by lazy {
        ViewModelProvider(requireActivity(), ViewModelProvider.NewInstanceFactory()).get(RecipeViewModel::class.java)
    }
    private val adapter by lazy { RecipeExpandableAdapter(context, viewModel) }
    private val headerWrapper by lazy {
        HeaderAdapterWrapper(
            context,
            viewModel
        ) { /*startPostponedEnterTransition()*/ header.visibility = View.INVISIBLE }
    }

    private var expandableItemManager: RecyclerViewExpandableItemManager? = null
    private lateinit var touchActionGuardManager: RecyclerViewTouchActionGuardManager
    private lateinit var menu: Menu

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        sharedElementReturnTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)

        /*postponeEnterTransition()*/

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            popFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        val recipe = arguments?.getParcelable("RECIPE") ?: Recipe(Recipe.Type.MEAL)
        viewModel.selectedRecipe.value = recipe

        if (arguments != null)
            UiUtils.bindHeader(recipe, UiUtils.HeaderViewHolder(view, imageView, name, category))

        viewModel.selectedImageUrl.value = viewModel.selectedRecipe.value?.imageURL?.toUri()

        viewModel.editMode.value = true

        viewModel.editMode.observe(this, Observer {
            adapter.notifyDataSetChanged()
        })

        viewModel.selectedRecipe.observe(this, Observer {
            viewModel.currentRecipe = it
            adapter.recipe = it
            adapter.notifyDataSetChanged()

            headerWrapper.recipe = it
            headerWrapper.notifyDataSetChanged()

            activity?.actionBar?.title = it.name
        })

        viewModel.selectedImageUrl.observe(this, Observer {
            if (it != null && it.toString().isNotEmpty()) {
                imageView.setPadding(0, 0, 0, 0)
                imageView.setImageURI(it)
            }
        })

        val eimSavedState =
            savedInstanceState?.getParcelable<Parcelable>(SAVED_STATE_EXPANDABLE_ITEM_MANAGER)

        expandableItemManager = RecyclerViewExpandableItemManager(eimSavedState)
        expandableItemManager?.setOnGroupExpandListener(this)
        expandableItemManager?.setOnGroupCollapseListener(this)
        adapter.expandableItemManager = expandableItemManager
        //expandableItemManager?.defaultGroupsExpandedState = true

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        touchActionGuardManager = RecyclerViewTouchActionGuardManager()
        touchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true)
        touchActionGuardManager.isEnabled = true


        //The order here is important
        itemAdapter = expandableItemManager?.createWrappedAdapter(adapter)
        itemAdapter = headerWrapper.setAdapter(itemAdapter!!)

        touchActionGuardManager.attachRecyclerView(list)
        expandableItemManager?.attachRecyclerView(list)


        adapter.expandableItemManager = expandableItemManager
        list.adapter = itemAdapter
        list.addItemDecoration(
            GroupDividerDecoration(
                context,
                ContextCompat.getColor(context, android.R.color.transparent),
                8f
            )
        )
    }

    override fun onResume() {
        super.onResume()

        (activity as AppCompatActivity).setSupportActionBar(view!!.toolbar)
        NavigationUI.setupActionBarWithNavController(activity!! as AppCompatActivity, navController)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_recipe, menu)
        this.menu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.pdf -> {
                Dexter.withActivity(activity)
                    .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                        }

                        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                            if (report.areAllPermissionsGranted()) {
                                setupFilePicker()
                            }
                        }
                    })
                    .check()
                true
            }
            R.id.play -> {
                viewModel.buildNotification(viewModel.currentRecipe)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // save current state to support screen rotation, etc...
        if (expandableItemManager != null) {
            outState.putParcelable(
                SAVED_STATE_EXPANDABLE_ITEM_MANAGER,
                expandableItemManager?.savedState
            )
        }
    }

    override fun onDestroyView() {
        list.adapter = null
        expandableItemManager?.release()
        touchActionGuardManager.release()

        if (itemAdapter != null)
            WrapperAdapterUtils.releaseAll(itemAdapter)

        super.onDestroyView()
    }

    private fun popFragment() {
        navController.popBackStack()
    }

    fun setupFilePicker() {
        DialogProperties().apply {
            selection_mode = DialogConfigs.SINGLE_MODE
            selection_type = DialogConfigs.DIR_SELECT
            root = Environment.getExternalStorageDirectory()
            error_dir = File(DialogConfigs.DEFAULT_DIR)
            offset = File(DialogConfigs.DEFAULT_DIR)
            extensions = null

            FilePickerDialog(getString(R.string.choose_directory), this).let {
                it.setDialogSelectionListener { result ->
                    menu.findItem(R.id.pdf).actionView = ProgressBar(context)
                    viewModel.exportToPdf(viewModel.currentRecipe, File(result.first()))
                        .observe(this@RecipeFragment, Observer {file ->
                            menu.findItem(R.id.pdf).actionView = null

                            Snackbar.make(view!!, R.string.pdf_created, Snackbar.LENGTH_LONG)
                                .setAction(R.string.open) {
                                    val target = Intent(Intent.ACTION_VIEW)
                                    target.setDataAndType(
                                        FileProvider.getUriForFile(
                                            context,
                                            context.applicationContext.packageName + ".provider",
                                            file
                                        ), "application/pdf"
                                    )
                                    target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                                    target.addFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION )

                                    val intent = Intent.createChooser(target, "Open File")
                                    intent.addFlags( Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION )
                                    try {
                                        startActivity(intent)
                                    } catch (e: ActivityNotFoundException) {
                                        Snackbar.make(
                                            view!!,
                                            R.string.no_viewer_found,
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }

                                }.show()
                        })

                }
                it.show(fragmentManager!!, "file_picker")
            }
        }
    }

    override fun onGroupExpand(groupPosition: Int, fromUser: Boolean, payload: Any?) {

    }

    override fun onGroupCollapse(groupPosition: Int, fromUser: Boolean, payload: Any?) {
    }
}

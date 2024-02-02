package zelgius.com.myrecipes.fragments


import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.transition.TransitionInflater
import android.view.*
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import zelgius.com.myrecipes.BuildConfig
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.adapters.GroupDividerDecoration
import zelgius.com.myrecipes.adapters.HeaderAdapterWrapper
import zelgius.com.myrecipes.adapters.RecipeExpandableAdapter
import zelgius.com.myrecipes.databinding.FragmentRecipeBinding
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.utils.UiUtils
import java.io.File


/**
 * A simple [Fragment] subclass.
 *
 */
class RecipeFragment : Fragment(),
    RecyclerViewExpandableItemManager.OnGroupExpandListener,
    RecyclerViewExpandableItemManager.OnGroupCollapseListener {

    companion object {
        const val SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager"

    }

    private var _binding: FragmentRecipeBinding? = null
    private val binding: FragmentRecipeBinding
        get() = _binding!!

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        expandableItemManager?.release()
        touchActionGuardManager.release()
    }

    private var itemAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null

    private lateinit var viewModel: RecipeViewModel
    private lateinit var adapter: RecipeExpandableAdapter
    private lateinit var headerWrapper: HeaderAdapterWrapper

    private var expandableItemManager: RecyclerViewExpandableItemManager? = null
    private lateinit var touchActionGuardManager: RecyclerViewTouchActionGuardManager
    private lateinit var menu: Menu

    private lateinit var navController: NavController
    private lateinit var selectDocument: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(RecipeViewModel::class.java)

        headerWrapper = HeaderAdapterWrapper(
            requireContext(),
            viewModel
        ) { /*startPostponedEnterTransition()*/ binding.header.root.visibility = View.INVISIBLE }

        adapter = RecipeExpandableAdapter(requireContext(), viewModel)

        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        sharedElementReturnTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)

        /*postponeEnterTransition()*/

        requireActivity().onBackPressedDispatcher.addCallback(this)
        {
            popFragment()
        }

        selectDocument = registerForActivityResult(ActivityResultContracts.CreateDocument())
        {
            if (it == null) return@registerForActivityResult

            viewModel.exportToPdf(recipe = viewModel.currentRecipe, it)
                .observe(this@RecipeFragment) { uri ->
                    menu.findItem(R.id.pdf).actionView = null

                    Snackbar.make(requireView(), R.string.pdf_created, Snackbar.LENGTH_LONG)
                        .setAction(R.string.open) {
                            requireContext().startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_VIEW)
                                        .setDataAndType(uri, "application/pdf")
                                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY),
                                    getString(R.string.select_file)
                                )
                            )
                        }
                        .show()
                }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        binding.apply {
            val recipe = arguments?.getParcelable<RecipeEntity>("RECIPE")?.apply {
                viewModel.loadRecipe(id!!)
            } ?: RecipeEntity(RecipeEntity.Type.MEAL)
            //viewModel.selectedRecipe.value = recipe

            if (arguments != null)
                UiUtils.bindHeader(
                    recipe,
                    UiUtils.HeaderViewHolder(view, header.imageView, header.name, header.category)
                )

            viewModel.selectedImageUrl.value = viewModel.selectedRecipe.value?.imageURL?.toUri()

            viewModel.editMode.value = true

            viewModel.editMode.observe(viewLifecycleOwner, {
                adapter.notifyDataSetChanged()
            })

            viewModel.selectedRecipe.observe(viewLifecycleOwner, {
                viewModel.currentRecipe = it
                adapter.recipe = it
                adapter.notifyDataSetChanged()

                headerWrapper.recipe = it
                headerWrapper.notifyDataSetChanged()

                activity?.actionBar?.title = it.name
            })

            viewModel.selectedImageUrl.observe(viewLifecycleOwner, {
                if (it != null && it.toString().isNotEmpty()) {
                    header.imageView.setPadding(0, 0, 0, 0)
                    header.imageView.setImageURI(it)
                }
            })

            val eimSavedState =
                savedInstanceState?.getParcelable<Parcelable>(SAVED_STATE_EXPANDABLE_ITEM_MANAGER)

            expandableItemManager = RecyclerViewExpandableItemManager(eimSavedState)
            expandableItemManager?.setOnGroupExpandListener(this@RecipeFragment)
            expandableItemManager?.setOnGroupCollapseListener(this@RecipeFragment)
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

            viewModel.pdfProgress.observe(viewLifecycleOwner) {
                requireActivity().invalidateOptionsMenu()
            }


            adapter.expandableItemManager = expandableItemManager
            list.adapter = itemAdapter
            list.addItemDecoration(
                GroupDividerDecoration(
                    requireContext(),
                    ContextCompat.getColor(requireContext(), android.R.color.transparent),
                    8f
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        NavigationUI.setupActionBarWithNavController(
            requireActivity() as AppCompatActivity,
            navController
        )
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        requireActivity().invalidateOptionsMenu()
    }

    private var progressMenuId: Int = 0
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_recipe, menu)
        this.menu = menu
        if (viewModel.pdfProgress.value == true) {
            menu.findItem(progressMenuId)?.apply {
                actionView = ProgressBar(requireContext())
            }
        } else {
            menu.findItem(progressMenuId)?.apply {
                actionView = null
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.pdf -> {
                progressMenuId = R.id.pdf
                selectDocument.launch("${viewModel.currentRecipe.name}.pdf")
                true
            }
            R.id.play -> {
                viewModel.buildNotification(viewModel.currentRecipe)
                true
            }

            R.id.share -> {
                progressMenuId = R.id.share
                val recipe = viewModel.currentRecipe
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    File(
                        requireContext()
                            .getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                        "${recipe.name.replace(File.separator, " _ ")}.pdf"
                    )
                )
                menu.findItem(R.id.share).apply {
                    actionView = ProgressBar(requireContext())
                }
                viewModel.exportToPdf(recipe, uri).observe(viewLifecycleOwner) {
                    menu.findItem(R.id.share).apply {
                        actionView = null
                    }

                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, it)
                        type = "application/pdf"
                    }

                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)
                }

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
        binding.list.adapter = null
        expandableItemManager?.release()
        touchActionGuardManager.release()

        if (itemAdapter != null)
            WrapperAdapterUtils.releaseAll(itemAdapter)

        super.onDestroyView()
    }

    private fun popFragment() {
        navController.popBackStack()
    }

    override fun onGroupExpand(groupPosition: Int, fromUser: Boolean, payload: Any?) {

    }

    override fun onGroupCollapse(groupPosition: Int, fromUser: Boolean, payload: Any?) {
    }
}

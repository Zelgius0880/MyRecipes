package zelgius.com.myrecipes.fragments


import android.os.Bundle
import android.os.Parcelable
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import kotlinx.android.synthetic.main.adapter_fragment_recipe.view.*
import kotlinx.android.synthetic.main.fragment_recipe.*
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
        ViewModelProviders.of(context).get(RecipeViewModel::class.java)
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

        val recipe =  arguments?.getParcelable("RECIPE") ?: Recipe(Recipe.Type.MEAL)
        viewModel.selectedRecipe.value = recipe

        if(arguments != null)
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

    override fun onGroupExpand(groupPosition: Int, fromUser: Boolean, payload: Any?) {

    }

    override fun onGroupCollapse(groupPosition: Int, fromUser: Boolean, payload: Any?) {
    }

}

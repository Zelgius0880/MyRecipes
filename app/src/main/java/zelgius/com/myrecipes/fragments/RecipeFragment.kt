package zelgius.com.myrecipes.fragments


import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import kotlinx.android.synthetic.main.activity_recipe.*
import kotlinx.android.synthetic.main.activity_recipe.toolbar
import kotlinx.android.synthetic.main.fragment_tab.view.*
import net.alhazmy13.mediapicker.Image.ImagePicker
import zelgius.com.myrecipes.NoticeDialogListener
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.adapters.GroupDividerDecoration
import zelgius.com.myrecipes.adapters.HeaderAdapterWrapper
import zelgius.com.myrecipes.adapters.RecipeEditAdapter
import zelgius.com.myrecipes.adapters.RecipeExpandableAdapter
import zelgius.com.myrecipes.dialogs.IngredientDialogFragment
import zelgius.com.myrecipes.dialogs.StepDialogFragment
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.utils.AnimationUtils
import zelgius.com.myrecipes.utils.colorSecondary
import kotlin.math.roundToInt


/**
 * A simple [Fragment] subclass.
 *
 */
class RecipeFragment : Fragment(), OnBackPressedListener, NoticeDialogListener,
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
        dragDropManager.release()
        touchActionGuardManager.release()
    }

    /*override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.do_not_move, R.anim.do_not_move)
    }*/

    private var vectorAnimation: AnimatedVectorDrawableCompat? = null

    private val context by lazy { activity as AppCompatActivity }
    private val viewModel by lazy {
        ViewModelProviders.of(context).get(RecipeViewModel::class.java)
    }
    private val adapter by lazy { RecipeExpandableAdapter(context, viewModel) }
    private val headerWrapper by lazy { HeaderAdapterWrapper(context, viewModel) }

    private var expandableItemManager: RecyclerViewExpandableItemManager? = null
    private lateinit var dragDropManager: RecyclerViewDragDropManager
    private lateinit var touchActionGuardManager: RecyclerViewTouchActionGuardManager
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.activity_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        val args = arguments
        viewModel.createDummySample() // TODO need to be removed

        ViewAnimationUtils.createCircularReveal(
            view, (fab.x + fab.width / 2).roundToInt(),
            (fab.y + fab.height / 2).roundToInt(),
            view.width.toFloat(),
            view.height.toFloat()
        )

        fab.menuLayouts = arrayOf(addStepLayout, addIngredientLayout)
        fab.animation =
            AnimatedVectorDrawableCompat.create(context, R.drawable.av_add_list_to_close)!! to
                    AnimatedVectorDrawableCompat.create(
                        context,
                        R.drawable.av_close_to_add_list
                    )!!

        vectorAnimation =
            AnimatedVectorDrawableCompat.create(context, R.drawable.av_add_list_to_add)

        if (args?.containsKey(AnimationUtils.EXTRA_CIRCULAR_REVEAL_SETTINGS) == true) {

            AnimationUtils.enterCircularRevealAnimation(
                context,
                rootLayout,
                args.getParcelable(AnimationUtils.EXTRA_CIRCULAR_REVEAL_SETTINGS)!!,
                context.colorSecondary,
                Color.WHITE
            )
        }

        viewModel.selectedRecipe.value =
            arguments?.getParcelable("RECIPE") ?: Recipe(Recipe.Type.MEAL)
        viewModel.editMode.value = true

        fab.setImageResource(R.drawable.ic_playlist_plus)

        viewModel.editMode.observe(this, Observer {
            adapter.notifyDataSetChanged()
        })

        viewModel.selectedRecipe.observe(this, Observer {
            viewModel.currentRecipe = it
            adapter.recipe = it
            adapter.notifyDataSetChanged()

            headerWrapper.recipe = it
            headerWrapper.notifyDataSetChanged()
        })

        //region RecyclerView Config
        val eimSavedState =
            savedInstanceState?.getParcelable<Parcelable>(SAVED_STATE_EXPANDABLE_ITEM_MANAGER)

        expandableItemManager = RecyclerViewExpandableItemManager(eimSavedState)
        expandableItemManager?.setOnGroupExpandListener(this)
        expandableItemManager?.setOnGroupCollapseListener(this)
        expandableItemManager?.defaultGroupsExpandedState = true

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        touchActionGuardManager = RecyclerViewTouchActionGuardManager()
        touchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true)
        touchActionGuardManager.isEnabled = true

        // drag & drop manager
        dragDropManager = RecyclerViewDragDropManager()
        dragDropManager.isCheckCanDropEnabled = true
        dragDropManager.setInitiateOnLongPress(true)
        dragDropManager.setInitiateOnMove(false)
        /*dragDropManager.setDraggingItemShadowDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.material_shadow_z3
            ) as NinePatchDrawable?
        )*/

        var itemAdapter = expandableItemManager?.createWrappedAdapter(adapter)
        itemAdapter = dragDropManager.createWrappedAdapter(itemAdapter!!)
        itemAdapter = headerWrapper.setAdapter(itemAdapter)

        touchActionGuardManager.attachRecyclerView(list)
        dragDropManager.attachRecyclerView(list)
        expandableItemManager?.attachRecyclerView(list)


        adapter.expandableItemManager = expandableItemManager
        adapter.dragDropManager = dragDropManager
        list.adapter = itemAdapter
        list.addItemDecoration(
            GroupDividerDecoration(
                context,
                ContextCompat.getColor(context, android.R.color.transparent),
                8f
            )
        )
        //endregion

        adapter.editIngredientListener = {
            IngredientDialogFragment.newInstance(it)
                .show(fragmentManager!!, "dialog_ingredient")
        }

        adapter.editStepListener = {
            it.new = false
            StepDialogFragment.newInstance(it)
                .show(fragmentManager!!, "dialog_step")
        }

        fab.menuLayouts = arrayOf(addStepLayout, addIngredientLayout)
        fab.animation =
            AnimatedVectorDrawableCompat.create(context, R.drawable.av_add_list_to_close)!! to
                    AnimatedVectorDrawableCompat.create(
                        context,
                        R.drawable.av_close_to_add_list
                    )!!

        addIngredient.setOnClickListener {
            IngredientDialogFragment().show(fragmentManager!!, "dialog_ingredient")
        }

        addStep.setOnClickListener {
            StepDialogFragment().show(fragmentManager!!, "dialog_step")
        }
    }

    override fun onResume() {
        super.onResume()

        (activity as AppCompatActivity).setSupportActionBar(view!!.toolbar)
        NavigationUI.setupActionBarWithNavController(activity!! as AppCompatActivity, navController)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // save current state to support screen rotation, etc...
        if (expandableItemManager != null) {
            outState.putParcelable(
                SAVED_STATE_EXPANDABLE_ITEM_MANAGER,
                expandableItemManager?.savedState
            )
        }
    }

    override fun onCreateOptionsMenu( menu: Menu,  inflater: MenuInflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_recipe, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                //endActivity()
                navController.popBackStack()

                val args = arguments
                if (args?.containsKey(AnimationUtils.EXTRA_CIRCULAR_REVEAL_SETTINGS) == true)
                    AnimationUtils
                        .exitCircularRevealAnimation(
                            context,
                            view!!,
                            args.getParcelable(AnimationUtils.EXTRA_CIRCULAR_REVEAL_SETTINGS)!!,
                            Color.WHITE,
                            context.colorSecondary
                        ) {
                            fragmentManager?.beginTransaction()?.remove(this)
                                ?.commitAllowingStateLoss()
                        }
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun onGroupExpand(groupPosition: Int, fromUser: Boolean, payload: Any?) {

    }

    override fun onGroupCollapse(groupPosition: Int, fromUser: Boolean, payload: Any?) {
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null && requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val paths = data.getStringArrayListExtra(ImagePicker.EXTRA_IMAGE_PATH)!!

            if (paths.isNotEmpty()) {
                viewModel.selectedImageUrl.value = Uri.parse("file://${paths.first()}")
            }
        }
    }

    /*private fun endActivity(resultCode: Int = Activity.RESULT_CANCELED) {
        with(intent.getParcelableExtra<AnimationUtils.RevealAnimationSetting>("EXTRA_CIRCULAR_REVEAL_SETTINGS")) {
            if (this != null) {
                *//*fab.setImageDrawable(vectorAnimation)
                vectorAnimation?.start()*//*

                zelgius.com.myrecipes.utils.AnimationUtils.exitCircularRevealAnimation(
                    this@RecipeActivity,
                    rootLayout,
                    this,
                    this@RecipeActivity.colorSecondary,
                    android.graphics.Color.WHITE
                ) {
                    setResult(resultCode)
                    finish()
                }
            }
        }
    }*/

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        when (dialog.tag) {
            "dialog_ingredient" -> {
                if (dialog is IngredientDialogFragment) {
                    dialog.ingredient.let {
                        if (it.new) {
                            viewModel.currentRecipe.ingredients.add(it)
                            it.sortOrder = viewModel.currentRecipe.ingredients.size
                            it.new = false

                            adapter.add(it)
                        } else {
                            adapter.update(it)
                        }
                    }
                }
            }

            "dialog_step" -> {
                if (dialog is StepDialogFragment) {
                    dialog.step.let {
                        if (it.new) {
                            viewModel.currentRecipe.steps.add(it)
                            it.order = viewModel.currentRecipe.steps.size
                            it.new = false

                            adapter.add(it)
                        } else {
                            adapter.update(it)
                        }
                    }
                }
            }
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
    }

}

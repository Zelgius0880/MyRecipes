package zelgius.com.myrecipes.fragments


import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.transition.TransitionInflater
import android.view.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.snackbar.Snackbar
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_recipe_edit.*
import kotlinx.android.synthetic.main.fragment_tab.view.*
import kotlinx.android.synthetic.main.layout_header_edit.*
import net.alhazmy13.mediapicker.Image.ImagePicker
import zelgius.com.myrecipes.MainActivity
import zelgius.com.myrecipes.NoticeDialogListener
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.adapters.EditHeaderAdapterWrapper
import zelgius.com.myrecipes.adapters.EditRecipeExpandableAdapter
import zelgius.com.myrecipes.adapters.GroupDividerDecoration
import zelgius.com.myrecipes.dialogs.IngredientDialogFragment
import zelgius.com.myrecipes.dialogs.StepDialogFragment
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.utils.UiUtils
import kotlin.math.roundToInt
import kotlin.math.sqrt


/**
 * A simple [Fragment] subclass.
 *
 */
class EditRecipeFragment : Fragment(), OnBackPressedListener, NoticeDialogListener,
    RecyclerViewExpandableItemManager.OnGroupExpandListener,
    RecyclerViewExpandableItemManager.OnGroupCollapseListener {

    companion object {
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

    private var itemAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null
    private var vectorAnimation: AnimatedVectorDrawableCompat? = null

    private val context by lazy { activity as AppCompatActivity }
    private val viewModel by lazy {
         ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(RecipeViewModel::class.java)
    }
    private val adapter by lazy { EditRecipeExpandableAdapter(context, viewModel) }
    private val headerWrapper by lazy {
        EditHeaderAdapterWrapper(
            context,
            viewModel
        ) { /*startPostponedEnterTransition()*/ header.visibility = View.INVISIBLE }
    }

    private var expandableItemManager: RecyclerViewExpandableItemManager? = null
    private lateinit var dragDropManager: RecyclerViewDragDropManager
    private lateinit var touchActionGuardManager: RecyclerViewTouchActionGuardManager
    private lateinit var  swipeManager: RecyclerViewSwipeManager

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments?.getBoolean("ADD") != true) {
            sharedElementEnterTransition =
                TransitionInflater.from(context).inflateTransition(android.R.transition.move)
            sharedElementReturnTransition =
                TransitionInflater.from(context).inflateTransition(android.R.transition.move)

            /*postponeEnterTransition()*/
        }

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
        return inflater.inflate(R.layout.fragment_recipe_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        val recipe =  arguments?.getParcelable("RECIPE") ?: Recipe(Recipe.Type.MEAL)
        viewModel.selectedRecipe.value = recipe

        if(arguments != null)
            UiUtils.bindHeader(recipe, UiUtils.HeaderViewHolder(view, imageView, editName, editCategory))

        if (arguments?.getBoolean("ADD") == true && ViewCompat.isAttachedToWindow(view)) {
            view.doOnPreDraw {
                val (width, height) = arrayOf(
                    rootLayout.width.toFloat(),
                    rootLayout.width.toFloat()
                )
                val finalRadius = sqrt((width * width + height * height).toDouble()).toFloat()

                ViewAnimationUtils.createCircularReveal(
                    rootLayout, (fab.x + fab.width / 2).roundToInt(),
                    (fab.y + fab.height / 2).roundToInt(),
                    0f,
                    finalRadius
                ).apply {
                    duration = 200L
                    start()
                }
            }
        }

        fab.menuLayouts = arrayOf(addStepLayout, addIngredientLayout)
        fab.animation =
            AnimatedVectorDrawableCompat.create(context, R.drawable.av_add_list_to_close)!! to
                    AnimatedVectorDrawableCompat.create(
                        context,
                        R.drawable.av_close_to_add_list
                    )!!

        vectorAnimation =
            AnimatedVectorDrawableCompat.create(context, R.drawable.av_add_list_to_add)

        viewModel.editMode.value = true

        fab.setImageResource(R.drawable.ic_playlist_plus)

        viewModel.editMode.observe(viewLifecycleOwner, {
            adapter.notifyDataSetChanged()
        })

        viewModel.selectedRecipe.observe(viewLifecycleOwner, {
            viewModel.currentRecipe = it
            adapter.recipe = it
            adapter.notifyDataSetChanged()

            headerWrapper.recipe = it
            headerWrapper.notifyDataSetChanged()
        })

        viewModel.selectedImageUrl.observe(viewLifecycleOwner, {
            if(it != null && it.toString().isNotEmpty()) {
                imageView.setPadding(0, 0, 0, 0)
                imageView.setImageURI(it)
            }
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

        swipeManager = RecyclerViewSwipeManager()

        //The order here is important
        itemAdapter = expandableItemManager?.createWrappedAdapter(adapter)
        itemAdapter = swipeManager.createWrappedAdapter(itemAdapter!!)
        itemAdapter = dragDropManager.createWrappedAdapter(itemAdapter!!)
        itemAdapter = headerWrapper.setAdapter(itemAdapter!!)

        touchActionGuardManager.attachRecyclerView(list)
        swipeManager.attachRecyclerView(list)
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

        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && fab.isShown) {
                    fab.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fab.show()
                }

                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        adapter.editIngredientListener = {
            IngredientDialogFragment.newInstance(it, this)
                .show(parentFragmentManager, "dialog_ingredient")
        }

        adapter.editStepListener = {
            it.new = false
            StepDialogFragment.newInstance(it, this)
                .show(parentFragmentManager, "dialog_step")
        }

        fab.menuLayouts = arrayOf(addStepLayout, addIngredientLayout)
        fab.animation =
            AnimatedVectorDrawableCompat.create(context, R.drawable.av_add_list_to_close)!! to
                    AnimatedVectorDrawableCompat.create(
                        context,
                        R.drawable.av_close_to_add_list
                    )!!

        addIngredient.setOnClickListener {
            IngredientDialogFragment.newInstance(this).show(parentFragmentManager, "dialog_ingredient")
        }

        addStep.setOnClickListener {
            StepDialogFragment.newInstance(this).show(parentFragmentManager, "dialog_step")
        }
    }

    override fun onResume() {
        super.onResume()

        (activity as AppCompatActivity).setSupportActionBar(requireView().toolbar)
        NavigationUI.setupActionBarWithNavController(requireActivity() as AppCompatActivity, navController)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(if(arguments?.getBoolean("ADD") == true) R.string.new_recipe else R.string.edit_recipe)
        }


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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_edit_recipe, menu)
    }

    override fun onDestroyView() {
        list.adapter = null
        expandableItemManager?.release()
        swipeManager.release()
        touchActionGuardManager.release()
        dragDropManager.release()

        if(itemAdapter != null)
            WrapperAdapterUtils.releaseAll(itemAdapter)

        super.onDestroyView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                //endActivity()
                //navController.popBackStack()

                popFragment()
                true
            }

            R.id.save -> {
                val recipe: Recipe = viewModel.currentRecipe
                headerWrapper.complete(recipe)
                recipe.imageURL = viewModel.selectedImageUrl.value.toString()
                adapter.complete(recipe)
                viewModel.currentRecipe = recipe // not really useful, just there in case
                viewModel.saveCurrentRecipe().observe(this, {
                    Snackbar.make(
                        (activity as MainActivity).coordinator,
                        R.string.recipe_saved,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    navController.popBackStack()
                })

                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    private fun popFragment() {

        if(arguments?.getBoolean("ADD") == true) {
            val (width, height) = arrayOf(rootLayout.width.toFloat(), rootLayout.width.toFloat())
            val initialRadius = sqrt((width * width + height * height).toDouble()).toFloat()
            ViewAnimationUtils.createCircularReveal(
                rootLayout,
                (fab.x + fab.width / 2).roundToInt(),
                (fab.y + fab.height / 2).roundToInt(),
                initialRadius,
                0f

            ).apply {
                duration = 100L
                start()
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {

                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        rootLayout.visibility = View.INVISIBLE
                        navController.popBackStack()
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }

                    override fun onAnimationStart(p0: Animator?) {
                    }

                })

            }
        } else {
            navController.popBackStack()

        }
    }

    override fun onGroupExpand(groupPosition: Int, fromUser: Boolean, payload: Any?) {

    }

    override fun onGroupCollapse(groupPosition: Int, fromUser: Boolean, payload: Any?) {
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)

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

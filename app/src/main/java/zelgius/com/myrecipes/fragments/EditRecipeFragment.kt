package zelgius.com.myrecipes.fragments


import android.animation.Animator
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
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.snackbar.Snackbar
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import zelgius.com.myrecipes.NoticeDialogListener
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.adapters.EditHeaderAdapterWrapper
import zelgius.com.myrecipes.adapters.EditRecipeExpandableAdapter
import zelgius.com.myrecipes.adapters.GroupDividerDecoration
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.databinding.FragmentRecipeEditBinding
import zelgius.com.myrecipes.dialogs.IngredientDialogFragment
import zelgius.com.myrecipes.dialogs.StepDialogFragment
import zelgius.com.myrecipes.utils.UiUtils
import kotlin.math.roundToInt
import kotlin.math.sqrt


/**
 * A simple [Fragment] subclass.
 *
 */
class EditRecipeFragment : Fragment(), NoticeDialogListener,
    RecyclerViewExpandableItemManager.OnGroupExpandListener,
    RecyclerViewExpandableItemManager.OnGroupCollapseListener {

    companion object {
        const val SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager"

    }

    override fun onDestroy() {
        super.onDestroy()

        expandableItemManager?.release()
        dragDropManager.release()
        touchActionGuardManager.release()

        _binding = null
    }

    /*override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.do_not_move, R.anim.do_not_move)
    }*/
    private var _binding: FragmentRecipeEditBinding? = null
    private val binding: FragmentRecipeEditBinding
        get() = _binding!!

    private var itemAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null
    private var vectorAnimation: AnimatedVectorDrawableCompat? = null

    private lateinit var viewModel: RecipeViewModel
    private lateinit var adapter: EditRecipeExpandableAdapter
    private lateinit var headerWrapper: EditHeaderAdapterWrapper

    private var expandableItemManager: RecyclerViewExpandableItemManager? = null
    private lateinit var dragDropManager: RecyclerViewDragDropManager
    private lateinit var touchActionGuardManager: RecyclerViewTouchActionGuardManager
    private lateinit var swipeManager: RecyclerViewSwipeManager

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(RecipeViewModel::class.java)

        adapter = EditRecipeExpandableAdapter(requireContext(), viewModel)

        headerWrapper = EditHeaderAdapterWrapper(
            requireContext(),
            viewModel
        ) { /*startPostponedEnterTransition()*/ binding.header.root.visibility = View.INVISIBLE }

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
        _binding = FragmentRecipeEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            val recipe = arguments?.getParcelable("RECIPE") ?: Recipe(type= viewModel.selectedType, name = "")
            viewModel.selectedRecipe.value = recipe

            if (arguments != null)
                UiUtils.bindHeader(
                    recipe,
                    UiUtils.HeaderViewHolder(
                        view,
                        header.imageView,
                        header.editName,
                        header.editCategory
                    )
                )

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
                AnimatedVectorDrawableCompat.create(
                    requireContext(),
                    R.drawable.av_add_list_to_close
                )!! to
                        AnimatedVectorDrawableCompat.create(
                            requireContext(),
                            R.drawable.av_close_to_add_list
                        )!!

            vectorAnimation =
                AnimatedVectorDrawableCompat.create(requireContext(), R.drawable.av_add_list_to_add)

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
                if (it != null && it.toString().isNotEmpty()) {
                    header.imageView.setPadding(0, 0, 0, 0)
                    header.imageView.setImageURI(it)
                }
            })

            val eimSavedState =
                savedInstanceState?.getParcelable<Parcelable>(SAVED_STATE_EXPANDABLE_ITEM_MANAGER)

            expandableItemManager = RecyclerViewExpandableItemManager(eimSavedState)
            expandableItemManager?.setOnGroupExpandListener(this@EditRecipeFragment)
            expandableItemManager?.setOnGroupCollapseListener(this@EditRecipeFragment)
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
                    requireContext(),
                    ContextCompat.getColor(requireContext(), android.R.color.transparent),
                    8f
                )
            )

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
                IngredientDialogFragment.newInstance(it, this@EditRecipeFragment)
                    .show(parentFragmentManager, "dialog_ingredient")
            }

            adapter.editStepListener = {
                //it.new = false
                StepDialogFragment.newInstance(it, this@EditRecipeFragment)
                    .show(parentFragmentManager, "dialog_step")
            }

            fab.menuLayouts = arrayOf(addStepLayout, addIngredientLayout)
            fab.animation =
                AnimatedVectorDrawableCompat.create(
                    requireContext(),
                    R.drawable.av_add_list_to_close
                )!! to
                        AnimatedVectorDrawableCompat.create(
                            requireContext(),
                            R.drawable.av_close_to_add_list
                        )!!

            addIngredient.setOnClickListener {
                IngredientDialogFragment.newInstance(this@EditRecipeFragment)
                    .show(parentFragmentManager, "dialog_ingredient")
            }

            addStep.setOnClickListener {
                StepDialogFragment.newInstance(this@EditRecipeFragment)
                    .show(parentFragmentManager, "dialog_step")
            }
        }
    }

    override fun onResume() {
        super.onResume()

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(if (arguments?.getBoolean("ADD") == true) R.string.new_recipe else R.string.edit_recipe)
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
        binding.list.adapter = null
        expandableItemManager?.release()
        swipeManager.release()
        touchActionGuardManager.release()
        dragDropManager.release()

        if (itemAdapter != null)
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
                val recipe = viewModel.currentRecipe
                headerWrapper.complete(recipe)
                //recipe.imageUrl = viewModel.selectedImageUrl.value.toString()
                adapter.complete(recipe)
                viewModel.currentRecipe = recipe // not really useful, just there in case
                viewModel.saveCurrentRecipe().observe(this, {
                    Snackbar.make(
                        requireActivity().findViewById(R.id.coordinator),
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
        binding.apply {
            if (arguments?.getBoolean("ADD") == true) {
                val (width, height) = arrayOf(
                    rootLayout.width.toFloat(),
                    rootLayout.width.toFloat()
                )
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
                        override fun onAnimationRepeat(p0: Animator) {

                        }

                        override fun onAnimationEnd(p0: Animator) {
                            rootLayout.visibility = View.INVISIBLE
                            navController.popBackStack()
                        }

                        override fun onAnimationCancel(p0: Animator) {
                        }

                        override fun onAnimationStart(p0: Animator) {
                        }

                    })

                }
            } else {
                navController.popBackStack()

            }
        }
    }

    override fun onGroupExpand(groupPosition: Int, fromUser: Boolean, payload: Any?) {

    }

    override fun onGroupCollapse(groupPosition: Int, fromUser: Boolean, payload: Any?) {
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
                        if (it.id == null) {
                            //viewModel.currentRecipe.ingredients.add(it)
                            //it.sortOrder = viewModel.currentRecipe.ingredients.size
                            //it.new = false

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
                        if (it.id == null) {
                            //viewModel.currentRecipe.steps.add(it)
                            //it.order = viewModel.currentRecipe.steps.size
                            //it.new = false

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

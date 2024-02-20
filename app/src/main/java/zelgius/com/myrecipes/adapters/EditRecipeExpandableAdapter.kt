package zelgius.com.myrecipes.adapters

import TextDrawable
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.card.MaterialCardView
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.expandable.*
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants.*
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.data.entities.IngredientForRecipe
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.entities.StepEntity
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.data.text
import zelgius.com.myrecipes.databinding.AdapterIngredientBinding
import zelgius.com.myrecipes.databinding.AdapterStepBinding
import zelgius.com.myrecipes.utils.UiUtils
import zelgius.com.myrecipes.utils.ViewUtils
import zelgius.com.myrecipes.utils.context
import zelgius.com.myrecipes.utils.dpToPx
import kotlin.math.min


val TAG = EditRecipeExpandableAdapter::class.simpleName

class EditRecipeExpandableAdapter(val context: Context, viewModel: RecipeViewModel) :
    AbstractExpandableItemAdapter<EditRecipeExpandableAdapter.StepSectionViewHolder, AbstractDraggableSwipeableItemViewHolder>(),
    ExpandableDraggableItemAdapter<EditRecipeExpandableAdapter.StepSectionViewHolder, AbstractDraggableSwipeableItemViewHolder>,
    ExpandableSwipeableItemAdapter<EditRecipeExpandableAdapter.StepSectionViewHolder, AbstractDraggableSwipeableItemViewHolder> {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    lateinit var provider: AdapterDataProvider<StepItem, DataItem>
    var expandableItemManager: RecyclerViewExpandableItemManager? = null
    var dragDropManager: RecyclerViewDragDropManager? = null

    var recipe = viewModel.currentRecipe
        set(value) {
            field = value
            createProvider()
        }

    var draggingStep: Step? = null

    init {
        createProvider()
        setHasStableIds(true)
    }

    private val alpha = 0.6f

    private fun createProvider() {
        val list = mutableListOf<Pair<StepItem, MutableList<DataItem>>>()

        list.add(StepItem(0, null) to recipe.ingredients
            .filter { it.step == null }
            .mapIndexed { i, item ->
                IngredientItem(
                    item.id ?: i.toLong(),
                    item
                )
            } // Flagged as 'No cast needed' but actually needed
            .toMutableList())

        recipe.steps.forEachIndexed { i, s ->
            list.add(StepItem(i + 1L, s) to recipe.ingredients
                .filter { it.step == s }
                .map { IngredientItem(it.id ?: 0L, it) }
                .toMutableList<DataItem>()
                .apply {
                    add(StepItem(i + 1L, s))
                }
            )
        }

        provider = AdapterDataProvider(list)
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
        this.layoutManager = recyclerView.layoutManager as LinearLayoutManager
    }

    class StepItem(adapterId: Long, val item: Step?) : DataItem(adapterId) {
        override val isSectionHeader: Boolean
            get() = false
    }

    class IngredientItem(adapterId: Long, val item: Ingredient) : DataItem(adapterId) {
        override val isSectionHeader: Boolean
            get() = false
    }


    var editStepListener: ((Step) -> Unit)? = null
    var editIngredientListener: ((Ingredient) -> Unit)? = null

    fun complete(recipe: Recipe) {
        for (i in 0 until provider.groupCount) {
            provider.getGroupItem(i).item?.let {
                //it.order = i
                //recipe.steps.add(it)
            }

            for (j in 0 until provider.getChildCount(i)) {
                val item = provider.getChildItem(i, j)
                if (item is IngredientItem) {

                    item.item.let {
                        //it.sortOrder = j
                        //it.step = provider.getGroupItem(i).item
                        //recipe.ingredients.add(it)
                    }
                }
            }
        }
    }

    //region ViewHolder
    inner class IngredientViewHolder(val binding: AdapterIngredientBinding) :
        AbstractDraggableSwipeableItemViewHolder(binding.root),
        ExpandableItemViewHolder {

        private val mExpandState = ExpandableItemState()
        override fun getExpandState(): ExpandableItemState = mExpandState

        override fun getExpandStateFlags(): Int = mExpandState.flags

        override fun setExpandStateFlags(flags: Int) {
            mExpandState.flags = flags
        }

        override fun getSwipeableContainerView(): View = binding.containerIngredient

        fun bind(parentPosition: Int, childPosition: Int) {
            val dragState = dragState
            if (itemView is MaterialCardView) binding.root.isDragged =
                dragState.isActive && dragState.isUpdated

            val item = (provider.getChildItem(parentPosition, childPosition) as IngredientItem).item

            if (itemView is MaterialCardView) {
                binding.root.cardElevation = binding.context.dpToPx(2f)
                binding.root.setCardBackgroundColor(
                    if (item.step == draggingStep && draggingStep != null)
                        context.getColor(R.color.md_orange_200)
                    /*else if (item.step == null)
                        context.getColor(R.color.md_white_1000)*/
                    else
                        context.getColor(R.color.md_blue_50)
                )
            }

            binding.ingredientName.text = item.text(context)
            UiUtils.getIngredientDrawable(binding.image, item)

            binding.root.setOnClickListener { editIngredientListener?.invoke(item) }

            if (item.optional == true || item.step?.optional == true) {
                binding.image.alpha = alpha
                binding.ingredientName.alpha = alpha
            }
        }
    }

    inner class StepSectionViewHolder(val binding: AdapterStepBinding) :
        AbstractDraggableSwipeableItemViewHolder(binding.root),
        ExpandableItemViewHolder {

        private val mExpandState = ExpandableItemState()
        override fun getExpandState(): ExpandableItemState = mExpandState

        override fun getExpandStateFlags(): Int = mExpandState.flags

        override fun setExpandStateFlags(flags: Int) {
            mExpandState.flags = flags
        }

        override fun getSwipeableContainerView(): View = binding.containerStep


        private val upToDown =
            AnimatedVectorDrawableCompat.create(context, R.drawable.avd_up_to_down)
        private val downToUp =
            AnimatedVectorDrawableCompat.create(context, R.drawable.avd_down_to_up)

        fun bind(position: Int) {
            val dragState = dragState
            if (itemView is MaterialCardView) binding.root.isDragged =
                dragState.isActive && dragState.isUpdated

            val item = provider.getGroupItem(position).item
            binding.root.setOnClickListener { }
            binding.expand.visibility = View.VISIBLE

            binding.step.text = item?.text ?: context.getString(R.string.ingredients)

            //Visibility for dragging
            if (!swipeState.isSwiping) {
                if (item == draggingStep && draggingStep != null) {
                    binding.step.visibility = View.VISIBLE
                    binding.expand.visibility = View.GONE
                    binding.stepImage.visibility = View.VISIBLE
                } else {
                    // Visibility for expand
                    with(if (expandState.isExpanded || item == null) View.GONE else View.VISIBLE) {
                        binding.step.visibility = this
                        binding.stepImage.visibility = this
                    }
                    binding.expand.visibility = View.VISIBLE
                }
            }

            //Visibility for first group (Ingredients)
            if (item == null) {
                binding.step.visibility = View.VISIBLE
                binding.step.setText(R.string.ingredients)
            }

            (itemView as MaterialCardView).setCardBackgroundColor(
                if (item == draggingStep && draggingStep != null)
                    context.getColor(R.color.md_deep_orange_200)
                /*else if (item == null)
                    context.getColor(R.color.md_white_1000)*/
                else
                    context.getColor(R.color.md_blue_50)

            )

            if (item != null) {
                binding.stepImage.setImageDrawable(
                    TextDrawable(binding.context.resources,
                        "${item.order}",
                        )
                )
            }

            if (expandState.isUpdated) {

                val animation = if (!expandState.isExpanded) {
                    upToDown
                } else {
                    downToUp
                }

                binding.expand.setImageDrawable(animation)
                animation?.start()

            }

            if (item?.optional == true) {
                binding.stepImage.alpha = alpha
                binding.step.alpha = alpha
            }
        }
    }

    open inner class StepViewHolder(val binding: AdapterStepBinding) :
        AbstractDraggableSwipeableItemViewHolder(binding.root),
        ExpandableItemViewHolder {
        private val mExpandState = ExpandableItemState()
        override fun getExpandState(): ExpandableItemState = mExpandState

        override fun getExpandStateFlags(): Int = mExpandState.flags

        override fun setExpandStateFlags(flags: Int) {
            mExpandState.flags = flags
        }

        override fun getSwipeableContainerView(): View = binding.containerStep

        fun bind(groupPosition: Int, childPosition: Int) {
            val dragState = dragState
            if (itemView is MaterialCardView) binding.root.isDragged =
                dragState.isActive && dragState.isUpdated

            val item = (provider.getChildItem(groupPosition, childPosition) as StepItem).item

            if (item == null) {
                binding.step.setText(R.string.ingredients)
                return
            }

            binding.step.text = item.text

            (itemView as MaterialCardView).setCardBackgroundColor(
                if (item == draggingStep && draggingStep != null)
                    context.getColor(R.color.md_orange_200)
                else context.getColor(R.color.md_blue_50)
            )

            binding.stepImage.setImageDrawable(
                TextDrawable(binding.context.resources, "${item.order}")
            )

            binding.root.setOnClickListener { editStepListener?.invoke(item) }

            //Visibility for dragging
            if (!swipeState.isSwiping) {
                if (item == draggingStep && draggingStep != null) {
                    binding.step.visibility = View.GONE
                    binding.stepImage.visibility = View.GONE
                    binding.behindViewStep.visibility = View.GONE
                } else {
                    // Visibility for expand
                    binding.step.visibility = View.VISIBLE
                    binding.stepImage.visibility = View.VISIBLE
                    binding.behindViewStep.visibility = View.VISIBLE
                }
            }

            if (item.optional) {
                binding.stepImage.alpha = alpha
                binding.step.alpha = alpha
            }
        }
    }

    override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): StepSectionViewHolder =
        StepSectionViewHolder(
            AdapterStepBinding.inflate(
                LayoutInflater.from(parent.context), parent,
                false
            )
        )


    override fun getChildItemViewType(groupPosition: Int, childPosition: Int): Int =
        if (provider.getChildItem(groupPosition, childPosition) is StepItem) R.layout.adapter_step
        else R.layout.adapter_ingredient

    override fun onCreateChildViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractDraggableSwipeableItemViewHolder =
        if (viewType == R.layout.adapter_ingredient)
            IngredientViewHolder(
                AdapterIngredientBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        else
            StepViewHolder(
                AdapterStepBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

    override fun getGroupId(groupPosition: Int): Long =
        provider.getGroupItem(groupPosition).adapterId


    override fun getChildId(groupPosition: Int, childPosition: Int): Long =
        provider.getChildItem(groupPosition, childPosition).adapterId


    override fun getGroupCount(): Int =
        provider.groupCount

    override fun getChildCount(groupPosition: Int): Int = provider.getChildCount(groupPosition)


    override fun onBindChildViewHolder(
        holder: AbstractDraggableSwipeableItemViewHolder,
        groupPosition: Int,
        childPosition: Int,
        viewType: Int
    ) {
        if (holder is StepViewHolder)
            holder.bind(groupPosition, childPosition)
        else if (holder is IngredientViewHolder)
            holder.bind(groupPosition, childPosition)
    }

    override fun onBindGroupViewHolder(
        holder: StepSectionViewHolder,
        groupPosition: Int,
        viewType: Int
    ) {
        holder.bind(groupPosition)
    }

    //endregion

    //region Add Remove Update

    fun add(item: Ingredient) {
        val i = provider.list.indexOfLast { item.step == it.first.item }
        if (i >= 0) {
            /*item.sortOrder =
                if (provider.getChildCount(i) > 0 && provider.getChildItem(
                        i,
                        provider.getChildCount(i) - 1
                    ) is StepItem
                )
                    provider.getChildCount(i) - 1
                else
                    provider.getChildCount(i)*/

            provider.addChildItem(
                i,
                item.sortOrder,
                IngredientItem(item.sortOrder.toLong(), item)
            )
            expandableItemManager?.notifyChildItemInserted(i, item.sortOrder)
        }
    }

    fun add(item: Step) {
        //item.order = provider.groupCount
        provider.addGroupItem(StepItem(provider.groupCount.toLong(), item))
        expandableItemManager?.notifyGroupItemInserted(item.order)

        val i = provider.list.indexOfLast { item == it.first.item }
        if (i >= 0) {
            provider.addChildItem(i, StepItem(item.order.toLong(), item))
            expandableItemManager?.notifyChildItemInserted(i, 0)
        }
    }

    fun update(item: Ingredient) {
        val i = provider.list.indexOfLast { item.step == it.first.item }
        if (i >= 0) {
            //item.sortOrder = provider.getChildCount(i)
            expandableItemManager?.notifyChildItemChanged(i,
                provider.list[i].second.indexOfFirst { it is IngredientItem && it.item == item }
            )
        }
    }

    fun update(item: Step) {
        //item.order = provider.groupCount
        val indexOf = provider.list.indexOfLast { item == it.first.item }
        expandableItemManager?.notifyGroupItemChanged(indexOf)

        provider.list[indexOf].second.forEachIndexed { i, _ ->
            expandableItemManager?.notifyChildItemChanged(indexOf, i)

        }
    }

    fun remove(item: Ingredient) {
        val i = provider.list.indexOfLast { item.step == it.first.item }
        if (i >= 0) {
            val j = provider.list[i].second.indexOfFirst { it is IngredientItem && it.item == item }
            //item.sortOrder = provider.getChildCount(i)
            provider.removeChildItem(i, j)
            expandableItemManager?.notifyChildItemRemoved(i, j)
        }
    }

    fun remove(item: Step) {
        val i = provider.list.indexOfLast { item == it.first.item }
        if (i >= 0) {
            /*provider.list[i].second.forEachIndexed { j, v ->
                if (v is IngredientItem) {
                    v.item.step = null
                    v.item.refStep = null
                    expandableItemManager?.notifyChildItemRemoved(i, j)
                    add(v.item)
                }
            }*/
            provider.removeGroupItem(i)
            expandableItemManager?.notifyGroupItemRemoved(i)
        }
    }

    //endregion

    //region move
    override fun onMoveGroupItem(fromGroupPosition: Int, toGroupPosition: Int) {
        //provider.getGroupItem(fromGroupPosition).item?.order = toGroupPosition
        //provider.getGroupItem(toGroupPosition).item?.order = fromGroupPosition

        provider.moveGroupItem(fromGroupPosition, toGroupPosition)
    }


    override fun onMoveChildItem(
        fromGroupPosition: Int,
        fromChildPosition: Int,
        toGroupPosition: Int,
        toChildPosition: Int
    ) {
        val from = provider.getChildItem(fromGroupPosition, fromChildPosition)
        val to = if (toChildPosition < provider.getChildCount(toGroupPosition))
            provider.getChildItem(toGroupPosition, toChildPosition)
        else null

        if (from is IngredientItem) {
            //from.item.step = provider.getGroupItem(toGroupPosition).item
            //from.item.sortOrder = toGroupPosition
        }

        if (to is IngredientItem) {
            //to.item.step = provider.getGroupItem(fromGroupPosition).item
            //to.item.sortOrder = fromGroupPosition
        }

        provider.moveChildItem(
            fromGroupPosition,
            fromChildPosition,
            toGroupPosition,

            when {
                fromGroupPosition == toGroupPosition || toGroupPosition == 0 -> toChildPosition
                provider.getChildCount(toGroupPosition) - 1 < 0 -> 0
                else -> min(toChildPosition, provider.getChildCount(toGroupPosition) - 1)
            }

        )
    }


//endregion

    //region Expand

    override fun onCheckCanExpandOrCollapseGroup(
        holder: StepSectionViewHolder,
        groupPosition: Int,
        x: Int,
        y: Int,
        expand: Boolean
    ): Boolean {
        /*if (provider.getGroupItem(groupPosition).isPinned) {
            // return false to raise View.OnClickListener#onClick() event
            return false;
        }
    */
        // check is enabled
        if (!(holder.binding.root.isEnabled && holder.binding.root.isClickable)) {
            return false
        }

        val containerView = holder.itemView
        val dragHandleView = holder.binding.stepImage

        val offsetX = containerView.left + (containerView.translationX + 0.5f).toInt()
        val offsetY = containerView.top + (containerView.translationY + 0.5f).toInt()

        return !ViewUtils.hitTest(dragHandleView, x - offsetX, y - offsetY)
    }

//endregion

    //region  Drag and Drop
    override fun onCheckGroupCanStartDrag(
        holder: StepSectionViewHolder,
        groupPosition: Int,
        x: Int,
        y: Int
    ): Boolean = provider.getGroupItem(groupPosition).item != null

    override fun onCheckChildCanStartDrag(
        holder: AbstractDraggableSwipeableItemViewHolder,
        groupPosition: Int,
        childPosition: Int,
        x: Int,
        y: Int
    ): Boolean = holder is IngredientViewHolder


    override fun onCheckGroupCanDrop(draggingGroupPosition: Int, dropGroupPosition: Int): Boolean =
        provider.getGroupItem(dropGroupPosition).item != null

    override fun onCheckChildCanDrop(
        draggingGroupPosition: Int,
        draggingChildPosition: Int,
        dropGroupPosition: Int,
        dropChildPosition: Int
    ): Boolean =
        if (dropGroupPosition != draggingGroupPosition) true
        else provider.getChildItem(
            dropGroupPosition,
            dropChildPosition
        ) is IngredientItem


    override fun onGetGroupItemDraggableRange(
        holder: StepSectionViewHolder,
        groupPosition: Int
    ): ItemDraggableRange? = null


    override fun onGetChildItemDraggableRange(
        holder: AbstractDraggableSwipeableItemViewHolder,
        groupPosition: Int,
        childPosition: Int
    ): ItemDraggableRange? = null

    override fun onChildDragFinished(
        fromGroupPosition: Int,
        fromChildPosition: Int,
        toGroupPosition: Int,
        toChildPosition: Int,
        result: Boolean
    ) {
        notifyDataSetChanged()
    }

    override fun onGroupDragFinished(
        fromGroupPosition: Int,
        toGroupPosition: Int,
        result: Boolean
    ) {
        draggingStep = null
        notifyDataSetChanged()
    }

    override fun onGroupDragStarted(groupPosition: Int) {
        draggingStep = provider.getGroupItem(groupPosition).item
        notifyDataSetChanged()
    }

    override fun onChildDragStarted(groupPosition: Int, childPosition: Int) {
        notifyDataSetChanged()
    }


//endregion

    //region Swipe

    override fun onSetGroupItemSwipeBackground(
        holder: StepSectionViewHolder,
        groupPosition: Int,
        type: Int
    ) {
        //nothing to do here
    }

    override fun onSwipeGroupItem(
        holder: StepSectionViewHolder,
        groupPosition: Int,
        result: Int
    ): SwipeResultAction? =
        when (result) {
            // swipe right
            RESULT_SWIPED_RIGHT, RESULT_SWIPED_LEFT ->
                GroupSwipeRightResultAction(this, groupPosition)
            else -> null
        }

    override fun onSwipeGroupItemStarted(holder: StepSectionViewHolder, groupPosition: Int) {
        draggingStep = provider.getGroupItem(groupPosition).item
        notifyDataSetChanged()
    }

    override fun onGetGroupItemSwipeReactionType(
        holder: StepSectionViewHolder,
        groupPosition: Int,
        x: Int,
        y: Int
    ): Int {
        if (!onCheckGroupCanStartDrag(holder, groupPosition, x, y)) {
            return REACTION_CAN_NOT_SWIPE_BOTH_H
        }

        return REACTION_CAN_SWIPE_BOTH_H
    }

    override fun onGetChildItemSwipeReactionType(
        holder: AbstractDraggableSwipeableItemViewHolder,
        groupPosition: Int,
        childPosition: Int,
        x: Int,
        y: Int
    ): Int {
        if (!onCheckChildCanStartDrag(holder, groupPosition, childPosition, x, y)) {
            return REACTION_CAN_NOT_SWIPE_BOTH_H
        }

        return REACTION_CAN_SWIPE_BOTH_H
    }

    override fun onSetChildItemSwipeBackground(
        holder: AbstractDraggableSwipeableItemViewHolder,
        groupPosition: Int,
        childPosition: Int,
        type: Int
    ) {
        // Nothing to change here
    }

    override fun onSwipeChildItemStarted(
        holder: AbstractDraggableSwipeableItemViewHolder,
        groupPosition: Int,
        childPosition: Int
    ) {
        //notifyDataSetChanged()
    }

    override fun onSwipeChildItem(
        holder: AbstractDraggableSwipeableItemViewHolder,
        groupPosition: Int,
        childPosition: Int,
        result: Int
    ): SwipeResultAction? {
        draggingStep = null
        return when (result) {
            // swipe right
            RESULT_SWIPED_RIGHT, RESULT_SWIPED_LEFT ->
                ChildSwipeRightResultAction(this, groupPosition, childPosition)
            else -> null
        }
    }


    private class ChildSwipeRightResultAction(
        private var adapterEdit: EditRecipeExpandableAdapter?,
        private val groupPosition: Int,
        private val childPosition: Int
    ) : SwipeResultActionRemoveItem() {

        override fun onPerformAction() {
            super.onPerformAction()

            /*adapterEdit?.provider?.removeChildItem(groupPosition, childPosition)
            adapterEdit?.expandableItemManager?.notifyChildItemRemoved(groupPosition, childPosition)*/

            val item = adapterEdit?.provider?.getChildItem(groupPosition, childPosition)
            if (item is IngredientItem) {
                //adapterEdit?.recipe?.ingredients?.remove(item.item)
                adapterEdit?.remove(item.item)
            }
        }

        override fun onCleanUp() {
            super.onCleanUp()
            // clear the references
            adapterEdit = null
        }
    }


    private class GroupSwipeRightResultAction(
        private var adapterEdit: EditRecipeExpandableAdapter?,
        private val groupPosition: Int
    ) : SwipeResultActionRemoveItem() {

        override fun onPerformAction() {
            super.onPerformAction()

            val item = adapterEdit?.provider?.getGroupItem(groupPosition)
            if (item is StepItem && item.item != null) {
                //adapterEdit?.recipe?.steps?.remove(item.item)
                adapterEdit?.remove(item.item)
            }
        }

        override fun onCleanUp() {
            super.onCleanUp()
            // clear the references
            adapterEdit = null
        }
    }


//endregion
}
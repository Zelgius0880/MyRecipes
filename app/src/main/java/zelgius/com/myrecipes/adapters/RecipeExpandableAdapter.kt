package zelgius.com.myrecipes.adapters

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.amulyakhare.textdrawable.TextDrawable
import com.google.android.material.card.MaterialCardView
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableDraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemState
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemViewHolder
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.adapter_ingredient.view.*
import kotlinx.android.synthetic.main.adapter_step.view.*
import kotlinx.android.synthetic.main.layout_header.view.*
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.dialogs.ImageDialogFragment
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.IngredientForRecipe
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.entities.Step
import zelgius.com.myrecipes.utils.*
import java.text.DecimalFormat
import kotlin.math.exp
import kotlin.math.min

val TAG = RecipeExpandableAdapter::class.simpleName

class RecipeExpandableAdapter(val context: Context, val viewModel: RecipeViewModel) :
    AbstractExpandableItemAdapter<RecipeExpandableAdapter.StepSectionViewHolder, AbstractDraggableItemViewHolder>(),
    ExpandableDraggableItemAdapter<RecipeExpandableAdapter.StepSectionViewHolder, AbstractDraggableItemViewHolder> {

    private lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: LinearLayoutManager
    lateinit var provider: AdapterDataProvider<StepItem, DataItem>
    var expandableItemManager: RecyclerViewExpandableItemManager? = null
    var dragDropManager: RecyclerViewDragDropManager? = null

    var recipe = viewModel.currentRecipe
    var draggingStep: Step? = null

    init {
        createProvider()
        setHasStableIds(true)
    }

    private fun createProvider() {
        val list = mutableListOf<Pair<StepItem, MutableList<DataItem>>>()

        list.add(StepItem(0, null) to recipe.ingredients
            .filter { it.step == null }
            .mapIndexed { i, item -> IngredientItem(item.id ?: i.toLong(), item) as DataItem }
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

    class IngredientItem(adapterId: Long, val item: IngredientForRecipe) : DataItem(adapterId) {
        override val isSectionHeader: Boolean
            get() = false
    }


    var editStepListener: ((Step) -> Unit)? = null
    var editIngredientListener: ((IngredientForRecipe) -> Unit)? = null

    //region ViewHolder
    inner class IngredientViewHolder(override val containerView: View) :
        AbstractDraggableItemViewHolder(containerView),
        ExpandableItemViewHolder, LayoutContainer {

        private val mExpandState = ExpandableItemState()
        override fun getExpandState(): ExpandableItemState = mExpandState

        override fun getExpandStateFlags(): Int = mExpandState.flags

        override fun setExpandStateFlags(flags: Int) {
            mExpandState.flags = flags
        }

        fun bind(parentPosition: Int, childPosition: Int) {
            val item = (provider.getChildItem(parentPosition, childPosition) as IngredientItem).item

            if (item.backgroundColor == 0)
                item.backgroundColor = itemView.context.getCompatColor(R.color.md_white_1000)

            if (itemView is CardView) {
                itemView.setCardBackgroundColor(item.backgroundColor)
                itemView.cardElevation = itemView.context.dpToPx(2f)
            }

            (itemView as MaterialCardView).setCardBackgroundColor(
                if (item.step == draggingStep && draggingStep != null)
                    context.getColor(
                        R.color.secondaryColor,
                        0.7f
                    )
                /*else if (item.step == null)
                    context.getColor(R.color.md_white_1000)*/
                else
                    context.getColor(R.color.md_blue_50)
            )


            val abrv = when (item.unit) {
                Ingredient.Unit.MILLILITER -> itemView.context.getString(R.string.milliliter_abrv)
                Ingredient.Unit.LITER -> itemView.context.getString(R.string.liter_abrv)
                Ingredient.Unit.UNIT -> itemView.context.getString(R.string.unit_abrv)
                Ingredient.Unit.TEASPOON -> itemView.context.getString(R.string.teaspoon_abrv)
                Ingredient.Unit.TABLESPOON -> itemView.context.getString(R.string.tablespoon_abrv)
                Ingredient.Unit.GRAMME -> itemView.context.getString(R.string.gramme_abrv)
                Ingredient.Unit.KILOGRAMME -> itemView.context.getString(R.string.kilogramme_abrv)
                Ingredient.Unit.CUP -> itemView.context.getString(R.string.cup_abrv)
            }

            if (item.unit != Ingredient.Unit.CUP) {
                itemView.ingredientName.text =
                    String.format(
                        "%s %s %s",
                        DecimalFormat("#0.##").format(item.quantity),
                        abrv,
                        item.name
                    )
            } else {
                val part1 = if (item.quantity.toInt() == 0) "" else "${item.quantity.toInt()} "

                val part2 = when ("${(item.quantity - item.quantity.toInt()).round(2)}".trim()) {
                    "0.0", "0" -> ""
                    "0.33", "0.34" -> "1/3 "
                    "0.66", "0.67" -> "2/3 "
                    "0.25" -> "1/4 "
                    "0.5" -> "1/2 "
                    "0.75" -> "3/4 "
                    else -> "${DecimalFormat("#0.##").format(item.quantity - item.quantity.toInt())} "
                }

                itemView.ingredientName.text =
                    String.format("%s%s%s %s", part1, part2, abrv, item.name)
            }
            UiUtils.getIngredientDrawable(itemView.image, item)

            itemView.deleteIngredient.setOnClickListener {
                recipe.ingredients.remove(item)
                remove(item)
            }

            itemView.setOnClickListener { editIngredientListener?.invoke(item) }
        }
    }

    inner class StepSectionViewHolder(override val containerView: View) :
        AbstractDraggableItemViewHolder(containerView),
        ExpandableItemViewHolder, LayoutContainer {

        private val mExpandState = ExpandableItemState()
        override fun getExpandState(): ExpandableItemState = mExpandState

        override fun getExpandStateFlags(): Int = mExpandState.flags

        override fun setExpandStateFlags(flags: Int) {
            mExpandState.flags = flags
        }


        private val upToDown =
            AnimatedVectorDrawableCompat.create(context, R.drawable.avd_up_to_down)
        private val downToUp =
            AnimatedVectorDrawableCompat.create(context, R.drawable.avd_down_to_up)

        fun bind(position: Int) {
            val item = provider.getGroupItem(position).item
            itemView.setOnClickListener { }
            itemView.expand.visibility = View.VISIBLE

            itemView.step.text = item?.text ?: context.getString(R.string.ingredients)

            //Visibility for dragging
            if (item == draggingStep && draggingStep != null) {
                itemView.step.visibility = View.VISIBLE
                itemView.deleteStep.visibility = View.GONE
                itemView.expand.visibility = View.GONE
                itemView.stepImage.visibility = View.VISIBLE
            } else {
                // Visibility for expand
                with(if (expandState.isExpanded || item == null) View.GONE else View.VISIBLE) {
                    itemView.step.visibility = this
                    itemView.deleteStep.visibility = this
                    itemView.stepImage.visibility = this
                }
                itemView.expand.visibility = View.VISIBLE
            }

            //Visibility for first group (Ingredients)
            if (item == null) {
                itemView.step.visibility = View.VISIBLE
                itemView.step.setText(R.string.ingredients)
            }

            (itemView as MaterialCardView).setCardBackgroundColor(
                if (item == draggingStep && draggingStep != null)
                    context.getColor(
                        R.color.secondaryColor,
                        0.7f
                    )
                /*else if (item == null)
                    context.getColor(R.color.md_white_1000)*/
                else
                    context.getColor(R.color.md_blue_50)

            )

            if (item != null) {
                itemView.stepImage.setImageDrawable(
                    TextDrawable.builder()
                        .beginConfig()
                        .fontSize(itemView.context.dpToPx(20f).toInt())
                        .width(itemView.context.dpToPx(36f).toInt())
                        .height(itemView.context.dpToPx(36f).toInt())
                        .bold()
                        .endConfig()
                        .buildRound(
                            "${item.order}",
                            ContextCompat.getColor(itemView.context, R.color.md_cyan_A700)
                        )
                )
            }

            if (expandState.isUpdated) {

                val animation = if (!expandState.isExpanded) {
                    upToDown
                } else {
                    downToUp
                }

                itemView.expand.setImageDrawable(animation)
                animation?.start()

            }
        }
    }

    open inner class StepViewHolder(override val containerView: View) :
        AbstractDraggableItemViewHolder(containerView),
        ExpandableItemViewHolder, LayoutContainer {
        private val mExpandState = ExpandableItemState()
        override fun getExpandState(): ExpandableItemState = mExpandState

        override fun getExpandStateFlags(): Int = mExpandState.flags

        override fun setExpandStateFlags(flags: Int) {
            mExpandState.flags = flags
        }

        fun bind(groupPosition: Int, childPosition: Int) {
            val item = (provider.getChildItem(groupPosition, childPosition) as StepItem).item

            if (item == null) {
                itemView.step.setText(R.string.ingredients)
                return
            }

            itemView.step.text = item.text

            (itemView as MaterialCardView).setCardBackgroundColor(
                if (item == draggingStep && draggingStep != null) context.getColor(
                    R.color.secondaryColor,
                    0.7f
                ) else context.getColor(R.color.md_blue_50)
            )

            itemView.deleteStep.setOnClickListener {
                recipe.steps.remove(item)
                remove(item)
            }

            itemView.stepImage.setImageDrawable(
                TextDrawable.builder()
                    .beginConfig()
                    .fontSize(itemView.context.dpToPx(20f).toInt())
                    .width(itemView.context.dpToPx(36f).toInt())
                    .height(itemView.context.dpToPx(36f).toInt())
                    .bold()
                    .endConfig()
                    .buildRound(
                        "${item.order}",
                        ContextCompat.getColor(itemView.context, R.color.md_cyan_A700)
                    )
            )

            itemView.setOnClickListener { editStepListener?.invoke(item) }

            //Visibility for dragging
            if (item == draggingStep && draggingStep != null) {
                itemView.step.visibility = View.GONE
                itemView.deleteStep.visibility = View.GONE
                itemView.stepImage.visibility = View.GONE
            } else {
                // Visibility for expand
                itemView.step.visibility = View.VISIBLE
                itemView.deleteStep.visibility = View.VISIBLE
                itemView.stepImage.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): StepSectionViewHolder =
        StepSectionViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.adapter_step,
                parent,
                false
            )
        )


    override fun getChildItemViewType(groupPosition: Int, childPosition: Int): Int =
        if (provider.getChildItem(groupPosition, childPosition) is StepItem) R.layout.adapter_step
        else R.layout.adapter_ingredient

    override fun onCreateChildViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractDraggableItemViewHolder =
        if (viewType == R.layout.adapter_ingredient)
            IngredientViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.adapter_ingredient,
                    parent,
                    false
                )
            )
        else
            StepViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.adapter_step,
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
        holder: AbstractDraggableItemViewHolder,
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

    fun add(item: IngredientForRecipe) {
        val i = provider.list.indexOfLast { item.step == it.first.item }
        if (i >= 0) {
            item.sortOrder =
                if (provider.getChildItem(i, provider.getChildCount(i) - 1) is StepItem)
                    provider.getChildCount(i) - 1
                else
                    provider.getChildCount(i)

            provider.addChildItem(
                i,
                item.sortOrder ,
                IngredientItem(item.sortOrder.toLong(), item)
            )
            expandableItemManager?.notifyChildItemInserted(i, item.sortOrder)
        }
    }

    fun add(item: Step) {
        item.order = provider.groupCount
        provider.addGroupItem(StepItem(provider.groupCount.toLong(), item))
        expandableItemManager?.notifyGroupItemInserted(item.order)

        val i = provider.list.indexOfLast { item == it.first.item }
        if (i >= 0) {
            provider.addChildItem(i, StepItem(item.order.toLong(), item))
            expandableItemManager?.notifyChildItemInserted(i, 0)
        }
    }

    fun update(item: IngredientForRecipe) {
        val i = provider.list.indexOfLast { item.step == it.first.item }
        if (i >= 0) {
            item.sortOrder = provider.getChildCount(i)
            expandableItemManager?.notifyChildItemChanged(i,
                provider.list[i].second.indexOfFirst { it is IngredientItem && it.item == item }
            )
        }
    }

    fun update(item: Step) {
        item.order = provider.groupCount
        expandableItemManager?.notifyGroupItemChanged(provider.list.indexOfLast { item == it.first.item })
    }

    fun remove(item: IngredientForRecipe) {
        val i = provider.list.indexOfLast { item.step == it.first.item }
        if (i >= 0) {
            val j = provider.list[i].second.indexOfFirst { it is IngredientItem && it.item == item }
            item.sortOrder = provider.getChildCount(i)
            provider.removeChildItem(i, j)
            expandableItemManager?.notifyChildItemRemoved(i, j)
        }
    }

    fun remove(item: Step) {
        val i = provider.list.indexOfLast { item == it.first.item }
        if (i >= 0) {
            provider.list[i].second.forEachIndexed { j, v ->
                if (v is IngredientItem) {
                    v.item.step = null
                    v.item.refStep = null
                    expandableItemManager?.notifyChildItemRemoved(i, j)
                    add(v.item)
                }
            }
            provider.removeGroupItem(i)
            expandableItemManager?.notifyGroupItemRemoved(i)
        }
    }

    //endregion

    //region move
    override fun onMoveGroupItem(fromGroupPosition: Int, toGroupPosition: Int) {
        provider.getGroupItem(fromGroupPosition).item?.order = toGroupPosition
        provider.getGroupItem(toGroupPosition).item?.order = fromGroupPosition

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
            from.item.step = provider.getGroupItem(toGroupPosition).item
            from.item.sortOrder = toGroupPosition
        }

        if (to is IngredientItem) {
            //to.item.step = provider.getGroupItem(fromGroupPosition).item
            to.item.sortOrder = fromGroupPosition
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
        /*if (provider.getGroupItem(groupPosition).isPinned()) {
            // return false to raise View.OnClickListener#onClick() event
            return false;
        }
    */
        // check is enabled
        if (!(holder.itemView.isEnabled && holder.itemView.isClickable)) {
            return false
        }

        val containerView = holder.itemView
        val dragHandleView = holder.itemView.stepImage

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
        holder: AbstractDraggableItemViewHolder,
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
        holder: AbstractDraggableItemViewHolder,
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

}
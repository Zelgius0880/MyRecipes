package zelgius.com.myrecipes.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.amulyakhare.textdrawable.TextDrawable
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemState
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemViewHolder
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.adapter_ingredient.view.*
import kotlinx.android.synthetic.main.adapter_step.view.*
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.IngredientForRecipe
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.entities.Step
import zelgius.com.myrecipes.utils.UiUtils
import zelgius.com.myrecipes.utils.ViewUtils
import zelgius.com.myrecipes.utils.dpToPx
import zelgius.com.myrecipes.utils.round
import java.text.DecimalFormat


class RecipeExpandableAdapter(val context: Context, viewModel: RecipeViewModel) :
    AbstractExpandableItemAdapter<RecipeExpandableAdapter.StepSectionViewHolder, AbstractExpandableItemViewHolder>() {

    private lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: LinearLayoutManager
    lateinit var provider: AdapterDataProvider<StepItem, DataItem>
    var expandableItemManager: RecyclerViewExpandableItemManager? = null
        set(value) {
            field = value
            value?.expandGroup(0)
        }

    var recipe = viewModel.currentRecipe
        set(value) {
            field = value
            createProvider()
        }

    init {
        createProvider()
        setHasStableIds(true)
        expandableItemManager?.expandGroup(0)

    }

    private fun createProvider() {
        val list = mutableListOf<Pair<StepItem, MutableList<DataItem>>>()

        list.add(StepItem(0, null) to recipe.ingredients
            .sortedWith(Comparator { o1, o2 ->
                when {
                    o1.step == null && o2.step != null -> 1
                    o2.step == null && o1.step != null -> -1
                    else -> o1.sortOrder - o2.sortOrder
                }
            })
            .mapIndexed { i, item ->
                IngredientItem(
                    item.id ?: i.toLong(),
                    item
                ) as DataItem
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

    class IngredientItem(adapterId: Long, val item: IngredientForRecipe) : DataItem(adapterId) {
        override val isSectionHeader: Boolean
            get() = false
    }

    //region ViewHolder
    inner class IngredientViewHolder(override val containerView: View) :
        AbstractExpandableItemViewHolder(containerView),
        ExpandableItemViewHolder, LayoutContainer {

        private val mExpandState = ExpandableItemState()
        override fun getExpandState(): ExpandableItemState = mExpandState

        override fun getExpandStateFlags(): Int = mExpandState.flags

        override fun setExpandStateFlags(flags: Int) {
            mExpandState.flags = flags
        }

        fun bind(parentPosition: Int, childPosition: Int) {
            val item = (provider.getChildItem(parentPosition, childPosition) as IngredientItem).item

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
        }
    }

    inner class StepSectionViewHolder(override val containerView: View) :
        AbstractExpandableItemViewHolder(containerView),
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

            itemView.step.text = item?.text ?: context.getString(R.string.all_ingredients)

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

            with(if (expandState.isExpanded || item == null) View.GONE else View.VISIBLE) {
                itemView.stepImage.visibility = this
            }
            itemView.expand.visibility = View.VISIBLE

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
        AbstractExpandableItemViewHolder(containerView),
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
    ): AbstractExpandableItemViewHolder =
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
        holder: AbstractExpandableItemViewHolder,
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

    //region Expand

    override fun onCheckCanExpandOrCollapseGroup(
        holder: StepSectionViewHolder,
        groupPosition: Int,
        x: Int,
        y: Int,
        expand: Boolean
    ): Boolean {
        if (provider.getGroupItem(groupPosition).isPinned) {
            // return false to raise View.OnClickListener#onClick() event
            return false
        }


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

}

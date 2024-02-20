package zelgius.com.myrecipes.adapters

import TextDrawable
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemState
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemViewHolder
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.databinding.AdapterIngredientBinding
import zelgius.com.myrecipes.databinding.AdapterStepBinding
import zelgius.com.myrecipes.data.entities.IngredientForRecipe
import zelgius.com.myrecipes.data.entities.StepEntity
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.asEntity
import zelgius.com.myrecipes.data.text
import zelgius.com.myrecipes.utils.UiUtils
import zelgius.com.myrecipes.utils.ViewUtils
import zelgius.com.myrecipes.utils.context


class RecipeExpandableAdapter(val context: Context, viewModel: RecipeViewModel) :
    AbstractExpandableItemAdapter<RecipeExpandableAdapter.StepSectionViewHolder, AbstractExpandableItemViewHolder>() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
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

    private val alpha = 0.6f

    private fun createProvider() {
        val list = mutableListOf<Pair<StepItem, MutableList<DataItem>>>()

        list.add(StepItem(0, null) to recipe.ingredients
            .sortedWith { o1, o2 ->
                when {
                    o1.step == null && o2.step != null -> -1
                    o2.step == null && o1.step != null -> 1
                    else -> o1.sortOrder - o2.sortOrder
                }
            }
            .mapIndexed { i, item ->
                IngredientItem(
                    item.id ?: i.toLong(),
                    item
                )
            } // Flagged as 'No cast needed' but actually needed
            .toMutableList())

        recipe.steps.forEachIndexed { i, s ->
            list.add(StepItem(i + 1L, s.asEntity()) to recipe.ingredients
                .filter { it.step == s }
                .map { IngredientItem(it.id ?: 0L, it) }
                .toMutableList<DataItem>()
                .apply {
                    add(StepItem(i + 1L, s.asEntity()))
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

    class StepItem(adapterId: Long, val item: StepEntity?) : DataItem(adapterId) {
        override val isSectionHeader: Boolean
            get() = false
    }

    class IngredientItem(adapterId: Long, val item: Ingredient) : DataItem(adapterId) {
        override val isSectionHeader: Boolean
            get() = false
    }

    //region ViewHolder
    inner class IngredientViewHolder(val binding: AdapterIngredientBinding) :
        AbstractExpandableItemViewHolder(binding.root),
        ExpandableItemViewHolder {

        private val mExpandState = ExpandableItemState()
        override fun getExpandState(): ExpandableItemState = mExpandState

        override fun getExpandStateFlags(): Int = mExpandState.flags

        override fun setExpandStateFlags(flags: Int) {
            mExpandState.flags = flags
        }

        fun bind(parentPosition: Int, childPosition: Int) {
            val item = (provider.getChildItem(parentPosition, childPosition) as IngredientItem).item

            binding.ingredientName.text = item.text(context)

            UiUtils.getIngredientDrawable(binding.image, item)

            if (item.optional == true || item.step?.optional == true) {
                binding.image.alpha = alpha
                binding.ingredientName.alpha = alpha
            }
        }
    }

    inner class StepSectionViewHolder(val binding: AdapterStepBinding) :
        AbstractExpandableItemViewHolder(binding.root),
        ExpandableItemViewHolder {

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
            binding.root.foreground = null
            binding.expand.visibility = View.VISIBLE

            binding.step.text = item?.text ?: context.getString(R.string.all_ingredients)

            if (item != null) {
                binding.stepImage.setImageDrawable(
                    TextDrawable(binding.context.resources, "${item.order}")
                )
            }

            with(if (expandState.isExpanded || item == null) View.GONE else View.VISIBLE) {
                binding.stepImage.visibility = this
                if (item != null) binding.step.visibility = this
                else binding.step.visibility = View.VISIBLE
            }
            binding.expand.visibility = View.VISIBLE

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
        AbstractExpandableItemViewHolder(binding.root),
        ExpandableItemViewHolder {
        private val mExpandState = ExpandableItemState()
        override fun getExpandState(): ExpandableItemState = mExpandState

        override fun getExpandStateFlags(): Int = mExpandState.flags

        override fun setExpandStateFlags(flags: Int) {
            mExpandState.flags = flags
        }

        fun bind(groupPosition: Int, childPosition: Int) {

            val item = (provider.getChildItem(groupPosition, childPosition) as StepItem).item

            if (item == null) {
                binding.step.setText(R.string.ingredients)
                return
            }

            binding.step.text = item.text


            binding.stepImage.setImageDrawable(
                TextDrawable(binding.context.resources, "${item.order}")
            )

            if (item.optional) {
                binding.stepImage.alpha = alpha
                binding.step.alpha = alpha
            }
        }
    }

    override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): StepSectionViewHolder =
        StepSectionViewHolder(
            AdapterStepBinding.inflate(
                LayoutInflater.from(parent.context),
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

}

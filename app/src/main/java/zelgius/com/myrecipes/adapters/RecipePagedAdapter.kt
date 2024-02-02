package zelgius.com.myrecipes.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.databinding.AdapterFragmentRecipeBinding
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.utils.context
import zelgius.com.myrecipes.utils.dpToPx


class RecipePagedAdapter(private val selectionChangeListener: (Boolean) -> Unit) :
    PagedListAdapter<RecipeEntity, RecipePagedAdapter.ViewHolder>(DIFF_CALLBACK),
    SwipeableItemAdapter<RecipePagedAdapter.ViewHolder> {

    var deleteListener: ((RecipeEntity) -> Unit)? = null
    var editListener: ((RecipeEntity, FragmentNavigator.Extras) -> Unit)? = null
    var clickListener: ((RecipeEntity, FragmentNavigator.Extras?) -> Unit)? = null

    var isSelectionEnabled = false
    private val selection = mutableListOf<RecipeEntity>()

    init {
        setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = getItem(position)


        if (recipe != null) {
            val binding = holder.binding

            binding.root.setOnLongClickListener {
                isSelectionEnabled = true
                selection.add(recipe)
                binding.root.isChecked = true
                selectionChangeListener(true)
                clickListener?.invoke(recipe, null)
                true
            }

            binding.name.text = recipe.name
            binding.category.text = when (recipe.type) {
                RecipeEntity.Type.MEAL -> binding.context.getString(R.string.meal)
                RecipeEntity.Type.DESSERT -> binding.context.getString(R.string.dessert)
                RecipeEntity.Type.OTHER -> binding.context.getString(R.string.other)
            }

            if (!recipe.imageURL.isNullOrEmpty() && recipe.imageURL != "null") {
                Picasso.get().apply {
                    /*setIndicatorsEnabled(true)
                    isLoggingEnabled = true*/
                }
                    .load(recipe.imageURL!!.toUri())
                    .resize(2048, 2048)
                    .centerCrop()
                    //.placeholder(R.drawable.ic_dish)
                    .into(binding.imageView, object : Callback {
                        override fun onSuccess() {
                        }

                        override fun onError(e: Exception?) {
                            e?.printStackTrace()
                        }

                    })

                binding.imageView.setPadding(0, 0, 0, 0)
            } else {
                binding.imageView.setImageResource(R.drawable.ic_dish)

                binding.context.let {
                    binding.imageView.setPadding(
                        it.dpToPx(8f).toInt(),
                        it.dpToPx(8f).toInt(),
                        it.dpToPx(8f).toInt(),
                        it.dpToPx(8f).toInt()
                    )

                }

            }

            binding.delete.setOnClickListener {
                if (holder.swiped)
                    deleteListener?.invoke(recipe)
                else binding.root.performClick()
            }

            binding.edit.setOnClickListener {
                if (holder.swiped) {
                    binding.materialCardView.transitionName = "cardView${recipe.id}"
                    binding.imageView.transitionName = "imageView${recipe.id}"
                    binding.name.transitionName = "name${recipe.id}"
                    binding.category.transitionName = "category${recipe.id}"

                    editListener?.invoke(
                        recipe, FragmentNavigatorExtras(
                            binding.materialCardView to "cardView${recipe.id}",
                            binding.imageView to "imageView${recipe.id}",
                            binding.name to "name${recipe.id}",
                            binding.category to "category${recipe.id}"
                        )
                    )
                } else binding.root.performClick()


                /*binding.imageView.transitionName = ""
                binding.name.transitionName = ""
                binding.category.transitionName = ""*/
            }

            binding.root.setOnClickListener {
                val extra = if (!isSelectionEnabled) {
                    binding.materialCardView.transitionName = "cardView${recipe.id}"
                    binding.imageView.transitionName = "imageView${recipe.id}"
                    binding.name.transitionName = "name${recipe.id}"
                    binding.category.transitionName = "category${recipe.id}"

                    FragmentNavigatorExtras(
                        binding.materialCardView to "cardView${recipe.id}",
                        binding.imageView to "imageView${recipe.id}",
                        binding.name to "name${recipe.id}",
                        binding.category to "category${recipe.id}"
                    )
                } else {
                    binding.root.isChecked = if (selection.contains(recipe)) {
                        selection.remove(recipe)
                        false
                    } else {
                        selection.add(recipe)
                        true
                    }

                    if (selection.isEmpty())
                        selectionChangeListener(false)

                    null
                }

                clickListener?.invoke(
                    recipe, extra
                )
                /*binding.imageView.transitionName = ""
                binding.name.transitionName = ""
                binding.category.transitionName = ""*/
            }

            binding.root.isChecked = selection.contains(recipe)

            // set swiping properties
            holder.isProportionalSwipeAmountModeEnabled = false
            holder.maxLeftSwipeAmount = -binding.context.dpToPx(160f)
            holder.maxRightSwipeAmount = 0f
            holder.swipeItemHorizontalSlideAmount =
                if (recipe.isPinned) -binding.context.dpToPx(160f) else 0f
        }
    }

    fun clearSelection() {
        selection.clear()
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            AdapterFragmentRecipeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onGetSwipeReactionType(holder: ViewHolder, position: Int, x: Int, y: Int): Int {
        return SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H
    }


    override fun onSwipeItemStarted(holder: ViewHolder, position: Int) {
        notifyItemChanged(position)
    }

    override fun onSetSwipeBackground(holder: ViewHolder, position: Int, type: Int) {
        if (type == SwipeableItemConstants.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND) {
            holder.binding.behindView.visibility = View.GONE
        } else {
            holder.binding.behindView.visibility = View.VISIBLE
        }
    }

    override fun onSwipeItem(holder: ViewHolder, position: Int, result: Int): SwipeResultAction? {
        Log.d(TAG, "onSwipeItem(position = $position, result = $result)")

        return when (result) {
            // swipe left --- pin
            SwipeableItemConstants.RESULT_SWIPED_LEFT -> {
                holder.swiped = true
                SwipeLeftResultAction(position)
            }
            // other --- do nothing
            SwipeableItemConstants.RESULT_SWIPED_RIGHT, SwipeableItemConstants.RESULT_CANCELED -> if (position != RecyclerView.NO_POSITION) {
                holder.swiped = false
                UnpinResultAction(position)
            } else {
                null
            }
            else -> if (position != RecyclerView.NO_POSITION) {
                holder.swiped = false
                UnpinResultAction(position)
            } else {
                null
            }
        }
    }


    companion object {

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RecipeEntity>() {
            override fun areItemsTheSame(oldItem: RecipeEntity, newItem: RecipeEntity): Boolean =
                oldItem.id == newItem.id


            override fun areContentsTheSame(oldItem: RecipeEntity, newItem: RecipeEntity): Boolean =
                oldItem == newItem

        }
    }

    inner class ViewHolder(val binding: AdapterFragmentRecipeBinding, var swiped: Boolean = false) :
        AbstractSwipeableItemViewHolder(binding.root) {
        override fun getSwipeableContainerView(): View = binding.contentView
    }

    inner class SwipeLeftResultAction internal constructor(
        private val position: Int
    ) :
        SwipeResultActionMoveToSwipedDirection() {
        private var mSetPinned: Boolean = false

        override fun onPerformAction() {
            super.onPerformAction()

            val item = getItem(position)

            if (item?.isPinned == false) {
                item.isPinned = true
                notifyItemChanged(position)
                mSetPinned = true
            }
        }

    }

    inner class UnpinResultAction internal constructor(
        private val position: Int
    ) : SwipeResultActionDefault() {

        override fun onPerformAction() {
            super.onPerformAction()

            val item = getItem(position)
            if (item?.isPinned == true) {
                item.isPinned = false
                notifyItemChanged(position)
            }
        }
    }

}
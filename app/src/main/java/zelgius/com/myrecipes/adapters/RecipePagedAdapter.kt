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
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.adapter_fragment_recipe.view.*
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.utils.dpToPx


class RecipePagedAdapter :
    PagedListAdapter<Recipe, RecipePagedAdapter.ViewHolder>(DIFF_CALLBACK),
    SwipeableItemAdapter<RecipePagedAdapter.ViewHolder> {

    var deleteListener: ((Recipe) -> Unit)? = null
    var editListener: ((Recipe, FragmentNavigator.Extras) -> Unit)? = null
    var clickListener: ((Recipe, FragmentNavigator.Extras) -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = getItem(position)


        if (recipe != null) {
            val itemView = holder.itemView

            itemView.name.text = recipe.name
            itemView.category.text = when (recipe.type) {
                Recipe.Type.MEAL -> itemView.context.getString(R.string.meal)
                Recipe.Type.DESSERT -> itemView.context.getString(R.string.dessert)
                Recipe.Type.OTHER -> itemView.context.getString(R.string.other)
            }

            if (!recipe.imageURL.isNullOrEmpty() && recipe.imageURL != "null") {
                Picasso.get().apply {
                    setIndicatorsEnabled(true)
                    isLoggingEnabled = true
                }
                    .load(recipe.imageURL!!.toUri())
                    .resize(2048, 2048)
                    .centerCrop()
                    //.placeholder(R.drawable.ic_dish)
                    .into(itemView.imageView, object : Callback {
                        override fun onSuccess() {
                        }

                        override fun onError(e: Exception?) {
                            e?.printStackTrace()
                        }

                    })

                itemView.imageView.setPadding(0, 0, 0, 0)
            } else {
                itemView.imageView.setImageResource(R.drawable.ic_dish)

                itemView.context.let {
                    itemView.imageView.setPadding(
                        it.dpToPx(8f).toInt(),
                        it.dpToPx(8f).toInt(),
                        it.dpToPx(8f).toInt(),
                        it.dpToPx(8f).toInt()
                    )

                }

            }

            itemView.delete.setOnClickListener {
                if (holder.swiped)
                    deleteListener?.invoke(recipe)
                else itemView.performClick()
            }

            itemView.edit.setOnClickListener {
                if (holder.swiped) {
                    itemView.materialCardView.transitionName = "cardView${recipe.id}"
                    itemView.imageView.transitionName = "imageView${recipe.id}"
                    itemView.name.transitionName = "name${recipe.id}"
                    itemView.category.transitionName = "category${recipe.id}"

                    editListener?.invoke(
                        recipe, FragmentNavigatorExtras(
                            itemView.materialCardView to "cardView${recipe.id}",
                            itemView.imageView to "imageView${recipe.id}",
                            itemView.name to "name${recipe.id}",
                            itemView.category to "category${recipe.id}"
                        )
                    )
                } else itemView.performClick()


                /*itemView.imageView.transitionName = ""
                itemView.name.transitionName = ""
                itemView.category.transitionName = ""*/
            }

            itemView.setOnClickListener {
                itemView.materialCardView.transitionName = "cardView${recipe.id}"
                itemView.imageView.transitionName = "imageView${recipe.id}"
                itemView.name.transitionName = "name${recipe.id}"
                itemView.category.transitionName = "category${recipe.id}"

                clickListener?.invoke(
                    recipe, FragmentNavigatorExtras(
                        itemView.materialCardView to "cardView${recipe.id}",
                        itemView.imageView to "imageView${recipe.id}",
                        itemView.name to "name${recipe.id}",
                        itemView.category to "category${recipe.id}"
                    )
                )

                /*itemView.imageView.transitionName = ""
                itemView.name.transitionName = ""
                itemView.category.transitionName = ""*/
            }


            // set swiping properties
            holder.isProportionalSwipeAmountModeEnabled = false
            holder.maxLeftSwipeAmount = -itemView.context.dpToPx(160f)
            holder.maxRightSwipeAmount = 0f
            holder.swipeItemHorizontalSlideAmount =
                if (recipe.isPinned) -itemView.context.dpToPx(160f) else 0f
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.adapter_fragment_recipe,
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
            holder.itemView.behindView.visibility = View.GONE
        } else {
            holder.itemView.behindView.visibility = View.VISIBLE
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

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Recipe>() {
            override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean =
                oldItem.id == newItem.id


            override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean =
                oldItem == newItem

        }
    }

    inner class ViewHolder(override val containerView: View, var swiped: Boolean = false) :
        AbstractSwipeableItemViewHolder(containerView), LayoutContainer {
        override fun getSwipeableContainerView(): View = itemView.contentView
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
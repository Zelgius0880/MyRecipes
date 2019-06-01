package zelgius.com.myrecipes.adapters

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import android.util.TypedValue
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
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

abstract class AbstractViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {
    abstract fun bind()

    var backgroundColor = itemView.context.getCompatColor(R.color.md_white_1000)
    open fun selectedBackground(changeView: Boolean) {
        val v = itemView

        backgroundColor = v.context.getCompatColor(R.color.secondaryColor)
        if (v is CardView && changeView) {
            v.setCardBackgroundColor(backgroundColor)
            v.cardElevation = v.context.dpToPx(2f)
        }
    }

    open fun notSelectedBackground(changeView: Boolean) {
        val v = itemView
        backgroundColor = v.context.getCompatColor(R.color.md_white_1000)
        if (v is CardView && changeView) {
            v.setCardBackgroundColor(backgroundColor)
            v.cardElevation = v.context.dpToPx(2f)
        }
    }
}


class RecipeEditAdapter(val context: Context, val viewModel: RecipeViewModel) :
    RecyclerView.Adapter<AbstractViewHolder>() {

    private lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: LinearLayoutManager

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
        this.layoutManager = recyclerView.layoutManager as LinearLayoutManager
    }

    var recipe: Recipe = viewModel.currentRecipe.apply {
        ingredients.add(IngredientForRecipe(null, 2.0, Ingredient.Unit.UNIT, "Eggs", "drawable://egg", 1, null, null))
        ingredients.add(
            IngredientForRecipe(
                null,
                500.0,
                Ingredient.Unit.GRAMME,
                "Flour",
                "drawable://flour",
                2,
                null,
                null
            )
        )
        ingredients.add(
            IngredientForRecipe(
                null,
                200.0,
                Ingredient.Unit.MILLILITER,
                "Water",
                "drawable://water",
                3,
                null,
                null
            )
        )
        ingredients.add(
            IngredientForRecipe(
                null,
                2.33,
                Ingredient.Unit.CUP,
                "Butter",
                "drawable://butter",
                4,
                null,
                null
            )
        )

        steps.add(Step(null, "Step 1", Int.MAX_VALUE, null).apply { new = true; order = 1 })
        steps.add(Step(null, "Step 2", Int.MAX_VALUE, null).apply { new = true; order = 2 })
        steps.add(Step(null, "Step 3", Int.MAX_VALUE, null).apply { new = true; order = 3 })
    }
    private val items = mutableListOf<Any>()

    var editStepListener: ((Step) -> Unit)? = null
    var editIngredientListener: ((IngredientForRecipe) -> Unit)? = null

    init {
        // adding at first the ingredients not linked to a step
        items.clear()
        items.addAll(recipe.ingredients.filter { it.refStep == null && it.step == null }.sortedBy { it.sortOrder })

        recipe.steps.forEach { s ->
            items.addAll(recipe.ingredients.filter { it.refStep == s.id && it.id != null || it.step == s }.sortedBy { it.sortOrder })
            items.add(s)
        }
    }

    val typeStringArray: Array<String> by lazy {
        context.resources.getStringArray(R.array.category_array)
    }

    override fun getItemViewType(position: Int): Int =
        when {
            position == 0 -> R.layout.layout_header
            items[position - 1] is IngredientForRecipe -> R.layout.adapter_ingredient
            items[position - 1] is Step -> R.layout.adapter_step
            else -> throw IllegalStateException("Don't know what to do")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder =
        when (viewType) {
            R.layout.layout_header -> HeaderViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    viewType,
                    parent,
                    false
                )
            )
            R.layout.adapter_ingredient -> IngredientViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    viewType,
                    parent,
                    false
                )
            )
            R.layout.adapter_step -> StepViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    viewType,
                    parent,
                    false
                )
            )
            else -> error("Unknown layout")
        }

    override fun getItemCount(): Int = 1 + items.size

    override fun onBindViewHolder(holder: AbstractViewHolder, position: Int) {
        when (position) {
            0 -> holder.bind()
            else -> holder.bind()
        }
    }


    fun addIngredient(item: IngredientForRecipe) {
        items.indexOfLast {
            if (it is IngredientForRecipe)
                it.refStep == item.refStep && it.step == null || it.step == item.step else false
        }.let {
            if (it < 0) {
                if (items.isEmpty())
                    items.add(item)
                else
                    items.add(0, item)

                notifyItemInserted(1)
            } else {
                if (it == items.size - 1) items.add(item)
                else items.add(it + 1, item)
                notifyItemChanged(it + 1) // + header position
                notifyItemInserted(it + 1 + 1) // + header position + 1
            }
        }
    }

    fun addStep(item: Step) {
        items.add(item)
        notifyItemInserted(items.size)
    }


    /**
     * Get the position of the first and the last viewHolder rendered in the layoutManager
     * @return IntArray [0] -> start position; [1] -> stop position
     */
    fun findFirstAndLAstElements(): IntArray {
        var vStart: RecyclerView.ViewHolder? = null
        var vStop: RecyclerView.ViewHolder? = null

        var start = 0
        var stop = itemCount - 1

        for (i in 0 until itemCount) {
            if (vStart == null && vStop == null) break

            vStart = recyclerView.findViewHolderForAdapterPosition(i)
            vStop = recyclerView.findViewHolderForAdapterPosition(itemCount - 1 - i)

            if (vStart != null) start = i
            if (vStop != null) stop = itemCount - 1 - i
        }

        return intArrayOf(start, stop)
    }

    fun setSelectedItemsFromStep(step: Step?) {

        val (start, stop) = findFirstAndLAstElements()
        items.forEachIndexed { index, item ->

            (recyclerView.findViewHolderForAdapterPosition(index + 1) as AbstractViewHolder).apply {
                if (item is Step && item == step || item is IngredientForRecipe && item.step == step)
                    selectedBackground(index in start..stop)
                else
                    notSelectedBackground(index in start..stop)
            }
        }
    }

    fun setNotSelectedItemsFromStep(step: Step) {
        val (start, stop) = findFirstAndLAstElements()

        items.forEachIndexed { index, item ->
            if (item is Step && item == step || item is IngredientForRecipe && item.step == step)
                (recyclerView.findViewHolderForAdapterPosition(index + 1) as AbstractViewHolder).apply {
                    notSelectedBackground(index in start..stop)
                }
        }
    }

    fun notifyIngredientChanged(item: IngredientForRecipe) {
        val index = items.indexOf(item) + 1

        if (index > 0)
            notifyItemChanged(index)
    }

    fun notifyStepChanged(item: Step) {
        val index = items.indexOf(item) + 1

        if (index > 0)
            notifyItemChanged(index)
    }

    /* fun startMovingStep(position: Int, step: Step) {
         var firstPositionRange = -1

         var i = position - 1
         while (i >= 0 && items[i] is IngredientForRecipe && (items[i] as IngredientForRecipe).step == step) {

         }
     }

     fun stopMovingStep(position: Int, step: Step) {

     }*/

    inner class HeaderViewHolder(view: View) : AbstractViewHolder(view) {
        override fun bind() {

            val category = when (recipe.type) {
                Recipe.Type.MEAL -> itemView.context.getString(R.string.meal)
                Recipe.Type.DESSERT -> itemView.context.getString(R.string.dessert)
                Recipe.Type.OTHER -> itemView.context.getString(R.string.other)
            }

            itemView.editName.editText?.setText(recipe.name)
            itemView.editCategory.setSelection(typeStringArray.indexOf(category))

            itemView.editImage.setOnClickListener { _ ->
                ImageDialogFragment().let {
                    if (context is AppCompatActivity)
                        it.show(context.supportFragmentManager, "image_dialog")
                }
            }

            if (context is LifecycleOwner)
                viewModel.selectedImageUrl.observe(context, Observer {
                    Picasso.get()
                        .load(it)
                        .resize(2048, 2048)
                        .centerCrop()
                        .into(itemView.imageView)
                })


            val spinnerArrayAdapter = ArrayAdapter<String>(
                context, R.layout.adapter_text_category,
                typeStringArray
            ) //selected item will look like a spinner set from XML
            spinnerArrayAdapter.setDropDownViewResource(
                android.R.layout
                    .simple_spinner_dropdown_item
            )
            itemView.editCategory.adapter = spinnerArrayAdapter
            itemView.editCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                }
            }
        }
    }

    inner class IngredientViewHolder(view: View) : AbstractViewHolder(view) {

        override fun bind() {

            if (itemView is CardView) {
                itemView.setCardBackgroundColor(backgroundColor)
                itemView.cardElevation = itemView.context.dpToPx(2f)
            }

            val item = items[adapterPosition - 1] as IngredientForRecipe
            val abrv = when (item.unit) {
                Ingredient.Unit.MILLILITER -> itemView.context.getString(R.string.milliliter_abrv)
                Ingredient.Unit.LITER -> itemView.context.getString(R.string.liter_abrv)
                Ingredient.Unit.UNIT -> itemView.context.getString(R.string.unit_abrv)
                Ingredient.Unit.TEASPOON -> itemView.context.getString(R.string.teaspoon_abrv)
                Ingredient.Unit.TABLESPOON -> itemView.context.getString(R.string.tablespoon)
                Ingredient.Unit.GRAMME -> itemView.context.getString(R.string.gramme_abrv)
                Ingredient.Unit.KILOGRAMME -> itemView.context.getString(R.string.kilogramme_abrv)
                Ingredient.Unit.CUP -> itemView.context.getString(R.string.cup_abrv)
            }

            itemView.unlinkIngredient.visibility =
                if (item.step != null || item.refStep != null) View.VISIBLE else View.GONE

            if (item.unit != Ingredient.Unit.CUP) {
                itemView.ingredientName.text =
                    String.format("%s %s %s", DecimalFormat("#0.##").format(item.quantity), abrv, item.name)
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
                items.removeAt(adapterPosition - 1)
                notifyItemRemoved(adapterPosition)
            }

            itemView.setOnClickListener { editIngredientListener?.invoke(item) }
        }
    }

    inner class StepViewHolder(view: View) : AbstractViewHolder(view) {
        override fun bind() {
            if (itemView is CardView) {
                itemView.setCardBackgroundColor(backgroundColor)
                itemView.cardElevation = itemView.context.dpToPx(2f)
            }

            val item = items[adapterPosition - 1] as Step
            itemView.step.text = item.text

            itemView.deleteStep.setOnClickListener {
                recipe.steps.remove(item)
                items.removeAt(adapterPosition - 1)
                notifyItemRemoved(adapterPosition)
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
        }
    }

    inner class DividerDecoration(
        context: Context,
        color: Int = Color.argb((255 * 0.2).toInt(), 0, 0, 0),
        heightDp: Float = 1f
    ) : RecyclerView.ItemDecoration() {

        private val mPaint: Paint = Paint()
        private val mHeightDp: Int

        init {
            mPaint.style = Paint.Style.FILL
            mPaint.color = color
            mHeightDp =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightDp, context.resources.displayMetrics)
                    .toInt()
        }

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)

            if (position >= 0) {
                val viewType = parent.adapter!!.getItemViewType(position)

                if (viewType == R.layout.layout_header) {
                    //outRect.set(0, 0, 0, mHeightDp)
                } else {
                    val previousViewType = if (position > 0) parent.adapter!!.getItemViewType(position - 1) else 0

                    if (viewType == R.layout.adapter_step && previousViewType == R.layout.adapter_ingredient) {
                        val s = items[position - 1] as Step
                        val i = items[position - 1 - 1] as IngredientForRecipe

                        if (i.refStep == s.id && s.id != null || s == i.step) {
                            outRect.setEmpty()
                        } else {
                            outRect.set(0, mHeightDp, 0, 0)
                        }
                    } else if (viewType == R.layout.adapter_ingredient && previousViewType == R.layout.adapter_ingredient) {
                        val i1 = items[position - 1] as IngredientForRecipe
                        val i2 = items[position - 1 - 1] as IngredientForRecipe

                        if (i2.refStep == i1.refStep && i1.refStep != null && i2.refStep != null || i1.step == i2.step)
                            outRect.setEmpty()
                        else
                            outRect.set(0, mHeightDp, 0, 0)

                    } else {
                        outRect.set(0, mHeightDp, 0, 0)
                    }
                }
            }
        }

        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            for (i in 0 until parent.childCount) {
                val view = parent.getChildAt(i)
                val position = parent.getChildAdapterPosition(view)
                if (position >= 0) {

                    val viewType = parent.adapter!!.getItemViewType(position)
                    val previousViewType = if (position > 0) parent.adapter!!.getItemViewType(position - 1) else 0

                    if (viewType != previousViewType && viewType != R.layout.layout_header) {
                        c.drawRect(
                            view.left.toFloat(),
                            view.top.toFloat() + mHeightDp,
                            view.right.toFloat(),
                            view.bottom.toFloat(),
                            mPaint
                        )
                    }
                }
            }
        }
    }

    inner class TouchHelper : ItemTouchHelper.Callback() {
        val TAG = RecipeEditAdapter.TouchHelper::class.java.name
        var orderChanged = false
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) =
            when (viewHolder) {
                is StepViewHolder, is IngredientViewHolder -> makeMovementFlags(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                    ItemTouchHelper.START or ItemTouchHelper.END
                )
                else -> makeMovementFlags(0, 0)
            }

        override fun chooseDropTarget(
            selected: RecyclerView.ViewHolder,
            dropTargets: MutableList<RecyclerView.ViewHolder>,
            curX: Int,
            curY: Int
        ): RecyclerView.ViewHolder {
            return dropTargets.find { it is StepViewHolder } ?: dropTargets.first()
        }

        override fun canDropOver(
            recyclerView: RecyclerView,
            current: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            if (current is HeaderViewHolder || target is HeaderViewHolder       // never can move the header
                || current is StepViewHolder && target is IngredientViewHolder  // a step never can be moved with an ingredient
            //|| current is IngredientViewHolder && target is StepViewHolder && target.adapterPosition == itemCount - 1// An ingredient never can be moved at the end of the list if there is a step
            ) return false
            if (current is StepViewHolder && target is StepViewHolder) return true

            val direction =
                target.adapterPosition - current.adapterPosition // drag direction:  > 0 if up to bellow, < 0 if bellow to up

            val step = when (target) {
                is IngredientViewHolder -> (items[target.adapterPosition - 1] as IngredientForRecipe).step
                is StepViewHolder -> items[target.adapterPosition - 1] as Step
                else -> null
            }
            val i = items[current.adapterPosition - 1] as IngredientForRecipe
            val oldStep = i.step


            if (oldStep != null && target is StepViewHolder && current is IngredientViewHolder)
                if (direction > 0 && step == oldStep) {

                    // already linked -> return false or else, the in ingredient can be
                    // bellow the step and still linked to it
                    return false
                } /*else if (direction < 0 && step != oldStep)
                // already linked -> return false or else, the in ingredient can be
                // bellow the step and still linked to it
                    return false*/

            i.step = step
            i.refStep = step?.id

            setSelectedItemsFromStep(step)

            orderChanged = true

            return true
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            if (target is HeaderViewHolder) return false
            if (target.adapterPosition < 0 || viewHolder.adapterPosition < 0) return false

            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition

            if (viewHolder is StepViewHolder) {
                /*var i = position - 1
                while (i >= 0 && items[i] is IngredientForRecipe && (items[i] as IngredientForRecipe).step == step) {

                }*/

                notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)

                val step = items[fromPosition - 1] as Step
                for (i in fromPosition - 1 downTo 1) { // the first element is the Header
                    val item = items[i - 1]
                    if (item !is IngredientForRecipe || item.step != step) break

                    val posDiff = fromPosition - i

                    Log.d(TAG, "from: $fromPosition, to: $toPosition, posDifd: $posDiff")
                    items.add(toPosition - 1 - posDiff, items.removeAt(fromPosition - 1 - posDiff))
                    notifyItemMoved(fromPosition - posDiff, toPosition - posDiff)

                }
            }

            items.add(toPosition - 1, items.removeAt(fromPosition - 1))
            notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)

            return true
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)

            when (actionState) {
                ItemTouchHelper.ACTION_STATE_IDLE -> { // Drag stop

                    if (viewHolder is StepViewHolder) {da
                        //stopMovingStep(viewHolder.adapterPosition - 1, items[viewHolder.adapterPosition - 1] as Step)
                        resetBackground()

                    } else if (orderChanged) { // the order has changed

                        /*val step = when (viewHolder) {
                            is IngredientViewHolder -> (items[viewHolder.adapterPosition - 1] as IngredientForRecipe).step
                            is StepViewHolder -> items[viewHolder.adapterPosition - 1] as Step
                            else -> null
                        }*/

                        //if (step != null) setNotSelectedItemsFromStep(step)
                        orderChanged = false

                        resetBackground()

                        notifyDataSetChanged()
                    }
                }

                ItemTouchHelper.ACTION_STATE_DRAG -> { // Drag Start
                    if (viewHolder is StepViewHolder) {
                        //startMovingStep(viewHolder.adapterPosition - 1, items[viewHolder.adapterPosition - 1] as Step)
                        val step = items[viewHolder.adapterPosition - 1] as Step
                        setSelectedItemsFromStep(step)
                    }
                }
            }
        }

        private fun resetBackground() {
            for (i in 0 until itemCount) {
                val vh = recyclerView.findViewHolderForAdapterPosition(i)
                if (vh is AbstractViewHolder) vh.notSelectedBackground(true)
            }
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val item = items[viewHolder.adapterPosition - 1]

            if (item is Step) {
                recipe.steps.remove(item)
                items.removeAt(viewHolder.adapterPosition - 1)
                notifyItemRemoved(viewHolder.adapterPosition)
            } else if (item is IngredientForRecipe) {
                recipe.ingredients.remove(item)
                items.removeAt(viewHolder.adapterPosition - 1)
                notifyItemRemoved(viewHolder.adapterPosition)
            }
        }
    }
}
package zelgius.com.myrecipes.adapters

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.entities.IngredientForRecipe
import zelgius.com.myrecipes.entities.Step

class GroupDividerDecoration(
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

                if (viewType < 0) { // if < 0, it is a section
                    outRect.set(0, mHeightDp, 0, 0)
                } else {
                    outRect.set(0, 0, 0, 0)
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

                if (viewType < 0) { // if < 0, it is a section
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
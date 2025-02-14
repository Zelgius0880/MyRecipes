import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.drawable.Drawable
import android.util.TypedValue

class TextDrawable(
    res: Resources,
    private val mText: CharSequence,
    private val textSize: Float? = null
) : Drawable() {
    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mIntrinsicWidth: Int
    private val mIntrinsicHeight: Int

    init {
        mPaint.color = DEFAULT_COLOR
        mPaint.textAlign = Align.CENTER
        val textSize = this.textSize ?: TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            DEFAULT_TEXTSIZE.toFloat(), res.displayMetrics
        )
        mPaint.textSize = textSize
        mIntrinsicWidth = (mPaint.measureText(mText, 0, mText.length) + .5).toInt()
        mIntrinsicHeight = mPaint.getFontMetricsInt(null)
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        canvas.drawText(
            mText, 0, mText.length,
            bounds.centerX().toFloat(), bounds.centerY().toFloat(), mPaint
        )
    }

    @Deprecated("")
    override fun getOpacity(): Int {
        return mPaint.alpha
    }

    override fun getIntrinsicWidth(): Int {
        return mIntrinsicWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mIntrinsicHeight
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(filter: ColorFilter?) {
        mPaint.colorFilter = filter
    }

    companion object {
        private const val DEFAULT_COLOR = Color.WHITE
        private const val DEFAULT_TEXTSIZE = 15
    }
}
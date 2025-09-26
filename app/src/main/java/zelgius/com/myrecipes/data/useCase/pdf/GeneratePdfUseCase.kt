package zelgius.com.myrecipes.data.useCase.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withTranslation
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.text
import zelgius.com.myrecipes.data.useCase.GenerateQrCodeUseCase
import zelgius.com.myrecipes.utils.UiUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt


class GeneratePdfUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val recipeRepository: RecipeRepository
) {
    companion object {
        const val A4_WIDTH = 4 * 595
        const val A4_HEIGHT = 4 * 842

        const val INGREDIENT_TEXT_SIZE = 24f
        const val INGREDIENT_IMAGE_SIZE = 50
        const val INGREDIENT_IMAGE_PADDING = 4f

        const val STEP_TEXT_SIZE = 36f
        const val STEP_IMAGE_SIZE = 50

        const val MARGINS_SMALL = 50
        const val MARGINS_HORIZONTAL = 200

        const val TITLE_TEXT_SIZE = 77f
        const val TITLE_IMAGE_SIZE = 200

    }

    private var pageNumber: Int = 0
    private lateinit var pageInfo: PdfDocument.PageInfo
    private lateinit var document: PdfDocument
    private lateinit var page: PdfDocument.Page

    private lateinit var canvas: Canvas
    private var linePosition: Int = 0

    private val textPaint = TextPaint()
    private val paint = Paint()

    private val alpha = 0.6f
    suspend fun execute(recipe: Recipe, outputStream: OutputStream, close: Boolean = true) =
        withContext(Dispatchers.IO) {
            drawRecipe(recipe)
            // write the document content
            val output = ByteArrayOutputStream()
            createFile(document, output)
            document.close()

            outputStream.write(output.toByteArray())
            outputStream.flush()

            if (close) {
                outputStream.close()
            }

            output.close()
        }

    private suspend fun drawRecipe(recipe: Recipe) {
        pageNumber = 1
        pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, pageNumber).create()
        document = PdfDocument()
        page = document.startPage(pageInfo)
        canvas = page.canvas

        linePosition = 200

        linePosition = drawTitle(recipe)

        linePosition += 100

        linePosition = drawSeparator()

        //Title All Ingredients
        linePosition += 100

        textPaint.apply {
            reset()
            color = context.getColor(R.color.md_black_1000)
            textSize = INGREDIENT_TEXT_SIZE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        Utils.drawText(
            canvas,
            linePosition,
            textPaint,
            context.getString(R.string.all_ingredients),
            MARGINS_HORIZONTAL
        )
        val ingredients =
            recipe.ingredients.sortedWith { o1, o2 ->
                when {
                    o1.step == null && o2.step != null -> -1
                    o2.step == null && o1.step != null -> 1
                    else -> o1.sortOrder - o2.sortOrder
                }
            }

        linePosition += 100
        ingredients.forEach {
            linePosition += 24
            drawIngredient(it)
        }

        recipe.steps.forEach {
            linePosition += 100

            drawStep(
                it,
                recipe.ingredients.filter { i -> i.step == it }.sortedBy { i -> i.sortOrder })
        }

        drawQrCode(recipe)

        document.finishPage(page)
    }


    private fun drawTitle(recipe: Recipe): Int {

        val bmp = Utils.scaleCenterCrop(
            recipe.imageUrl?.toUri()?.let {
                if (it.scheme == "content")
                    BitmapFactory.decodeStream(context.contentResolver.openInputStream(it))
                else null
            }
                ?: ContextCompat.getDrawable(context, R.drawable.ic_dish)!!
                    .toBitmap(TITLE_IMAGE_SIZE, TITLE_IMAGE_SIZE),
            TITLE_IMAGE_SIZE, TITLE_IMAGE_SIZE
        )

        val matrix = Matrix()
        matrix.postScale(0.5f, 0.5f)

        canvas.drawBitmap(bmp, MARGINS_HORIZONTAL.toFloat(), linePosition.toFloat(), null)

        textPaint.apply {
            reset()
            color = context.getColor(R.color.md_black_1000)
            textSize = TITLE_TEXT_SIZE
        }

        linePosition = max(
            linePosition + bmp.height,
            Utils.drawText(
                canvas,
                linePosition,
                textPaint,
                recipe.name,
                MARGINS_HORIZONTAL + bmp.width + 100
            )
        )

        return linePosition
    }

    /**
     * Draw a horizontal line separator.
     * linePosition will be set : linePosition += separator width
     * @param margin Float  the margin in pixels for the start and the end. The line will be drawn from margin to A4_WIDTH - margin
     * @param color Int     the color of the line (by default #B0BBC5 -> blue grey 200)
     * @return Int          linePosition + separator width (8px)
     */
    private fun drawSeparator(
        margin: Float = 300f,
        @ColorInt color: Int = "#B0BBC5".toColorInt()
    ): Int {
        val width = 8f
        paint.apply {
            reset()
            this.color = color
            this.strokeWidth = width
            this.strokeCap = Paint.Cap.ROUND

            canvas.drawLine(
                margin,
                linePosition.toFloat(),
                A4_WIDTH - margin,
                linePosition.toFloat(),
                this
            )
        }

        linePosition += width.toInt()
        return linePosition
    }


    /**
     * Draw the ingredient's image with its quantity (quantity + Unit) and its name -> call of IngredientForRecipe.text().
     * linePosition will be set : max(image.height, text.height)
     * @param item IngredientForRecipe  the ingredient to render
     * @return Int                      linePosition += max(image.height, text.height)
     */
    private fun drawIngredient(
        item: Ingredient,
        maxWidth: Int = A4_WIDTH,
        margin: Int = MARGINS_HORIZONTAL
    ): Int {

        val bmp = Utils.scaleCenterCrop(
            UiUtils.getDrawableForImageView(context, item, padding = INGREDIENT_IMAGE_PADDING)
                .toBitmap(INGREDIENT_IMAGE_SIZE, INGREDIENT_IMAGE_SIZE),
            INGREDIENT_IMAGE_SIZE,
            INGREDIENT_IMAGE_SIZE
        )

        textPaint.apply {
            reset()
            color = context.getColor(R.color.md_black_1000)
            textSize = INGREDIENT_TEXT_SIZE

            if (item.optional == true || item.step?.optional == true)
                alpha = (this@GeneratePdfUseCase.alpha * 255).roundToInt()
        }

        paint.apply {
            reset()

            if (item.optional == true || item.step?.optional == true)
                alpha = (this@GeneratePdfUseCase.alpha * 255).roundToInt()
        }

        val text = item.text(context)

        val builder = StaticLayout.Builder.obtain(
            text,
            0,
            text.length,
            textPaint,
            maxWidth - bmp.width - 50
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        val staticLayout = builder.build()

        val height = max(bmp.height, staticLayout.height)
        if (!canvas.isHighEnough(height, linePosition)) {
            nextPage()
        }

        if (staticLayout.height > bmp.height) {
            Utils.drawText(
                canvas, linePosition, textPaint, item.text(context),
                margin + MARGINS_SMALL + bmp.width
            )

            canvas.drawBitmap(
                bmp,
                margin.toFloat(),
                linePosition + staticLayout.height / 2f - bmp.height / 2f,
                paint
            )
        } else {
            canvas.drawBitmap(bmp, margin.toFloat(), linePosition.toFloat(), paint)

            Utils.drawText(
                canvas,
                linePosition + bmp.height / 2 - staticLayout.height / 2,
                textPaint,
                item.text(context),
                margin + MARGINS_SMALL + bmp.width
            )
        }

        linePosition += height

        return linePosition
    }


    private fun drawStep(step: Step, list: List<Ingredient>): Int {
        val bmp = Utils.scaleCenterCrop(
            UiUtils.getDrawableForText(
                context,
                "${step.order}",
                ContextCompat.getColor(context, R.color.md_blue_800)
            ).toBitmap(STEP_IMAGE_SIZE, STEP_IMAGE_SIZE),
            STEP_IMAGE_SIZE,
            STEP_IMAGE_SIZE
        )

        if (!canvas.isHighEnough(bmp.height, linePosition)) nextPage()

        val margin = MARGINS_HORIZONTAL.toFloat()

        paint.apply {
            reset()
            if (step.optional)
                alpha = (this@GeneratePdfUseCase.alpha * 255).roundToInt()
        }
        canvas.drawBitmap(bmp, margin, linePosition.toFloat(), paint)


        var tempPosition = linePosition

        var colChanged = false
        var ingredientPosition = linePosition

        list.forEachIndexed { i, item ->
            if (i > 0) linePosition += 25

            drawIngredient(
                item, maxWidth = A4_WIDTH / 2,
                margin = if (i < ceil(list.size / 2f)) {
                    margin.toInt() + STEP_IMAGE_SIZE + 50
                } else {
                    if (!colChanged) {
                        linePosition = tempPosition
                        colChanged = true
                    }
                    50 + A4_WIDTH / 2
                }
            )

            if (!colChanged) ingredientPosition = linePosition
        }

        paint.apply {
            reset()
            this.color = context.getColor(R.color.md_blue_grey_200)
            this.strokeWidth = 8f
            this.strokeCap = Paint.Cap.ROUND

            canvas.drawLine(
                A4_WIDTH / 2f - 4f,
                tempPosition.toFloat(),
                A4_WIDTH / 2f - 4f,
                ingredientPosition.toFloat(),
                this
            )
        }


        //if (linePosition >= tempPosition) // position has changed to a new page

        linePosition = if (tempPosition < ingredientPosition)
            ingredientPosition + 50
        else
            tempPosition


        textPaint.apply {
            reset()
            color = context.getColor(R.color.md_black_1000)
            textSize = STEP_TEXT_SIZE

            if (step.optional)
                alpha = (this@GeneratePdfUseCase.alpha * 255).roundToInt()
        }

        //linePosition = tempPosition
        tempPosition = linePosition
        drawLongText(
            step.text,
            A4_WIDTH - bmp.width - 2 * margin.toInt(),
            margin.toInt() + bmp.width + 75,
            paint = textPaint.apply {
                reset()
                color = context.getColor(R.color.md_black_1000)
                textSize = STEP_TEXT_SIZE

                if (step.optional)
                    alpha = (this@GeneratePdfUseCase.alpha * 255).roundToInt()
            }
        )

        linePosition = if (linePosition < tempPosition) linePosition // new page
        else max(
            linePosition,
            tempPosition + STEP_IMAGE_SIZE
        ) // max between the text bottom and the image bottom
        return linePosition
    }

    private fun drawLongText(
        s: String,
        width: Int,
        x: Int,
        marginBottom: Int = 200,
        paint: TextPaint? = null
    ): Int {
        var text = s

        if (paint == null)
            textPaint.apply {
                reset()
                color = context.getColor(R.color.md_black_1000)
                textSize = STEP_TEXT_SIZE
            }
        else textPaint.set(paint)

        var staticLayout = StaticLayout.Builder.obtain(
            text,
            0,
            text.length,
            textPaint,
            width
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()

        while (!canvas.isHighEnough(staticLayout.height, linePosition, marginBottom)) {
            var height = 0
            val builder = StringBuilder()

            for (i in 0 until staticLayout.lineCount) {
                height += staticLayout.getLineBottom(i) - staticLayout.getLineTop(i)
                if (!canvas.isHighEnough(height, linePosition, marginBottom)) break

                builder.append(
                    text.substring(
                        staticLayout.getLineStart(i),
                        staticLayout.getLineEnd(i)
                    )
                )
            }

            Utils.drawText(canvas, linePosition, textPaint, builder.toString(), x)

            text = text.substring(builder.length - 1).trim()

            staticLayout = StaticLayout.Builder.obtain(
                text,
                0,
                text.length,
                textPaint,
                width
            )
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .build()

            nextPage()
        }

        Utils.drawText(canvas, linePosition, textPaint, text, x)
        linePosition += staticLayout.height

        return linePosition
    }


    private suspend fun drawQrCode(recipe: Recipe): Int {
        val bmp = GenerateQrCodeUseCase().execute(
            recipeRepository.getBytesForQr(recipe), dotColor = Color.BLACK
        ) ?: return linePosition

        val margin = 200
        textPaint.apply {
            reset()
            color = context.getColor(R.color.md_black_1000)
            textSize = 24f
        }

        if (canvas.isHighEnough(bmp.height, linePosition)) {
            canvas.drawBitmap(
                bmp,
                margin.toFloat(),
                A4_HEIGHT - margin - bmp.height.toFloat(),
                null
            )
            Utils.drawText(
                canvas,
                A4_HEIGHT - margin - bmp.height + 15,
                textPaint,
                context.getString(R.string.qrcode_explanation),
                margin + bmp.width + 25
            )
            linePosition = A4_HEIGHT - bmp.height
        } else {
            nextPage()
            canvas.drawBitmap(bmp, margin.toFloat(), linePosition.toFloat(), null)
            Utils.drawText(
                canvas,
                linePosition + 15,
                textPaint,
                context.getString(R.string.qrcode_explanation),
                margin + bmp.width + 25
            )
            linePosition += bmp.height
        }


        return linePosition
    }

    /**
     * End the current page, start a new page, reset the linePosition and set the canvas with the new page
     */
    private fun nextPage() {
        document.finishPage(page)
        ++pageNumber
        pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, pageNumber).create()
        page = document.startPage(pageInfo)
        canvas = page.canvas
        linePosition = 200
    }


    private fun createFile(document: PdfDocument, outputStream: OutputStream) {
        try {
            document.writeTo(outputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        // close the document
    }

}

object Utils {


    fun scaleCenterCrop(
        source: Bitmap, newHeight: Int,
        newWidth: Int
    ): Bitmap {
        val sourceWidth = source.width
        val sourceHeight = source.height

        val xScale = newWidth.toFloat() / sourceWidth
        val yScale = newHeight.toFloat() / sourceHeight
        val scale = max(xScale, yScale)

        // Now get the size of the source bitmap when scaled
        val scaledWidth = scale * sourceWidth
        val scaledHeight = scale * sourceHeight

        val left = (newWidth - scaledWidth) / 2
        val top = (newHeight - scaledHeight) / 2

        val targetRect =
            RectF(left, top, left + scaledWidth, top + scaledHeight)

        val dest = createBitmap(newWidth, newHeight, source.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dest)
        canvas.drawBitmap(source, null, targetRect, null)

        return dest
    }

    fun drawText(
        canvas: Canvas,
        linePosition: Int,
        paint: TextPaint,
        text: String,
        x: Int,
        maxWidth: Int = GeneratePdfUseCase.A4_WIDTH,
        marginEnd: Int = GeneratePdfUseCase.MARGINS_HORIZONTAL
    ): Int {
        val builder = StaticLayout.Builder.obtain(
            text,
            0,
            text.length,
            paint,
            maxWidth - x - marginEnd
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        val staticLayout = builder.build()

        canvas.withTranslation(x.toFloat(), linePosition.toFloat()) {
            staticLayout.draw(canvas)
        }

        return linePosition + staticLayout.height
    }

}

fun Canvas.isHighEnough(height: Int, linePosition: Int, marginBottom: Int = 200) =
    this.height >= height + linePosition + marginBottom
package zelgius.com.myrecipes.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Base64
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withTranslation
import androidx.core.net.toUri
import com.amulyakhare.textdrawable.TextDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.glxn.qrgen.android.QRCode
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.entities.IngredientForRecipe
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.entities.Step
import zelgius.com.myrecipes.utils.Utils.drawText
import zelgius.com.myrecipes.utils.Utils.scaleCenterCrop
import zelgius.com.myrecipes.utils.Utils.zipBytes
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class PdfGenerator(val context: Context) {
    companion object {
        const val A4_WIDTH = 4 * 595
        const val A4_HEIGHT = 4 * 842
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
    suspend fun createPdf(recipe: Recipe, uri: Uri) =
        withContext(Dispatchers.IO) {
            drawRecipe(recipe)
            // write the document content
            val output = context.contentResolver.openOutputStream(uri)!!
            createFile(document, output)
            document.close()

            uri
        }

    private fun drawRecipe(recipe: Recipe) {
        pageNumber = 1
        pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, pageNumber).create()
        document = PdfDocument()
        page = document.startPage(pageInfo)
        canvas = page.canvas
        linePosition = 0

        linePosition = 200
        // create a new document
        linePosition = drawTitle(recipe)

        linePosition += 100

        linePosition = drawSeparator()

        //Title All Ingredients
        linePosition += 100

        textPaint.apply {
            reset()
            color = context.getColor(R.color.md_black_1000)
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        drawText(
            canvas,
            linePosition,
            textPaint,
            context.getString(R.string.all_ingredients),
            200
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
            //drawSeparator(margin = 700f)
            //linePosition += 100

            drawStep(
                it,
                recipe.ingredients.filter { i -> i.step == it }.sortedBy { i -> i.sortOrder })
        }

        drawQrCode(recipe)

        document.finishPage(page)
    }

    suspend fun createPdf(recipes: List<Recipe>, uri: Uri) =
        withContext(Dispatchers.IO) {
            val zipOut = ZipOutputStream(context.contentResolver.openOutputStream(uri)!!)
            recipes.forEach {
                drawRecipe(it)

                val entry = ZipEntry("${it.name.replace(File.separator, "_")}.pdf")
                zipOut.putNextEntry(entry)
                document.writeTo(zipOut)
                document.close()
            }

            zipOut.close()
            uri
        }

    /**
     * Draw the title of the document which is the name of the recipe and the image of the recipe
     * @param recipe    Recipe the targeted recipe
     * @return Int      Return the line position reaches after the title
     */
    private fun drawTitle(recipe: Recipe): Int {

        val bmp = scaleCenterCrop(
            recipe.imageURL?.toUri()?.let {
                if (it.scheme == "content")
                    BitmapFactory.decodeStream(context.contentResolver.openInputStream(it))
                else null
            }
                ?: ContextCompat.getDrawable(context, R.drawable.ic_dish)!!.toBitmap(400, 400),
            400, 400
        )

        val matrix = Matrix()
        matrix.postScale(0.5f, 0.5f)

        canvas.drawBitmap(bmp, 200f, linePosition.toFloat(), null)

        textPaint.apply {
            reset()
            color = context.getColor(R.color.md_black_1000)
            textSize = 144f
        }

        linePosition = max(
            linePosition + bmp.height,
            drawText(canvas, linePosition, textPaint, recipe.name, 200 + bmp.width + 100)
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
        @ColorInt color: Int = Color.parseColor("#B0BBC5")
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
        item: IngredientForRecipe,
        maxWidth: Int = A4_WIDTH,
        margin: Int = 200
    ): Int {

        val width = 100

        val bmp = scaleCenterCrop(
            UiUtils.getDrawableForImageView(context, item, padding = 6f).toBitmap(width, width),
            width,
            width
        )

        textPaint.apply {
            reset()
            color = context.getColor(R.color.md_black_1000)
            textSize = 48f

            if (item.optional == true || item.step?.optional == true)
                alpha = (this@PdfGenerator.alpha * 255).roundToInt()
        }

        paint.apply {
            reset()

            if (item.optional == true || item.step?.optional == true)
                alpha = (this@PdfGenerator.alpha * 255).roundToInt()
        }

        val text = IngredientForRecipe.text(context, item)

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
            drawText(
                canvas, linePosition, textPaint, IngredientForRecipe.text(context, item),
                margin + 100 + bmp.width
            )

            canvas.drawBitmap(
                bmp,
                margin.toFloat(),
                linePosition + staticLayout.height / 2f - bmp.height / 2f,
                paint
            )
        } else {
            canvas.drawBitmap(bmp, margin.toFloat(), linePosition.toFloat(), paint)

            drawText(
                canvas,
                linePosition + bmp.height / 2 - staticLayout.height / 2,
                textPaint,
                IngredientForRecipe.text(context, item),
                margin + 100 + bmp.width
            )
        }

        linePosition += height

        return linePosition
    }


    private fun drawStep(step: Step, list: List<IngredientForRecipe>): Int {
        val width = 200
        val bmp = scaleCenterCrop(
            TextDrawable.builder()
                .beginConfig()
                .fontSize(75)
                .width(width)
                .height(width)
                .bold()
                .endConfig()
                .buildRound(
                    "${step.order}",
                    ContextCompat.getColor(context, R.color.md_cyan_A700)
                ).toBitmap(width, width),
            width,
            width
        )

        if (!canvas.isHighEnough(bmp.height, linePosition)) nextPage()

        val margin = 200f

        paint.apply {
            reset()
            if (step.optional)
                alpha = (this@PdfGenerator.alpha * 255).roundToInt()
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
                    margin.toInt() + width + 50
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
            textSize = 72f

            if (step.optional)
                alpha = (this@PdfGenerator.alpha * 255).roundToInt()
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
                textSize = 72f

                if (step.optional)
                    alpha = (this@PdfGenerator.alpha * 255).roundToInt()
            }
        )

        linePosition = if (linePosition < tempPosition) linePosition // new page
        else max(
            linePosition,
            tempPosition + width
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
                textSize = 72f
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

            drawText(canvas, linePosition, textPaint, builder.toString(), x)

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

        drawText(canvas, linePosition, textPaint, text, x)
        linePosition += staticLayout.height

        return linePosition
    }


    private fun drawQrCode(recipe: Recipe): Int {
        recipe.imageURL = null
        val bytes = zipBytes("", recipe.toProtoBuff().toByteArray())
        val bmp = QRCode.from(Base64.encodeToString(bytes, Base64.NO_PADDING))
            .withSize(400, 400)
            .bitmap()

        /*val bmp = QRCode.from(String(bytes, Charset.forName("UTF-8")))
            .withSize(250, 250)
            .bitmap()*/


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
            drawText(
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
            drawText(
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

        val dest = Bitmap.createBitmap(
            newWidth, newHeight,
            source.config
        )
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
        maxWidth: Int = PdfGenerator.A4_WIDTH,
        marginEnd: Int = 200
    ): Int {
        val builder = StaticLayout.Builder.obtain(
            text,
            0,
            text.length,
            paint,
            maxWidth - x - marginEnd
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        /*.setLineSpacing(1f, 0)
        .setIncludePad(false)
        .setMaxLines(5)*/
        val staticLayout = builder.build()

        canvas.withTranslation(x.toFloat(), linePosition.toFloat()) {
            staticLayout.draw(canvas)
        }

        return linePosition + staticLayout.height
    }

    @Throws(IOException::class)
    fun zipBytes(filename: String, input: ByteArray): ByteArray {
        val baos = ByteArrayOutputStream()
        val zos = ZipOutputStream(baos)
        val entry = ZipEntry(filename.substring(0, min(filename.length, 0xFFFF)))
        entry.size = input.size.toLong()
        zos.putNextEntry(entry)
        zos.write(input)
        zos.closeEntry()
        zos.close()
        return baos.toByteArray()
    }
}

fun Canvas.isHighEnough(height: Int, linePosition: Int, marginBottom: Int = 200) =
    this.height >= height + linePosition + marginBottom
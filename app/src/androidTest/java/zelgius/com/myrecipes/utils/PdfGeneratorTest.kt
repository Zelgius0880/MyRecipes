package zelgius.com.myrecipes.utils

import android.app.Application
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.createDummySample
import zelgius.com.myrecipes.data.entities.StepEntity
import java.io.File

class PdfGeneratorTest {

    private val context by lazy { ApplicationProvider.getApplicationContext<Application>()!! }

    private val viewModel: RecipeViewModel by lazy { RecipeViewModel(context) }

    @Test
    fun createPdf() {
        runBlocking {

            val file = File("/storage/emulated/0/Download")

            // Test with default image
            PdfGenerator(context).createPdf(viewModel.createDummySample().apply {
                name = "Default $name"
                //imageURL = "file:/storage/emulated/0/Android/data/zelgius.com.myrecipes/files/Pictures/23"
            }, file.toUri())

            // Test with storage image
            PdfGenerator(context).createPdf(viewModel.createDummySample().apply {
                name = "Storage $name"
                imageURL =
                    "file:/storage/emulated/0/Android/data/zelgius.com.myrecipes/files/Pictures/23"
            }, file.toUri())


            // Test with storage image
            PdfGenerator(context).createPdf(recipe = viewModel.createDummySample().apply {
                name = "Long Step $name"
                steps.add(
                    StepEntity(
                        id = null,
                        text = LONG_STRING + LONG_STRING + LONG_STRING, order = steps.size + 1,
                        optional = false,
                        refRecipe = null
                    )
                )
            }, file.toUri())


            //Thread.sleep(300000)
            assertTrue(File(file, "${viewModel.currentRecipe.name}.pdf").exists())
        }
    }
}


const val LONG_STRING =
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed ante odio, cursus nec mattis bibendum, iaculis sit amet urna. Nam blandit finibus dolor ac placerat. Aenean sit amet ligula scelerisque, porta sem in, varius nibh. Nulla efficitur vehicula nibh eu pellentesque. Donec vel risus ultrices, fringilla orci ac, volutpat urna. Suspendisse vehicula venenatis magna, quis euismod magna sodales in. Ut ac placerat purus. Suspendisse volutpat lorem eget ante facilisis pharetra at nec felis. Quisque convallis nunc velit, tincidunt malesuada leo ullamcorper at. Nullam ut tellus ornare, rutrum erat sed, pulvinar velit. Phasellus lacinia tempus mauris.\n" +
            "\n" +
            "Etiam posuere enim eget diam auctor, vel faucibus orci efficitur. Aliquam erat volutpat. Ut gravida tellus nec fringilla mattis. Duis aliquet tortor eget posuere ullamcorper. Sed eget mauris et lectus vehicula vehicula tincidunt in quam. Nulla sodales, ante eleifend tempor ultrices, nisi dui placerat magna, tincidunt eleifend justo dui aliquam ante. Maecenas eu est tortor. Pellentesque euismod nibh dignissim purus tempus, vitae ullamcorper purus mollis. Vivamus fringilla egestas eros non rutrum. Aenean sagittis nisl non pretium ultrices. Maecenas cursus neque sed velit placerat bibendum.\n" +
            "\n"
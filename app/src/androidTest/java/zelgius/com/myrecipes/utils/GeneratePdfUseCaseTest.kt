package zelgius.com.myrecipes.utils

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import zelgius.com.myrecipes.data.AppDatabase
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.useCase.pdf.GeneratePdfUseCase
import zelgius.com.myrecipes.ui.preview.createDummyModel
import zelgius.com.myrecipes.worker.WorkerRepository
import java.io.File
import java.io.IOException

class GeneratePdfUseCaseTest {

    private val context by lazy { ApplicationProvider.getApplicationContext<Application>()!! }
    private val recipeDao by lazy { db.recipeDao }
    private val stepDao by lazy { db.stepDao }
    private val ingredientDao by lazy { db.ingredientDao }

    private val recipeRepository by lazy { RecipeRepository(recipeDao, stepDao, ingredientDao) }
    private val workerRepository = WorkerRepository(context, DataStoreRepository(context))


    @Test
    fun createPdf() {
        runBlocking {

            // Test with default image
            val recipe = createDummyModel().let {
                it.copy(
                    ingredients = it.ingredients + Ingredient(
                        name = "Test",
                        recipe = it,
                        quantity = 2.5,
                        unit = Ingredient.Unit.Kilogramme,
                        id = null,
                        step = null,
                        optional = false,
                        sortOrder = 0,
                        idIngredient = null,
                        imageUrl = null
                    )
                )
            }
            val file = File("/storage/emulated/0/Download")

            val file1 = File(file, "${recipe.name}0.pdf")
            val file2 = File(file, "${recipe.name}1.pdf")
            val file3 = File(file, "${recipe.name}2.pdf")

            file1.delete()
            file2.delete()
            file3.delete()

            GeneratePdfUseCase(context, recipeRepository).execute(
                recipe,
                file1.outputStream()
            )

            // Test with storage image
            GeneratePdfUseCase(context, recipeRepository).execute(
                recipe.copy(
                    name = "Storage ${recipe.name}",
                    imageUrl = "android.resource://zelgius.com.myrecipes.debug/drawable/i_28"
                ), file2.outputStream()
            )

            // Test with storage image
            GeneratePdfUseCase(context, recipeRepository).execute(
                recipe.copy(
                    name = "Storage ${recipe.name}",
                    steps = recipe.steps + Step(
                        text = LONG_STRING + LONG_STRING + LONG_STRING,
                        recipe = recipe
                    )
                ), file3.outputStream()
            )

            assertTrue(file1.exists())
            assertTrue(file2.exists())
            assertTrue(file3.exists())
        }
    }

    companion object {
        private var _db: AppDatabase? = null
        private val context by lazy { ApplicationProvider.getApplicationContext<Application>()!! }
        private val db get() = _db!!

        @BeforeClass
        @JvmStatic
        fun createDb() {
            _db = AppDatabase.getInstance(context, true)

        }

        @AfterClass
        @JvmStatic
        @Throws(IOException::class)
        fun closeDb() {
            db.close()
        }
    }
}


const val LONG_STRING =
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed ante odio, cursus nec mattis bibendum, iaculis sit amet urna. Nam blandit finibus dolor ac placerat. Aenean sit amet ligula scelerisque, porta sem in, varius nibh. Nulla efficitur vehicula nibh eu pellentesque. Donec vel risus ultrices, fringilla orci ac, volutpat urna. Suspendisse vehicula venenatis magna, quis euismod magna sodales in. Ut ac placerat purus. Suspendisse volutpat lorem eget ante facilisis pharetra at nec felis. Quisque convallis nunc velit, tincidunt malesuada leo ullamcorper at. Nullam ut tellus ornare, rutrum erat sed, pulvinar velit. Phasellus lacinia tempus mauris.\n" +
            "\n" +
            "Etiam posuere enim eget diam auctor, vel faucibus orci efficitur. Aliquam erat volutpat. Ut gravida tellus nec fringilla mattis. Duis aliquet tortor eget posuere ullamcorper. Sed eget mauris et lectus vehicula vehicula tincidunt in quam. Nulla sodales, ante eleifend tempor ultrices, nisi dui placerat magna, tincidunt eleifend justo dui aliquam ante. Maecenas eu est tortor. Pellentesque euismod nibh dignissim purus tempus, vitae ullamcorper purus mollis. Vivamus fringilla egestas eros non rutrum. Aenean sagittis nisl non pretium ultrices. Maecenas cursus neque sed velit placerat bibendum.\n" +
            "\n"

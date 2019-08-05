package zelgius.com.myrecipes

import android.app.Application
import android.os.Environment
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.paging.toLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.IngredientForRecipe
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.repository.observeOnce
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import com.facebook.stetho.Stetho
import zelgius.com.myrecipes.repository.AppDatabase
import zelgius.com.myrecipes.repository.RecipeRepository
import java.io.File
import kotlin.concurrent.thread
import android.content.ActivityNotFoundException
import androidx.core.content.ContextCompat.startActivity
import android.content.Intent




@RunWith(MockitoJUnitRunner::class)
class RecipeViewModelTest {
    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    val context by lazy { ApplicationProvider.getApplicationContext<Application>()!! }

    private val viewModel: RecipeViewModel by lazy { RecipeViewModel(context) }

    private val recipe = Recipe(null, "Recipe for testing", "image", Recipe.Type.OTHER).apply {
        ingredients.add(
            IngredientForRecipe(
                null,
                2.0,
                Ingredient.Unit.KILOGRAMME,
                "test",
                "test",
                2,
                null,
                null
            )
        )
    }

    @Before
    fun init() {
        Stetho.initializeWithDefaults(context)

    }

    @Test
    fun newRecipe() {
        viewModel.newRecipe(recipe).observeOnce {
            println(it)
            assertEquals(recipe, it)
        }
    }

    @Test
    fun saveCurrentRecipe() {

        val latch = CountDownLatch(2)


        viewModel.createDummySample()

        viewModel.saveCurrentRecipe().observeForever { r ->

            //assertTrue(b)

            viewModel.loadRecipe(viewModel.currentRecipe.id!!).observeForever {

                compareRecipe(it, viewModel.currentRecipe)
                latch.countDown()


                viewModel.currentRecipe.name = "Test Update"
                viewModel.currentRecipe.ingredients.removeAt(1)
                viewModel.currentRecipe.steps.removeAt(0)

                viewModel.saveCurrentRecipe().observeForever { r ->

                    //assertTrue(b)

                    viewModel.loadRecipe(viewModel.currentRecipe.id!!).observeForever {
                        compareRecipe(it!!, viewModel.currentRecipe)

                        latch.countDown()
                    }
                }


            }

        }

        latch.await(30, TimeUnit.SECONDS)



        assertTrue(latch.count == 0L)
    }

    private fun compareRecipe(o1: Recipe?, o2: Recipe) {
        assertNotNull(o1!!)
        assertEquals(o1, o2)

        assertEquals(o1.steps.size, o2.steps.size)
        o1.steps.forEach { s ->
            assertNotNull(o2.steps.find { s == it })
        }

        assertEquals(o1.ingredients.size, o2.ingredients.size)
        o1.ingredients.forEach { i ->
            assertNotNull(o2.ingredients.find { i == it })
        }
    }


    @Test
    fun saveRecipe() {

        val latch = CountDownLatch(2)
        viewModel.createDummySample()

        viewModel.saveRecipe(viewModel.currentRecipe).observeForever { r ->

            //assertTrue(b)

            viewModel.loadRecipe(r.id!!).observeForever {

                assertNotNull(it!!)
                compareRecipe(it, r)
                latch.countDown()

                r.name = "Test Update"
                r.ingredients.removeAt(1)
                r.steps.removeAt(0)

                viewModel.saveCurrentRecipe().observeForever { r ->

                    //assertTrue(b)

                    viewModel.loadRecipe(viewModel.currentRecipe.id!!).observeForever {
                        compareRecipe(it!!, r)

                        latch.countDown()

                        Thread.sleep(5000)

                        assertTrue(
                            File(
                                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                                "${it.id}"
                            )
                                .exists()
                        )
                    }
                }
            }
        }

        latch.await(30, TimeUnit.SECONDS)
        assertTrue(latch.count == 0L)

    }

    @Test
    fun delete() {
        val latch = CountDownLatch(1)
        viewModel.createDummySample()

        viewModel.saveRecipe(viewModel.currentRecipe).observeForever { r ->

            //assertTrue(b)
            Thread.sleep(5000)

            viewModel.delete(r).observeForever { b ->

                if (b) {
                    viewModel.loadRecipe(r.id!!).observeForever {
                        assertNull(it)
                        latch.countDown()
                    }
                }
            }
        }

        latch.await(30, TimeUnit.SECONDS)
        assertTrue(latch.count == 0L)
    }

    @Test
    fun search() {
        var latch = CountDownLatch(1)
        viewModel.createDummySample()


        viewModel.saveRecipe(viewModel.currentRecipe).observeForever {
            latch.countDown()
        }

        latch.await(30, TimeUnit.SECONDS)
        assertTrue(latch.count == 0L)

        latch = CountDownLatch(2)

        viewModel.searchResult.observeForever{
            assertTrue(it.size > 0)
            latch.countDown()
        }

        viewModel.search("Test").observeForever{
            assertTrue(it.size > 0)
            latch.countDown()
        }
        latch.await(30, TimeUnit.SECONDS)
        assertTrue(latch.count == 0L)




    }

    @Rule @JvmField
    val mRuntimePermissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Rule @JvmField
    val mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Test
    fun exportToPdf() {
        val file = File("/storage/emulated/0/Download")
        viewModel.createDummySample()
        viewModel.currentRecipe.imageURL = "file:/storage/emulated/0/Android/data/zelgius.com.myrecipes/files/Pictures/23"
        val latch = CountDownLatch(1)

        viewModel.exportToPdf(viewModel.currentRecipe, file).observeOnce {
            assertTrue(file.exists())

            latch.count
        }

        latch.await(10, TimeUnit.SECONDS)


        /*val target = Intent(Intent.ACTION_VIEW)
        target.setDataAndType(file.toUri(), "application/pdf")
        target.flags = Intent.FLAG_ACTIVITY_NEW_TASK // FLAG_ACTIVITY_NEW_TASK only for testing

        val intent = Intent.createChooser(target, "Open File")
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            fail(e.message)
        }*/
    }
}
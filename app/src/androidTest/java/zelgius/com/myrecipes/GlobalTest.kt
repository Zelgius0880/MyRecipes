package zelgius.com.myrecipes

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.repository.OneTimeObserver
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.protobuff.RecipeProto
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.min

class GlobalTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context by lazy { ApplicationProvider.getApplicationContext<Application>()!! }

    private val viewModel: RecipeViewModel by lazy { RecipeViewModel(context) }

    @Test
    fun compareSizeProtoBuffJSON() {
        viewModel.createDummySample()
        val json = Gson().toJson(viewModel.currentRecipe)

        val protoBuff = viewModel.currentRecipe.toProtoBuff().toByteArray()

        println("Before zipping: json -> ${json.toByteArray().size} \t protoBuff -> ${protoBuff.size}")


        val zippedJson = zipBytes("Test", json.toByteArray())
        val zippedProtoBuff = zipBytes("Test", protoBuff)
        println("After zipping: json -> ${zippedJson.size} \t protoBuff -> ${zippedProtoBuff.size}")
        println(
            "After Base64: json -> ${String(Base64.getEncoder().encode(zippedJson)).length} \t" +
                    " protoBuff -> ${String(Base64.getEncoder().encode(zippedProtoBuff)).length}"
        )

        println(
            "After Base64: json -> ${String(Base64.getEncoder().encode(zippedJson))} \t" +
                    " protoBuff -> ${String(Base64.getEncoder().encode(zippedProtoBuff))}"
        )


    }

    @Test
    fun testingProtoBuff() {
        viewModel.createDummySample()
        val protoBuff = viewModel.currentRecipe.toProtoBuff().toByteArray()

        val recipe = RecipeEntity(RecipeProto.Recipe.parseFrom(protoBuff))
        assertEquals(viewModel.currentRecipe, recipe)

        viewModel.currentRecipe.steps.forEach {
            assertTrue(recipe.steps.contains(it))
        }

        viewModel.currentRecipe.ingredients.forEach {
            assertTrue(recipe.ingredients.contains(it))
        }
    }


    @Test
    fun createDefault() = runBlocking {
        val viewModel = RecipeViewModel(context)
        val recipeRepository = RecipeRepository(context)

        var list = listOf<RecipeEntity>()
        var latch = CountDownLatch(1)

        recipeRepository.get().observeOnce {
            list = it
            latch.countDown()
        }

        latch.await()

        list = list.mapNotNull {
            recipeRepository.getFull(it.id!!)
        }

        latch = CountDownLatch(1)
        list.forEach {
            viewModel.delete(it).observeOnce {
                latch.countDown()
            }
        }
        latch.await(100, TimeUnit.MILLISECONDS)

        val defaults = listOf(
            context.getString(R.string.default_1),
            context.getString(R.string.default_2),
            context.getString(R.string.default_3)
        )

        defaults.forEach {
            latch = CountDownLatch(1)
            viewModel.saveFromQrCode(it).observeOnce {
                latch.countDown()
            }
            latch.await()
        }
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

    fun <T> LiveData<T>.observeOnce(onChangeHandler: (T) -> Unit) {
        val observer = OneTimeObserver(handler = onChangeHandler)
        observe(observer, observer)
    }
}
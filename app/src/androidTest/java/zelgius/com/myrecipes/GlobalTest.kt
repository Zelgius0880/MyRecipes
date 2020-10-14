package zelgius.com.myrecipes

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.protobuff.RecipeProto
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.min

class GlobalTest {

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
        println("After Base64: json -> ${String(Base64.getEncoder().encode(zippedJson)).length} \t" +
                " protoBuff -> ${String(Base64.getEncoder().encode(zippedProtoBuff)).length}")

        println("After Base64: json -> ${String(Base64.getEncoder().encode(zippedJson))} \t" +
                " protoBuff -> ${String(Base64.getEncoder().encode(zippedProtoBuff))}")


    }

    @Test
    fun testingProtoBuff(){
        viewModel.createDummySample()
        val protoBuff = viewModel.currentRecipe.toProtoBuff().toByteArray()

        val recipe =  Recipe(RecipeProto.Recipe.parseFrom(protoBuff))
        assertEquals(viewModel.currentRecipe,recipe)

        viewModel.currentRecipe.steps.forEach {
            assertTrue(recipe.steps.contains(it))
        }

        viewModel.currentRecipe.ingredients.forEach {
            assertTrue(recipe.ingredients.contains(it))
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
}
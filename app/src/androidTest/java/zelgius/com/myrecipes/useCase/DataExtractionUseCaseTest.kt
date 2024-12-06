package zelgius.com.myrecipes.useCase

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.functions.functions
import com.zelgius.myrecipes.ia.usecase.DataExtractionUseCase
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import zelgius.com.myrecipes.TestDataBase
import zelgius.com.myrecipes.TestDataBase.db
import zelgius.com.myrecipes.data.repository.IngredientRepository
import java.net.URL

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class DataExtractionUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val ingredientDao by lazy { db.ingredientDao }
    private val ingredientRepository by lazy { IngredientRepository(ingredientDao) }

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUp() {
            val appContext = ApplicationProvider.getApplicationContext<Application>()!!
            FirebaseApp.initializeApp(appContext)
            Firebase.functions.useEmulator("10.0.2.2", 5001)
            TestDataBase.createDb()
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            TestDataBase.closeDb()
        }
    }


    @Test
    fun extractingDataFromPdf() = runBlocking {

        val useCase = object : DataExtractionUseCase(ingredientRepository) {
            override val callable = Firebase.functions
                .getHttpsCallableFromUrl(URL("http://10.0.2.2:5001/piclock-c9af5/europe-west2/extractRecipe"))
        }
        val recipe = useCase.execute(
            InstrumentationRegistry.getInstrumentation().context.assets.open("pdf/test.pdf").readBytes(),
            locale = "en_US"
        ).getOrNull()
        assertNotNull(recipe)
        assert(recipe!!.steps.isNotEmpty())
        assert(recipe.ingredients.isNotEmpty())
        assert(recipe.name.isNotEmpty())
        assert(recipe.ingredients.any { it.idIngredient != null })
        assert(recipe.ingredients.any { it.step != null })
    }
}
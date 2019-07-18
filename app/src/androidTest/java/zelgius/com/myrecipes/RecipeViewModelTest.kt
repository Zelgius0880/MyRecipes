package zelgius.com.myrecipes

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.test.core.app.ApplicationProvider
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.IngredientForRecipe
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.repository.observeOnce
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import android.R.attr.countDown
import android.R.attr.data
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.lifecycle.Observer
import com.facebook.stetho.Stetho


@RunWith(MockitoJUnitRunner::class)
class RecipeViewModelTest{
    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    val context by lazy { ApplicationProvider.getApplicationContext<Application>()!! }

    private val viewModel: RecipeViewModel by lazy { RecipeViewModel(context) }

    private val recipe = Recipe(null, "Recipe for testing", "image", Recipe.Type.OTHER).apply {
        ingredients.add(IngredientForRecipe(null, 2.0, Ingredient.Unit.KILOGRAMME, "test", "test",2 ,  null, null))
    }

    @Before
    fun init(){
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
    fun saveCurrentRecipe(){

        val latch =  CountDownLatch(1)
        viewModel.createDummySample()

        viewModel.saveCurrentRecipe().observeForever { b ->

            //assertTrue(b)

            if(b) {
                viewModel.loadRecipe(viewModel.currentRecipe.id!!).observeForever {

                    assertNotNull(it!!)
                    assertEquals(it, viewModel.currentRecipe)

                    it.steps.forEach { s ->
                        assertNotNull(viewModel.currentRecipe.steps.find { s == it })
                    }

                    it.ingredients.forEach { i ->
                        assertNotNull(viewModel.currentRecipe.ingredients.find { i == it })
                    }
                }
                latch.countDown()
            }
        }

        latch.await(30, TimeUnit.SECONDS)
        assertTrue(latch.count == 0L)
    }
}
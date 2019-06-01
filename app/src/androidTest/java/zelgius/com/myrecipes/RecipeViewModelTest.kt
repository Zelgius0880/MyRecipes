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

@RunWith(MockitoJUnitRunner::class)
class RecipeViewModelTest{
    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    val context by lazy { ApplicationProvider.getApplicationContext<Application>()!! }

    private val viewModel: RecipeViewModel by lazy { RecipeViewModel(context) }

    private val recipe = Recipe(null, "Recipe for testing", "image", Recipe.Type.OTHER).apply {
        ingredients.add(IngredientForRecipe(null, 2.0, Ingredient.Unit.KILOGRAMME, "test", "test", null))
    }

    @Test
    fun newRecipe() {
        viewModel.newRecipe(recipe).observeOnce {
            assertEquals(recipe, it)
        }
    }
}
package zelgius.com.myrecipes

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import zelgius.com.myrecipes.data.AppDatabase
import zelgius.com.myrecipes.data.repository.IngredientRepository
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.repository.StepRepository
import zelgius.com.myrecipes.data.useCase.DeleteRecipeUseCase
import zelgius.com.myrecipes.data.useCase.SaveRecipeUseCase
import zelgius.com.myrecipes.utils.DEFAULT_BASE_64
import zelgius.com.myrecipes.utils.TestHelper
import zelgius.com.myrecipes.utils.assertEquals
import java.io.IOException


@RunWith(AndroidJUnit4ClassRunner::class)
class RestoreRecipeTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val recipeDao by lazy { db.recipeDao }
    private val stepDao by lazy { db.stepDao }
    private val ingredientDao by lazy { db.ingredientDao }

    private val recipeRepository by lazy { RecipeRepository(recipeDao, stepDao, ingredientDao) }

    private val saveRecipeUseCase by lazy {
        SaveRecipeUseCase(
            recipeRepository = recipeRepository,
            stepRepository = StepRepository(stepDao),
            ingredientRepository = IngredientRepository(ingredientDao),
            database = db,
        )
    }

    private val deleteRecipeUseCase by lazy {
        DeleteRecipeUseCase(
            recipeRepository = recipeRepository,
            stepRepository = StepRepository(stepDao),
            ingredientRepository = IngredientRepository(ingredientDao),
            database = db,
        )
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

    @Test
    fun undo() = runBlocking {
        val recipe = with(TestHelper.getFromQr(DEFAULT_BASE_64)) {
            copy(ingredients = ingredients.mapIndexed { index, ingredient ->
                ingredient.copy(
                    sortOrder = index + 1
                )
            }, steps = steps.mapIndexed { index, step -> step.copy(order = index + 1) })
        }

        val id = saveRecipeUseCase.execute(recipe)

        deleteRecipeUseCase.execute(recipeRepository.getFullFlow(id).first()!!)

        assertNull(recipeRepository.getFullFlow(id).first())


        val restored = recipeRepository.getFullFlow(
            saveRecipeUseCase.execute(recipe.copy(id = null))
        ).first()
        assertNotNull(restored)

        recipe.assertEquals(restored!!)
    }

}
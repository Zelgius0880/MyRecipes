package zelgius.com.myrecipes.useCase

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import zelgius.com.myrecipes.data.repository.AppDatabase
import zelgius.com.myrecipes.data.repository.IngredientRepository
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.repository.StepRepository
import zelgius.com.myrecipes.data.useCase.SaveRecipeUseCase
import zelgius.com.myrecipes.utils.TestHelper
import java.io.IOException

@RunWith(AndroidJUnit4ClassRunner::class)
class SaveRecipeUseCaseTest {
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

    companion object {
        private var _db: AppDatabase? = null
        private val context by lazy { ApplicationProvider.getApplicationContext<Application>()!! }
        private val db get() = _db!!
        private const val DEFAULT_BASE_64 =
            "UEsDBBQACAgIABlxalEAAAAAAAAAAAAAAAAAAAAAjY/PSsNAEIfTpik1osR4UHrQIV40IE3Bqngx\n" +
                    "f9CbIAgelY3drkvjJuxu0MfyGXwiH8HdNJD2kODedmZ+33xj21FRZBgStMSHhndg7z/QL5DvGCgj\n" +
                    "HM8pZlI4Pe/EPk5KWTWQDgjVl7n6UwZCcloIp+959lGsOIAkTC8D+PmGBBY5h4vgfBbAB2WO6Udb\n" +
                    "hn7ha+ha91le8rEz5+gTpRm+mUwWugKG3+YRDK8N/3GFMJzQHVX2p+Jsg1IZKsp/pANTEf2a+BK6\n" +
                    "w7iUEvPx3hovrUpgVNujZtZ6KgnaPEDoCvS6DwhrxEituyNE6++uMTAh0G8naOGgJvzeKmG0VANg\n" +
                    "ticGKjFrEtsxfUM8zRmSGAbdsasmtvOMGM0yBKsbrU7DP1BLBwg/FjLQIQEAAFgCAABQSwECFAAU\n" +
                    "AAgICAAZcWpRPxYy0CEBAABYAgAAAAAAAAAAAAAAAAAAAAAAAAAAUEsFBgAAAAABAAEALgAAAE8B\n" +
                    "AAAAAA"

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
    @Throws(Exception::class)
    fun insert() {
        runBlocking {
            val recipe = TestHelper.getFromQr(DEFAULT_BASE_64)
            val id = saveRecipeUseCase.execute(recipe)
            val saved =
                recipeRepository.getFull(id)?.copy(id = null) ?: error("Saved recipe no found")

            // Removing ids to compare with original
            val savedSteps = saved.steps.map { it.copy(id = null) }.toTypedArray()
            assertArrayEquals(recipe.steps.toTypedArray(), savedSteps)

            // Removing ids to compare with original
            val savedIngredients =
                saved.ingredients.map { it.copy(id = null, step = it.step?.copy(id = null)) }
                    .toTypedArray()
            assertArrayEquals(recipe.ingredients.toTypedArray(), savedIngredients)
        }

    }
}
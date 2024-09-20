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
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Ingredient.Unit
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.data.AppDatabase
import zelgius.com.myrecipes.data.repository.IngredientRepository
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.repository.StepRepository
import zelgius.com.myrecipes.data.useCase.SaveRecipeUseCase
import zelgius.com.myrecipes.utils.DEFAULT_BASE_64
import zelgius.com.myrecipes.utils.TestHelper
import zelgius.com.myrecipes.utils.assertEquals
import java.io.IOException
import kotlin.random.Random

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
            val recipe = with(TestHelper.getFromQr(DEFAULT_BASE_64)) {
                copy(ingredients = ingredients.mapIndexed { index, ingredient ->
                    ingredient.copy(
                        sortOrder = index + 1
                    )
                }, steps = steps.mapIndexed { index, step -> step.copy(order = index + 1) })
            }

            val id = saveRecipeUseCase.execute(recipe)
            val saved =
                recipeRepository.getFull(id) ?: error("Saved recipe no found")

            recipe.assertEquals(saved)
        }
    }

    @Test
    @Throws(Exception::class)
    fun update() {
        runBlocking {
            var recipe = TestHelper.getFromQr(DEFAULT_BASE_64).let {
                val id = saveRecipeUseCase.execute(it)
                recipeRepository.getFull(id) ?: error("Saved recipe no found")
            }

            recipe = recipe.copy(
                type = Recipe.Type.Other,
                name = "New name",
                steps = recipe.steps.mapIndexed { index, step ->
                    if (index == 1) step.copy(text = "${step.text} Updated")
                    else step
                }.toMutableList().apply {
                    removeLastOrNull()
                    add(Step(id = null, text = "New step", order = 3, recipe = null))
                },
                ingredients = recipe.ingredients.mapIndexed { index, ingredient ->
                    if (index == 0 || index == 1) ingredient.copy(
                        step = null,
                    )
                    else ingredient
                }.toMutableList().apply {
                    remove(random())
                    remove(random())

                    add(ingredient( name = "New Ingredient"))
                    add(ingredient( name = "New Ingredient", step = recipe.steps.first()))

                    add(ingredient(name = "New Ingredient", step = recipe.steps.first()))
                    add(first().copy(id= null, unit = Unit.entries.random(), quantity = Random.nextDouble(5.0, 50.0), step = recipe.steps.first()))
                }.mapIndexed { index, ingredient ->
                    ingredient.copy(sortOrder = index + 1)
                }
            )

            saveRecipeUseCase.execute(recipe)

            val saved = recipeRepository.getFull(recipe.id!!) ?: error("Saved recipe no found")

            recipe.assertEquals(saved)
        }
    }

    private fun ingredient(recipe: Recipe? = null,
                           quantity: Double = Random.nextDouble(1.0, 100.0),
                           unit: Unit = Unit.entries.random(),
                           name: String,
                           imageUrl: String? = null,
                           optional: Boolean = Random.nextBoolean(),
                           sortOrder: Int = Random.nextInt(),
                           step: Step? = null,
                           id: Long ? = null,
    ) = Ingredient(
        quantity = quantity,
        unit = unit,
        name = name,
        id = id,
        step = step,
        recipe = recipe,
        idIngredient = null,
        imageUrl = imageUrl,
        sortOrder = sortOrder,
        optional = optional,
    )


}
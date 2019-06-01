package zelgius.com.myrecipes.repository

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.IngredientForRecipe
import zelgius.com.myrecipes.entities.Recipe

@RunWith(AndroidJUnit4ClassRunner::class)
class IngredientRepositoryTest: AbstractDatabaseTest() {

    private val ingredientDao by lazy { db.ingredientDao }
    private val recipeDao by lazy { db.recipeDao }
    private val defaultItem = IngredientForRecipe(null, 2.0, Ingredient.Unit.KILOGRAMME, "test", "test", null)
    private val recipe = Recipe(null, "Recipe for testing", "image", Recipe.Type.OTHER)
    private val repo by lazy { IngredientRepository(context) }


    @Test
    fun get() {
        insert()
        ingredientDao.get().observeOnce{
            assertTrue(it.isNotEmpty())
        }
    }

    @Test
    fun insert() {
        runBlocking {
            recipe.id = recipeDao.insert(recipe)
            repo.insert(defaultItem, recipe)
        }

        val item = ingredientDao.getForRecipe(recipeId = recipe.id!!)
        assertEquals(item.first(), defaultItem)
    }
}
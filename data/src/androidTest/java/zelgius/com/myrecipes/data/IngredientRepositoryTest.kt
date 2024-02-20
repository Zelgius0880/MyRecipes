package zelgius.com.myrecipes.data/*
package zelgius.com.myrecipes.repository

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.IngredientForRecipe
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.IngredientRepository

@RunWith(AndroidJUnit4ClassRunner::class)
class IngredientRepositoryTest: AbstractDatabaseTest() {

    private val ingredientDao by lazy { db.ingredientDao }
    private val recipeDao by lazy { db.recipeDao }
    private val defaultItem = IngredientForRecipe(null, 2.0, IngredientEntity.Unit.KILOGRAMME, "test", "test", 0, null,  null)
    private val recipe = RecipeEntity(null, "Recipe for testing", "image", RecipeEntity.Type.OTHER)
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
            val item = ingredientDao.getForRecipe(recipeId = recipe.id!!)
            assertEquals(item.first(), defaultItem)
        }
    }
}*/

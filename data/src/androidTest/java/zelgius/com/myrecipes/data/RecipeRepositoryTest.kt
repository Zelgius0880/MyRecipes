package zelgius.com.myrecipes.data/*
package zelgius.com.myrecipes.repository

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.RecipeRepository

@RunWith(AndroidJUnit4ClassRunner::class)
class RecipeRepositoryTest : AbstractDatabaseTest() {
    private val recipeDao by lazy { db.recipeDao }

    private val recipe = RecipeEntity(null, "Recipe for testing", "image", RecipeEntity.Type.OTHER)
    private val repo by lazy { RecipeRepository(context) }

    @Test
    @Throws(Exception::class)
    fun insert() {
        runBlocking {
            recipe.id = repo.insert(recipe)

            Assert.assertEquals(recipeDao.coroutineGet(recipe.id!!), recipe)
        }

    }

    @Test
    fun get() {
        insert()

        recipeDao.getAll().observeOnce {
            Assert.assertTrue(it.isNotEmpty())
        }
    }
}*/

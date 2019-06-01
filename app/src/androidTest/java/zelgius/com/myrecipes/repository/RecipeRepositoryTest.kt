package zelgius.com.myrecipes.repository

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import zelgius.com.myrecipes.entities.Recipe

@RunWith(AndroidJUnit4ClassRunner::class)
class RecipeRepositoryTest : AbstractDatabaseTest() {
    private val recipeDao by lazy { db.recipeDao }

    private val recipe = Recipe(null, "Recipe for testing", "image", Recipe.Type.OTHER)
    val repo by lazy { RecipeRepository(context) }

    @Test
    @Throws(Exception::class)
    fun insert() {
        runBlocking {
            recipe.id = repo.insert(recipe)

            Assert.assertEquals(recipeDao.blockingGet(recipe.id!!), recipe)
        }

    }

    @Test
    fun get() {
        insert()

        recipeDao.getAll().observeOnce {
            Assert.assertTrue(it.isNotEmpty())
        }
    }
}
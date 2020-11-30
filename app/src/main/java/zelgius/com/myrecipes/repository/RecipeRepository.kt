package zelgius.com.myrecipes.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zelgius.com.myrecipes.entities.Recipe

class RecipeRepository(
    context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context)
) {


    fun get() =
        database.recipeDao.getAll()

    suspend fun getFull(id: Long): Recipe? =
        withContext(Dispatchers.Default) {
            database.recipeDao.coroutineGet(id)?.apply {
                steps.addAll(database.stepDao.blockingGet(id))
                ingredients.addAll(database.ingredientDao.getForRecipe(id))
                ingredients.forEach {
                    if (it.refStep != null) it.step = steps.find { s -> s.id == it.refStep }
                }
            }
        }


    fun pagedMeal() =
        database.recipeDao.pagedMeal()


    fun pagedDessert() =
        database.recipeDao.pagedDessert()


    fun pagedOther() =
        database.recipeDao.pagedOther()

    fun pagedSearch(name: String) =
        database.recipeDao.pagedSearch(name)

    suspend fun insert(recipe: Recipe): Long =
        withContext(Dispatchers.Default) {
            database.recipeDao.insert(recipe)
        }

    suspend fun update(recipe: Recipe): Int =
        withContext(Dispatchers.Default) {
            database.recipeDao.update(recipe)
        }

    suspend fun delete(recipe: Recipe): Int =
        withContext(Dispatchers.Default) {
            database.recipeDao.delete(recipe)
        }
}
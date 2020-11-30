package zelgius.com.myrecipes.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.entities.Step


class StepRepository(
    context: Context,
    database: AppDatabase = AppDatabase.getInstance(context)
) {

    private val dao = database.stepDao

    fun get(recipe: Recipe) =
        dao.get(recipe.id!!)


    /**
     * Insert the Step.
     * @param item Step
     * @return Long the id of the inserted item
     */
    suspend fun insert(item: Step): Long =
        withContext(Dispatchers.Default) {
            dao.insert(item)
        }


    /**
     * Update the Step
     * @param item Step
     * @return Int the  umber of rows affected
     */
    suspend fun update(item: Step): Int =
        withContext(Dispatchers.Default) {
            dao.update(item)
        }


    suspend fun delete(item: Step): Int =
        withContext(Dispatchers.Default) {
            dao.delete(item)
        }


    suspend fun delete(item: Recipe): Int =
        withContext(Dispatchers.Default) {
            dao.deleteFromRecipe(item.id!!)
        }

    /**
     * For the given recipe, delete all steps except the steps in params
     * @param recipe Recipe the targeted recipe
     * @param steps Step    the steps to keep
     * @return Int          the number of rows affected
     */
    suspend fun deleteAllButThem(recipe: Recipe, steps: List<Step>): Int =
        withContext(Dispatchers.Default) {
            dao.delete(recipe.id!!, *steps.map { it.id!! }.toLongArray())
        }
}
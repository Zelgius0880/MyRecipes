package zelgius.com.myrecipes.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.entities.StepEntity


class StepRepository(
    context: Context,
    database: AppDatabase = AppDatabase.getInstance(context)
) {

    private val dao = database.stepDao

    fun get(recipe: RecipeEntity) =
        dao.get(recipe.id!!)


    /**
     * Insert the Step.
     * @param item Step
     * @return Long the id of the inserted item
     */
    suspend fun insert(item: StepEntity): Long =
        withContext(Dispatchers.Default) {
            dao.insert(item)
        }


    /**
     * Update the Step
     * @param item Step
     * @return Int the  umber of rows affected
     */
    suspend fun update(item: StepEntity): Int =
        withContext(Dispatchers.Default) {
            dao.update(item)
        }


    suspend fun delete(item: StepEntity): Int =
        withContext(Dispatchers.Default) {
            dao.delete(item)
        }


    suspend fun delete(item: RecipeEntity): Int =
        withContext(Dispatchers.Default) {
            dao.deleteFromRecipe(item.id!!)
        }

    /**
     * For the given recipe, delete all steps except the steps in params
     * @param recipe Recipe the targeted recipe
     * @param steps Step    the steps to keep
     * @return Int          the number of rows affected
     */
    suspend fun deleteAllButThem(recipe: RecipeEntity, steps: List<StepEntity>): Int =
        withContext(Dispatchers.Default) {
            dao.delete(recipe.id!!, *steps.map { it.id!! }.toLongArray())
        }
}
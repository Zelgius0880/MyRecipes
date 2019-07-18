package zelgius.com.myrecipes.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zelgius.com.myrecipes.entities.*


class StepRepository(context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val dao = database.stepDao

    fun get(recipe: Recipe) =
        dao.get(recipe.id!!)


    /**
     * Insert the Step. Set the
     * @param item Step
     * @return Long the id of the inserted item
     */
    suspend fun insert(item: Step): Long =
        withContext(Dispatchers.Default) {
            dao.insert(item)
        }

}
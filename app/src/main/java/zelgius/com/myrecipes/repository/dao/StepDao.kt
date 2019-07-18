package zelgius.com.myrecipes.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import zelgius.com.myrecipes.entities.Step

@Dao
interface StepDao {

    @Insert
    suspend fun insert(item: Step):Long

    @Update
    suspend fun update(vararg item: Step): Int

    @Delete
    suspend fun delete(vararg item: Step): Int


    @Query("SELECT * FROM step WHERE :recipe = ref_recipe")
    fun get(recipe: Long): LiveData<List<Step>>

    @Query("SELECT * FROM step WHERE :recipe = ref_recipe")
    suspend fun blockingGet(recipe: Long): List<Step>
}
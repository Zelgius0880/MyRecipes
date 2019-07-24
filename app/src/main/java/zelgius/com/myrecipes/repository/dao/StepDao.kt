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

    /**
     * Remove all RecipeIngredient for the given recipe where ref_ingredient are not in ids
     * @param recipeId Long     the id of the recipe
     * @param ids LongArray     a list of ids of steps to keep
     * @return Int              The number of rows affected
     */
    @Query("DELETE FROM Step WHERE id NOT IN (:ids) AND ref_recipe = :recipeId")
    suspend fun delete(recipeId: Long, vararg ids: Long): Int

    @Query("DELETE FROM Step WHERE ref_recipe = :recipeId")
    suspend fun deleteFromRecipe(recipeId: Long): Int
}
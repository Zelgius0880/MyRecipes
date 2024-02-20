package zelgius.com.myrecipes.data.repository.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import zelgius.com.myrecipes.data.entities.StepEntity

@Dao
interface StepDao {

    @Insert
    suspend fun insert(item: StepEntity):Long

    @Update
    suspend fun update(vararg item: StepEntity): Int

    @Delete
    suspend fun delete(vararg item: StepEntity): Int


    @Query("SELECT * FROM step WHERE :recipe = ref_recipe ORDER BY `order`")
    suspend fun get(recipe: Long): List<StepEntity>


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
package zelgius.com.myrecipes.data.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.IngredientForRecipe
import zelgius.com.myrecipes.data.entities.RecipeIngredient

@Dao
interface IngredientDao {

    @Insert
    suspend fun insert(ingredient: IngredientEntity): Long

    @Insert
    suspend fun insert(join: RecipeIngredient): Long

    @Update
    suspend fun update(join: RecipeIngredient): Int

    @Query("SELECT * FROM ingredient ORDER BY name")
    suspend fun get(): List<IngredientEntity>

    @Query("SELECT * FROM IngredientForRecipe WHERE refRecipe = :recipeId ORDER BY sortOrder")
    suspend fun getForRecipe(recipeId: Long): List<IngredientForRecipe>

    @Query("SELECT id FROM RecipeIngredient WHERE ref_ingredient = :ingredientId AND ref_recipe = :recipeId")
    suspend fun getId(ingredientId: Long, recipeId: Long): Long?

    @Query("SELECT * FROM Ingredient WHERE name = :name LIMIT 1")
    suspend fun get(name: String): IngredientEntity?

    @Query("DELETE FROM RecipeIngredient WHERE ref_ingredient = :ingredientId AND ref_recipe = :recipeId")
    suspend fun deleteJoin(ingredientId: Long, recipeId: Long): Int

    /**
     * Remove all RecipeIngredient for the given recipe where ref_ingredient are not in ingredientIds
     * @param recipeId Long             the id of the recipe
     * @param ingredientIds LongArray   a list of ids of ingredients to keep
     * @return Int                      The number of rows affected
     */
    @Query("DELETE FROM RecipeIngredient WHERE ref_ingredient NOT IN (:ingredientIds) AND ref_recipe = :recipeId")
    suspend fun deleteJoin(recipeId: Long, vararg ingredientIds: Long): Int

    @Query("DELETE FROM RecipeIngredient WHERE ref_recipe = :recipeId")
    suspend fun deleteFromRecipe(recipeId: Long): Int
}
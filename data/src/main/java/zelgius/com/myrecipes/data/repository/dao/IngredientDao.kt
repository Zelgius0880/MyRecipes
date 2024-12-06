package zelgius.com.myrecipes.data.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.IngredientForRecipe
import zelgius.com.myrecipes.data.entities.RecipeIngredient
import zelgius.com.myrecipes.data.entities.SimpleIngredientEntity

@Dao
interface IngredientDao {

    @Insert
    suspend fun insert(ingredient: IngredientEntity): Long

    @Update
    suspend fun update(join: IngredientEntity): Int

    @Insert
    suspend fun insert(join: RecipeIngredient): Long

    @Update
    suspend fun update(join: RecipeIngredient): Int

    @Query("SELECT * FROM ingredient ORDER BY name")
    suspend fun get(): List<IngredientEntity>

    @Query("SELECT * FROM ingredient WHERE (image_url IS NULL OR image_url = '' OR image_url = 'null') AND generation_enabled ORDER BY name")
    suspend fun getAllWithoutImages(): List<IngredientEntity>

    @Query("SELECT * FROM ingredient ORDER BY name")
    fun getFlow(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM IngredientForRecipe WHERE refRecipe = :recipeId ORDER BY sortOrder")
    suspend fun getForRecipe(recipeId: Long): List<IngredientForRecipe>

    @Query("SELECT id FROM RecipeIngredient WHERE ref_ingredient = :ingredientId AND ref_recipe = :recipeId")
    suspend fun getId(ingredientId: Long, recipeId: Long): Long?

    @Query("SELECT * FROM Ingredient WHERE name = :name OR :imageUrl IS NOT NULL AND image_url = :imageUrl LIMIT 1")
    suspend fun get(name: String, imageUrl: String?): IngredientEntity?

    @Query("DELETE FROM RecipeIngredient WHERE ref_ingredient = :ingredientId AND ref_recipe = :recipeId")
    suspend fun deleteJoin(ingredientId: Long, recipeId: Long): Int

    @Query("DELETE FROM RecipeIngredient WHERE id NOT IN (:ids) AND ref_recipe = :recipeId")
    suspend fun deleteJoin(recipeId: Long, vararg ids: Long): Int

    @Query("DELETE FROM RecipeIngredient WHERE ref_recipe = :recipeId")
    suspend fun deleteFromRecipe(recipeId: Long): Int


    @Query("DELETE FROM Ingredient WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("SELECT DISTINCT i.id, i.name, i.image_url, i.seed, i.prompt, i.generation_enabled, ri.ref_recipe IS NULL AS removable FROM Ingredient i " +
            "LEFT OUTER JOIN RecipeIngredient ri ON i.id = ri.ref_ingredient " +
            "ORDER BY removable DESC, i.name")
    fun getSimpleIngredients(): Flow<List<SimpleIngredientEntity>>

    @Query("SELECT * FROM Ingredient WHERE name = :name  COLLATE NOCASE LIMIT 1")
    suspend fun getIngredientByName(name: String): IngredientEntity?

}
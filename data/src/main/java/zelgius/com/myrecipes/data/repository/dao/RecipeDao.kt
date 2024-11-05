package zelgius.com.myrecipes.data.repository.dao

import androidx.paging.DataSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import zelgius.com.myrecipes.data.entities.RecipeEntity

@Dao
interface RecipeDao {

    @Insert
    suspend fun insert(recipe: RecipeEntity):Long

    @Update
    suspend fun update(vararg recipe: RecipeEntity): Int

    @Delete
    suspend fun delete(vararg recipe: RecipeEntity): Int

    @Query("SELECT * FROM recipe ORDER BY name")
    suspend fun getAll(): List<RecipeEntity>

    @Query("SELECT * FROM recipe WHERE image_url IS NULL OR image_url == '' OR image_url = 'null' ORDER BY name")
    suspend fun getAllWithoutImages(): List<RecipeEntity>

    // Coroutine get
    @Query("SELECT * FROM recipe WHERE id = :id ORDER BY name")
    suspend fun get(id: Long): RecipeEntity?

    @Query("SELECT * FROM recipe WHERE id = :id ORDER BY name")
    fun getFlow(id: Long): Flow<RecipeEntity?>


    //Paging
    @Query("SELECT * FROM recipe ORDER BY name")
    fun pagedAll(): DataSource.Factory<Int, RecipeEntity>

    @Query("SELECT * FROM recipe WHERE name LIKE '%'||:name||'%' ORDER BY name")
    fun pagedSearch(name: String): DataSource.Factory<Int, RecipeEntity>

    @Query("SELECT * FROM recipe WHERE type = 'MEAL' ORDER BY name")
    fun pagedMeal(): DataSource.Factory<Int, RecipeEntity>


    @Query("SELECT * FROM recipe WHERE type = 'DESSERT' ORDER BY name")
    fun pagedDessert(): DataSource.Factory<Int, RecipeEntity>


    @Query("SELECT * FROM recipe WHERE type = 'OTHER' ORDER BY name")
    fun pagedOther(): DataSource.Factory<Int, RecipeEntity>

}
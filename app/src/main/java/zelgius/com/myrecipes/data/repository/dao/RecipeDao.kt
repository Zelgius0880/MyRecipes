package zelgius.com.myrecipes.data.repository.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import zelgius.com.myrecipes.data.entities.RecipeEntity

@Dao
interface RecipeDao {

    @Insert
    suspend fun insert(recipe: RecipeEntity):Long

    @Update
    suspend fun update(vararg recipe: RecipeEntity): Int

    @Update
    fun blockingUpdate(vararg recipe: RecipeEntity): Int

    @Delete
    suspend fun delete(vararg recipe: RecipeEntity): Int

    @Query("SELECT * FROM recipe ORDER BY name")
    fun getAll(): LiveData<List<RecipeEntity>>

    @Query("SELECT * FROM recipe ORDER BY name")
    suspend fun blockingGetAll(): List<RecipeEntity>


    @Query("SELECT * FROM recipe WHERE type = 'MEAL' ORDER BY name")
    fun getMeal(): LiveData<List<RecipeEntity>>


    @Query("SELECT * FROM recipe WHERE type = 'DESSERT' ORDER BY name")
    fun getDessert(): LiveData<List<RecipeEntity>>


    @Query("SELECT * FROM recipe WHERE type = 'OTHER' ORDER BY name")
    fun getOther(): LiveData<List<RecipeEntity>>

    // Blocking get
    @Query("SELECT * FROM recipe WHERE id = :id ORDER BY name")
    fun blockingGet(id: Long): RecipeEntity?

    // Coroutine get
    @Query("SELECT * FROM recipe WHERE id = :id ORDER BY name")
    suspend fun coroutineGet(id: Long): RecipeEntity?

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
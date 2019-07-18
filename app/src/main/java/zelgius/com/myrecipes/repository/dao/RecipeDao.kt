package zelgius.com.myrecipes.repository.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.Recipe

@Dao
interface RecipeDao {

    @Insert
    suspend fun insert(recipe: Recipe):Long

    @Update suspend fun update(vararg recipe: Recipe): Int

    @Delete suspend fun delete(vararg recipe: Recipe): Int

    @Query("SELECT * FROM recipe ORDER BY name")
    fun getAll(): LiveData<List<Recipe>>


    @Query("SELECT * FROM recipe WHERE type = 'MEAL' ORDER BY name")
    fun getMeal(): LiveData<List<Recipe>>


    @Query("SELECT * FROM recipe WHERE type = 'DESSERT' ORDER BY name")
    fun getDessert(): LiveData<List<Recipe>>


    @Query("SELECT * FROM recipe WHERE type = 'OTHER' ORDER BY name")
    fun getOther(): LiveData<List<Recipe>>


    // Blocking get
    @Query("SELECT * FROM recipe WHERE id = :id ORDER BY name")
    suspend fun blockingGet(id: Long): Recipe

    //Paging
    @Query("SELECT * FROM recipe ORDER BY name")
    fun pagedAll(): DataSource.Factory<Int, Recipe>


    @Query("SELECT * FROM recipe WHERE type = 'MEAL' ORDER BY name")
    fun pagedMeal(): DataSource.Factory<Int, Recipe>


    @Query("SELECT * FROM recipe WHERE type = 'DESSERT' ORDER BY name")
    fun pagedDessert(): DataSource.Factory<Int, Recipe>


    @Query("SELECT * FROM recipe WHERE type = 'OTHER' ORDER BY name")
    fun pagedOther(): DataSource.Factory<Int, Recipe>

}
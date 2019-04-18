package zelgius.com.myrecipes.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.Recipe

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipe ORDER BY name")
    fun getAll(): LiveData<Recipe>


    @Query("SELECT * FROM recipe WHERE type = 'MEAL' ORDER BY name")
    fun getMeal(): LiveData<Recipe>


    @Query("SELECT * FROM recipe WHERE type = 'DESSERT' ORDER BY name")
    fun getDessert(): LiveData<Recipe>


    @Query("SELECT * FROM recipe WHERE type = 'OTHER' ORDER BY name")
    fun getOther(): LiveData<Recipe>


    // Blocking get
    @Query("SELECT * FROM recipe WHERE id = :id ORDER BY name")
    fun blockingGet(id: Long): Recipe
}
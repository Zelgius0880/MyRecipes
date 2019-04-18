package zelgius.com.myrecipes.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import zelgius.com.myrecipes.entities.Ingredient

@Dao
interface IngredientDao {

    @Query("SELECT * FROM ingredient ORDER BY name")
    fun get(): LiveData<Ingredient>
}
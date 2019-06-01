package zelgius.com.myrecipes.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.IngredientForRecipe
import zelgius.com.myrecipes.entities.RecipeIngredient

@Dao
interface IngredientDao {

    @Insert
    suspend fun insert(ingredient: Ingredient): Long

    @Insert
    suspend fun insert(join: RecipeIngredient): Long

    @Query("SELECT * FROM ingredient ORDER BY name")
    fun get(): LiveData<List<Ingredient>>

    @Query("SELECT * FROM IngredientForRecipe WHERE refRecipe = :recipeId")
    fun getForRecipe(recipeId: Long): List<IngredientForRecipe>
}
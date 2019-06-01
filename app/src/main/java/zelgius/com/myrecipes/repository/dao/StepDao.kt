package zelgius.com.myrecipes.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Query
import zelgius.com.myrecipes.entities.Step

interface StepDao {
    @Query("SELECT * FROM step WHERE :recipe = ref_recipe")
    fun get(recipe: Long): LiveData<Step>
}
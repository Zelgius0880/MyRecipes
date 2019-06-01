package zelgius.com.myrecipes.repository

import android.app.Application
import android.content.Context
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.Recipe

class RecipeRepository(context: Context) {

    private val database = AppDatabase.getInstance(context)

    fun get() =
        database.recipeDao.getAll()

    fun pagedMeal() =
        database.recipeDao.pagedMeal()


    fun pagedDessert() =
        database.recipeDao.pagedDessert()


    fun pagedOther() =
        database.recipeDao.pagedOther()

    suspend fun insert(recipe: Recipe): Long {
        val id = database.recipeDao.insert(recipe)
        return id
    }
}
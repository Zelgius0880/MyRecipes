package zelgius.com.myrecipes.repository

import android.content.Context
import zelgius.com.myrecipes.entities.*


class IngredientRepository(context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val dao = database.ingredientDao

    fun get() =
        dao.get()


    suspend fun insert(item: IngredientForRecipe, recipe: Recipe, step: Step? = null): Long {
        val id = if (item.id == null)
            dao.insert(Ingredient(null, item.name, item.imageUrl))
        else item.id!!

        dao.insert(RecipeIngredient(null, item.quantity, item.unit, 0, id, recipe.id, step?.id))

        item.refRecipe = recipe.id!!
        item.id = id

        return id
    }

}
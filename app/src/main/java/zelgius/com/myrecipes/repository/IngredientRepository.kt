package zelgius.com.myrecipes.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zelgius.com.myrecipes.entities.*


class IngredientRepository(context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val dao = database.ingredientDao

    fun get() =
        dao.get()


    /**
     * Insert the IngredientForRecipe (create a RecipeIngredient join). If the id of the ingredient is null, it insert the ingredient too
     * @param item IngredientForRecipe
     * @param recipe Recipe
     * @param step Step?
     * @return Long the id of the inserted item
     */
    suspend fun insert(item: IngredientForRecipe, recipe: Recipe): Long =
        withContext(Dispatchers.Default) {
            // Heavy work

            val id = if (item.id == null)
                dao.insert(Ingredient(null, item.name, item.imageUrl))
            else item.id!!

            dao.insert(
                RecipeIngredient(
                    null,
                    item.quantity,
                    item.unit,
                    item.sortOrder,
                    id,
                    recipe.id,
                    item.step?.id
                )
            )

            item.refRecipe = recipe.id!!
            item.id = id

            id
        }


}
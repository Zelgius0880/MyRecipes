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

    suspend fun get(name: String) =
        withContext(Dispatchers.Default) {
            dao.get(name)
        }


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

    suspend fun delete(item: IngredientForRecipe): Int =
        withContext(Dispatchers.Default) {
            dao.deleteJoin(item.id!!, item.refRecipe!!)
        }


    suspend fun delete(item: Recipe): Int =
        withContext(Dispatchers.Default) {
            dao.deleteFromRecipe(item.id!!)
        }


    /**
     * Update the Step
     * @param item Step
     * @return Int the  number of rows affected
     */
    suspend fun update(item: IngredientForRecipe): Int =
        withContext(Dispatchers.Default) {
            val id = dao.getId(item.id!!, item.refRecipe!!)

            if(id == null) {
                dao.insert(
                    RecipeIngredient(
                        null,
                        item.quantity,
                        item.unit,
                        item.sortOrder,
                        item.id,
                        item.refRecipe,
                        item.refStep
                    )
                )
                0
            }else
                dao.update(RecipeIngredient(id, item.quantity, item.unit, item.sortOrder, item.id, item.refRecipe, item.refStep))
        }


    /**
     * For the given recipe, delete all joins with the ingredients except the ingredients in params
     * @param recipe Recipe                     the targeted recipe
     * @param ingredients IngredientForRecipe   the ingredients to keep
     * @return Int                              the number of rows affected
     */
    suspend fun deleteAllButThem(recipe: Recipe, ingredients: List<IngredientForRecipe>): Int =
        withContext(Dispatchers.Default) {
            dao.deleteJoin(recipe.id!!, *ingredients.map { it.id!! }.toLongArray())
        }

}
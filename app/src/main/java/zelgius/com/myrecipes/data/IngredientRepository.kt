package zelgius.com.myrecipes.data

import androidx.lifecycle.liveData
import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.IngredientForRecipe
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.entities.RecipeIngredient
import zelgius.com.myrecipes.data.entities.asModel
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.asEntity
import zelgius.com.myrecipes.data.repository.dao.IngredientDao
import javax.inject.Inject


class IngredientRepository @Inject constructor(
    private val dao: IngredientDao
) {
    fun get() = liveData {
        emit(dao.get().map { it.asModel() })
    }

    suspend fun get(name: String) = dao.get(name)


    /**
     * Insert the IngredientForRecipe (create a RecipeIngredient join). If the id of the ingredient is null, it insert the ingredient too
     * @param item IngredientForRecipe
     * @param recipe Recipe
     * @return Long the id of the inserted item
     */
    suspend fun insert(item: Ingredient, recipe: Recipe): Long {
        // Heavy work

        val id = if (item.id == null)
            dao.insert(IngredientEntity(null, item.name, item.imageUrl))
        else item.id!!

        dao.insert(
            RecipeIngredient(
                null,
                item.quantity,
                item.unit.asEntity(),
                item.optional,
                item.sortOrder,
                id,
                recipe.id,
                item.step?.id
            )
        )

        return id
    }

    suspend fun delete(item: Ingredient): Int =
        dao.deleteJoin(item.id!!, item.recipe?.id!!)


    suspend fun delete(item: Recipe): Int =
        dao.deleteFromRecipe(item.id!!)


    /**
     * Update the Step
     * @param item Step
     * @return Int the  number of rows affected
     */
    suspend fun update(item: Ingredient): Int {
        val id = dao.getId(item.id!!, item.recipe!!.id!!)

        return if (id == null) {
            dao.insert(
                RecipeIngredient(
                    null,
                    item.quantity,
                    item.unit.asEntity(),
                    item.optional,
                    item.sortOrder,
                    item.id,
                    item.recipe!!.id!!,
                    item.step?.id
                )
            )
            0
        } else
            dao.update(
                RecipeIngredient(
                    id,
                    item.quantity,
                    item.unit.asEntity(),
                    item.optional,
                    item.sortOrder,
                    item.id,
                    item.recipe!!.id!!,
                    item.step?.id
                )
            )
    }


    /**
     * For the given recipe, delete all joins with the ingredients except the ingredients in params
     * @param recipe Recipe                     the targeted recipe
     * @param ingredients IngredientForRecipe   the ingredients to keep
     * @return Int                              the number of rows affected
     */
    suspend fun deleteAllButThem(
        recipe: Recipe,
        ingredients: List<Ingredient>
    ): Int =
        dao.deleteJoin(recipe.id!!, *ingredients.map { it.id!! }.toLongArray())


}
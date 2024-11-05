package zelgius.com.myrecipes.data.repository

import kotlinx.coroutines.flow.map
import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.RecipeIngredient
import zelgius.com.myrecipes.data.entities.asModel
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.SimpleIngredient
import zelgius.com.myrecipes.data.model.asEntity
import zelgius.com.myrecipes.data.repository.dao.IngredientDao


class IngredientRepository(
    private val dao: IngredientDao
) {
    suspend fun get() = dao.get().map { it.asModel() }

    fun getFlow() = dao.getFlow().map { it.map { i -> i.asModel() } }

    suspend fun get(name: String, imageUrl: String? = null) = dao.get(name, imageUrl)


    /**
     * Insert the IngredientForRecipe (create a RecipeIngredient join). If the id of the ingredient is null, it insert the ingredient too
     * @param item IngredientForRecipe
     * @param recipe Recipe
     * @return Long the id of the inserted item
     */
    suspend fun insert(item: Ingredient, recipe: Recipe): Ingredient {

        val idIngredient = item.idIngredient ?: dao.get(item.name, item.imageUrl)?.id
        ?: dao.insert(IngredientEntity(null, item.name, item.imageUrl))

        val id = dao.insert(
            RecipeIngredient(
                null,
                item.quantity,
                item.unit.asEntity(),
                item.optional,
                item.sortOrder,
                idIngredient,
                recipe.id,
                item.step?.id
            )
        )

        return item.copy(id = id, idIngredient = idIngredient)
    }

    suspend fun delete(item: Ingredient): Int =
        dao.deleteJoin(item.idIngredient!!, item.recipe?.id!!)


    suspend fun delete(item: Recipe): Int =
        dao.deleteFromRecipe(item.id!!)


    /**
     * Update the Step
     * @param item Step
     * @return Int the  number of rows affected
     */
    suspend fun update(item: Ingredient): Ingredient {
        val id = if (item.id == null) {
            dao.insert(
                RecipeIngredient(
                    null,
                    item.quantity,
                    item.unit.asEntity(),
                    item.optional,
                    item.sortOrder,
                    item.idIngredient,
                    item.recipe?.id,
                    item.step?.id
                )
            )
        } else {
            dao.update(
                RecipeIngredient(
                    item.id,
                    item.quantity,
                    item.unit.asEntity(),
                    item.optional,
                    item.sortOrder,
                    item.idIngredient,
                    item.recipe?.id,
                    item.step?.id
                )
            )

            item.id
        }

        return item.copy(id = id)
    }


    /**
     * For the given recipe, delete all joins with the ingredients except the ingredients in params
     * @param recipe Recipe                     the targeted recipe
     * @param ingredients IngredientForRecipe   the ingredients to keep
     * @return Int                              the number of rows affected
     */
    suspend fun deleteAllButThem(
        ingredients: List<Ingredient>,
        recipeId: Long
    ): Int =
        dao.deleteJoin(recipeId = recipeId, *ingredients.map { it.id }.filterNotNull().toLongArray())


    suspend fun getAllWithoutImage() = dao.getAllWithoutImages()

    fun getSimpleIngredients() = dao.getSimpleIngredients().map { it.map { it.asModel() }}

    suspend fun deleteIngredient(id: Long) = dao.delete(id)
    suspend fun update(ingredient: SimpleIngredient) = dao.update(ingredient.asEntity())
}
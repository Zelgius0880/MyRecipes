package zelgius.com.myrecipes.data

import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.entities.StepEntity
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.data.model.asEntity
import zelgius.com.myrecipes.data.repository.dao.StepDao
import javax.inject.Inject


class StepRepository @Inject constructor(
    private val dao: StepDao
) {

    suspend fun get(recipe: RecipeEntity) =
        dao.get(recipe.id!!)


    /**
     * Insert the Step.
     * @param item Step
     * @return Long the id of the inserted item
     */
    suspend fun insert(item: Step): Long =
        dao.insert(item.asEntity())


    /**
     * Update the Step
     * @param item Step
     * @return Int the  umber of rows affected
     */
    suspend fun update(item: Step): Int =
        dao.update(item.asEntity())


    suspend fun delete(item: Step): Int =
        dao.delete(item.asEntity())


    suspend fun delete(item: Recipe): Int =
        dao.deleteFromRecipe(item.id!!)


    /**
     * For the given recipe, delete all steps except the steps in params
     * @param recipe Recipe the targeted recipe
     * @param steps Step    the steps to keep
     * @return Int          the number of rows affected
     */
    suspend fun deleteAllButThem(recipe: Recipe, steps: List<Step>): Int =
        dao.delete(recipe.id!!, *steps.map { it.id!! }.toLongArray())

}
package zelgius.com.myrecipes.data.repository

import zelgius.com.myrecipes.data.entities.asModel
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.asEntity
import zelgius.com.myrecipes.data.repository.dao.IngredientDao
import zelgius.com.myrecipes.data.repository.dao.RecipeDao
import zelgius.com.myrecipes.data.repository.dao.StepDao

class RecipeRepository (
    private val recipeDao: RecipeDao,
    private val stepDao: StepDao,
    private val ingredientDao: IngredientDao
) {


    suspend fun get() = recipeDao.getAll()

    suspend fun getFull(id: Long): Recipe? =
        recipeDao.coroutineGet(id)?.apply {
            steps.addAll(stepDao.get(id))
            ingredients.addAll(ingredientDao.getForRecipe(id))
            ingredients.forEach {
                if (it.refStep != null) {
                    it.step = steps.find { s -> s.id == it.refStep }
                    it.step?.ingredients?.add(it)
                }
            }
        }?.asModel()


    val pagedMeal
        get() = recipeDao.pagedMeal().map { it.asModel() }.asPagingSourceFactory()


    val pagedDessert
        get() = recipeDao.pagedDessert().map { it.asModel() }.asPagingSourceFactory()


    val pagedOther
        get() = recipeDao.pagedOther().map { it.asModel() }.asPagingSourceFactory()


    //TODO Remove as soon as possible
    val pagedMealLegacy
        get() = recipeDao.pagedMeal().map { it.asModel() }.asPagingSourceFactory()


    //TODO Remove as soon as possible
    val pagedDessertLegacy
        get() = recipeDao.pagedDessert().map { it.asModel() }.asPagingSourceFactory()


    //TODO Remove as soon as possible
    val pagedOtherLegacy
        get() = recipeDao.pagedOther().map { it.asModel() }.asPagingSourceFactory()

    fun pagedSearch(name: String) =
        recipeDao.pagedSearch(name).map { it.asModel() }.asPagingSourceFactory()

    suspend fun insert(recipe: Recipe): Long =
        recipeDao.insert(recipe.asEntity())


    suspend fun update(recipe: Recipe): Int =
        recipeDao.update(recipe.asEntity())


    suspend fun delete(recipe: Recipe): Int =
        recipeDao.delete(recipe.asEntity())

}
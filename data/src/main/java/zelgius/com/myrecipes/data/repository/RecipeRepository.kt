package zelgius.com.myrecipes.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.entities.RecipeImageUrlUpdate
import zelgius.com.myrecipes.data.entities.asModel
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.asEntity
import zelgius.com.myrecipes.data.repository.dao.IngredientDao
import zelgius.com.myrecipes.data.repository.dao.RecipeDao
import zelgius.com.myrecipes.data.repository.dao.StepDao
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val stepDao: StepDao,
    private val ingredientDao: IngredientDao
) {


    suspend fun get() = recipeDao.getAll()

    suspend fun getFull(id: Long): Recipe? =
        getFullEntity(id)?.asModel()

    private suspend fun getFullEntity(id: Long): RecipeEntity? =
        recipeDao.get(id)?.apply {
            steps.addAll(stepDao.get(id))
            ingredients.addAll(ingredientDao.getForRecipe(id))
            ingredients.forEach {
                if (it.refStep != null) {
                    it.step = steps.find { s -> s.id == it.refStep }
                    it.step?.ingredients?.add(it)
                }
            }
        }

    fun getFullFlow(id: Long): Flow<Recipe?> =
        recipeDao.getFlow(id).map { entity ->
            if (entity == null) return@map null

            entity.steps.addAll(stepDao.get(id))
            entity.ingredients.addAll(ingredientDao.getForRecipe(id))
            entity.ingredients.forEach {
                if (it.refStep != null) {
                    it.step = entity.steps.find { s -> s.id == it.refStep }
                    it.step?.ingredients?.add(it)
                }
            }

            entity.asModel()
        }


    val pagedMeal
        get() = recipeDao.pagedMeal().map { it.asModel() }.asPagingSourceFactory()


    val pagedDessert
        get() = recipeDao.pagedDessert().map { it.asModel() }.asPagingSourceFactory()


    val pagedOther
        get() = recipeDao.pagedOther().map { it.asModel() }.asPagingSourceFactory()


    suspend fun insert(recipe: Recipe): Long =
        recipeDao.insert(recipe.asEntity())


    suspend fun update(recipe: Recipe): Int =
        recipeDao.update(recipe.asEntity())

    suspend fun update(recipe: RecipeEntity): Int =
        recipeDao.update(recipe)


    suspend fun delete(recipe: Recipe): Int =
        recipeDao.delete(recipe.asEntity())

    suspend fun getBytesForQr(recipe: Recipe): ByteArray {
        val id = recipe.id?: return ByteArray(0)
        val entity = getFullEntity(id)?: return ByteArray(0)

        val bytes = entity.copy(imageURL = null).toProtoBuff().toByteArray()
        // zip bytes
        val baos = ByteArrayOutputStream()
        val zos = ZipOutputStream(baos)
        val entry = ZipEntry("")
        entry.size = bytes.size.toLong()
        zos.putNextEntry(entry)
        zos.write(bytes)
        zos.closeEntry()
        zos.close()

        return baos.toByteArray()
    }

    suspend fun updateUrlImage(id: Long, url: String) = recipeDao.update(RecipeImageUrlUpdate(id, url))
}
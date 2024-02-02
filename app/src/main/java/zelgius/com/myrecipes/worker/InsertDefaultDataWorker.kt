package zelgius.com.myrecipes.worker

import android.content.Context
import android.os.Environment
import android.util.Base64
import androidx.core.content.FileProvider
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import zelgius.com.myrecipes.BuildConfig
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.repository.AppDatabase
import zelgius.com.myrecipes.data.repository.IngredientRepository
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.repository.StepRepository
import zelgius.com.myrecipes.utils.unzip
import zelgius.com.protobuff.RecipeProto
import java.io.File

class InsertDefaultDataWorker(val appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val recipes = arrayOf(
            appContext.getString(R.string.default_1),
            appContext.getString(R.string.default_2),
            appContext.getString(R.string.default_3),
        )

        val ingredientRepository = IngredientRepository(appContext)
        val recipeRepository = RecipeRepository(appContext)
        val stepRepository = StepRepository(appContext)

        runBlocking {
            recipes.forEachIndexed { index, s ->
                val bytes = Base64.decode(s, Base64.NO_PADDING).unzip()

                @Suppress("BlockingMethodInNonBlockingContext")
                val proto =
                    coroutineScope {
                        RecipeProto.Recipe.parseFrom(bytes)
                    }
                val recipe = RecipeEntity(proto)

                recipe.ingredients.forEach {
                    it.id = ingredientRepository.get(it.name)?.id
                    it.step = recipe.steps.find { s -> s == it.step }
                }

                recipe.id = recipeRepository.insert(recipe)

                recipe.steps.forEach {
                    it.refRecipe = recipe.id
                    it.id = stepRepository.insert(it)
                }

                recipe.ingredients.forEach {
                    it.refRecipe = recipe.id
                    it.refStep = it.step?.id

                    if (it.id == null)
                        it.id = ingredientRepository.insert(it, recipe)
                    else
                        ingredientRepository.update(it)
                }

                val dao = AppDatabase.getInstance(appContext).recipeDao
                val targetFile =
                    FileProvider.getUriForFile(
                        appContext,
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        File(
                            appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            "${recipe.id}"
                        )
                    )

                val input = when (index) {
                    0 -> appContext.assets.open("cougnou.jpg")
                    1 -> appContext.assets.open("apple-cake.jpg")
                    2 -> appContext.assets.open("gingerbread.jpg")
                    else -> error("")
                }
                val output = appContext.contentResolver.openOutputStream(targetFile)
                if (output != null) {
                    input.copyTo(output)

                    recipe.apply {
                        imageURL = targetFile.toString()
                        dao.blockingUpdate(recipe)
                    }

                    input.close()
                }
            }
        }

        return Result.success()
    }

}
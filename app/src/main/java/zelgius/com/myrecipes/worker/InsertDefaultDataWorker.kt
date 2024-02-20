package zelgius.com.myrecipes.worker

import android.content.Context
import android.os.Environment
import android.util.Base64
import androidx.core.content.FileProvider
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import zelgius.com.myrecipes.BuildConfig
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.repository.AppDatabase
import zelgius.com.myrecipes.data.IngredientRepository
import zelgius.com.myrecipes.data.RecipeRepository
import zelgius.com.myrecipes.data.StepRepository
import zelgius.com.myrecipes.data.entities.asModel
import zelgius.com.myrecipes.utils.unzip
import zelgius.com.protobuff.RecipeProto
import java.io.File

@HiltWorker
class InsertDefaultDataWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
    private val stepRepository: StepRepository,
) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val recipes = arrayOf(
            context.getString(R.string.default_1),
            context.getString(R.string.default_2),
            context.getString(R.string.default_3),
        )

        recipes.forEachIndexed { index, s ->
            val bytes = Base64.decode(s, Base64.NO_PADDING).unzip()

            @Suppress("BlockingMethodInNonBlockingContext")
            val proto =
                coroutineScope {
                    RecipeProto.Recipe.parseFrom(bytes)
                }
            var recipe = RecipeEntity(proto)

            recipe.ingredients.forEach {
                it.id = ingredientRepository.get(it.name)?.id
                it.step = recipe.steps.find { s -> s == it.step }
            }

            recipe = recipe.copy(id = recipeRepository.insert(recipe.asModel()))

            val steps = recipe.steps.toList()
            recipe.steps.clear()

            recipe.steps.addAll(steps.map {
                it.copy(
                    refRecipe = recipe.id,
                    id = stepRepository.insert(it.asModel(recipe.asModel()))
                )
            })

            val ingredients = recipe.ingredients.toList()
            recipe.ingredients.clear()

            recipe.ingredients.addAll(ingredients.map {
                val id = if (it.id == null)
                    ingredientRepository.insert(it.asModel(recipe.asModel()), recipe.asModel())
                else {
                    ingredientRepository.update(it.asModel(recipe.asModel()))
                    it.id
                }

                it.copy(
                    id = id ,
                    refRecipe = recipe.id,
                    refStep = it.step?.id
                )
            })

            val dao = AppDatabase.getInstance(context).recipeDao
            val targetFile =
                FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    File(
                        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "${recipe.id}"
                    )
                )

            val input = when (index) {
                0 -> context.assets.open("cougnou.jpg")
                1 -> context.assets.open("apple-cake.jpg")
                2 -> context.assets.open("gingerbread.jpg")
                else -> error("")
            }
            val output = context.contentResolver.openOutputStream(targetFile)
            if (output != null) {
                input.copyTo(output)

                recipe = recipe.copy(
                    imageURL = targetFile.toString()
                )
                dao.blockingUpdate(recipe)

                input.close()
            }
        }

        return Result.success()
    }

}
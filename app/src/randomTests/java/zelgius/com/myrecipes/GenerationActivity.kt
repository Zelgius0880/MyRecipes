package zelgius.com.myrecipes

import android.content.Context
import android.os.Bundle
import android.util.Base64
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.zelgius.myrecipes.ia.worker.ImageGenerationWorker
import dagger.Module
import dagger.Provides
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.data.AppDatabase
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.entities.asModel
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import zelgius.com.myrecipes.data.repository.IngredientRepository
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.repository.StepRepository
import zelgius.com.myrecipes.data.repository.dao.IngredientDao
import zelgius.com.myrecipes.data.repository.dao.RecipeDao
import zelgius.com.myrecipes.data.repository.dao.StepDao
import zelgius.com.myrecipes.data.useCase.SaveRecipeUseCase
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.utils.unzip
import zelgius.com.myrecipes.worker.WorkerRepository
import zelgius.com.protobuff.RecipeProto
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@AndroidEntryPoint
class GenerationActivity : AppCompatActivity() {

    @Inject
    @Named("test")
    lateinit var recipeRepository: RecipeRepository

    @Inject
    @Named("test")
    lateinit var stepRepository: StepRepository

    @Inject
    @Named("test")
    lateinit var ingredientRepository: IngredientRepository

    @Inject
    @Named("test")
    lateinit var database: AppDatabase

    @Inject
    @Named("test")
    lateinit var workerRepository: WorkerRepository

    val workerStateFlow = MutableStateFlow<WorkInfo.State?>(null)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val worker = OneTimeWorkRequestBuilder<TestImageGenerationWorker>()
            .setInputData(
                Data.Builder().build()
            )
            /*.setConstraints(Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiresDeviceIdle(true)
                .build()
            )*/
            .build()

        lifecycleScope.launch {
            val userCase = SaveRecipeUseCase(
                recipeRepository, stepRepository, ingredientRepository, workerRepository, database
            )

            userCase.execute(
                getFromQr().copy(imageUrl = null, name = "Gateau aux pommes")
                    .let {
                        it.copy(ingredients = it.ingredients.map { i ->
                            if (i.imageUrl.isNullOrBlank()) {
                                i.copy(
                                    name = when (it.name) {
                                        "Baking" -> "Noisette"
                                        "Bicaronate" -> "Citron"
                                        else -> "Fraise"
                                    }
                                )
                            } else i
                        })
                    })

            WorkManager.getInstance(this@GenerationActivity).apply {
                cancelAllWork()
                enqueue(worker)
            }

            WorkManager.getInstance(this@GenerationActivity).getWorkInfoByIdFlow(worker.id)
                .collect {
                    workerStateFlow.value = it?.state
                }
        }


        setContent {
            val workerState by workerStateFlow.collectAsState()
            val result by WorkManager.getInstance(this@GenerationActivity)
                .getWorkInfoByIdFlow(worker.id)
                .mapNotNull {
                    it?.outputData?.getStringArray(ImageGenerationWorker.RESULT_KEY)?.toList()
                }
                .collectAsState(emptyList<List<String>>())


            AppTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        "Generation Tester",
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onPrimary)
                    )
                    Text(
                        when (workerState) {
                            null -> "Not started"
                            else -> workerState.toString()
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onPrimary),
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(result) {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .fillMaxWidth()
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(it)
                                        .crossfade(true)
                                        .build(),
                                    error = painterResource(R.drawable.ic_dish),
                                    placeholder = painterResource(R.drawable.ic_dish),
                                    contentDescription = null,
                                    contentScale = ContentScale.Inside,
                                    modifier = Modifier
                                        .fillMaxSize()
                                )
                            }
                        }
                    }
                }


            }
        }
    }


    companion object {
        fun getFromQr(base64: String = DEFAULT_BASE_64): Recipe {
            val bytes = Base64.decode(base64, Base64.NO_PADDING).unzip()
            val proto = RecipeProto.Recipe.parseFrom(bytes)

            val recipe = RecipeEntity(proto)

            recipe.ingredients.forEach {
                it.step = recipe.steps.find { s -> s == it.step }
            }

            val steps = recipe.steps.toList()
            recipe.steps.clear()
            recipe.steps.addAll(steps.map {
                it.copy(
                    refRecipe = recipe.id,
                )
            })

            val ingredients = recipe.ingredients.toList()
            recipe.ingredients.clear()

            recipe.ingredients.addAll(ingredients)

            return recipe.asModel()
        }

        const val DEFAULT_BASE_64 =
            "UEsDBBQACAgIABlxalEAAAAAAAAAAAAAAAAAAAAAjY/PSsNAEIfTpik1osR4UHrQIV40IE3Bqngx\n" + "f9CbIAgelY3drkvjJuxu0MfyGXwiH8HdNJD2kODedmZ+33xj21FRZBgStMSHhndg7z/QL5DvGCgj\n" + "HM8pZlI4Pe/EPk5KWTWQDgjVl7n6UwZCcloIp+959lGsOIAkTC8D+PmGBBY5h4vgfBbAB2WO6Udb\n" + "hn7ha+ha91le8rEz5+gTpRm+mUwWugKG3+YRDK8N/3GFMJzQHVX2p+Jsg1IZKsp/pANTEf2a+BK6\n" + "w7iUEvPx3hovrUpgVNujZtZ6KgnaPEDoCvS6DwhrxEituyNE6++uMTAh0G8naOGgJvzeKmG0VANg\n" + "ticGKjFrEtsxfUM8zRmSGAbdsasmtvOMGM0yBKsbrU7DP1BLBwg/FjLQIQEAAFgCAABQSwECFAAU\n" + "AAgICAAZcWpRPxYy0CEBAABYAgAAAAAAAAAAAAAAAAAAAAAAAAAAUEsFBgAAAAABAAEALgAAAE8B\n" + "AAAAAA"
    }
}


@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Provides
    @Singleton
    @Named("test")
    fun database(@ApplicationContext context: Context) = AppDatabase.getInstance(context, true)

    @Provides
    @Singleton
    @Named("test")
    fun ingredientDao(@Named("test") appDatabase: AppDatabase) = appDatabase.ingredientDao

    @Provides
    @Singleton
    @Named("test")
    fun stepDao(@Named("test") appDatabase: AppDatabase) = appDatabase.stepDao

    @Provides
    @Singleton
    @Named("test")
    fun recipeDao(@Named("test") appDatabase: AppDatabase) = appDatabase.recipeDao

    @Provides
    @Singleton
    @Named("test")
    fun recipeRepository(
        @Named("test") recipeDao: RecipeDao,
        @Named("test") stepDao: StepDao,
        @Named("test") ingredientDao: IngredientDao
    ) = RecipeRepository(recipeDao, stepDao, ingredientDao)

    @Provides
    @Singleton
    @Named("test")
    fun stepRepository(@Named("test") stepDao: StepDao) = StepRepository(stepDao)

    @Provides
    @Singleton
    @Named("test")
    fun ingredientRepository(@Named("test") ingredientDao: IngredientDao) =
        IngredientRepository(ingredientDao)

    @Provides
    @Singleton
    @Named("test")
    fun workerRepository(@ApplicationContext context: Context) =
        WorkerRepository(context, DataStoreRepository(context))
}

@HiltWorker
class TestImageGenerationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    @Named("test") recipeDao: RecipeDao,
    @Named("test") ingredientDao: IngredientDao, dataStoreRepository: DataStoreRepository
) : ImageGenerationWorker(
    context, params, recipeDao, ingredientDao, dataStoreRepository
)
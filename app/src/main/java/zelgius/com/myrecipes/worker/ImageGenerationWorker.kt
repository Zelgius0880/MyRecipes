package zelgius.com.myrecipes.worker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Environment.DIRECTORY_PICTURES
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.mediapipe.framework.image.BitmapExtractor
import com.google.mediapipe.tasks.vision.imagegenerator.ImageGenerator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import zelgius.com.myrecipes.BuildConfig
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import zelgius.com.myrecipes.data.repository.dao.IngredientDao
import zelgius.com.myrecipes.data.repository.dao.RecipeDao
import java.io.File
import kotlin.math.abs
import kotlin.random.Random

@HiltWorker
open class ImageGenerationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val dataStoreRepository: DataStoreRepository,
) : CoroutineWorker(context, params) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager


    private val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        .setContentTitle(context.getString(R.string.channel_name))
        .setTicker(context.getString(R.string.channel_name))
        .setSmallIcon(R.drawable.art_track_24px)
        .setOnlyAlertOnce(true)
        .setOngoing(true)
        .addAction(
            android.R.drawable.ic_delete,
            context.getString(R.string.cancel),
            WorkManager.getInstance(context)
                .createCancelPendingIntent(id)
        )

    val results = mutableListOf<String>()

    override suspend fun doWork(): Result {
        val options = ImageGenerator.ImageGeneratorOptions.builder()
            .setImageGeneratorModelDirectory(IA_MODEL_PATH)
            .build()

        setForeground(createForegroundInfo(createNotification("",0, 100, true, null)))

        val recipes = recipeDao.getAllWithoutImages()
        val ingredients = ingredientDao.getAllWithoutImages()

        val imageGenerator = ImageGenerator.createFromOptions(context, options)

        val maxProgress = ITERATION_COUNT * (recipes.size + ingredients.size)


        try {
            generateRecipes(recipes, imageGenerator, maxProgress)
            generateIngredients(ingredients, imageGenerator, maxProgress)

            dataStoreRepository.setStillNeedToGenerate(false)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        } finally {
            imageGenerator.close()
        }

        return Result.success(
            Data.Builder()
                .putStringArray(RESULT_KEY, results.toTypedArray())
                .build()
        )
    }

    private suspend fun generateRecipes(
        recipes: List<RecipeEntity>,
        imageGenerator: ImageGenerator,
        maxProgress: Int,
    ) {
        recipes.forEachIndexed { index, r ->
            Log.i(TAG, "Generating image for ${r.name}")
            generate(
                imageGenerator, context.getString(
                    R.string.recipe_prompt_generation,
                    r.name
                )
            ) { iteration, bitmap ->
                val progress = ITERATION_COUNT * index + iteration
                if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        createNotification(r.name, progress, maxProgress, false, bitmap)
                    )
                Log.i(TAG, "Progress $progress / $maxProgress")
            }?.let {
                val fileName = save(it, "R_${r.id}")
                recipeDao.update(r.copy(imageURL = fileName))
                results.add(fileName)
                setProgress(
                    Data.Builder()
                        .putStringArray(RESULT_KEY, results.toTypedArray())
                        .build()
                )
            }
        }
    }


    private suspend fun generateIngredients(
        ingredients: List<IngredientEntity>,
        imageGenerator: ImageGenerator,
        maxProgress: Int,
    ) {
        val recipeSize = maxProgress - ingredients.size * ITERATION_COUNT

        ingredients.forEachIndexed { index, i ->
            Log.i(TAG, "Generating image for ${i.name}")
            generate(
                imageGenerator, context.getString(
                    R.string.ingrient_prompt_generation,
                    i.name
                )
            ) { iteration, bitmap ->
                val progress = recipeSize + index * ITERATION_COUNT + iteration
                if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        createNotification(i.name, progress, maxProgress, false, bitmap)
                    )
                Log.i(TAG, "Progress $progress / $maxProgress")
            }?.let {
                val fileName = save(it, "R_${i.id}")
                ingredientDao.update(i.copy(imageURL = fileName))
                results.add(fileName)
                setProgress(
                    Data.Builder()
                        .putStringArray(RESULT_KEY, results.toTypedArray())
                        .build()
                )
            }
        }
    }

    private fun generate(
        imageGenerator: ImageGenerator,
        prompt: String,
        seed: Int = abs(Random.nextInt()),
        onUpdate: (iteration: Int, bitmap: Bitmap?) -> Unit
    ): Bitmap? {
        var bitmap: Bitmap? = null
        var displayedBmp: Bitmap? = null
        Log.i(TAG, "Prompt: $prompt")

        imageGenerator.setInputs(prompt, ITERATION_COUNT, seed)

        for (i in 0 until ITERATION_COUNT) {
            Log.i(TAG, "Iteration $i/$ITERATION_COUNT")
            val result = imageGenerator.execute(true)

            bitmap = result?.generatedImage()?.let {
                BitmapExtractor.extract(it)
            } ?: BLANK_BITMAP

            if (i % 5 == 0) displayedBmp = bitmap
            onUpdate(i, displayedBmp)
        }
        onUpdate(ITERATION_COUNT - 1, bitmap)

        return bitmap
    }

    private fun save(
        bitmap: Bitmap,
        fileName: String
    ): String {
        val targetFile =
            FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".fileprovider",
                File(
                    context.getExternalFilesDir(DIRECTORY_PICTURES),
                    fileName
                )
            )

        val output = context.contentResolver.openOutputStream(targetFile)

        if (output != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }

        output?.close()

        Log.i(TAG, "Saved image to $targetFile")
        return targetFile.toString()
    }

    private fun createForegroundInfo(notification: Notification): ForegroundInfo {
        createChannel()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(
                NOTIFICATION_ID, notification,
            )
        }
    }

    private fun createNotification(
        name: String,
        progress: Int = 0,
        max: Int = 100,
        indeterminate: Boolean = false,
        bitmap: Bitmap?
    ): Notification {
        return notificationBuilder
            .setProgress(max, progress, indeterminate)
            .setLargeIcon(bitmap)
            .setSubText(name)
            .setOngoing(false)
            .build()
    }

    private fun createChannel() {
        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = descriptionText

        notificationManager.createNotificationChannel(mChannel)
    }

    companion object {
        const val IA_MODEL_PATH = "/data/local/tmp/image_generator/bins/"
        private const val NOTIFICATION_ID = 42
        private const val CHANNEL_ID = "Generation"
        private const val ITERATION_COUNT = 20

        private const val TAG = "ImageGenerationWorker"
        const val RESULT_KEY = "results"


        val BLANK_BITMAP
            get() = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
                .apply {
                    val canvas = Canvas(this)
                    val paint = Paint()
                    paint.color = Color.WHITE
                    canvas.drawPaint(paint)
                }

        val modelExists get() = File(IA_MODEL_PATH).exists()
    }
}
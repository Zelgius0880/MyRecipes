package com.zelgius.myrecipes.ia.worker

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
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import com.google.mediapipe.framework.image.BitmapExtractor
import com.google.mediapipe.tasks.vision.imagegenerator.ImageGenerator
import com.zelgius.myrecipes.ia.R
import dagger.assisted.Assisted
import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.logger.Logger
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import zelgius.com.myrecipes.data.repository.dao.IngredientDao
import zelgius.com.myrecipes.data.repository.dao.RecipeDao
import java.io.File
import kotlin.math.abs
import kotlin.random.Random

@HiltWorker
open class ImageGenerationWorker @dagger.assisted.AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: androidx.work.WorkerParameters,
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
            R.drawable.baseline_close_24,
            context.getString(R.string.cancel),
            WorkManager.Companion.getInstance(context)
                .createCancelPendingIntent(id)
        )

    val results = mutableListOf<String>()

    override suspend fun doWork(): Result {

        var imageGenerator: ImageGenerator? = null
        try {
            val options = ImageGenerator.ImageGeneratorOptions.builder()
                .setImageGeneratorModelDirectory(IA_MODEL_PATH)
                .build()

            setForeground(createForegroundInfo(createNotification("", 0, 100, true, null)))

            val recipes = recipeDao.getAllWithoutImages()
            val ingredients = ingredientDao.getAllWithoutImages()
            imageGenerator = ImageGenerator.createFromOptions(context, options)

            val maxProgress = ITERATION_COUNT * (recipes.size + ingredients.size)

            generateRecipes(recipes, imageGenerator, maxProgress)
            generateIngredients(ingredients, imageGenerator, maxProgress)

            dataStoreRepository.setStillNeedToGenerate(false)
        } catch (e: Exception) {
            e.printStackTrace()

            return Result.failure()
        } finally {
            imageGenerator?.close()
        }

        return Result.success(
            androidx.work.Data.Builder()
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
            Logger.i("Generating image for ${r.name}")
            val seed = abs(Random.nextInt())
            generate(
                imageGenerator, context.getString(
                    R.string.recipe_prompt_generation,
                    r.name.lowercase()
                ), seed
            ) { iteration, bitmap ->
                val progress = ITERATION_COUNT * index + iteration
                notify(r.name, progress, maxProgress, bitmap)
                Logger.i("Progress $progress / $maxProgress")
            }?.let {
                val fileName = save(it, "R_${r.id}")
                recipeDao.update(r.copy(imageURL = fileName, seed = seed))
                results.add(fileName)
                setProgress(
                    androidx.work.Data.Builder()
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
            Logger.i("Generating image for ${i.name}")
            val seed = abs(Random.nextInt())
            generate(
                imageGenerator, context.getString(
                    R.string.ingredient_prompt_generation,
                    i.prompt?.takeIf { it.isNotBlank() } ?: i.name.lowercase()
                ), seed
            ) { iteration, bitmap ->
                val progress = recipeSize + index * ITERATION_COUNT + iteration
                notify(i.name, progress, maxProgress, bitmap)
                Logger.i("Progress $progress / $maxProgress")
            }?.let {
                val fileName = save(it, "I_${i.id}")
                ingredientDao.update(i.copy(imageURL = fileName, seed = seed))
                results.add(fileName)
                setProgress(
                    androidx.work.Data.Builder()
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
        Logger.i("Prompt: $prompt")

        imageGenerator.setInputs(prompt, ITERATION_COUNT, seed)

        for (i in 0 until ITERATION_COUNT) {
            Logger.i("Iteration $i/$ITERATION_COUNT")
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
                "${context.packageName}.fileprovider",
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

        Logger.i("Saved image to $targetFile")
        return targetFile.toString()
    }

    private fun createForegroundInfo(notification: Notification): androidx.work.ForegroundInfo {
        createChannel()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            androidx.work.ForegroundInfo(
                NOTIFICATION_ID,
                notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            androidx.work.ForegroundInfo(
                NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            androidx.work.ForegroundInfo(
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

    private fun notify(
        name: String,
        progress: Int,
        maxProgress: Int,
        bitmap: Bitmap?
    ) {
        if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
            notificationManager.notify(
                NOTIFICATION_ID,
                createNotification(name, progress, maxProgress, false, bitmap)
            )
    }

    companion object {
        const val IA_MODEL_PATH = "/data/local/tmp/image_generator/bins/"
        private const val NOTIFICATION_ID = 42
        private const val CHANNEL_ID = "Generation"
        private const val ITERATION_COUNT = 20

        const val TAG = "ImageGenerationWorker"
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

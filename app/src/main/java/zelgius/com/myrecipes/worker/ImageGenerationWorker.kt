package zelgius.com.myrecipes.worker

import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment.DIRECTORY_PICTURES
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.mediapipe.framework.image.BitmapExtractor
import com.google.mediapipe.tasks.vision.imagegenerator.ImageGenerator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import zelgius.com.myrecipes.BuildConfig
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.repository.dao.RecipeDao
import java.io.File

@HiltWorker
class ImageGenerationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val recipeDao: RecipeDao
) : CoroutineWorker(context, params) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager


    override suspend fun doWork(): Result {
        /*val options = ImageGenerator.ImageGeneratorOptions.builder()
            .setImageGeneratorModelDirectory(MODEL_PATH)
            .build()
        setForeground(createForegroundInfo())

        val imageGenerator = ImageGenerator.createFromOptions(context, options)

        recipeDao.getAllWithoutImages().forEach {
            val result = imageGenerator.generate(prompt, iteration, seed)
            val bitmap = BitmapExtractor.extract(result?.generatedImage())

            val targetFile =
                FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    File(
                        context.getExternalFilesDir(DIRECTORY_PICTURES),
                        "${it.id}"
                    )
                )

            val output = context.contentResolver.openOutputStream(targetFile)

            if(output != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)

                recipeDao.update(it.copy(imageURL = targetFile.toString()))
            }

            output?.close()
        }
*/
        return Result.success()
    }

/*private fun createForegroundInfo(): ForegroundInfo {
    val id = context.getString(R.string.notification_channel_id)
    val title = context.getString(R.string.notification_title)
    val cancel = context.getString(R.string.cancel_download)
    // This PendingIntent can be used to cancel the worker
    val intent = WorkManager.getInstance(context)
        .createCancelPendingIntent(getId())

    createChannel()


    val notification = NotificationCompat.Builder(applicationContext, id)
        .setContentTitle(title)
        .setTicker(title)
        .setContentText(progress)
        .setSmallIcon(R.drawable.art_track_24px)
        .setOngoing(true)
        // Add the cancel action to the notification which can
        // be used to cancel the worker
        .addAction(android.R.drawable.ic_delete, cancel, intent)
        .build()

    return ForegroundInfo(NOTIFICATION_ID, notification)
}*/

private fun createChannel() {
    // TODO Create a Notification channel
}

companion object {
    const val MODEL_PATH = "/data/local/tmp/image_generator/bins/"
    private const val NOTIFICATION_ID = 42
}
}
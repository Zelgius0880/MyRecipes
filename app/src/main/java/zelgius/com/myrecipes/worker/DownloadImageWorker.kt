package zelgius.com.myrecipes.worker

import android.content.Context
import android.os.Environment.DIRECTORY_PICTURES
import android.webkit.URLUtil
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zelgius.com.myrecipes.BuildConfig
import zelgius.com.myrecipes.data.AppDatabase
import java.io.File
import java.net.URL

class DownloadImageWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        try {
            val dao = AppDatabase.getInstance(appContext).recipeDao

            val url = inputData.getString("URL")
            val input = if (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) {
                // Download Image from URL
                withContext(Dispatchers.IO) {
                    URL(url).openStream()
                }
            } else if (URLUtil.isContentUrl(url)) {
                // Get image from content resolver
                url?.toUri()?.let {
                    appContext.contentResolver.openInputStream(it)
                }
            } else
                return Result.failure()

            val recipeId = inputData.getLong("ID", 0L)
            val targetFile =
                FileProvider.getUriForFile(
                    appContext,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    File(
                        appContext.getExternalFilesDir(DIRECTORY_PICTURES),
                        "R_$recipeId"
                    )
                )

            val output = appContext.contentResolver.openOutputStream(targetFile)
            if (input != null && output != null) {
                input.copyTo(output)

                dao.get(recipeId)?.copy(
                    imageURL = targetFile.toString()
                )?.let {
                    dao.update(it)
                }

                withContext(Dispatchers.IO) {
                    input.close()
                }
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Result.failure()
    }

}
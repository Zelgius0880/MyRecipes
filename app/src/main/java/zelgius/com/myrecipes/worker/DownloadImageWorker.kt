package zelgius.com.myrecipes.worker

import android.content.Context
import android.os.Environment.DIRECTORY_PICTURES
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import zelgius.com.myrecipes.TAG
import zelgius.com.myrecipes.repository.AppDatabase
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class DownloadImageWorker(val appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        try {
            val dao = AppDatabase.getInstance(appContext).recipeDao
            val inputStream =
                URL(inputData.getString("URL")).openStream()   // Download Image from URL

            val recipeId = inputData.getLong("ID", 0L)
            val targetFile = File(appContext.getExternalFilesDir(DIRECTORY_PICTURES), "$recipeId")

            Files.copy(
                inputStream,
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )

            val recipe = dao.blockingGet(recipeId)
            recipe?.apply {
                imageURL = targetFile.toURI().toString()
                dao.blockingUpdate(recipe)
            }

            inputStream.close()

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Result.failure()
    }

}
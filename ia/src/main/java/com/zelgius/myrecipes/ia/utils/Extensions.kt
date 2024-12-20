package com.zelgius.myrecipes.ia.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment.DIRECTORY_PICTURES
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.work.ListenableWorker
import com.zelgius.myrecipes.ia.R
import zelgius.com.myrecipes.data.logger.Logger
import java.io.File

fun Context.save(bitmap: Bitmap, fileName: String): String {
    val targetFile =
        FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            File(
                getExternalFilesDir(DIRECTORY_PICTURES),
                fileName
            )
        )

    val output = contentResolver.openOutputStream(targetFile)

    if (output != null) {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }

    output?.close()

    Logger.i("Saved image to $targetFile")
    return targetFile.toString()
}

fun ListenableWorker.notificationBuilder(context: Context, channelId: String, pendingIntent: PendingIntent): NotificationCompat.Builder {
    val name = context.getString(R.string.channel_name)
    val descriptionText = context.getString(R.string.channel_description)
    val importance = NotificationManager.IMPORTANCE_NONE
    val mChannel = NotificationChannel(channelId, name, importance)
    mChannel.description = descriptionText

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
    notificationManager.createNotificationChannel(mChannel)

    return NotificationCompat.Builder(applicationContext, channelId)
        .setContentTitle(context.getString(R.string.channel_name))
        .setTicker(context.getString(R.string.channel_name))
        .setSmallIcon(R.drawable.art_track_24px)
        .setOnlyAlertOnce(true)
        .setOngoing(true)
        .addAction(
            R.drawable.baseline_close_24,
            context.getString(R.string.cancel),
            pendingIntent
        )
}
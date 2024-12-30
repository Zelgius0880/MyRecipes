package com.zelgius.myrecipes.ia.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment.DIRECTORY_PICTURES
import androidx.core.content.FileProvider
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

    contentResolver.delete(targetFile, null, null)
    val output = contentResolver.openOutputStream(targetFile)

    if (output != null) {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }

    output?.close()

    Logger.i("Saved image to $targetFile")
    return targetFile.toString()
}

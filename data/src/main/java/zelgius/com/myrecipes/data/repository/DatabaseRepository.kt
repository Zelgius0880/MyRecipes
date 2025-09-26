package zelgius.com.myrecipes.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zelgius.com.myrecipes.data.AppDatabase
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject


class DatabaseRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val db: AppDatabase
) {
    companion object {
        const val SQLITE_WALFILE_SUFFIX = "-wal"
        const val SQLITE_SHMFILE_SUFFIX = "-shm"
    }

    suspend fun backupDatabase(outputStream: OutputStream): Boolean = withContext(Dispatchers.IO) {
        val zipOutputStream = ZipOutputStream(outputStream)
        val dbFile = context.getDatabasePath(AppDatabase.DATABASE)
        val dbWalFile = File(dbFile.path + SQLITE_WALFILE_SUFFIX)
        val dbShmFile = File(dbFile.path + SQLITE_SHMFILE_SUFFIX)

        //checkpoint()

        try {
            zipOutputStream.use {
                it.addFile(dbFile)
                it.addFile(dbWalFile)
                it.addFile(dbShmFile)
                true
            }
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     *  Restore the database and then restart the App
     */
    suspend fun restoreDatabase(backup: InputStream): Boolean =
        withContext(Dispatchers.IO) {
            val dbpath = db.openHelper.readableDatabase.path ?: return@withContext false

            val dbFile = File(dbpath)

            val zipMemoryBackup = ByteArrayOutputStream().apply {
                backupDatabase(this)
            }

            val files = listOf(
                dbFile,
                File(dbFile.path + SQLITE_WALFILE_SUFFIX),
                File(dbFile.path + SQLITE_SHMFILE_SUFFIX)
            ).filter { it.exists() }

            fun restore(inputStream: InputStream) {
                inputStream.processZipFile { entry, inputStream ->
                    files.find { it.name == entry.name }?.let {
                        inputStream.copyTo(it.outputStream())
                    }
                }
            }

            try {
                restore(backup)
                checkpoint()

                true
            } catch (e: IOException) {
                e.printStackTrace()
                restore(ByteArrayInputStream(zipMemoryBackup.toByteArray()))
                return@withContext false
            }
        }

    @Throws(IOException::class)
    fun ZipOutputStream.addFile(file: File, entryName: String = file.name) {
        val buffer = ByteArray(1024)
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(file)
            val entry = ZipEntry(entryName)
            putNextEntry(entry)
            var length: Int
            while (fis.read(buffer).also {
                    length = it
                } > 0) {
                write(buffer, 0, length)
            }
            closeEntry()
        } catch (e: Exception) {
            throw IOException(e)
        } finally {
            fis?.close()
        }
    }

    private fun checkpoint() {
        val db = db.openHelper.writableDatabase
        db.query("PRAGMA wal_checkpoint(FULL);")
        db.query("PRAGMA wal_checkpoint(TRUNCATE);")
    }

    @Throws(IOException::class)
    fun InputStream.processZipFile(
        processEntry: (ZipEntry, ZipInputStream) -> Unit
    ) {
        ZipInputStream(this).use { zipInputStream ->
            var zipEntry = zipInputStream.nextEntry
            while (zipEntry != null) {
                processEntry(zipEntry, zipInputStream)
                zipEntry = zipInputStream.nextEntry
            }
        }
    }

}
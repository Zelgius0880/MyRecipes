package zelgius.com.myrecipes.filepicker.utils

import android.content.Context
import android.content.pm.PackageManager
import zelgius.com.myrecipes.filepicker.model.FileListItem
import java.io.File
import java.util.ArrayList
import java.util.Collections

/**
 * @author akshay sunil masram
 */
class Utility {

    private fun hasSupportLibraryInClasspath(): Boolean {
        try {
            Class.forName("com.android.support:appcompat-v7")
            return true
        } catch (ex: ClassNotFoundException) {
            ex.printStackTrace()
        }

        return false
    }

    companion object {

        fun checkStorageAccessPermissions(context: Context): Boolean {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val permission = "android.permission.READ_EXTERNAL_STORAGE"
                val res = context.checkCallingOrSelfPermission(permission)
                return res == PackageManager.PERMISSION_GRANTED
            } else {
                return true
            }
        }

        fun prepareFileListEntries(
            internalList: MutableList<FileListItem>,
            inter: File,
            filter: ExtensionFilter
        ): List<FileListItem> {
            var internalList = internalList
            try {

                for (name in inter.listFiles(filter)!!) {
                    if (name.canRead()) {
                        val item = FileListItem()
                        item.filename = name.name
                        item.isDirectory = name.isDirectory
                        item.location = name.absolutePath
                        item.time = name.lastModified()
                        internalList.add(item)
                    }
                }
                internalList.sort()
            } catch (e: NullPointerException) {
                e.printStackTrace()
                internalList = ArrayList()
            }

            return internalList
        }
    }
}

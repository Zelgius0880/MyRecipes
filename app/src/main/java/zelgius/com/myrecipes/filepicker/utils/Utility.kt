package zelgius.com.myrecipes.filepicker.utils

import android.content.Context
import android.content.pm.PackageManager
import zelgius.com.myrecipes.filepicker.model.FileListItem
import java.io.File
import java.util.*

/**
 * @author akshay sunil masram
 */
class Utility {


    companion object {

        fun checkStorageAccessPermissions(context: Context): Boolean {
            val permission = "android.permission.READ_EXTERNAL_STORAGE"
            val res = context.checkCallingOrSelfPermission(permission)
            return res == PackageManager.PERMISSION_GRANTED
        }

        fun prepareFileListEntries(
            internalList: MutableList<FileListItem>,
            inter: File,
            filter: ExtensionFilter
        ): List<FileListItem> {
            var list = internalList
            try {

                for (name in inter.listFiles(filter)!!) {
                    if (name.canRead()) {
                        val item = FileListItem()
                        item.filename = name.name
                        item.isDirectory = name.isDirectory
                        item.location = name.absolutePath
                        item.time = name.lastModified()
                        list.add(item)
                    }
                }
                list.sort()
            } catch (e: NullPointerException) {
                e.printStackTrace()
                list = ArrayList()
            }

            return list
        }
    }
}

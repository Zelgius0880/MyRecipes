package zelgius.com.myrecipes.filepicker.utils

import zelgius.com.myrecipes.filepicker.model.DialogConfigs
import zelgius.com.myrecipes.filepicker.model.DialogProperties
import java.io.File
import java.io.FileFilter
import java.util.*

/**
 * @author akshay sunil masram
 */
class ExtensionFilter(properties: DialogProperties) : FileFilter {
    private var validExtensions: List<String>? = null
    private val properties: DialogProperties
    override fun accept(file: File): Boolean {
        if (file.isDirectory && file.canRead()) {
            return true
        } else if (properties.selectionType == DialogConfigs.DIR_SELECT) {
            return false
        } else {
            val name = file.name.toLowerCase(Locale.getDefault())
            for (ext in validExtensions!!) {
                if (name.endsWith(ext)) {
                    return true
                }
            }
        }
        return false
    }

    init {
        validExtensions = if (properties.extensions != null) {
            properties.extensions
        } else {
            ArrayList()
        }
        this.properties = properties
    }
}
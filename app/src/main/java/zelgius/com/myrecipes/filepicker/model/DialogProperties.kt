package zelgius.com.myrecipes.filepicker.model

import java.io.File

/**
 * @author akshay sunil masram
 */
data class DialogProperties (
    var selectionMode: Int = DialogConfigs.SINGLE_MODE,
    var selectionType: Int = DialogConfigs.FILE_SELECT,
    var root: File = File(DialogConfigs.DEFAULT_DIR),
    var errorDir: File = File(DialogConfigs.DEFAULT_DIR),
    var offset: File = File(DialogConfigs.DEFAULT_DIR),

) {
    @JvmField
    var extensions: List<String>? = null
/*
    constructor(selectionMode: Int, selectionType: Int, root: File, errorDir: File, offset: File, extensions: List<String>)
            : this(selectionMode, selectionType, root, errorDir, offset) {
        this.extensions = extensions
    }*/
}
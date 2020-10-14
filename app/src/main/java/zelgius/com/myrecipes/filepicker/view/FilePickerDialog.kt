package zelgius.com.myrecipes.filepicker.view

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager

import zelgius.com.myrecipes.filepicker.controller.adapters.FileListAdapter
import zelgius.com.myrecipes.filepicker.model.DialogConfigs
import zelgius.com.myrecipes.filepicker.model.DialogProperties
import zelgius.com.myrecipes.filepicker.model.FileListItem
import zelgius.com.myrecipes.filepicker.model.MarkedItemList
import zelgius.com.myrecipes.filepicker.utils.ExtensionFilter
import zelgius.com.myrecipes.filepicker.utils.Utility
import zelgius.com.myrecipes.filepicker.widget.MaterialCheckbox
import kotlinx.android.synthetic.main.dialog_file_picker.*
import kotlinx.android.synthetic.main.dialog_file_picker.view.*
import zelgius.com.myrecipes.R
import java.io.File
import java.util.ArrayList

/**
 * @author akshay sunil masram
 */
class FilePickerDialog(private val  titleStr: String? = null) : DialogFragment() {

    constructor(titleStr: String?, properties: DialogProperties): this(titleStr) {
        this.properties = properties
        filter = ExtensionFilter(properties)
        internalList = ArrayList()
    }


    private lateinit var contentView: View
    private val ctx by lazy {activity!!}
    private var properties: DialogProperties = DialogProperties()
    private var callbacks: ((Array<String>)->Unit)? = null
    private var internalList: MutableList<FileListItem> = mutableListOf()
    private var filter: ExtensionFilter =  ExtensionFilter(properties)
    private var mFileListAdapter: FileListAdapter? = null
    private var select: Button? = null
    private var positiveBtnNameStr: String? = null
    private var negativeBtnNameStr: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        contentView = View.inflate(ctx, R.layout.dialog_file_picker, null)

        return AlertDialog.Builder(ctx)
            .setView(contentView)
            .setPositiveButton(R.string.choose_button_label) {_,_ ->}
            .setNegativeButton(R.string.cancel_button_label) {_,_ ->}
            .setTitle(titleStr?: "")
            .setOnKeyListener { _,  keyCode,  event ->
                if (keyCode == KeyEvent.KEYCODE_BACK &&
                    event.action == KeyEvent.ACTION_UP &&
                    !event.isCanceled) {
                    onBackPressed()
                } else false
            }

            .create().apply {
                setOnShowListener {
                    val cancel = getButton(AlertDialog.BUTTON_NEGATIVE)
                    select = getButton(AlertDialog.BUTTON_POSITIVE)
                    val size = MarkedItemList.getFileCount()
                    if (size == 0) {
                        select!!.isEnabled = false
                    }

                    select!!.setOnClickListener {
                        val paths = MarkedItemList.getSelectedPaths()
                            callbacks?.invoke(paths)
                        dismiss()
                    }

                    cancel.setOnClickListener { cancel() }

                    if (negativeBtnNameStr != null) {
                        cancel.text = negativeBtnNameStr
                    }

                    mFileListAdapter = FileListAdapter(internalList, context, properties)
                    mFileListAdapter!!.setNotifyItemCheckedListener {
                        positiveBtnNameStr = if (positiveBtnNameStr == null)
                            context.getString(R.string.choose_button_label)
                        else
                            positiveBtnNameStr
                        val size = MarkedItemList.getFileCount()
                        if (size == 0) {
                            select!!.isEnabled = false
                            select!!.text = positiveBtnNameStr
                        } else {
                            select!!.isEnabled = true
                            val buttonLabel = "$positiveBtnNameStr ($size) "
                            select!!.text = buttonLabel
                        }
                        if (properties.selectionMode == DialogConfigs.SINGLE_MODE) {
                            /*  If a single file has to be selected, clear the previously checked
                                 *  checkbox from the list.
                                 */
                            mFileListAdapter!!.notifyDataSetChanged()
                        }
                    }
                    positiveBtnNameStr = if (positiveBtnNameStr == null)
                        getString(R.string.choose_button_label)
                    else
                        positiveBtnNameStr
                    select!!.text = positiveBtnNameStr
                    if (Utility.checkStorageAccessPermissions(context)) {
                        val currLoc: File
                        internalList.clear()
                        if (properties.offset.isDirectory && validateOffsetPath()) {
                            currLoc = File(properties.offset.absolutePath)
                            val parent = FileListItem()
                            parent.filename = ctx.getString(R.string.label_parent_dir)
                            parent.isDirectory = true
                            parent.location = currLoc.parentFile!!.absolutePath
                            parent.time = currLoc.lastModified()
                            internalList.add(parent)
                        } else if (properties.root.exists() && properties.root.isDirectory) {
                            currLoc = File(properties.root.absolutePath)
                        } else {
                            currLoc = File(properties.errorDir.absolutePath)
                        }
                        dname.text = currLoc.name
                        dirPath.text = currLoc.absolutePath
                        internalList = Utility.prepareFileListEntries(internalList, currLoc, filter).toMutableList()

                        mFileListAdapter?.listItem = internalList
                        mFileListAdapter?.notifyDataSetChanged()
                        mFileListAdapter?.clickListener = listener
                    }


                    recyclerView.layoutManager = LinearLayoutManager(ctx)
                    recyclerView.adapter = mFileListAdapter


                }
            }
    }



    private fun validateOffsetPath(): Boolean {
        val offsetPath = properties.offset.absolutePath
        val rootPath = properties.root.absolutePath
        return offsetPath != rootPath && offsetPath.contains(rootPath)
    }

    private val listener: (View, Int) -> Unit =  { view, i ->
        if (internalList.size > i) {
            val fitem = internalList[i]
            if (fitem.isDirectory) {
                if (File(fitem.location).canRead()) {
                    val currLoc = File(fitem.location)
                    contentView.dname.text = currLoc.name
                    contentView.dirPath.text = currLoc.absolutePath
                    internalList.clear()
                    if (currLoc.name != properties.root.name) {
                        val parent = FileListItem()
                        parent.filename = ctx.getString(R.string.label_parent_dir)
                        parent.isDirectory = true
                        parent.location = currLoc.parentFile!!.absolutePath
                        parent.time = currLoc.lastModified()
                        internalList.add(parent)
                    }
                    internalList = Utility.prepareFileListEntries(internalList, currLoc, filter).toMutableList()

                    mFileListAdapter?.listItem = internalList
                    mFileListAdapter?.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, R.string.error_dir_access, Toast.LENGTH_SHORT).show()
                }
            } else {
                val fmark = view.findViewById<MaterialCheckbox>(R.id.file_mark)
                fmark.performClick()
            }
        }
    }


    fun setDialogSelectionListener(callbacks: (Array<String>)->Unit) {
        this.callbacks = callbacks
    }

    fun setPositiveBtnName(positiveBtnNameStr: CharSequence?) {
        if (positiveBtnNameStr != null) {
            this.positiveBtnNameStr = positiveBtnNameStr.toString()
        } else {
            this.positiveBtnNameStr = null
        }
    }

    fun setNegativeBtnName(negativeBtnNameStr: CharSequence?) {
        if (negativeBtnNameStr != null) {
            this.negativeBtnNameStr = negativeBtnNameStr.toString()
        } else {
            this.negativeBtnNameStr = null
        }
    }

    /*fun markFiles(paths: List<String>?) {
        if (paths != null && paths.size > 0) {
            if (properties.selection_mode == DialogConfigs.SINGLE_MODE) {
                val temp = File(paths[0])
                when (properties.selection_type) {
                    DialogConfigs.DIR_SELECT -> if (temp.exists() && temp.isDirectory) {
                        val item = FileListItem()
                        item.filename = temp.name
                        item.isDirectory = temp.isDirectory
                        item.isMarked = true
                        item.time = temp.lastModified()
                        item.location = temp.absolutePath
                        MarkedItemList.addSelectedItem(item)
                    }

                    DialogConfigs.FILE_SELECT -> if (temp.exists() && temp.isFile) {
                        val item = FileListItem()
                        item.filename = temp.name
                        item.isDirectory = temp.isDirectory
                        item.isMarked = true
                        item.time = temp.lastModified()
                        item.location = temp.absolutePath
                        MarkedItemList.addSelectedItem(item)
                    }

                    DialogConfigs.FILE_AND_DIR_SELECT -> if (temp.exists()) {
                        val item = FileListItem()
                        item.filename = temp.name
                        item.isDirectory = temp.isDirectory
                        item.isMarked = true
                        item.time = temp.lastModified()
                        item.location = temp.absolutePath
                        MarkedItemList.addSelectedItem(item)
                    }
                }
            } else {
                for (path in paths) {
                    when (properties!!.selection_type) {
                        DialogConfigs.DIR_SELECT -> {
                            var temp = File(path)
                            if (temp.exists() && temp.isDirectory) {
                                val item = FileListItem()
                                item.filename = temp.name
                                item.isDirectory = temp.isDirectory
                                item.isMarked = true
                                item.time = temp.lastModified()
                                item.location = temp.absolutePath
                                MarkedItemList.addSelectedItem(item)
                            }
                        }

                        DialogConfigs.FILE_SELECT -> {
                            temp = File(path)
                            if (temp.exists() && temp.isFile()) {
                                val item = FileListItem()
                                item.filename = temp.getName()
                                item.isDirectory = temp.isDirectory()
                                item.isMarked = true
                                item.time = temp.lastModified()
                                item.location = temp.getAbsolutePath()
                                MarkedItemList.addSelectedItem(item)
                            }
                        }

                        DialogConfigs.FILE_AND_DIR_SELECT -> {
                            temp = File(path)
                            if (temp.exists() && (temp.isFile() || temp.isDirectory())) {
                                val item = FileListItem()
                                item.filename = temp.getName()
                                item.isDirectory = temp.isDirectory()
                                item.isMarked = true
                                item.time = temp.lastModified()
                                item.location = temp.getAbsolutePath()
                                MarkedItemList.addSelectedItem(item)
                            }
                        }
                    }
                }
            }
        }
    }*/


    private fun onBackPressed(): Boolean {
        //currentDirName is dependent on dname
        val currentDirName = contentView.dname.text.toString()
        if (internalList.size > 0) {
            val fitem = internalList[0]
            val currLoc = File(fitem.location)
            if (currentDirName == properties.root.name || !currLoc.canRead()) {
                return false
            } else {
                contentView.dname.text = currLoc.name
                contentView.dirPath.text = currLoc.absolutePath
                internalList.clear()
                if (currLoc.name != properties.root.name) {
                    val parent = FileListItem()
                    parent.filename = ctx.getString(R.string.label_parent_dir)
                    parent.isDirectory = true
                    parent.location = currLoc.parentFile!!.absolutePath
                    parent.time = currLoc.lastModified()
                    internalList.add(parent)
                }
                internalList = Utility.prepareFileListEntries(internalList, currLoc, filter).toMutableList()
                mFileListAdapter?.listItem = internalList
                mFileListAdapter?.notifyDataSetChanged()
            }
        } else {
            return false
        }

        return true
    }

    override fun dismiss() {
        MarkedItemList.clearSelectionList()
        internalList.clear()
        super.dismiss()
    }
}

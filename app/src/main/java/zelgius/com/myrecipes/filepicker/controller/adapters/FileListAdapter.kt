package zelgius.com.myrecipes.filepicker.controller.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.filepicker.model.DialogConfigs
import zelgius.com.myrecipes.filepicker.model.DialogProperties
import zelgius.com.myrecipes.filepicker.model.FileListItem
import zelgius.com.myrecipes.filepicker.model.MarkedItemList
import zelgius.com.myrecipes.filepicker.widget.MaterialCheckbox
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @author akshay sunil masram
 */
class FileListAdapter(
    var listItem: List<FileListItem>,
    private val context: Context,
    private val properties: DialogProperties
) : RecyclerView.Adapter<FileListAdapter.ViewHolder>() {

    var clickListener: ((View, Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.adapter_file_item, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItem[position]

        holder.itemView.setOnClickListener { clickListener?.invoke(holder.itemView, position) }

        if (MarkedItemList.hasItem(item.location)) {
            val animation = AnimationUtils.loadAnimation(context, R.anim.marked_item_animation)
            holder.itemView.animation = animation
        } else {
            val animation = AnimationUtils.loadAnimation(context, R.anim.unmarked_item_animation)
            holder.itemView.animation = animation
        }
        if (item.isDirectory) {
            holder.typeIcon.setImageResource(R.mipmap.ic_type_folder)
            holder.typeIcon.setColorFilter(
                context.resources.getColor(
                    R.color.primaryColor,
                    context.theme
                )
            )
            if (properties.selectionType == DialogConfigs.FILE_SELECT) {
                holder.fmark.visibility = View.INVISIBLE
            } else {
                holder.fmark.visibility = View.VISIBLE
            }
        } else {
            holder.typeIcon.setImageResource(R.mipmap.ic_type_file)
            holder.typeIcon.setColorFilter(
                context.resources.getColor(
                    R.color.colorAccent,
                    context.theme
                )
            )
            if (properties.selectionType == DialogConfigs.DIR_SELECT) {
                holder.fmark.visibility = View.INVISIBLE
            } else {
                holder.fmark.visibility = View.VISIBLE
            }
        }
        holder.typeIcon.contentDescription = item.filename
        holder.name.text = item.filename
        val sdate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val stime = SimpleDateFormat("hh:mm aa", Locale.getDefault())
        val date = Date(item.time)
        if (position == 0 && item.filename.startsWith(context.getString(R.string.label_parent_dir))) {
            holder.type.setText(R.string.label_parent_directory)
        } else {
            holder.type.text = String.format(
                "%s %s, %s",
                context.getString(R.string.last_edit), sdate.format(date), stime.format(date)
            )
        }
        if (holder.fmark.visibility == View.VISIBLE) {
            if (position == 0 && item.filename.startsWith(context.getString(R.string.label_parent_dir))) {
                holder.fmark.visibility = View.INVISIBLE
            }
            holder.fmark.isChecked = MarkedItemList.hasItem(item.location)
        }

        holder.fmark.setOnCheckedChangedListener { _, isChecked ->
            item.isMarked = isChecked
            if (item.isMarked) {
                if (properties.selectionMode == DialogConfigs.MULTI_MODE) {
                    MarkedItemList.addSelectedItem(item)
                } else {
                    MarkedItemList.addSingleFile(item)
                }
            } else {
                MarkedItemList.removeSelectedItem(item.location)
            }
            notifyItemChecked?.invoke()
        }
    }

    private var notifyItemChecked: (() -> Unit)? = null

    override fun getItemCount(): Int =
        listItem.size

    override fun getItemId(i: Int): Long = i.toLong()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var typeIcon: ImageView = itemView.findViewById(R.id.image_type)
        internal var name: TextView = itemView.findViewById(R.id.fname)
        internal var type: TextView = itemView.findViewById(R.id.ftype)
        internal var fmark: MaterialCheckbox = itemView.findViewById(R.id.file_mark)

    }

    fun setNotifyItemCheckedListener(notifyItemChecked: () -> Unit) {
        this.notifyItemChecked = notifyItemChecked
    }
}

package com.dashlane.attachment.ui

import android.content.Context
import android.graphics.Color
import android.text.format.Formatter
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.dashlane.R
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class AttachmentViewHolder(val v: View) : EfficientViewHolder<AttachmentItem>(v) {

    interface OnIconClickListener {
        fun onIconClicked(item: AttachmentItem, position: Int)
    }

    private val icon = findViewByIdEfficient<ImageView>(R.id.attachment_item_icon)!!
    private val name = findViewByIdEfficient<TextView>(R.id.attachment_item_name)!!
    private val description = findViewByIdEfficient<TextView>(R.id.attachment_item_description)!!
    private val progressBar = findViewByIdEfficient<ProgressBar>(R.id.attachment_item_progress)!!
    var iconClickListener: OnIconClickListener? = null

    override fun updateView(context: Context, item: AttachmentItem?) {
        item ?: return
        icon.setOnClickListener {
            iconClickListener?.onIconClicked(item, bindingAdapterPosition)
        }
        icon.setOnLongClickListener {
            iconClickListener?.onIconClicked(item, bindingAdapterPosition)
            true
        }

        name.text = item.filename
        if (item.downloadState == AttachmentItem.DownloadState.DOWNLOADING) {
            icon.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            progressBar.progress = item.downloadProgress
            description.text = context.getString(R.string.downloading_file_progress, "${item.downloadProgress}%")
        } else {
            icon.visibility = View.VISIBLE
            progressBar.visibility = View.GONE

            val userModificationDatetime = item.userModificationDatetime
            if (userModificationDatetime != null) {
                description.text = context.getString(
                    R.string.attachment_item_description_date_size,
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                        .format(Instant.ofEpochSecond(userModificationDatetime).atZone(ZoneId.systemDefault())),
                    Formatter.formatShortFileSize(context, item.localSize!!)
                )
            }
            if (item.downloadState == AttachmentItem.DownloadState.DOWNLOADED) {
                
                icon.setImageResource(R.drawable.ic_attachment_clipboard_downloaded)
            } else {
                icon.setImageResource(R.drawable.ic_attachment_clipboard_default)
            }
        }

        if (item.selected) {
            v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.item_highlighted_background_tint))
            icon.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            icon.setImageResource(R.drawable.ic_attachment_selected)
        } else {
            v.setBackgroundColor(Color.TRANSPARENT)
        }
    }
}
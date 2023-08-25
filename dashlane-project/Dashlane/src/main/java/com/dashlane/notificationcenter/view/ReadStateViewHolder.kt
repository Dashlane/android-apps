package com.dashlane.notificationcenter.view

import android.content.Context
import android.graphics.Typeface
import android.text.format.DateUtils
import android.view.View
import android.widget.TextView
import com.dashlane.R
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder
import java.util.Locale

open class ReadStateViewHolder<T : NotificationItem>(view: View) : EfficientViewHolder<T>(view) {

    override fun updateView(context: Context, item: T?) {
        item ?: return
        initTitleField(item)
        initDateField(item)
        initDescriptionField(item)
    }

    private fun initTitleField(item: T) {
        findViewByIdEfficient<TextView>(R.id.title)!!.apply {
            setTypeface(
                typeface,
                if (item.isRead()) {
                    Typeface.NORMAL
                } else {
                    Typeface.BOLD
                }
            )
        }
    }

    private fun initDescriptionField(item: T) {
        findViewByIdEfficient<TextView>(R.id.description)!!.apply {
            val style = Typeface.NORMAL.takeIf { item.isRead() } ?: Typeface.BOLD
            setTypeface(typeface, style)
        }
    }

    private fun initDateField(item: T) {
        findViewByIdEfficient<TextView>(R.id.date)?.apply {
            val style = Typeface.NORMAL.takeIf { item.isRead() } ?: Typeface.BOLD
            setTypeface(typeface, style)
        }
    }

    private fun NotificationItem.isRead(): Boolean = actionItemsRepository.isRead(this)

    internal fun formatDateForNotification(accessTimestamp: Long, currentTimeMillis: Long): String {
        return DateUtils.getRelativeTimeSpanString(
            accessTimestamp,
            currentTimeMillis,
            DateUtils.MINUTE_IN_MILLIS,
            0
        ).toString()
            .lowercase(Locale.getDefault())
    }
}
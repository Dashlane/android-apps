package com.dashlane.security.darkwebmonitoring.item

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dashlane.R
import com.dashlane.darkweb.DarkWebEmailStatus
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.drawable.ContactDrawable
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder



data class DarkWebEmailItem(val emailStatus: DarkWebEmailStatus) : DashlaneRecyclerAdapter.ViewTypeProvider {

    var deleteListener: DeleteListener? = null

    override fun getViewType() = VIEW_TYPE

    class ViewHolder(val v: View) : EfficientViewHolder<DarkWebEmailItem>(v) {

        private var isClickable = false

        init {
            findViewByIdEfficient<View>(R.id.delete)!!
                .setOnClickListener {
                    val item = `object` ?: return@setOnClickListener
                    item.deleteListener?.onDeleteClicked(item.emailStatus)
                }
        }

        override fun updateView(context: Context, item: DarkWebEmailItem?) {
            item ?: return

            val email = item.emailStatus.email
            val status = item.emailStatus.status

            isClickable = status == DarkWebEmailStatus.STATUS_DISABLED

            view.isClickable = isClickable
            view.isFocusable = isClickable

            val contactDrawable = ContactDrawable.newInstance(context, email)

            findViewByIdEfficient<ImageView>(R.id.icon)!!.setImageDrawable(contactDrawable)
            setText(R.id.item_line1, email)

            val statusString = when (status) {
                DarkWebEmailStatus.STATUS_DISABLED -> context.getString(R.string.dark_web_status_disabled)
                DarkWebEmailStatus.STATUS_PENDING -> context.getString(R.string.dark_web_status_pending)
                DarkWebEmailStatus.STATUS_ACTIVE -> context.getString(R.string.dark_web_status_active)
                else -> null
            }

            val statusTextColor = context.getColor(
                when (status) {
                    DarkWebEmailStatus.STATUS_DISABLED -> R.color.text_danger_quiet
                    DarkWebEmailStatus.STATUS_PENDING -> R.color.text_warning_quiet
                    DarkWebEmailStatus.STATUS_ACTIVE -> R.color.text_positive_quiet
                    else -> R.color.text_neutral_quiet
                }
            )

            findViewByIdEfficient<TextView>(R.id.item_line2)!!.apply {
                text = statusString
                setTextColor(statusTextColor)
            }
        }

        override fun isClickable() = isClickable

        override fun isLongClickable() = isClickable
    }

    interface DeleteListener {
        fun onDeleteClicked(item: DarkWebEmailStatus)
    }

    companion object {
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType<DarkWebEmailItem>(
            R.layout.item_dark_web_email,
            ViewHolder::class.java
        )
    }
}
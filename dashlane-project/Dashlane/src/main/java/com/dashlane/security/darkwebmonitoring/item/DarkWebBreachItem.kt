package com.dashlane.security.darkwebmonitoring.item

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.dashlane.R
import com.dashlane.security.identitydashboard.breach.BreachWrapper
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.thumbnail.ThumbnailDomainIconView
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

data class DarkWebBreachItem(val breach: BreachWrapper) : DashlaneRecyclerAdapter.ViewTypeProvider {

    var selected = false

    override fun getViewType() = VIEW_TYPE

    class ViewHolder(v: View) : EfficientViewHolder<DarkWebBreachItem>(v) {

        override fun updateView(context: Context, item: DarkWebBreachItem?) {
            item ?: return
            val newAlertIcon = view.findViewById<ImageView>(R.id.new_alert)
            val thumbnail = view.findViewById<ThumbnailDomainIconView>(R.id.thumbnail)
            val icon = view.findViewById<ImageView>(R.id.icon)
            val domainView = view.findViewById<TextView>(R.id.domain)
            val selectedBackground = view.findViewById<View>(R.id.selected_background)
            val domain = item.breach.publicBreach.domains?.firstOrNull()

            domainView.text = domain ?: context.getString(R.string.dwm_alert_unknown_domain)

            if (item.breach.localBreach.viewed) {
                newAlertIcon.visibility = View.GONE
                domainView.setTypeface(null, Typeface.NORMAL)
            } else {
                newAlertIcon.visibility = View.VISIBLE
                domainView.setTypeface(null, Typeface.BOLD)
            }

            view.findViewById<TextView>(R.id.email).text = item.breach.publicBreach.impactedEmails?.takeIf {
                it.isNotEmpty()
            }?.joinToString()

            if (item.selected) {
                thumbnail.visibility = View.INVISIBLE
                icon.visibility = View.VISIBLE
            } else {
                thumbnail.visibility = View.VISIBLE
                icon.visibility = View.GONE
                thumbnail.domainUrl = domain
            }
            selectedBackground.isVisible = item.selected
        }
    }

    companion object {
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType(
            R.layout.item_dark_web_breach,
            ViewHolder::class.java
        )
    }
}

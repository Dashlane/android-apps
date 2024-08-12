package com.dashlane.ui.screens.fragments.userdata

import android.content.Context
import android.view.View
import android.widget.TextView
import com.dashlane.R
import com.dashlane.design.component.compat.view.ThumbnailViewType
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.thumbnail.ThumbnailDomainIconView
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class DomainItem(val domain: String) : DashlaneRecyclerAdapter.ViewTypeProvider {
    override fun getViewType() = VIEW_TYPE

    class ViewHolder(val v: View) : EfficientViewHolder<DomainItem>(v) {
        override fun updateView(context: Context, item: DomainItem?) {
            val domain = item?.domain.takeUnless { it.isNullOrEmpty() }
            val thumbnail = view.findViewById<ThumbnailDomainIconView>(R.id.websiteIcon)

            if (domain != null) {
                thumbnail.thumbnailType = ThumbnailViewType.VAULT_ITEM_DOMAIN_ICON.value
                thumbnail.domainUrl = domain
            } else {
                thumbnail.thumbnailType = ThumbnailViewType.ICON.value
                thumbnail.iconRes = R.drawable.ic_action_add_outlined
            }
            view.findViewById<TextView>(R.id.listTextView).text = domain
                ?: context.getString(R.string.fragment_credential_create_step1_add_empty_item)
        }
    }

    companion object {
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType(
            R.layout.autocomplete_textview_websites_adapter,
            ViewHolder::class.java
        )
    }
}

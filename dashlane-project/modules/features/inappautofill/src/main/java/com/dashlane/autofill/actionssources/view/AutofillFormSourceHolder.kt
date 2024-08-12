package com.dashlane.autofill.actionssources.view

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dashlane.autofill.api.R
import com.dashlane.design.component.compat.view.ThumbnailView
import com.dashlane.design.component.compat.view.ThumbnailViewType
import com.dashlane.ui.thumbnail.ThumbnailDomainIconView
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class AutofillFormSourceHolder(v: View) :
    EfficientViewHolder<AutofillFormSourceViewTypeProviderFactory.AutofillFormSourceWrapper>(v) {
    private var authentifiantTitle: TextView = v.findViewById(R.id.item_title)
    private var authentifiantSubtitle: TextView = v.findViewById(R.id.item_subtitle)
    private var authentifiantThumbnailDomain: ThumbnailDomainIconView = v.findViewById(R.id.item_thumbnail)
    private var authentifiantThumbnailIcon: ThumbnailView = v.findViewById(R.id.item_icon)
    private var authentifiantAppIcon: ImageView = v.findViewById(R.id.item_icon_app)

    override fun updateView(
        context: Context,
        itemWrapper: AutofillFormSourceViewTypeProviderFactory.AutofillFormSourceWrapper?
    ) {
        if (itemWrapper != null) {
            authentifiantTitle.text = itemWrapper.title
            authentifiantSubtitle.text = itemWrapper.type

            val isApp = itemWrapper.isApp()
            val appIcon = itemWrapper.getAppDrawable(context)
            val urlDomain = itemWrapper.getUrlDomain()

            when {
                isApp && appIcon != null -> {
                    authentifiantThumbnailDomain.visibility = View.INVISIBLE
                    authentifiantThumbnailIcon.visibility = View.INVISIBLE
                    authentifiantAppIcon.visibility = View.VISIBLE
                    authentifiantAppIcon.setImageDrawable(appIcon)
                }
                isApp && appIcon == null -> {
                    showUnknownApp()
                }
                else -> {
                    authentifiantThumbnailDomain.visibility = View.VISIBLE
                    authentifiantThumbnailIcon.visibility = View.INVISIBLE
                    authentifiantAppIcon.visibility = View.INVISIBLE
                    authentifiantThumbnailDomain.domainUrl = urlDomain
                }
            }
        }
    }

    private fun showUnknownApp() {
        authentifiantThumbnailDomain.visibility = View.VISIBLE
        authentifiantThumbnailIcon.visibility = View.INVISIBLE
        authentifiantAppIcon.visibility = View.INVISIBLE
        authentifiantThumbnailDomain.thumbnailType = ThumbnailViewType.ICON.value
        authentifiantThumbnailDomain.iconRes = R.drawable.ic_auto_login_outlinedauto_login_empty_state
    }
}

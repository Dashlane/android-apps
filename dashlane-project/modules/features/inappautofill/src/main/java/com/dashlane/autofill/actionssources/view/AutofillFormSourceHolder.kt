package com.dashlane.autofill.actionssources.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dashlane.autofill.api.R
import com.dashlane.ui.drawable.PlaceholderForTextDrawableFactory
import com.dashlane.util.getThemeAttrColor
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class AutofillFormSourceHolder(v: View) : EfficientViewHolder<AutofillFormSourceViewTypeProviderFactory.AutofillFormSourceWrapper>(v) {
    private var authentifiantTitle: TextView = v.findViewById(R.id.item_title)
    private var authentifiantSubtitle: TextView = v.findViewById(R.id.item_subtitle)
    private var authentifiantIcon: ImageView = v.findViewById(R.id.item_icon)

    override fun updateView(context: Context, itemWrapper: AutofillFormSourceViewTypeProviderFactory.AutofillFormSourceWrapper?) {
        itemWrapper?.let {
            updateViewWithContent(it.title, it.type, it.buildDrawable(context))
        } ?: updateViewWithEmpty(context)
    }

    private fun updateViewWithContent(title: String? = null, type: String? = null, icon: Drawable) {
        authentifiantTitle.text = title
        authentifiantSubtitle.text = type
        authentifiantIcon.setImageDrawable(icon)
    }

    private fun updateViewWithEmpty(context: Context) {
        val icon = PlaceholderForTextDrawableFactory.buildDrawable(
            context,
            "",
            context.getThemeAttrColor(R.attr.colorPrimary),
            context.getThemeAttrColor(R.attr.colorOnPrimary)
        )
        updateViewWithContent(icon = icon)
    }
}

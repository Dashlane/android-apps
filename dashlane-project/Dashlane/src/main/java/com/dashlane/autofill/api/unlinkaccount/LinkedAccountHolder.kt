package com.dashlane.autofill.api.unlinkaccount

import android.content.Context
import android.view.View
import android.widget.TextView
import com.dashlane.autofill.api.R
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class LinkedAccountHolder(v: View) : EfficientViewHolder<LinkedAccountViewTypeProviderFactoryImpl.Wrapper>(v) {
    private var authentifiantTitle: TextView = v.findViewById(R.id.item_title)
    private var authentifiantSubtitle: TextView = v.findViewById(R.id.item_subtitle)

    override fun updateView(context: Context, itemWrapper: LinkedAccountViewTypeProviderFactoryImpl.Wrapper?) {
        itemWrapper?.let {
            updateView(it.title, it.subtitle)
        } ?: updateView()
    }

    private fun updateView(title: String? = null, type: String? = null) {
        authentifiantTitle.text = title
        authentifiantSubtitle.text = type
    }
}

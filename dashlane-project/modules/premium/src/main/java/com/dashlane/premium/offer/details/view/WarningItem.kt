package com.dashlane.premium.offer.details.view

import android.content.Context
import android.view.View
import com.dashlane.premium.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.model.TextResource
import com.dashlane.ui.widgets.view.Infobox
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

internal class WarningItem(
    val content: TextResource
) : DashlaneRecyclerAdapter.ViewTypeProvider {

    override fun getViewType() = VIEW_TYPE

    class ViewHolder(view: View) : EfficientViewHolder<WarningItem>(view) {
        private val infobox = findViewByIdEfficient<Infobox>(R.id.offer_details_warning_infobox)!!
        override fun updateView(context: Context, item: WarningItem?) {
            item?.apply {
                infobox.text = content.format(context.resources)
            }
        }
    }

    companion object {
        val VIEW_TYPE: DashlaneRecyclerAdapter.ViewType<WarningItem> =
            DashlaneRecyclerAdapter.ViewType(R.layout.offer_details_warning_item, ViewHolder::class.java)
    }
}
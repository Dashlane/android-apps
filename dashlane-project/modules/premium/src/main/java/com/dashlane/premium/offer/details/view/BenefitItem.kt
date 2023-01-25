package com.dashlane.premium.offer.details.view

import android.content.Context
import android.view.View
import com.dashlane.premium.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.util.DiffUtilComparator
import com.dashlane.ui.model.TextResource
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

internal class BenefitItem(
    val benefit: TextResource
) : DashlaneRecyclerAdapter.ViewTypeProvider,
    DiffUtilComparator<TextResource> {

    override fun getViewType() = VIEW_TYPE

    override fun isItemTheSame(item: TextResource) = benefit == item

    override fun isContentTheSame(item: TextResource) = benefit == item

    class ViewHolder(view: View) : EfficientViewHolder<BenefitItem>(view) {
        override fun updateView(context: Context, item: BenefitItem?) {
            item?.apply {
                val text = benefit.format(context.resources)
                setText(R.id.offer_details_benefit_item_text, text)
            }
        }
    }

    companion object {

        val VIEW_TYPE: DashlaneRecyclerAdapter.ViewType<BenefitItem> =
            DashlaneRecyclerAdapter.ViewType(R.layout.offer_details_benefit_item, ViewHolder::class.java)
    }
}
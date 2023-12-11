package com.dashlane.premium.current.ui

import android.content.Context
import android.view.View
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.ACTION_CLICK
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import com.dashlane.premium.R
import com.dashlane.premium.current.model.CurrentPlan
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.util.DiffUtilComparator
import com.dashlane.ui.model.TextResource
import com.dashlane.util.onInitializeAccessibilityNodeInfo
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

internal class CurrentBenefitItem(
    val benefit: CurrentPlan.Benefit
) : DashlaneRecyclerAdapter.ViewTypeProvider,
    DiffUtilComparator<TextResource> {

    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<*> =
        DashlaneRecyclerAdapter.ViewType(
            R.layout.current_plan_benefit_item,
            ViewHolder::class.java
        )

    override fun isItemTheSame(item: TextResource) = benefit.textResource == item

    override fun isContentTheSame(item: TextResource) = benefit.textResource == item

    class ViewHolder(view: View) : EfficientViewHolder<CurrentBenefitItem>(view) {
        private var actionAvailable = false

        override fun updateView(context: Context, item: CurrentBenefitItem?) {
            item?.apply {
                val text = benefit.textResource.format(context.resources)
                actionAvailable = benefit.action != null
                setText(R.id.current_benefit_text, text)
                setMoreInfo(available = actionAvailable)
            }
        }

        
        override fun isClickable() = actionAvailable
        override fun isLongClickable() = false

        private fun setMoreInfo(available: Boolean) {
            view.isClickable = available
            view.isFocusable = available
            view.onInitializeAccessibilityNodeInfo { info ->
                if (available) {
                    val label = view.context.getString(R.string.current_benefit_more_info_description)
                    info.addAction(AccessibilityActionCompat(ACTION_CLICK, label))
                } else {
                    info.removeAction(AccessibilityActionCompat.ACTION_CLICK)
                }
            }
            setVisibility(R.id.current_benefit_more_info_icon, View.VISIBLE.takeIf { available } ?: View.GONE)
        }
    }
}
package com.dashlane.ui.screens.settings.list.general.labs

import android.content.Context
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import com.dashlane.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class DashlaneLabsItem(
    val labsFeatureFlip: Pair<UserFeaturesChecker.FeatureFlip, Boolean>
) : DashlaneRecyclerAdapter.ViewTypeProvider {

    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<*> =
        DashlaneRecyclerAdapter.ViewType(
            R.layout.item_dashlane_labs,
            ViewHolder::class.java
        )

    class ViewHolder(view: View) : EfficientViewHolder<DashlaneLabsItem>(view) {
        override fun updateView(context: Context, item: DashlaneLabsItem?) {
            view.findViewById<SwitchCompat>(R.id.ff_switch).also {
                it.text = item?.labsFeatureFlip?.first?.value
                it.isChecked = item?.labsFeatureFlip?.second ?: false
            }
        }
    }
}
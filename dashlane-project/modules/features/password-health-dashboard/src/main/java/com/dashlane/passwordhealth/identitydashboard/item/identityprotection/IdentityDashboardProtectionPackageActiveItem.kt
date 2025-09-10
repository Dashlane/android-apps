package com.dashlane.passwordhealth.identitydashboard.item.identityprotection

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.passwordhealth.R
import com.dashlane.passwordhealth.identitydashboard.item.IdentityDashboardItem
import com.dashlane.ui.legacy.recyclerview.DashlaneRecyclerAdapter
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class IdentityDashboardProtectionPackageActiveItem(
    val listener: ActiveProtectionPackageListener,
) : IdentityDashboardItem {
    override fun getViewType() = VIEW_TYPE

    class ViewHolder(val item: View) : EfficientViewHolder<IdentityDashboardProtectionPackageActiveItem>(item) {
        override fun updateView(context: Context, item: IdentityDashboardProtectionPackageActiveItem?) {
            item ?: return
            val composeView = findViewByIdEfficient<ComposeView>(R.id.id_protection_compose_view) ?: return
            composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            composeView.setContent {
                DashlaneTheme {
                    IdentityDashboardProtectionPackageActive(
                        onActiveProtectionLearnMoreClick = item.listener::onActiveProtectionLearnMoreClick,
                        onActiveRestorationLearnMoreClick = item.listener::onActiveRestorationLearnMoreClick,
                    )
                }
            }
        }
    }

    interface ActiveProtectionPackageListener {
        fun onActiveProtectionLearnMoreClick()

        fun onActiveRestorationLearnMoreClick()
    }

    companion object {
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType(
            R.layout.item_id_protection_package_active,
            ViewHolder::class.java,
        )
    }
}
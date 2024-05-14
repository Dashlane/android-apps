package com.dashlane.ui.activities.fragments.list.action

import android.view.View
import com.dashlane.R
import com.dashlane.item.subview.quickaction.QuickActionProvider
import com.dashlane.navigation.Navigator
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.vault.summary.SummaryObject

class QuickActionsItemAction(
    private val quickActionProvider: QuickActionProvider,
    private val item: SummaryObject,
    private val itemListContext: ItemListContext,
    private val navigator: Navigator
) : ListItemAction {

    override val icon: Int = R.drawable.ic_item_action_more

    override val contentDescription: Int = R.string.and_accessibility_quick_action

    override val viewId: Int = R.id.quick_actions_open_menu

    override val visibility: Int
        get() {
            val hasQuickActions = quickActionProvider.getQuickActions(item, itemListContext).isNotEmpty()
            return if (hasQuickActions) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        }

    override fun onClickItemAction(v: View, item: SummaryObject) {
        navigator.goToQuickActions(item.id, itemListContext)
    }
}
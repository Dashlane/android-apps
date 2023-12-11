package com.dashlane.ui.activities.fragments.list.action

import android.view.View
import com.dashlane.R
import com.dashlane.hermes.generated.definitions.AnyPage
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
        val originPage = itemListContext.toAnyPage()
        navigator.goToQuickActions(item.id, itemListContext, originPage)
    }

    private fun ItemListContext.toAnyPage(): AnyPage? = when (this.container) {
        ItemListContext.Container.ALL_ITEMS -> AnyPage.ITEM_ALL_LIST
        ItemListContext.Container.IDS_LIST -> AnyPage.ITEM_ID_LIST
        ItemListContext.Container.PAYMENT_LIST -> AnyPage.ITEM_PAYMENT_LIST
        ItemListContext.Container.PERSONAL_INFO_LIST -> AnyPage.ITEM_PERSONAL_INFO_LIST
        ItemListContext.Container.CREDENTIALS_LIST -> AnyPage.ITEM_CREDENTIAL_LIST
        ItemListContext.Container.SECURE_NOTE_LIST -> AnyPage.ITEM_SECURE_NOTE_LIST
        ItemListContext.Container.SEARCH -> AnyPage.SEARCH

        
        ItemListContext.Container.CSV_IMPORT,
        ItemListContext.Container.PASSWORD_HEALTH,
        ItemListContext.Container.SHARING,
        ItemListContext.Container.PASSKEYS_LIST,
        ItemListContext.Container.NONE -> null
    }
}
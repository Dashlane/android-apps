package com.dashlane.ui.quickactions

import com.dashlane.ui.adapter.ItemListContext
import com.skocken.presentation.presenter.BasePresenter

class QuickActionsPresenter : QuickActionsContract.Presenter,
    BasePresenter<QuickActionsContract.DataProvider, QuickActionsContract.ViewProxy>() {

    override fun getActions(itemId: String, itemListContext: ItemListContext) {
        val actions = provider.getActions(itemId, itemListContext)
        view.setActions(actions)
    }

    override fun getItemDetails(itemId: String) {
        val icon = provider.getItemIcon(context!!, itemId)
        val title = provider.getItemTitle(context!!, itemId)
        view.setItemDetail(icon, title)
    }
}
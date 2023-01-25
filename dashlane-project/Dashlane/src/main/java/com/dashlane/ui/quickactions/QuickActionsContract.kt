package com.dashlane.ui.quickactions

import android.content.Context
import android.graphics.drawable.Drawable
import com.dashlane.item.subview.Action
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.vault.summary.SummaryObject
import com.skocken.presentation.definition.Base

interface QuickActionsContract {
    interface ViewProxy : Base.IView {
        fun setActions(actions: List<Action>)
        fun setItemDetail(drawable: Drawable?, title: String?)
    }

    interface Presenter : Base.IPresenter {
        fun getActions(itemId: String, itemListContext: ItemListContext)
        fun getItemDetails(itemId: String)
    }

    interface DataProvider : Base.IDataProvider {
        fun getVaultItem(itemId: String): SummaryObject?
        fun getActions(itemId: String, itemListContext: ItemListContext): List<Action>
        fun getItemIcon(context: Context, itemId: String): Drawable?
        fun getItemTitle(context: Context, itemId: String): String?
    }

    interface Logger {
        fun logOpenQuickActions(summaryObject: SummaryObject, itemListContext: ItemListContext)
        fun logCloseQuickActions(originPage: String?)
    }
}
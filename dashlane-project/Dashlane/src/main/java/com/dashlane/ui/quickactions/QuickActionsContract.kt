package com.dashlane.ui.quickactions

import android.content.Context
import com.dashlane.item.subview.Action
import com.dashlane.ui.VaultItemImageHelper
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.vault.summary.SummaryObject
import com.skocken.presentation.definition.Base

interface QuickActionsContract {
    interface ViewProxy : Base.IView {
        fun setActions(actions: List<Action>)
        fun setItemDetail(
            title: String?,
            thumbnailType: Int?,
            thumbnailIconRes: Int?,
            thumbnailColorRes: Int?,
            thumbnailUrlDomain: String?
        )
    }

    interface Presenter : Base.IPresenter {
        fun getActions(itemId: String, itemListContext: ItemListContext)
        fun getItemDetails(itemId: String)
    }

    interface DataProvider : Base.IDataProvider {
        fun getVaultItem(itemId: String): SummaryObject?
        fun getActions(itemId: String, itemListContext: ItemListContext): List<Action>
        fun getItemThumbnail(context: Context, itemId: String): VaultItemImageHelper.ThumbnailViewConfiguration?
        fun getItemTitle(context: Context, itemId: String): String?
    }

    interface Logger {
        fun logOpenQuickActions(summaryObject: SummaryObject, itemListContext: ItemListContext)
        fun logCloseQuickActions(originPage: String?)
    }
}
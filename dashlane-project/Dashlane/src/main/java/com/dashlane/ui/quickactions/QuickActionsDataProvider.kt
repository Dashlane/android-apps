package com.dashlane.ui.quickactions

import android.content.Context
import com.dashlane.item.subview.Action
import com.dashlane.item.subview.quickaction.QuickActionProvider
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.ui.VaultItemImageHelper
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.DataIdentifierListTextResolver
import com.skocken.presentation.provider.BaseDataProvider
import javax.inject.Inject

class QuickActionsDataProvider @Inject constructor(
    private val genericDataQuery: GenericDataQuery,
    private val dataIdentifierListTextResolver: DataIdentifierListTextResolver,
    private val quickActionProvider: QuickActionProvider
) : QuickActionsContract.DataProvider, BaseDataProvider<QuickActionsContract.Presenter>() {

    override fun getVaultItem(itemId: String): SummaryObject? {
        val query = vaultFilter { specificUid(itemId) }
        return genericDataQuery.queryFirst(query)
    }

    override fun getActions(itemId: String, itemListContext: ItemListContext): List<Action> {
        return getVaultItem(itemId)?.let {
            quickActionProvider.getQuickActions(it, itemListContext)
        } ?: emptyList()
    }

    override fun getItemThumbnail(context: Context, itemId: String): VaultItemImageHelper.ThumbnailViewConfiguration? {
        return getVaultItem(itemId)?.let {
            return VaultItemImageHelper.getThumbnailViewConfiguration(it)
        }
    }

    override fun getItemTitle(context: Context, itemId: String): String? {
        getVaultItem(itemId)?.let {
            return dataIdentifierListTextResolver.getLine1(it).text
        }
        return null
    }
}
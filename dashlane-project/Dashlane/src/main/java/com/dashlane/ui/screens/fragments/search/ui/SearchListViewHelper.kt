package com.dashlane.ui.screens.fragments.search.ui

import android.content.Context
import com.dashlane.R
import com.dashlane.search.MatchedSearchResult
import com.dashlane.search.SearchableSettingItem
import com.dashlane.ui.activities.fragments.list.ItemWrapperOneColViewHolder
import com.dashlane.ui.activities.fragments.list.wrapper.ItemWrapperProvider
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemWrapper
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.HeaderItem
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.screens.fragments.search.SearchRequest
import com.dashlane.search.textfactory.SearchListTextResolver
import com.dashlane.ui.screens.settings.SearchableSettingInRecyclerView
import com.dashlane.vault.summary.SummaryObject

object SearchListViewHelper {

    private val viewType: DashlaneRecyclerAdapter.ViewType<VaultItemWrapper<out SummaryObject>>
        get() = DashlaneRecyclerAdapter.ViewType(
            R.layout.item_dataidentifier,
            ItemWrapperOneColViewHolder::class.java
        )

    fun getWrappedList(
        context: Context,
        items: List<MatchedSearchResult>,
        request: SearchRequest,
        query: String?,
        searchListTextResolver: SearchListTextResolver,
        itemWrapperProvider: ItemWrapperProvider
    ): List<DashlaneRecyclerAdapter.ViewTypeProvider> {
        val list: MutableList<DashlaneRecyclerAdapter.ViewTypeProvider> = ArrayList()
        val resultCount = items.size

        addHeaderIfNecessary(context, request, list)

        
        val baseListContext =
            ItemListContext.Container.SEARCH.asListContext(section = request.toSection())

        items.forEachIndexed { i, matchResult ->
            getItemToAdd(
                context,
                matchResult,
                baseListContext.copy(i, resultCount),
                query,
                searchListTextResolver = searchListTextResolver,
                itemWrapperProvider = itemWrapperProvider
            )?.let { itemToAdd ->
                list.add(itemToAdd)
            }
        }
        return list
    }

    private fun addHeaderIfNecessary(
        context: Context,
        request: SearchRequest,
        list: MutableList<DashlaneRecyclerAdapter.ViewTypeProvider>
    ) {
        val header = when (request) {
            SearchRequest.DefaultRequest.FromRecent -> HeaderItem(context.getString(R.string.search_screen_header_last_searched))
            is SearchRequest.FromQuery -> return
        }
        list.add(header)
    }

    private fun getItemToAdd(
        context: Context,
        matchResult: MatchedSearchResult,
        container: ItemListContext,
        query: String?,
        searchListTextResolver: SearchListTextResolver,
        itemWrapperProvider: ItemWrapperProvider
    ): DashlaneRecyclerAdapter.ViewTypeProvider? {
        val item = matchResult.item
        if (item is SummaryObject) {
            val baseItemWrapper = itemWrapperProvider(item, container) ?: return null
            val itemWrapper = SearchItemWrapper(matchResult, query, searchListTextResolver, baseItemWrapper)

            itemWrapper.setViewType(viewType)
            itemWrapper.allowTeamspaceIcon = true
            return itemWrapper
        } else if (item is SearchableSettingItem && item.isSettingVisible(context)) {
            return SearchableSettingInRecyclerView(
                item = item,
                targetText = query,
                matchField = matchResult.match.field
            )
        }
        return null
    }
}
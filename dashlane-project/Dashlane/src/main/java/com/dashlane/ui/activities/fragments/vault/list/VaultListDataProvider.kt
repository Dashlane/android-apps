package com.dashlane.ui.activities.fragments.vault.list

import android.content.Context
import com.dashlane.core.DataSync
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.ui.activities.fragments.list.wrapper.ItemWrapperProvider
import com.dashlane.ui.activities.fragments.vault.Filter
import com.dashlane.ui.activities.fragments.vault.VaultItemViewTypeProvider
import com.dashlane.ui.activities.fragments.vault.provider.CategoryHeaderProvider
import com.dashlane.ui.activities.fragments.vault.provider.FirstLetterHeaderProvider
import com.dashlane.ui.activities.fragments.vault.provider.HeaderProvider
import com.dashlane.ui.activities.fragments.vault.provider.RecentHeaderProvider
import com.dashlane.ui.activities.fragments.vault.provider.SuggestedHeaderProvider
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.HeaderItem
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.adapter.ItemListContext.Container
import com.dashlane.ui.adapter.ItemListContext.Section
import com.dashlane.ui.adapters.text.factory.DataIdentifierTypeTextFactory
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.mostRecentAccessTime
import com.dashlane.vault.util.IdentityNameHolderService
import com.dashlane.vault.util.comparatorAlphabeticAllVisibleItems
import com.dashlane.vault.util.comparatorAlphabeticAuthentifiant
import com.dashlane.vault.util.comparatorAlphabeticSecureNote
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class VaultListDataProvider @Inject constructor(
    private val dataSync: DataSync,
    private val itemWrapperProvider: ItemWrapperProvider,
    private val identityNameHolderService: IdentityNameHolderService,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : VaultList.DataProvider {

    private val recentComparator = compareByDescending<SummaryObject> { it.mostRecentAccessTime }

    enum class BoundedListSortMode(val limit: Int) {
        SUGGESTED(6),
        MOST_RECENT(3);
    }

    enum class UnboundedListSortMode {
        ALPHABETICAL,
        CATEGORY,
        MOST_RECENT;
    }

    override fun syncData() {
        dataSync.sync(Trigger.MANUAL)
    }

    override suspend fun generateViewTypeProviderList(
        items: List<SummaryObject>,
        filter: Filter,
        sortMode: UnboundedListSortMode,
        context: Context
    ): List<DashlaneRecyclerAdapter.ViewTypeProvider> = withContext(defaultDispatcher) {
        val itemListContext = filter.toItemListContext(sortMode.toSection())
        summaryToViewTypeProvider(
            items.sort(sortMode, filter),
            getHeaderProvider(sortMode, items, context),
            itemListContext,
            context
        )
    }

    override suspend fun generateViewTypeProviderHighlightList(
        items: List<SummaryObject>,
        filter: Filter,
        sortMode: BoundedListSortMode,
        context: Context
    ): List<DashlaneRecyclerAdapter.ViewTypeProvider> = withContext(defaultDispatcher) {
        val itemListContext = filter.toItemListContext(sortMode.toSection())
        summaryToViewTypeProvider(
            items.sortBoundedList(sortMode),
            getHeaderProvider(sortMode),
            itemListContext,
            context
        )
    }

    private fun Filter.toItemListContext(section: Section): ItemListContext {
        return when (this) {
            Filter.ALL_VISIBLE_VAULT_ITEM_TYPES -> Container.ALL_ITEMS
            Filter.FILTER_PASSWORD -> Container.CREDENTIALS_LIST
            Filter.FILTER_SECURE_NOTE -> Container.SECURE_NOTE_LIST
            Filter.FILTER_PAYMENT -> Container.PAYMENT_LIST
            Filter.FILTER_PERSONAL_INFO -> Container.PERSONAL_INFO_LIST
            Filter.FILTER_ID -> Container.IDS_LIST
        }.asListContext().copy(section = section)
    }

    private fun UnboundedListSortMode.toSection(): Section {
        return when (this) {
            UnboundedListSortMode.ALPHABETICAL -> Section.ALPHABETICAL
            UnboundedListSortMode.CATEGORY -> Section.CATEGORY
            UnboundedListSortMode.MOST_RECENT -> Section.MOST_RECENT
        }
    }

    private fun BoundedListSortMode.toSection(): Section {
        return when (this) {
            BoundedListSortMode.SUGGESTED -> Section.SUGGESTED
            BoundedListSortMode.MOST_RECENT -> Section.MOST_RECENT
        }
    }

    private fun List<SummaryObject>.sort(sortMode: UnboundedListSortMode, filter: Filter): List<SummaryObject> =
        when (sortMode) {
            UnboundedListSortMode.ALPHABETICAL -> when (filter) {
                Filter.FILTER_PASSWORD ->
                    sortedWith(compareBy(comparatorAlphabeticAuthentifiant(identityNameHolderService)) { it })
                Filter.FILTER_SECURE_NOTE ->
                    sortedWith(compareBy(comparatorAlphabeticSecureNote()) { it as SummaryObject.SecureNote })
                Filter.ALL_VISIBLE_VAULT_ITEM_TYPES ->
                    sortedWith(compareBy(comparatorAlphabeticAllVisibleItems(identityNameHolderService)) { it })
                else -> this 
            }
            UnboundedListSortMode.MOST_RECENT -> sortedWith(recentComparator)
            UnboundedListSortMode.CATEGORY -> sortedBy { it.syncObjectType }
        }

    private fun List<SummaryObject>.sortBoundedList(sortMode: BoundedListSortMode): List<SummaryObject> {
        val limit = sortMode.limit
        return when (sortMode) {
            BoundedListSortMode.SUGGESTED -> getSuggestedList(limit)
            BoundedListSortMode.MOST_RECENT -> getRecentList(limit)
        }
    }

    private fun List<SummaryObject>.getSuggestedList(limit: Int): List<SummaryObject> {
        val mostUsed = filter { it.locallyUsedCount > 0 }.sortedByDescending { it.locallyUsedCount }
        val mostRecent = sortedWith(recentComparator)
        val suggestedList = mutableSetOf<SummaryObject>().run {
            addAll(mostUsed.take(limit.div(2)))
            addAll(mostRecent.take(limit))
            take(limit)
        }

        return suggestedList.filterByLocalUsage(limit)
    }

    private fun List<SummaryObject>.getRecentList(limit: Int): List<SummaryObject> =
        sortedWith(recentComparator).take(limit).filterByLocalUsage(limit)

    private fun List<SummaryObject>.filterByLocalUsage(limit: Int): List<SummaryObject> {
        
        val localUpdatedDateRecent = filter { it.locallyUsedCount > 0 }.sortedByDescending { it.locallyViewedDate }

        
        if (localUpdatedDateRecent.isEmpty()) {
            return this
        }

        
        if (localUpdatedDateRecent.size > limit) {
            return localUpdatedDateRecent.take(limit)
        }

        
        
        return (localUpdatedDateRecent + this).toSet().take(limit)
    }

    private fun summaryToViewTypeProvider(
        items: List<SummaryObject>,
        headerProvider: HeaderProvider,
        itemListContext: ItemListContext,
        context: Context
    ): List<DashlaneRecyclerAdapter.ViewTypeProvider> {
        val vaultItemsList: ArrayList<DashlaneRecyclerAdapter.ViewTypeProvider> = arrayListOf()
        var lastHeader: String? = null

        items.forEachIndexed { index, item ->
            
            val itemListContextWithPosition = itemListContext.copy(position = index, count = items.size)
            val viewTypeProvider = VaultItemViewTypeProvider(
                item,
                itemListContextWithPosition,
                itemWrapperProvider
            )
            lastHeader = vaultItemsList.addHeaderIfNeeded(
                context,
                headerProvider,
                lastHeader,
                viewTypeProvider
            )
            vaultItemsList.add(viewTypeProvider)
        }
        return vaultItemsList
    }

    private fun getHeaderProvider(
        sortMode: UnboundedListSortMode,
        items: List<SummaryObject>,
        context: Context
    ): HeaderProvider =
        when (sortMode) {
            UnboundedListSortMode.ALPHABETICAL -> FirstLetterHeaderProvider
            UnboundedListSortMode.CATEGORY -> CategoryHeaderProvider(loadCategoriesFor(items, context))
            UnboundedListSortMode.MOST_RECENT -> RecentHeaderProvider
        }

    private fun getHeaderProvider(sortMode: BoundedListSortMode): HeaderProvider =
        when (sortMode) {
            BoundedListSortMode.SUGGESTED -> SuggestedHeaderProvider
            BoundedListSortMode.MOST_RECENT -> RecentHeaderProvider
        }

    private fun loadCategoriesFor(list: List<SummaryObject>, context: Context): Map<SyncObjectType, String?> {
        
        return list.map { it.syncObjectType }
            .distinct()
            .associateWith {
                context.getString(
                    DataIdentifierTypeTextFactory.getStringResId(it)
                )
            }
    }

    private fun ArrayList<DashlaneRecyclerAdapter.ViewTypeProvider>.addHeaderIfNeeded(
        context: Context,
        headerProvider: HeaderProvider,
        lastHeader: String?,
        item: DashlaneRecyclerAdapter.ViewTypeProvider
    ): String? {
        val newHeader = headerProvider.getHeaderFor(context, item, identityNameHolderService)
        if (newHeader != null && (lastHeader == null || lastHeader != newHeader)) {
            add(HeaderItem(newHeader))
        }
        return newHeader ?: lastHeader
    }
}
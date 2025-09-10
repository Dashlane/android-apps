@file:OptIn(ExperimentalCoroutinesApi::class)

package com.dashlane.home.vaultlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.home.vaultlist.ListItemState.VaultItemState
import com.dashlane.navigation.Navigator
import com.dashlane.quickaction.QuickActionProvider
import com.dashlane.securefile.extensions.hasAttachments
import com.dashlane.sync.DataSync
import com.dashlane.sync.DataSyncState
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.adapter.ItemListContext.Container
import com.dashlane.ui.adapter.util.toHighlight
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.vault.Filter
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.VaultItemsRepository
import com.dashlane.vault.item.VaultListItemState
import com.dashlane.vault.item.VaultListItemStateFactory
import com.dashlane.vault.item.comparator.alphabeticComparator
import com.dashlane.vault.item.comparator.mostRecentAccessTimeComparator
import com.dashlane.vault.item.comparator.summaryObjectTypeComparator
import com.dashlane.vault.item.header.getAlphabeticalHeader
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.identity.IdentityNameHolderService
import com.dashlane.vault.toItemType
import com.dashlane.vault.util.isProtected
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@Suppress("LargeClass")
@HiltViewModel
class VaultListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vaultListItemStateFactory: VaultListItemStateFactory,
    private val identityNameHolderService: IdentityNameHolderService,
    private val vaultItemsRepository: VaultItemsRepository,
    private val vaultListResourceProvider: VaultListResourceProvider,
    private val navigator: Navigator,
    private val vaultItemCopyService: VaultItemCopyService,
    private val quickActionProvider: QuickActionProvider,
    private val dataSync: DataSync,
    @DefaultCoroutineDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
    private val vaultItemLogger: VaultItemLogger,
) : ViewModel() {
    private val filter: Filter by lazy {
        savedStateHandle[EXTRA_FILTER] ?: Filter.ALL_VISIBLE_VAULT_ITEM_TYPES
    }

    private val extraContentJobs = mutableMapOf<String, Job>()
    private val _state = MutableStateFlow(VaultListState())
    val state: StateFlow<VaultListState> = _state.asStateFlow()

    init {
        observeVaultItems()
    }

    private fun observeVaultItems() {
        vaultItemsRepository.vaultItems
            .onStart { _state.update { it.copy(isLoading = true) } }
            .mapLatest { items ->
                items.asSequence()
                    .filter { filter.contains(it.syncObjectType) }
            }
            .onEach { _state.update { it.copy(isRefreshing = false) } }
            .distinctUntilChanged()
            .mapLatest { items ->
                items.buildVaultListItem()
            }
            .catch {
                    tag = "VAULT_LIST",
                    message = "Error loading vault items",
                    throwable = it,
                )
            }
            .onEach { items ->
                val isSyncRunning =
                    dataSync.dataSyncState.replayCache.firstOrNull() is DataSyncState.Active

                if (items.isEmpty()) {
                    
                    
                    if (!isSyncRunning || state.value.items.isNotEmpty()) {
                        _state.update { state ->
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                items = emptyList(),
                                emptyState = buildEmptyState(),
                            )
                        }
                    }

                    return@onEach
                }

                _state.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        items = items,
                        emptyState = null,
                    )
                }
                items.forEach {
                    if (it is VaultItemState) {
                        loadItemExtraContent(it.summaryObject)
                    }
                }
            }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { state ->
                state.copy(isRefreshing = true)
            }

            vaultItemsRepository.refresh()
        }
    }

    fun onItemClick(summaryItemId: String, itemListContext: ItemListContext) {
        viewModelScope.launch {
            getSummaryObject(summaryItemId)
                ?.let { summaryObject ->
                    vaultItemLogger.logSelect(
                        highlight = itemListContext.section.toHighlight(),
                        itemId = summaryObject.id,
                        index = itemListContext.positionInContainerSection,
                        itemType = summaryObject.syncObjectType.toItemType(),
                        totalCount = itemListContext.sectionCount,
                    )

                    navigator.goToItem(summaryItemId, summaryObject.syncObjectType.xmlObjectName)
                }
        }
    }

    fun onLongClick(summaryItemId: String, itemListContext: ItemListContext) {
        onMoreClicked(summaryItemId, itemListContext)
    }

    fun onCopyClicked(summaryItemId: String, itemListContext: ItemListContext) {
        viewModelScope.launch {
            getSummaryObject(summaryItemId)
                ?.let { summaryObject ->
                    vaultItemCopyService.handleCopy(
                        item = summaryObject,
                        copyField = CopyField.Password,
                        itemListContext = itemListContext,
                    )
                }
        }
    }

    fun onMoreClicked(summaryItemId: String, itemListContext: ItemListContext) {
        viewModelScope.launch {
            navigator.goToQuickActions(summaryItemId, itemListContext)
        }
    }

    private fun loadItemExtraContent(item: SummaryObject) {
        extraContentJobs[item.id]?.cancel()
        extraContentJobs[item.id] = viewModelScope.launch(defaultDispatcher) {
            val isAttachmentIconVisible = item.hasAttachments()
            val isSharedIconVisible = item.isShared
            val isLockIconVisible = item.isProtected
            val isPasskeyIconVisible = item is SummaryObject.Passkey
            val isCopyButtonVisible = vaultItemCopyService.hasContent(item, CopyField.Password)
            val isMoreButtonVisible = quickActionProvider.hasQuickActions(item)

            _state.update { state ->
                state.copy(
                    items = state.items.map {
                        if (it is VaultItemState && it.summaryObject.id == item.id) {
                            it.copy(
                                vaultItemState = it.vaultItemState.copy(
                                    isAttachmentIconVisible = isAttachmentIconVisible,
                                    isSharedIconVisible = isSharedIconVisible,
                                    isLockIconVisible = isLockIconVisible,
                                    isPasskeyIconVisible = isPasskeyIconVisible,
                                    isCopyButtonVisible = isCopyButtonVisible,
                                    isMoreButtonVisible = isMoreButtonVisible,
                                    extraContentLoaded = true,
                                ),
                            )
                        } else {
                            it
                        }
                    },
                ).also { extraContentJobs.remove(item.id) }
            }
        }
    }

    private suspend fun getSummaryObject(summaryItemId: String): SummaryObject? =
        vaultItemsRepository.vaultItems.firstOrNull()
            ?.firstOrNull { it.id == summaryItemId }

    private fun Sequence<SummaryObject>.buildVaultListItem(): List<ListItemState> {
        val baseItemListContext = ItemListContext(
            container = when (filter) {
                Filter.ALL_VISIBLE_VAULT_ITEM_TYPES -> Container.ALL_ITEMS
                Filter.FILTER_PASSWORD -> Container.CREDENTIALS_LIST
                Filter.FILTER_SECURE_NOTE -> Container.SECURE_NOTE_LIST
                Filter.FILTER_PAYMENT -> Container.PAYMENT_LIST
                Filter.FILTER_PERSONAL_INFO -> Container.PERSONAL_INFO_LIST
                Filter.FILTER_ID -> Container.IDS_LIST
                Filter.FILTER_SECRET -> Container.SECRETS_LIST
                Filter.FILTER_WIFI -> Container.NONE
            },
        )

        val sortMode = getAppropriateSortMode(count(), filter)
        val grouped = when (sortMode) {
            VaultListMode.ALPHABETICAL ->
                sortedWith(alphabeticComparator(identityNameHolderService))
                    .groupBy { it.getAlphabeticalHeader(identityNameHolderService) }

            VaultListMode.CATEGORY ->
                sortedWith(summaryObjectTypeComparator())
                    .groupBy { getCategoryHeader(it) }
                    .mapValues { (_, items) ->
                        items.sortedWith(alphabeticComparator(identityNameHolderService))
                    }

            VaultListMode.MOST_RECENT ->
                sortedWith(mostRecentAccessTimeComparator())
                    .groupBy { vaultListResourceProvider.vaultListMostRecentHeader }
        }

        val listBaseItemListContext = baseItemListContext.copy(
            section = when (sortMode) {
                VaultListMode.ALPHABETICAL -> ItemListContext.Section.ALPHABETICAL
                VaultListMode.CATEGORY -> ItemListContext.Section.CATEGORY
                VaultListMode.MOST_RECENT -> ItemListContext.Section.MOST_RECENT
            },
        )

        val listItemState = grouped
            .map { (header, items) ->
                buildList {
                    add(ListItemState.HeaderItemState(title = header))
                    addAll(
                        items.mapIndexed { index, summaryObject ->
                            VaultItemState(
                                key = summaryObject.id,
                                vaultItemState = vaultListItemStateFactory.buildVaultListItemState(
                                    summaryObject,
                                ),
                                summaryObject = summaryObject,
                                itemListContext = listBaseItemListContext.copy(
                                    indexInContainerSection = index,
                                    sectionCount = items.size,
                                ),
                            )
                        },
                    )
                }
            }
            .flatten()

        return buildHighlightedList(baseItemListContext) + listItemState
    }

    private fun Sequence<SummaryObject>.buildHighlightedList(baseItemListContext: ItemListContext): List<ListItemState> {
        if (count() < ITEM_COUNT_THRESHOLD) {
            return emptyList()
        }

        val highlightedListMode: HighlightedListMode = getAppropriateHighlightedListMode(filter)
        val highlightedList = when (highlightedListMode) {
            HighlightedListMode.SUGGESTED -> getSuggestedList(highlightedListMode.limit)
            HighlightedListMode.MOST_RECENT -> getRecentList(highlightedListMode.limit)
        }

        val highlightedListSortedAndGrouped =
            highlightedList.sortedWith(mostRecentAccessTimeComparator())
                .groupBy {
                    when (highlightedListMode) {
                        HighlightedListMode.SUGGESTED -> vaultListResourceProvider.vaultListSuggestedHeader
                        HighlightedListMode.MOST_RECENT -> vaultListResourceProvider.vaultListMostRecentHeader
                    }
                }

        val highlightedBaseItemListContext = baseItemListContext.copy(
            section = when (highlightedListMode) {
                HighlightedListMode.SUGGESTED -> ItemListContext.Section.SUGGESTED
                HighlightedListMode.MOST_RECENT -> ItemListContext.Section.MOST_RECENT
            },
        )

        return highlightedListSortedAndGrouped
            .map { (header, items) ->
                buildList {
                    add(ListItemState.HeaderItemState(title = header))
                    addAll(
                        items.mapIndexed { index, summaryObject ->
                            VaultItemState(
                                key = "highlighted_${summaryObject.id}",
                                vaultItemState = vaultListItemStateFactory.buildVaultListItemState(
                                    summaryObject,
                                ),
                                summaryObject = summaryObject,
                                itemListContext = highlightedBaseItemListContext.copy(
                                    indexInContainerSection = index,
                                    sectionCount = items.size,
                                ),
                            )
                        },
                    )
                }
            }
            .flatten()
    }

    private fun getAppropriateSortMode(count: Int, filter: Filter): VaultListMode =
        if (count < ITEM_COUNT_THRESHOLD) {
            VaultListMode.MOST_RECENT
        } else {
            when (filter) {
                Filter.ALL_VISIBLE_VAULT_ITEM_TYPES,
                Filter.FILTER_PASSWORD,
                Filter.FILTER_SECURE_NOTE,
                Filter.FILTER_SECRET,
                Filter.FILTER_WIFI,
                -> VaultListMode.ALPHABETICAL
                Filter.FILTER_PAYMENT,
                Filter.FILTER_PERSONAL_INFO,
                Filter.FILTER_ID,
                -> VaultListMode.CATEGORY
            }
        }

    private fun getAppropriateHighlightedListMode(filter: Filter): HighlightedListMode =
        when {
            filter == Filter.ALL_VISIBLE_VAULT_ITEM_TYPES -> HighlightedListMode.SUGGESTED
            else -> HighlightedListMode.MOST_RECENT
        }

    private fun Sequence<SummaryObject>.getSuggestedList(limit: Int): Sequence<SummaryObject> {
        val mostUsed = filter { it.locallyUsedCount > 0 }
            .sortedByDescending { it.locallyUsedCount }
            .take(limit.div(2))
        val mostRecent = sortedWith(mostRecentAccessTimeComparator())
            .take(limit - mostUsed.count())

        val suggestedList = buildSet {
            addAll(mostUsed)
            addAll(mostRecent)
        }

        return suggestedList.asSequence().filterByLocalUsage(limit)
    }

    private fun Sequence<SummaryObject>.getRecentList(limit: Int): Sequence<SummaryObject> =
        sortedWith(mostRecentAccessTimeComparator()).take(limit).filterByLocalUsage(limit)

    private fun Sequence<SummaryObject>.filterByLocalUsage(limit: Int): Sequence<SummaryObject> {
        
        val localUpdatedDateRecent =
            filter { it.locallyUsedCount > 0 }.sortedByDescending { it.locallyViewedDate }

        
        if (localUpdatedDateRecent.count() == 0) {
            return this
        }

        
        if (localUpdatedDateRecent.count() > limit) {
            return localUpdatedDateRecent.take(limit)
        }

        
        
        return (localUpdatedDateRecent + this).distinct().take(limit)
    }

    
    private fun buildEmptyState(): VaultListEmptyState = when (filter) {
        Filter.ALL_VISIBLE_VAULT_ITEM_TYPES -> VaultListEmptyState(
            icon = IconTokens.itemLoginOutlined,
            title = vaultListResourceProvider.vaultListEmptyAllTitle,
            description = vaultListResourceProvider.vaultListEmptyAllDescription,
        )
        Filter.FILTER_PASSWORD -> VaultListEmptyState(
            icon = IconTokens.itemLoginOutlined,
            title = vaultListResourceProvider.vaultListEmptyPasswordTitle,
            description = vaultListResourceProvider.vaultListEmptyPasswordDescription,
        )
        Filter.FILTER_SECURE_NOTE -> VaultListEmptyState(
            icon = IconTokens.itemSecureNoteOutlined,
            title = vaultListResourceProvider.vaultListEmptySecureNoteTitle,
            description = vaultListResourceProvider.vaultListEmptySecureNoteDescription,
        )
        Filter.FILTER_PAYMENT -> VaultListEmptyState(
            icon = IconTokens.itemPaymentOutlined,
            title = vaultListResourceProvider.vaultListEmptyPaymentTitle,
            description = vaultListResourceProvider.vaultListEmptyPaymentDescription,
        )
        Filter.FILTER_PERSONAL_INFO -> VaultListEmptyState(
            icon = IconTokens.itemPersonalInfoOutlined,
            title = vaultListResourceProvider.vaultListEmptyPersonalInfoTitle,
            description = vaultListResourceProvider.vaultListEmptyPersonalInfoDescription,
        )
        Filter.FILTER_ID -> VaultListEmptyState(
            icon = IconTokens.itemIdOutlined,
            title = vaultListResourceProvider.vaultListEmptyIdTitle,
            description = vaultListResourceProvider.vaultListEmptyIdDescription,
        )
        Filter.FILTER_SECRET -> VaultListEmptyState(
            icon = IconTokens.itemSecretOutlined,
            title = vaultListResourceProvider.vaultListEmptySecretTitle,
            description = vaultListResourceProvider.vaultListEmptySecretDescription,
        )
        Filter.FILTER_WIFI -> VaultListEmptyState(
            icon = IconTokens.itemWifiOutlined,
            title = vaultListResourceProvider.vaultListEmptyWiFiTitle,
            description = vaultListResourceProvider.vaultListEmptyWiFiDescription,
        )
    }

    private fun getCategoryHeader(summaryObject: SummaryObject): String =
        vaultListResourceProvider.getCategoryHeader(summaryObject.syncObjectType)

    

    private companion object {
        const val EXTRA_FILTER = "extra_filter"
        const val ITEM_COUNT_THRESHOLD = 7
    }

    private enum class VaultListMode {
        ALPHABETICAL,
        CATEGORY,
        MOST_RECENT,
    }

    private enum class HighlightedListMode(val limit: Int) {
        SUGGESTED(6),
        MOST_RECENT(3),
    }
}

data class VaultListState(
    val isRefreshing: Boolean = false,
    val isLoading: Boolean = false,
    val items: List<ListItemState> = emptyList(),
    val emptyState: VaultListEmptyState? = null,
)

data class VaultListEmptyState(
    val icon: IconToken,
    val title: String,
    val description: String,
)

sealed class ListItemState {
    abstract val key: String

    data class HeaderItemState(
        val title: String,
        override val key: String = title,
    ) : ListItemState()

    data class VaultItemState(
        val vaultItemState: VaultListItemState,
        val itemListContext: ItemListContext,
        val summaryObject: SummaryObject,
        override val key: String,
    ) : ListItemState()
}
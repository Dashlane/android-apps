package com.dashlane.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.quickaction.QuickActionProvider
import com.dashlane.search.helper.SearchResultHelper
import com.dashlane.securefile.extensions.hasAttachments
import com.dashlane.storage.userdata.accessor.FrequentSearch
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.vault.VaultItemsRepository
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.isProtected
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchLogger: SearchLogger,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val searchResultHelper: SearchResultHelper,
    private val quickActionProvider: QuickActionProvider,
    private val vaultItemCopyService: VaultItemCopyService,
    private val vaultItemsRepository: VaultItemsRepository,
    private val frequentSearch: FrequentSearch,
) : ViewModel() {
    private val searchChannel = Channel<SearchRequest>(
        capacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val stateFlow: MutableViewStateFlow<SearchState.ViewState, SearchState.SideEffect> =
        MutableViewStateFlow(
            SearchState.ViewState.SearchResult(
                isLoading = true,
                searchRequest = SearchRequest.FromRecent,
                items = emptyList(),
            ),
        )
    val searchState: ViewStateFlow<SearchState.ViewState, SearchState.SideEffect>
        get() = stateFlow

    init {
        
        searchChannel.consumeAsFlow()
            .flowOn(defaultDispatcher)
            .flatMapLatest { request ->
                
                searchFlow(request = request)
            }
            .onEach { viewState -> stateFlow.update { viewState } }
            .launchIn(viewModelScope)

        
        viewModelScope.launch {
            vaultItemsRepository.vaultItems.collect {
                searchChannel.send(searchState.viewState.value.searchRequest)
            }
        }
    }

    override fun onCleared() {
        onClose()
    }

    private fun searchFlow(request: SearchRequest): Flow<SearchState.ViewState> = flow {
        stateFlow.update {
            SearchState.ViewState.SearchResult(
                isLoading = true,
                searchRequest = request,
                items = it.items,
            )
        }

        val vaultItems = vaultItemsRepository.vaultItems.replayCache.firstOrNull() ?: emptyList()

        val searchList = when (request) {
            is SearchRequest.FromQuery -> {
                searchResultHelper.buildFromQuery(
                    vaultItems = vaultItems,
                    queryRequest = request,
                )
            }

            is SearchRequest.FromRecent -> searchResultHelper.buildRecentSearchResult()
        }
        stateFlow.update {
            if (searchList.isEmpty() && request is SearchRequest.FromQuery) {
                SearchState.ViewState.EmptyState(
                    isLoading = false,
                    searchRequest = request,
                )
            } else {
                SearchState.ViewState.SearchResult(
                    isLoading = false,
                    searchRequest = request,
                    items = searchList,
                )
            }
        }
    }

    fun showRecentSearches() {
        viewModelScope.launch {
            searchChannel.send(SearchRequest.FromRecent)
        }
    }

    fun searchFromQuery(query: String) {
        searchChannel.trySend(SearchRequest.FromQuery(query))
    }

    fun onVaultItemClick(item: SearchListItem.Item.Vault) {
        viewModelScope.launch {
            val state = stateFlow.value
            (state.searchRequest as? SearchRequest.FromQuery)?.let { request ->
                searchLogger.logClick(
                    typedCharCount = request.query.charCount(),
                    resultCount = state.items.filterNot { it is SearchListItem.Header }.size,
                )
                markItemAsSearched(item)
            }
            stateFlow.send(
                SearchState.SideEffect.OpenItemDetails(
                    itemId = item.summaryObject.id,
                    itemType = item.summaryObject.syncObjectType,
                ),
            )
        }
    }

    fun onCopyActionClick(item: SearchListItem.Item.Vault) {
        viewModelScope.launch {
            if (item.summaryObject.syncObjectType != SyncObjectType.AUTHENTIFIANT) {
                return@launch
            }
            markItemAsSearched(item)
            vaultItemCopyService.handleCopy(item.summaryObject, CopyField.Password)
        }
    }

    fun onMoreActionClick(item: SearchListItem.Item.Vault, itemListContext: ItemListContext) {
        viewModelScope.launch {
            markItemAsSearched(item)
            stateFlow.send(
                SearchState.SideEffect.OpenQuickActions(
                    item.itemState.itemId,
                    itemListContext,
                ),
            )
        }
    }

    fun onClose() {
        val state = searchState.viewState.value
        val searchRequest = (state.searchRequest as? SearchRequest.FromQuery) ?: return

        searchLogger.logClose(
            typedCharCount = searchRequest.query.charCount(),
            resultCount = state.items.filterNot { it is SearchListItem.Header }.size,
        )
    }

    fun loadItemExtraContent(item: SummaryObject) {
        viewModelScope.launch {
            val isAttachmentIconVisible = item.hasAttachments()
            val isSharedIconVisible = item.isShared
            val isLockIconVisible = item.isProtected
            val isPasskeyIconVisible = item is SummaryObject.Passkey
            val isCopyButtonVisible = vaultItemCopyService.hasContent(item, CopyField.Password)
            val isMoreButtonVisible = quickActionProvider.hasQuickActions(item)

            stateFlow.update { state ->
                if (state !is SearchState.ViewState.SearchResult) {
                    return@launch
                }
                state.copy(
                    items = state.items.map {
                        if (it is SearchListItem.Item.Vault && it.itemState.itemId == item.id) {
                            it.copy(
                                itemState = it.itemState.copy(
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
                )
            }
        }
    }

    private suspend fun markItemAsSearched(item: SearchListItem.Item.Vault) {
        frequentSearch.markedAsSearched(
            itemId = item.summaryObject.id,
            syncObjectType = item.summaryObject.syncObjectType,
        )
    }
}

private fun String?.charCount() = this?.length ?: 0
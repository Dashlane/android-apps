package com.dashlane.ui.activities.fragments.vault.list

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.events.AppEvents
import com.dashlane.events.registerAsFlow
import com.dashlane.navigation.Navigator
import com.dashlane.sync.DataSync
import com.dashlane.sync.DataSyncState
import com.dashlane.ui.activities.fragments.vault.ScrollToTopEvent
import com.dashlane.ui.activities.fragments.vault.list.VaultListDataProvider.BoundedListSortMode
import com.dashlane.ui.activities.fragments.vault.list.VaultListDataProvider.UnboundedListSortMode
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.widgets.view.empty.EmptyScreenViewProvider
import com.dashlane.ui.widgets.view.empty.IDsEmptyScreen
import com.dashlane.ui.widgets.view.empty.PasswordsEmptyScreen
import com.dashlane.ui.widgets.view.empty.PaymentsEmptyScreen
import com.dashlane.ui.widgets.view.empty.PersonalInfoEmptyScreen
import com.dashlane.ui.widgets.view.empty.SecureNotesEmptyScreen
import com.dashlane.ui.widgets.view.empty.VaultAllItemsEmptyScreen
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.home.vaultlist.Filter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VaultListViewModel @Inject constructor(
    private val provider: VaultList.DataProvider,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    val appEvents: AppEvents,
    val navigator: Navigator,
    private val dataSync: DataSync,
    @field:SuppressLint("StaticFieldLeak") @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel(),
    VaultList.ViewModel {

    private val filter: Filter =
        savedStateHandle.get<Filter>(VaultListFragment.EXTRA_FILTER) ?: Filter.ALL_VISIBLE_VAULT_ITEM_TYPES

    private val vaultListMutableStateFlow = MutableStateFlow<VaultListState>(
        VaultListState.EmptyInfo(VaultListData(filter, emptyList(), true))
    )

    val vaultListStateFlow = vaultListMutableStateFlow.asStateFlow()

    val scrollToTopFlow = appEvents.registerAsFlow(
        this@VaultListViewModel,
        clazz = ScrollToTopEvent::class.java,
        deliverLastEvent = false
    ).shareIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0))

    private val isSyncRunning: Boolean
        get() = dataSync.dataSyncState.replayCache.firstOrNull() == DataSyncState.Active

    override fun onRefresh() {
        provider.syncData()
    }

    fun refreshItemList(allItems: List<SummaryObject>) {
        viewModelScope.launch(mainCoroutineDispatcher) {
            val previousStateHasItems = vaultListMutableStateFlow.value.data.list.isNotEmpty()
            vaultListMutableStateFlow.update { currentState ->
                VaultListState.Refreshing(currentState.data.copy(isLoading = true))
            }

            val allItemsFiltered = withContext(defaultCoroutineDispatcher) {
                allItems.filter {
                    filter.contains(it.syncObjectType)
                }
            }

            if (allItemsFiltered.isEmpty()) {
                
                
                if (!isSyncRunning || previousStateHasItems) {
                    vaultListMutableStateFlow.update { currentState ->
                        VaultListState.EmptyInfo(
                            currentState.data.copy(isLoading = false),
                            displayEmptyInfo = true
                        )
                    }
                }
                return@launch
            }

            val allItemsSortMode = getAppropriateSortMode(allItemsFiltered, filter)
            val allItemsProvider =
                provider.generateViewTypeProviderList(
                    allItemsFiltered,
                    filter,
                    allItemsSortMode,
                    context
                )

            val highlightedItemsProvider = getHighlightedItemsProvider(allItemsFiltered)

            vaultListMutableStateFlow.update { currentState ->
                VaultListState.ItemList(
                    currentState.data.copy(
                        list = highlightedItemsProvider + allItemsProvider,
                        isLoading = false
                    )
                )
            }
        }
    }

    fun getEmptyScreenViewProvider(
        filter: Filter,
        alignTop: Boolean
    ): EmptyScreenViewProvider = when (filter) {
        Filter.ALL_VISIBLE_VAULT_ITEM_TYPES -> VaultAllItemsEmptyScreen.newInstance(
            context,
            alignTop
        )
        Filter.FILTER_PASSWORD -> PasswordsEmptyScreen.newInstance(context, alignTop)
        Filter.FILTER_SECURE_NOTE -> SecureNotesEmptyScreen.newInstance(
            context,
            alignTop
        )
        Filter.FILTER_PAYMENT -> PaymentsEmptyScreen.newInstance(context, alignTop)
        Filter.FILTER_PERSONAL_INFO -> PersonalInfoEmptyScreen.newInstance(
            context,
            alignTop
        )
        Filter.FILTER_ID -> IDsEmptyScreen.newInstance(context, alignTop)
    }

    private fun getAppropriateSortMode(
        allItems: List<SummaryObject>,
        filter: Filter
    ): UnboundedListSortMode =
        if (allItems.size < ITEM_COUNT_THRESHOLD) {
            UnboundedListSortMode.MOST_RECENT
        } else {
            if (filter == Filter.FILTER_PASSWORD ||
                filter == Filter.FILTER_SECURE_NOTE ||
                filter == Filter.ALL_VISIBLE_VAULT_ITEM_TYPES
            ) {
                UnboundedListSortMode.ALPHABETICAL
            } else {
                UnboundedListSortMode.CATEGORY
            }
        }

    private suspend fun getHighlightedItemsProvider(allItemsFiltered: List<SummaryObject>): List<DashlaneRecyclerAdapter.ViewTypeProvider> {
        val highlightedItemsSortMode = if (filter == Filter.ALL_VISIBLE_VAULT_ITEM_TYPES) {
            BoundedListSortMode.SUGGESTED
        } else {
            BoundedListSortMode.MOST_RECENT
        }
        return if (allItemsFiltered.size > ITEM_COUNT_THRESHOLD - 1) {
            provider.generateViewTypeProviderHighlightList(
                allItemsFiltered,
                filter,
                highlightedItemsSortMode,
                context
            )
        } else {
            emptyList()
        }
    }

    companion object {
        const val ITEM_COUNT_THRESHOLD = 7
    }
}
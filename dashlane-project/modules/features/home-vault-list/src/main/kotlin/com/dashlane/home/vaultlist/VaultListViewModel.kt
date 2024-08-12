package com.dashlane.home.vaultlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.vault.textfactory.list.DataIdentifierListTextResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject



@HiltViewModel
internal class VaultListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val genericDataQuery: GenericDataQuery,
    private val dataIdentifierListTextResolver: DataIdentifierListTextResolver
) : ViewModel() {

    private val filter: Filter by lazy {
        savedStateHandle.get<Filter>(EXTRA_FILTER) ?: Filter.ALL_VISIBLE_VAULT_ITEM_TYPES
    }

    
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val _state = MutableStateFlow(VaultListState())
    val state = _state.asStateFlow()

    fun viewStarted() {
        viewModelScope.launch {
            fetchItems()
        }
    }

    private suspend fun fetchItems() {
        
        withContext(ioDispatcher) {
            val vaultItems = genericDataQuery.queryAll(
                genericFilter {
                    specificDataType(filter)
                    forCurrentSpace()
                }
            )

            _state.update { state ->
                state.copy(
                    isLoading = false,
                    items = vaultItems.map {
                        ListItemState.VaultItemState(
                            id = it.id,
                            title = dataIdentifierListTextResolver.getLine1(it).text,
                        )
                    }
                )
            }
        }
    }

    private companion object {
        private const val EXTRA_FILTER = "extra_filter"
    }
}

internal data class VaultListState(
    val isLoading: Boolean = true,
    val items: List<ListItemState> = emptyList(),
    val isEmpty: Boolean = items.isEmpty(),
)

internal sealed class ListItemState {
    abstract val key: String

    data class HeaderItemState(
        val title: String,
    ) : ListItemState() {
        override val key: String = title
    }

    data class VaultItemState(
        val id: String,
        val title: String,
    ) : ListItemState() {
        override val key: String = id
    }
}
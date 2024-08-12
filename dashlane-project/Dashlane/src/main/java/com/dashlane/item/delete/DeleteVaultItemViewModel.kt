package com.dashlane.item.delete

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteVaultItemViewModel @Inject constructor(
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val provider: DeleteVaultItemProvider,
    private val logger: DeleteVaultItemLogger,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val navArgs = DeleteVaultItemFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private val stateFlow: MutableStateFlow<UiState>

    init {
        stateFlow = MutableStateFlow(
            if (navArgs.isShared) {
                UiState(itemId = navArgs.itemId, mode = WarningMode.SHARING, deleting = false)
            } else {
                UiState(itemId = navArgs.itemId, mode = WarningMode.NORMAL, deleting = false)
            }
        )
    }

    val uiState: StateFlow<UiState> = stateFlow.asStateFlow()
    private val navigationStateFlow = Channel<NavigationState>()
    val navigationState: Flow<NavigationState> = navigationStateFlow.receiveAsFlow()

    fun deleteItem() {
        logger.logItemDeletionConfirmed()
        viewModelScope.launch(ioDispatcher) {
            stateFlow.update { stateFlow.value.copy(deleting = true) }
            runCatching {
                val response = provider.deleteItem(stateFlow.value.itemId)
                if (response) {
                    navigationStateFlow.send(NavigationState.Success)
                } else {
                    navigationStateFlow.send(NavigationState.Failure)
                }
            }.onFailure {
                navigationStateFlow.send(NavigationState.Failure)
            }
        }
    }

    data class UiState(
        val itemId: String,
        val mode: WarningMode,
        val deleting: Boolean
    )

    sealed class NavigationState {
        data object Success : NavigationState()
        data object Failure : NavigationState()
    }

    enum class WarningMode { NORMAL, SHARING }
}
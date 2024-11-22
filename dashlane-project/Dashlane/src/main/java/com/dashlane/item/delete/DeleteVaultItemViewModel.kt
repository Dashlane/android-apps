package com.dashlane.item.delete

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.State
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.vault.item.delete.DeleteVaultItemLogger
import com.dashlane.vault.item.delete.DeleteVaultItemProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

@HiltViewModel
class DeleteVaultItemViewModel @Inject constructor(
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainCoroutineDispatcher private val mainDispatcher: CoroutineDispatcher,
    private val provider: DeleteVaultItemProvider,
    private val logger: DeleteVaultItemLogger,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val navArgs = DeleteVaultItemFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val _stateFlow: MutableViewStateFlow<UiState, NavigationState>
    val stateFlow: ViewStateFlow<UiState, NavigationState>
        get() = _stateFlow

    init {
        val initialUiState = if (navArgs.isShared) {
            UiState(itemId = navArgs.itemId, mode = WarningMode.SHARING, deleting = false)
        } else {
            UiState(itemId = navArgs.itemId, mode = WarningMode.NORMAL, deleting = false)
        }
        _stateFlow = MutableViewStateFlow(initialUiState)
    }

    fun deleteItem() {
        logger.logItemDeletionConfirmed()

        flow {
            val itemId = stateFlow.viewState.value.itemId
            val response = provider.deleteItem(stateFlow.viewState.value.itemId)
            emit(itemId to response)
        }
            .flowOn(ioDispatcher)
            .map { (itemId, response) -> if (response) NavigationState.Success(itemId) else NavigationState.Failure }
            .onStart { _stateFlow.update { state -> state.copy(deleting = true) } }
            .catch { emit(NavigationState.Failure) }
            .flowOn(mainDispatcher)
            .onEach { navigationState ->
                _stateFlow.update { state -> state.copy(deleting = false) }
                _stateFlow.send(navigationState)
            }
            .launchIn(viewModelScope)
    }

    data class UiState(
        val itemId: String,
        val mode: WarningMode,
        val deleting: Boolean
    ) : State.View

    sealed class NavigationState : State.SideEffect {
        data class Success(val itemId: String) : NavigationState()
        data object Failure : NavigationState()
    }

    enum class WarningMode { NORMAL, SHARING }
}
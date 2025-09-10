package com.dashlane.wear.vault.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authenticator.Totp
import com.dashlane.authenticator.otp
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.State
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.wear.vault.WearVaultItem
import com.dashlane.wear.vault.details.WearItemDetailsState.View.Loading
import com.dashlane.wear.vault.toWearVaultItem
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class WearItemDetailViewModel @Inject constructor(
    private val vaultDataQuery: VaultDataQuery,
) : ViewModel() {
    private val _stateFlow = MutableViewStateFlow<WearItemDetailsState.View, WearItemDetailsState.SideEffect>(Loading)
    val stateFlow: ViewStateFlow<WearItemDetailsState.View, WearItemDetailsState.SideEffect> = _stateFlow

    fun loadItem(itemId: String) {
        flow<WearItemDetailsState> {
            val item = vaultDataQuery.query(
                vaultFilter {
                    specificUid(itemId)
                },
            ) ?: throw IllegalStateException("Item is null")
            val otp = (item.syncObject as? SyncObject.Authentifiant)?.otp()
            
            item.toSummary<SummaryObject>().toWearVaultItem(otp as? Totp)?.let {
                emit(WearItemDetailsState.View.Ready(it))
            }
        }.onStart {
            emit(Loading)
        }.catch { throwable ->
            emit(WearItemDetailsState.View.Error)
        }.onEach { state ->
            when (state) {
                is WearItemDetailsState.View -> _stateFlow.update { state }
                is WearItemDetailsState.SideEffect -> _stateFlow.send(state)
            }
        }.launchIn(viewModelScope)
    }
}

sealed class WearItemDetailsState : State {
    sealed class View : WearItemDetailsState(), State.View {
        data object Loading : View()

        data class Ready(val item: WearVaultItem) : View()

        data object Error : View()
    }

    sealed class SideEffect : WearItemDetailsState(), State.SideEffect
}
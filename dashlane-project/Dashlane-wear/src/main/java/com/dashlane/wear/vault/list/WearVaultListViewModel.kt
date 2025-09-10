package com.dashlane.wear.vault.list

import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.devicediscovery.MobileExchangeClient
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.State
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.sync.DataSyncHelper
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.wear.TypeSupportUtil.supportedVaultItemTypes
import com.dashlane.wear.vault.WearVaultItem
import com.dashlane.wear.vault.list.WearVaultState.SideEffect.GoToItem
import com.dashlane.wear.vault.list.WearVaultState.SideEffect.GoToSettings
import com.dashlane.wear.vault.list.WearVaultState.SideEffect.OpenSupportedServices
import com.dashlane.wear.vault.toWearVaultItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class WearVaultListViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val syncHelper: DataSyncHelper,
    private val vaultDataQuery: VaultDataQuery,
    private val accountStatusRepository: AccountStatusRepository,
    private val mobileExchangeClient: MobileExchangeClient,
) : ViewModel() {
    private var lastSyncTime = syncHelper.lastSyncTime ?: Instant.EPOCH

    private val _stateFlow =
        MutableViewStateFlow<WearVaultState.View, WearVaultState.SideEffect>(
            WearVaultState.View(items = emptyList()),
        )
    val stateFlow: ViewStateFlow<WearVaultState.View, WearVaultState.SideEffect> = _stateFlow

    fun onViewStarted() {
        val currentSession = sessionManager.session
        requireNotNull(currentSession)

        viewModelScope.launch {
            
            accountStatusRepository.refreshFor(currentSession)
            trySync(currentSession)
        }
        reloadVaultList()
    }

    private fun reloadVaultList() {
        flow<WearVaultState> {
            val items = vaultDataQuery
                .queryAll(vaultFilter { specificDataType(supportedVaultItemTypes) })
                .map { it.toSummary<SummaryObject>() }
            emit(
                _stateFlow.value.copy(
                    items = items.mapNotNull { it.toWearVaultItem() },
                    showSupportedServicesAction = canShowSupportedServicesAction(),
                    isLoading = false,
                    error = null,
                ),
            )
        }.onStart {
            emit(_stateFlow.value.copy(isLoading = true, error = null))
        }.catch { throwable ->
            emit(_stateFlow.value.copy(isLoading = false, error = "Session was not loaded"))
        }.onEach { state ->
            when (state) {
                is WearVaultState.View -> _stateFlow.update { state }
                is WearVaultState.SideEffect -> {
                    _stateFlow.send(state)
                }
            }
        }.launchIn(viewModelScope)
    }

    private suspend fun canShowSupportedServicesAction(): Boolean =
        mobileExchangeClient.listPairedDevices().any { it.isNearby }

    private suspend fun trySync(session: Session) {
        
        if (lastSyncTime.isAfter(Instant.now().minus(5, ChronoUnit.MINUTES))) {
            return
        }
        _stateFlow.update { it.copy(isLoading = true, error = null) }
        runCatching {
            syncHelper.runSync(session = session, origin = Trigger.LOGIN)
            lastSyncTime = Instant.now()
        }.onFailure {
        }
        reloadVaultList()
    }

    fun onItemClick(itemId: String) {
        viewModelScope.launch {
            _stateFlow.send(GoToItem(itemId = itemId))
        }
    }

    fun onCheckSupportedServicesClick() {
        viewModelScope.launch {
            _stateFlow.send(
                OpenSupportedServices(
                    mobileIntent = Intent(Intent.ACTION_VIEW)
                        .addCategory(Intent.CATEGORY_BROWSABLE)
                        .setData(SUPPORTED_SERVICES_URL.toUri()),
                ),
            )
        }
    }

    fun onSettingClick() {
        viewModelScope.launch {
            _stateFlow.send(GoToSettings)
        }
    }

    companion object {
        private const val SUPPORTED_SERVICES_URL = "https://passkeys-directory.dashlane.com/"
    }
}

sealed class WearVaultState : State {
    data class View(
        val items: List<WearVaultItem>,
        val isLoading: Boolean = false,
        val error: String? = null,
        val showSupportedServicesAction: Boolean = false,
    ) : WearVaultState(), State.View

    sealed class SideEffect : WearVaultState(), State.SideEffect {
        data class GoToItem(val itemId: String) : SideEffect()

        data class OpenSupportedServices(val mobileIntent: Intent) : SideEffect()

        data object GoToSettings : SideEffect()
    }
}
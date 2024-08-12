package com.dashlane.secrettransfer.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.user.UserAccountInfo
import com.dashlane.account.UserAccountStorage
import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.secrettransfer.GetKeyExchangeTransferInfoService
import com.dashlane.session.SessionManager
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting

const val SALT = "AXbCCLBYulWaVNWT/YfT+SiuhBOlFqLFaPPI5/8XIio="

@HiltViewModel
class SecretTransferViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val userAccountStorage: UserAccountStorage,
    private val getKeyExchangeTransferInfoService: GetKeyExchangeTransferInfoService,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val stateFlow = MutableStateFlow<SecretTransferState>(SecretTransferState.Initial)
    val uiState = stateFlow.asStateFlow()

    fun viewStarted() {
        if (stateFlow.value is SecretTransferState.Initial) {
            val isPasswordless = sessionManager.session?.username
                ?.let { username -> userAccountStorage[username]?.accountType }
                .let { accountType -> accountType is UserAccountInfo.AccountType.InvisibleMasterPassword }

            if (isPasswordless) {
                checkPendingTransfer()
            } else {
                viewModelScope.launch { stateFlow.emit(SecretTransferState.GoToIntro(false)) }
            }
        }
    }

    fun onRefreshClicked() {
        checkPendingTransfer()
    }

    @VisibleForTesting
    fun checkPendingTransfer() {
        flow {
            val data = getKeyExchangeTransferInfo()
            emit(data.transfer)
        }
            .flowOn(ioDispatcher)
            .map { transfer ->
                if (transfer == null) {
                    SecretTransferState.GoToIntro(true)
                } else {
                    SecretTransferState.ShowTransfer(transfer = transfer.toSecretTransfer())
                }
            }
            .flowOn(defaultDispatcher)
            .catch {
                emit(SecretTransferState.GoToIntro(true))
            }
            .onStart { emit(SecretTransferState.Loading) }
            .onEach { state -> stateFlow.emit(state) }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    suspend fun getKeyExchangeTransferInfo(): GetKeyExchangeTransferInfoService.Data {
        val session = sessionManager.session ?: throw IllegalStateException("Session was null")
        val response = getKeyExchangeTransferInfoService.execute(session.authorization)
        return response.data
    }
}

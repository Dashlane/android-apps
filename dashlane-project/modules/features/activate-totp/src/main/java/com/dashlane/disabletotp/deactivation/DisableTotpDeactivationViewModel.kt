package com.dashlane.disabletotp.deactivation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.account.UserAccountStorage
import com.dashlane.account.UserSecuritySettings
import com.dashlane.activatetotp.ActivateTotpAuthenticatorConnection
import com.dashlane.activatetotp.ActivateTotpServerKeyChanger
import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.authentication.AuthTotpDeactivationService
import com.dashlane.server.api.endpoints.authentication.AuthVerificationTotpService
import com.dashlane.session.SessionManager
import com.dashlane.session.isServerKeyNotNull
import com.dashlane.ui.screens.settings.Use2faSettingStateRefresher
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
internal class DisableTotpDeactivationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sessionManager: SessionManager,
    private val authVerificationTotpService: AuthVerificationTotpService,
    private val authTotpDeactivationService: AuthTotpDeactivationService,
    private val userAccountStorage: UserAccountStorage,
    private val use2faSettingStateRefresher: Use2faSettingStateRefresher,
    private val activateTotpServerKeyChanger: ActivateTotpServerKeyChanger,
    private val activateTotpAuthenticatorConnection: ActivateTotpAuthenticatorConnection,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val mutableSharedFlow = MutableSharedFlow<DisableTotpDeactivationState>(replay = 1)
    val sharedFlow = mutableSharedFlow.asSharedFlow()

    fun disableTotp() {
        flow<DisableTotpDeactivationState> {
            val session = requireNotNull(sessionManager.session) { "session == null" }
            val otp = DisableTotpDeactivationFragmentArgs.fromSavedStateHandle(savedStateHandle).otp

            val authTicket =
                authVerificationTotpService.execute(AuthVerificationTotpService.Request(login = session.userId, otp = otp)).data.authTicket

            if (session.appKey.isServerKeyNotNull) {
                
                activateTotpServerKeyChanger.updateServerKey(newServerKey = null, authTicket)
            } else {
                
                authTotpDeactivationService.execute(
                    userAuthorization = session.authorization,
                    request = AuthTotpDeactivationService.Request(authTicket = AuthTotpDeactivationService.Request.AuthTicket(authTicket))
                )

                userAccountStorage.saveSecuritySettings(
                    username = session.username,
                    securitySettings = UserSecuritySettings(isToken = true)
                )
            }

            use2faSettingStateRefresher.refresh()
            activateTotpAuthenticatorConnection.deleteDashlaneTokenAsync(session.userId).await()

            emit(DisableTotpDeactivationState.Success)
        }
            .catch { error -> emit(DisableTotpDeactivationState.Error(error = error)) }
            .onEach { state -> mutableSharedFlow.emit(state) }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
    }
}

internal sealed class DisableTotpDeactivationState {
    object Success : DisableTotpDeactivationState()
    data class Error(val error: Throwable) : DisableTotpDeactivationState()
}

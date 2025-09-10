package com.dashlane.disabletotp.deactivation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.account.UserAccountStorage
import com.dashlane.authentication.twofa.Use2faSettingStateRefresher
import com.dashlane.changemasterpassword.MasterPasswordChanger
import com.dashlane.crypto.keys.isServerKeyNotNull
import com.dashlane.server.api.endpoints.authentication.DeactivateTOTPService
import com.dashlane.server.api.endpoints.authentication.PerformTotpVerificationService
import com.dashlane.session.SessionManager
import com.dashlane.session.authorization
import com.dashlane.user.UserSecuritySettings
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
internal class DisableTotpDeactivationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sessionManager: SessionManager,
    private val performTotpVerificationService: PerformTotpVerificationService,
    private val deactivateTOTPService: DeactivateTOTPService,
    private val userAccountStorage: UserAccountStorage,
    private val use2faSettingStateRefresher: Use2faSettingStateRefresher,
    private val masterPasswordChanger: MasterPasswordChanger,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val mutableSharedFlow = MutableSharedFlow<DisableTotpDeactivationState>(replay = 1)
    val sharedFlow = mutableSharedFlow.asSharedFlow()

    fun disableTotp() {
        flow<DisableTotpDeactivationState> {
            val session = requireNotNull(sessionManager.session) { "session == null" }
            val otp = DisableTotpDeactivationFragmentArgs.fromSavedStateHandle(savedStateHandle).otp

            val authTicket =
                performTotpVerificationService.execute(
                    PerformTotpVerificationService.Request(login = session.userId, otp = otp),
                ).data.authTicket

            if (session.appKey.isServerKeyNotNull) {
                
                masterPasswordChanger.updateServerKey(newServerKey = null, authTicket)
            } else {
                
                deactivateTOTPService.execute(
                    userAuthorization = session.authorization,
                    request = DeactivateTOTPService.Request(authTicket = DeactivateTOTPService.Request.AuthTicket(authTicket)),
                )

                userAccountStorage.saveSecuritySettings(
                    username = session.username,
                    securitySettings = UserSecuritySettings(isToken = true),
                )
            }

            use2faSettingStateRefresher.refresh()

            emit(DisableTotpDeactivationState.Success)
        }
            .catch { error ->
                emit(DisableTotpDeactivationState.Error(error = error))
            }
            .onEach { state -> mutableSharedFlow.emit(state) }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
    }
}

internal sealed class DisableTotpDeactivationState {
    object Success : DisableTotpDeactivationState()

    data class Error(val error: Throwable) : DisableTotpDeactivationState()
}
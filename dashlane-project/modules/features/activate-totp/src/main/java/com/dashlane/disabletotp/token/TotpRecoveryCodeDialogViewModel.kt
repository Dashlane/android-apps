package com.dashlane.disabletotp.token

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.network.webservices.authentication.OtpPhoneLostService
import com.dashlane.session.SessionManager
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class TotpRecoveryCodeDialogViewModel @Inject constructor(
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val otpPhoneLostService: OtpPhoneLostService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val mutableSharedFlow = MutableSharedFlow<SmsRecoveryCodeDialogState>()
    val sharedFlow = mutableSharedFlow.asSharedFlow()

    fun sendRecoveryBySms(email: String? = null) {
        flow {
            val userId = email ?: requireNotNull(sessionManager.session).userId
            emit(otpPhoneLostService.execute(userId))
        }
            .map { response ->
                when (response.code) {
                    200 -> SmsRecoveryCodeDialogState.Success
                    else -> SmsRecoveryCodeDialogState.Error
                }
            }
            .catch { emit(SmsRecoveryCodeDialogState.Error) }
            .onEach { state -> mutableSharedFlow.emit(state) }
            .flowOn(ioDispatcher)
            .launchIn(viewModelScope)
    }
}

sealed class SmsRecoveryCodeDialogState {
    object Success : SmsRecoveryCodeDialogState()
    object Error : SmsRecoveryCodeDialogState()
}

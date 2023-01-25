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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@HiltViewModel
internal class DisableTotpEnterTokenViewModel @Inject constructor(
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val otpPhoneLostService: OtpPhoneLostService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val mutableSharedFlow = MutableSharedFlow<DisableTotpEnterTokenState>()
    val sharedFlow = mutableSharedFlow.asSharedFlow()

    fun sendRecoveryBySms() {
        flowOf(requireNotNull(sessionManager.session))
            .map { session -> otpPhoneLostService.execute(session.userId) }
            .map { response ->
                when (response.code) {
                    200 -> DisableTotpEnterTokenState.Success
                    else -> DisableTotpEnterTokenState.Error
                }
            }
            .catch { emit(DisableTotpEnterTokenState.Error) }
            .onEach { state -> mutableSharedFlow.emit(state) }
            .flowOn(ioDispatcher)
            .launchIn(viewModelScope)
    }
}

internal sealed class DisableTotpEnterTokenState {
    object Success : DisableTotpEnterTokenState()
    object Error : DisableTotpEnterTokenState()
}

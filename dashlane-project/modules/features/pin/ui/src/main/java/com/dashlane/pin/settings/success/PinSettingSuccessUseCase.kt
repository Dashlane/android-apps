package com.dashlane.pin.settings.success

import com.dashlane.pin.PinSetupRepository
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun checkCredentialAreSaved(
    sessionManager: SessionManager,
    sessionCredentialsSaver: SessionCredentialsSaver,
    pinSetupRepository: PinSetupRepository,
    state: PinSettingsSuccessState.View.Initial,
): Flow<PinSettingsSuccessEvent> {
    return flow {
        val session = sessionManager.session ?: run {
            emit(PinSettingsSuccessEvent.Cancel)
            return@flow
        }
        val storedMP = sessionCredentialsSaver.areCredentialsSaved(session.username)
        if (storedMP) {
            
            pinSetupRepository.savePinValue(session, state.data.pin)
            emit(PinSettingsSuccessEvent.PinSaved)
        } else {
            emit(PinSettingsSuccessEvent.CredentialsNotSaved)
        }
    }
}

fun savePin(
    sessionManager: SessionManager,
    pinSetupRepository: PinSetupRepository,
    state: PinSettingsSuccessState.View.Loading,
): Flow<PinSettingsSuccessEvent> {
    return flow {
        val session = sessionManager.session ?: run {
            emit(PinSettingsSuccessEvent.Cancel)
            return@flow
        }
        pinSetupRepository.savePinValue(session, state.data.pin)
        emit(PinSettingsSuccessEvent.PinSaved)
    }
}
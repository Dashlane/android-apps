package com.dashlane.accountsummary.changelogin

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.ChangeLoginEmailFlowStep
import com.dashlane.hermes.generated.definitions.ChangeLoginError
import com.dashlane.hermes.generated.events.user.UserChangeLoginEmail
import com.dashlane.server.api.endpoints.user.UserRequestLoginChange
import com.dashlane.server.api.endpoints.user.exceptions.LoginUnavailableException
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.SessionManager
import com.dashlane.session.authorization
import com.dashlane.util.isValidEmail
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

fun loadEmail(
    sessionManager: SessionManager,
    logRepository: LogRepository,
    state: ChangeLoginEmailState.View.Initial,
): Flow<ChangeLoginEmailEvent> {
    return flow {
        logRepository.queueEvent(UserChangeLoginEmail(changeLoginEmailFlowStep = ChangeLoginEmailFlowStep.START_EMAIL_CHANGE))

        val session = sessionManager.session ?: run {
            emit(ChangeLoginEmailEvent.Cancel)
            return@flow
        }
        emit(ChangeLoginEmailEvent.CurrentEmailLoaded(session.username.email))
    }
}

fun requestLoginChange(
    sessionManager: SessionManager,
    userRequestLoginChange: UserRequestLoginChange,
    logRepository: LogRepository,
    ioCoroutineDispatcher: CoroutineDispatcher,
    state: ChangeLoginEmailState.View.Loading,
): Flow<ChangeLoginEmailEvent> {
    return flow<ChangeLoginEmailEvent> {
        logRepository.queueEvent(UserChangeLoginEmail(changeLoginEmailFlowStep = ChangeLoginEmailFlowStep.SUBMIT_EMAIL_CHANGE))

        val session = sessionManager.session ?: throw IllegalStateException("requestLoginChange session is null")
        val newLoginEmail = state.data.newLoginEmail?.text ?: throw IllegalStateException("requestLoginChange newLoginEmail is null")

        if (newLoginEmail.isBlank() || !newLoginEmail.isValidEmail()) {
            throw IllegalArgumentException("requestLoginChange newLoginEmail is invalid")
        }

        userRequestLoginChange.execute(session.authorization, UserRequestLoginChange.Request(newLogin = newLoginEmail))

        emit(ChangeLoginEmailEvent.EmailValid(newLoginEmail))
    }
        .flowOn(ioCoroutineDispatcher)
        .catch { throwable ->

            val (error, logErrorName) = when (throwable) {
                is IllegalArgumentException -> ChangeLoginEmailError.InvalidEmail to ChangeLoginError.INVALID_EMAIL
                is LoginUnavailableException -> ChangeLoginEmailError.EmailUnavailable to ChangeLoginError.EXISTING_EMAIL
                is DashlaneApiException -> ChangeLoginEmailError.Network to ChangeLoginError.EXISTING_EMAIL
                else -> ChangeLoginEmailError.Generic to ChangeLoginError.GENERIC
            }

            logRepository.queueEvent(UserChangeLoginEmail(logErrorName, ChangeLoginEmailFlowStep.ERROR))
            emit(ChangeLoginEmailEvent.Error(error))
        }
}
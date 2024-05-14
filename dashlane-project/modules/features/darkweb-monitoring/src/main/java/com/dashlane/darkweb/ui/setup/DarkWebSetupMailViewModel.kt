package com.dashlane.darkweb.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.darkweb.DarkWebMonitoringManager
import com.dashlane.storage.userdata.EmailSuggestionProvider
import com.dashlane.util.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class DarkWebSetupMailViewModel @Inject constructor(
    private val emailSuggestionProvider: EmailSuggestionProvider,
    private val darkWebMonitoringManager: DarkWebMonitoringManager
) : ViewModel(), DarkWebSetupMailViewModelContract {
    private val command = Channel<Command>()

    override val suggestions = viewModelScope.async { emailSuggestionProvider.getAllEmails() }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state = command.receiveAsFlow()
        .flatMapLatest { command ->
            when (command) {
                Command.Cancel -> {
                    flowOf(DarkWebSetupMailState.Canceled)
                }
                Command.Clear -> flowOf(DarkWebSetupMailState.Idle)
                is Command.OptIn -> flow {
                    val mail = command.mail
                    emit(DarkWebSetupMailState.InProgress(mail))

                    emit(
                        when {
                            mail.isBlank() -> {
                                DarkWebSetupMailState.Failed.EmptyMail(mail)
                            }
                            !mail.isValidEmail() -> {
                                DarkWebSetupMailState.Failed.InvalidMail(mail)
                            }
                            else -> when (darkWebMonitoringManager.optIn(mail)) {
                                "OK" -> {
                                    DarkWebSetupMailState.Succeed(mail)
                                }
                                "EMAIL_IS_INVALID" -> {
                                    DarkWebSetupMailState.Failed.InvalidMail(mail)
                                }
                                "USER_HAS_TOO_MANY_SUBSCRIPTIONS" -> {
                                    DarkWebSetupMailState.Failed.LimitReached(mail)
                                }
                                "USER_HAS_ALREADY_AN_ACTIVE_SUBSCRIPTION" -> {
                                    DarkWebSetupMailState.Succeed(mail)
                                }
                                else -> DarkWebSetupMailState.Failed.Unknown(mail)
                            }
                        }
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DarkWebSetupMailState.Idle)

    override fun onOptInClicked(mail: String) {
        command.trySend(Command.OptIn(mail.lowercase()))
    }

    override fun onMailChanged(mail: String) {
        if (mail.lowercase() != state.value.mail) {
            command.trySend(Command.Clear)
        }
    }

    override fun onCancel() {
        command.trySend(Command.Cancel)
    }

    private sealed class Command {
        data class OptIn(val mail: String) : Command()
        object Clear : Command()
        object Cancel : Command()
    }
}

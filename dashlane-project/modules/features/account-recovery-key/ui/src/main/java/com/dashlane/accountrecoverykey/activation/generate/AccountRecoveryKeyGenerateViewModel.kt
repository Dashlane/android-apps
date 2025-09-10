package com.dashlane.accountrecoverykey.activation.generate

import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateEvent.ArkActivated
import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateEvent.BackPressed
import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateEvent.CancelConfirmed
import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateEvent.CancelDialogDismissed
import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateEvent.Continue
import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateEvent.Copy
import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateEvent.ViewStarted
import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateSideEffect.Cancel
import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateSideEffect.Copying
import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateSideEffect.GoToConfirm
import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateState.Cancelling
import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateState.DisplayARK
import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateState.Initial
import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateState.Loading
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class AccountRecoveryKeyGenerateViewModel @Inject constructor(
    useCases: MutableList<UseCase<AccountRecoveryKeyGenerateState, AccountRecoveryKeyGenerateEvent>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<AccountRecoveryKeyGenerateState, AccountRecoveryKeyGenerateSideEffect, AccountRecoveryKeyGenerateEvent>(
        useCases = useCases,
        defaultDispatcher = defaultDispatcher,
    ) {
    init {
        start(createStateMachine())
    }

    internal fun createStateMachine() =
        StateMachine.create<AccountRecoveryKeyGenerateState, AccountRecoveryKeyGenerateEvent, AccountRecoveryKeyGenerateSideEffect> {
            initialState(Initial(AccountRecoveryKeyGenerateData()))
            state<Initial> {
                on<ViewStarted> {
                    if (data.accountRecoveryKey.isNotSemanticallyNull()) {
                        transitionTo(DisplayARK(data))
                    } else {
                        transitionTo(Loading(data))
                    }
                }
            }
            state<Loading> {
                on<ArkActivated> { event ->
                    transitionTo(
                        DisplayARK(
                            data = data.copy(
                                accountType = event.accountType,
                                accountRecoveryKey = event.formattedAccountRecoveryKey,
                            ),
                        ),
                    )
                }
            }
            state<DisplayARK> {
                on<Copy> {
                    sideEffect(Copying(formattedAccountRecoveryKey = data.accountRecoveryKey ?: ""))
                }
                on<Continue> {
                    sideEffect(GoToConfirm)
                }
                on<BackPressed> {
                    transitionTo(Cancelling(data))
                }
            }
            state<Cancelling> {
                on<CancelDialogDismissed> {
                    transitionTo(DisplayARK(data))
                }
                on<CancelConfirmed> {
                    transitionTo(DisplayARK(data), sideEffect = Cancel)
                }
            }
        }
}
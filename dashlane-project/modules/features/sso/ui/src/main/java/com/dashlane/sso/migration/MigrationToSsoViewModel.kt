package com.dashlane.sso.migration

import androidx.lifecycle.SavedStateHandle
import com.dashlane.sso.login.LoginSsoInfo
import com.dashlane.sso.migration.MigrationToSsoActivity.Companion.KEY_IS_NITRO_PROVIDER
import com.dashlane.sso.migration.MigrationToSsoActivity.Companion.KEY_LOGIN
import com.dashlane.sso.migration.MigrationToSsoActivity.Companion.KEY_SERVICE_PROVIDER_URL
import com.dashlane.sso.migration.MigrationToSsoActivity.Companion.KEY_TOTP_AUTH_TICKET
import com.dashlane.sso.migration.MigrationToSsoEvent.Cancel
import com.dashlane.sso.migration.MigrationToSsoEvent.Continue
import com.dashlane.sso.migration.MigrationToSsoEvent.Error
import com.dashlane.sso.migration.MigrationToSsoEvent.LinkClicked
import com.dashlane.sso.migration.MigrationToSsoEvent.SsoCompleted
import com.dashlane.sso.migration.MigrationToSsoEvent.Success
import com.dashlane.sso.migration.MigrationToSsoState.SideEffect
import com.dashlane.sso.migration.MigrationToSsoState.View
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class MigrationToSsoViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, MigrationToSsoEvent>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle,
) : UDFViewModel<View, SideEffect, MigrationToSsoEvent>(
        useCases = useCases,
        defaultDispatcher = defaultDispatcher,
    ) {
    init {
        val login = savedStateHandle.get<String>(KEY_LOGIN)
        val serviceProviderUrl = savedStateHandle.get<String>(KEY_SERVICE_PROVIDER_URL)
        val isNitroProvider = savedStateHandle.get<Boolean>(KEY_IS_NITRO_PROVIDER) ?: false
        val totpAuthTicket = savedStateHandle.get<String>(KEY_TOTP_AUTH_TICKET)

        val initialState = View.Initial(MigrationToSsoData(login, serviceProviderUrl, isNitroProvider, totpAuthTicket))
        start(createStateMachine(initialState))
    }

    @Suppress("LongMethod")
    internal fun createStateMachine(initialState: View) = StateMachine.create<View, MigrationToSsoEvent, SideEffect> {
        initialState(initialState)
        state<View.Initial> {
            on<Continue> {
                if (data.login == null || data.serviceProviderUrl == null) {
                    sideEffect(SideEffect.Cancel)
                } else {
                    transitionTo(
                        state = View.LoadingSsoLogin(data.copy(isLoading = true)),
                        sideEffect = SideEffect.GoToSso(
                            LoginSsoInfo(
                                data.login,
                                data.serviceProviderUrl,
                                data.isNitroProvider,
                            ),
                        ),
                    )
                }
            }
            on<LinkClicked> {
                sideEffect(SideEffect.OpenLink(it.url))
            }
            on<Cancel> {
                transitionTo(View.Logout(data))
            }
        }
        state<View.LoadingSsoLogin> {
            on<SsoCompleted> {
                transitionTo(View.LoadingMigration(data, it.result))
            }
        }
        state<View.Logout> {
            on<Cancel> {
                sideEffect(SideEffect.Cancel)
            }
        }
        state<View.LoadingMigration> {
            on<Success> {
                sideEffect(SideEffect.Finish(it.shouldLaunchInitialSync, it.goToApplicationLock))
            }
            on<Error> {
                transitionTo(View.Initial(data.copy(isLoading = false)), SideEffect.ShowError(it.error))
            }
            on<Cancel> {
                transitionTo(View.Logout(data))
            }
        }
    }
}
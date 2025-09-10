package com.dashlane.vpn.thirdparty

import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.vpn.thirdparty.VpnThirdPartyState.SideEffect
import com.dashlane.vpn.thirdparty.VpnThirdPartyState.View
import com.dashlane.vpn.thirdparty.VpnThirdPartyState.View.Account
import com.dashlane.vpn.thirdparty.VpnThirdPartyState.View.Activate
import com.dashlane.vpn.thirdparty.VpnThirdPartyState.View.Initial
import com.dashlane.vpn.thirdparty.VpnThirdPartyState.View.Loading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class VpnThirdPartyViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, VpnThirdPartyEvent>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<View, SideEffect, VpnThirdPartyEvent>(
        useCases = useCases,
        defaultDispatcher = defaultDispatcher,
    ) {
    init {
        start(createStateMachine())
    }

    @Suppress("LongMethod")
    internal fun createStateMachine() =
        StateMachine.create<View, VpnThirdPartyEvent, SideEffect> {
            initialState(Initial(VpnThirdPartyData()))
            state<Initial> {
                on<VpnThirdPartyEvent.ViewResumed> {
                    transitionTo(Loading(data))
                }
            }
            state<Activate> {
                on<VpnThirdPartyEvent.ViewResumed> {
                    transitionTo(Loading(data))
                }
                on<VpnThirdPartyEvent.SetupVpnClick> {
                    transitionTo(
                        state = Loading(data),
                        sideEffect = SideEffect.GoToSetupVpn(data.defaultEmail, data.userListSuggestion),
                    )
                }
            }
            state<Loading> {
                on<VpnThirdPartyEvent.ViewResumed> {
                    transitionTo(Loading(data))
                }
                on<VpnThirdPartyEvent.AccountLoaded> {
                    transitionTo(
                        Account(
                            data.copy(
                                accountLogin = it.login,
                                accountPassword = it.password,
                                actionType = if (it.isHotspotShieldInstalled) {
                                    VpnThirdPartyData.ActionType.LAUNCH_APP
                                } else {
                                    VpnThirdPartyData.ActionType.DOWNLOAD_APP
                                },
                                accountId = it.itemId,
                                accountDomain = it.domain,
                            ),
                        ),
                    )
                }
                on<VpnThirdPartyEvent.NoAccountLoaded> {
                    transitionTo(Activate(data.copy(defaultEmail = it.defaultEmail, userListSuggestion = it.userListSuggestion)))
                }
            }
            state<Account> {
                on<VpnThirdPartyEvent.ViewResumed> {
                    transitionTo(Loading(data))
                }
                on<VpnThirdPartyEvent.CopyLoginClick> {
                    transitionTo(View.CopyLogin(data))
                }
                on<VpnThirdPartyEvent.CopyPasswordClick> {
                    transitionTo(View.CopyPassword(data))
                }
                on<VpnThirdPartyEvent.ShowFaq> {
                    transitionTo(Account(data = data.copy(showFaq = true)))
                }
                on<VpnThirdPartyEvent.DismissFaq> {
                    transitionTo(Account(data = data.copy(showFaq = false)))
                }
                on<VpnThirdPartyEvent.DownloadAppClick> {
                    transitionTo(View.DownloadApp(data))
                }
                on<VpnThirdPartyEvent.LaunchAppClick> {
                    transitionTo(View.LaunchApp(data))
                }
                on<VpnThirdPartyEvent.VpnIntroArticleClick> {
                    sideEffect(SideEffect.GoToVpnIntroArticle)
                }
                on<VpnThirdPartyEvent.VpnFaqArticleClick> {
                    sideEffect(SideEffect.GoToVpnFaqArticle)
                }
            }
            state<View.CopyLogin> {
                on<VpnThirdPartyEvent.Copied> {
                    transitionTo(state = Account(data))
                }
            }
            state<View.CopyPassword> {
                on<VpnThirdPartyEvent.Copied> {
                    transitionTo(state = Account(data))
                }
            }
            state<View.DownloadApp> {
                on<VpnThirdPartyEvent.IntentLoaded> {
                    transitionTo(state = Account(data), sideEffect = SideEffect.DownloadApp(it.intent))
                }
            }
            state<View.LaunchApp> {
                on<VpnThirdPartyEvent.IntentLoaded> {
                    transitionTo(state = Account(data), sideEffect = SideEffect.LaunchApp(it.intent))
                }
            }
        }
}
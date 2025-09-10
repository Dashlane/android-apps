package com.dashlane.settings.main.misc

import android.os.Build
import com.dashlane.settings.MainMisc
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.DialogNegative
import com.dashlane.settings.SettingsEvent.DialogPositive
import com.dashlane.settings.SettingsEvent.DisplayDialog
import com.dashlane.settings.SettingsEvent.Error
import com.dashlane.settings.SettingsEvent.InviteFriendIntent
import com.dashlane.settings.SettingsEvent.ItemClicked
import com.dashlane.settings.SettingsEvent.Logout
import com.dashlane.settings.SettingsEvent.Search
import com.dashlane.settings.SettingsEvent.SettingsLoaded
import com.dashlane.settings.SettingsEvent.SocialMediaClick
import com.dashlane.settings.main.misc.SettingsMainMiscState.SideEffect
import com.dashlane.settings.main.misc.SettingsMainMiscState.SideEffect.GoToDebug
import com.dashlane.settings.main.misc.SettingsMainMiscState.SideEffect.GoToSendLove
import com.dashlane.settings.main.misc.SettingsMainMiscState.SideEffect.GoToUrl
import com.dashlane.settings.main.misc.SettingsMainMiscState.View
import com.dashlane.settings.main.misc.SettingsMainMiscState.View.DisplaySettings
import com.dashlane.settings.main.misc.SettingsMainMiscState.View.UpdateInviteFriend
import com.dashlane.settings.main.misc.SettingsMainMiscState.View.UpdateLogout
import com.dashlane.settings.main.misc.SettingsMainMiscState.View.UpdateVersion
import com.dashlane.settings.search
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class SettingsMainMiscViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, SettingsEvent<MainMisc>>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<View, SideEffect, SettingsEvent<MainMisc>>(useCases = useCases, defaultDispatcher = defaultDispatcher) {
    init {
        start(createStateMachine())
    }

    @Suppress("LongMethod")
    internal fun createStateMachine() = StateMachine.create<View, SettingsEvent<MainMisc>, SideEffect> {
        initialState(View.Initial(SettingsMainMiscData()))
        state<View.Initial> {
            on<SettingsLoaded<MainMisc>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(miscSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<DisplaySettings> {
            on<SettingsLoaded<MainMisc>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(miscSettings = it.settings, filteredSettings = filteredSettings)))
            }
            on<ItemClicked<MainMisc>> {
                val (state, sideEffect) = reduceItemClicked(it)
                transitionTo(state, sideEffect)
            }
            on<DialogNegative<MainMisc>> {
                transitionTo(state = DisplaySettings(data.copy(dialog = null)))
            }
            on<DialogPositive<MainMisc>> {
                transitionTo(state = View.DialogConfirmed(data))
            }
            on<SocialMediaClick> {
                sideEffect(GoToUrl(it.url))
            }
            on<Search<MainMisc>> { event ->
                val filteredSettings = data.miscSettings.search(event.searchQuery)
                transitionTo(DisplaySettings(data.copy(filteredSettings = filteredSettings, searchQuery = event.searchQuery)))
            }
        }
        state<View.DialogConfirmed> {
            on<Logout> {
                transitionTo(DisplaySettings(data.copy(dialog = null)), SideEffect.GoToLogout)
            }
        }
        state<UpdateInviteFriend> {
            on<Error<MainMisc>> {
                transitionTo(DisplaySettings(data), SideEffect.DisplayToast(MainMiscToast.Error(it.error)))
            }
            on<InviteFriendIntent> {
                transitionTo(DisplaySettings(data), SideEffect.GoToInviteFriend(it.intent))
            }
        }
        state<UpdateLogout> {
            on<DisplayDialog<MainMisc>> {
                transitionTo(DisplaySettings(data.copy(dialog = it.dialog)))
            }
        }
        state<UpdateVersion> {
            on<SettingsLoaded<MainMisc>> {
                val sideEffect = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    SideEffect.DisplayToast(MainMiscToast.Copy)
                } else {
                    null
                }
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(miscSettings = it.settings, filteredSettings = filteredSettings)), sideEffect)
            }
        }
    }

    private fun View.reduceItemClicked(event: ItemClicked<MainMisc>): Pair<View, SideEffect?> {
        return when (event.id) {
            MainMisc.InviteFriend -> UpdateInviteFriend(data) to null
            MainMisc.SendLove -> DisplaySettings(data) to GoToSendLove
            MainMisc.Logout -> UpdateLogout(data) to null
            MainMisc.Version -> UpdateVersion(data) to null
            MainMisc.Debug -> DisplaySettings(data) to GoToDebug
            else -> this to null
        }
    }
}
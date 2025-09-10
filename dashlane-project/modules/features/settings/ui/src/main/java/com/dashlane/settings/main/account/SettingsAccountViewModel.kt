package com.dashlane.settings.main.account

import com.dashlane.settings.Account
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.ItemClicked
import com.dashlane.settings.SettingsEvent.Search
import com.dashlane.settings.SettingsEvent.SettingsLoaded
import com.dashlane.settings.main.account.SettingsAccountState.SideEffect
import com.dashlane.settings.main.account.SettingsAccountState.SideEffect.GoToAccountStatus
import com.dashlane.settings.main.account.SettingsAccountState.SideEffect.GoToCurrentPlan
import com.dashlane.settings.main.account.SettingsAccountState.View
import com.dashlane.settings.main.account.SettingsAccountState.View.DisplaySettings
import com.dashlane.settings.main.account.SettingsAccountState.View.Initial
import com.dashlane.settings.search
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class SettingsAccountViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, SettingsEvent<Account>>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<View, SideEffect, SettingsEvent<Account>>(useCases = useCases, defaultDispatcher = defaultDispatcher) {
    init {
        start(createStateMachine())
    }

    internal fun createStateMachine() = StateMachine.create<View, SettingsEvent<Account>, SideEffect> {
        initialState(Initial(SettingsAccountData()))
        state<Initial> {
            on<SettingsLoaded<Account>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(accountSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<DisplaySettings> {
            on<SettingsLoaded<Account>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(accountSettings = it.settings, filteredSettings = filteredSettings)))
            }
            on<ItemClicked<Account>> {
                val (state, sideEffect) = reduceItemClicked(it)
                transitionTo(state, sideEffect)
            }
            on<Search<Account>> { event ->
                val filteredSettings = data.accountSettings.search(event.searchQuery)
                transitionTo(DisplaySettings(data.copy(filteredSettings = filteredSettings, searchQuery = event.searchQuery)))
            }
        }
    }

    private fun View.reduceItemClicked(event: ItemClicked<Account>): Pair<View, SideEffect?> {
        return when (event.id) {
            Account.Plan -> DisplaySettings(data) to GoToCurrentPlan
            Account.AccountSummary -> DisplaySettings(data) to GoToAccountStatus
            else -> this to null
        }
    }
}
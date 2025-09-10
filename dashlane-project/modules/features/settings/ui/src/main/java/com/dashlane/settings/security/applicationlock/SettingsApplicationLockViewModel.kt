package com.dashlane.settings.security.applicationlock

import com.dashlane.settings.ApplicationLock
import com.dashlane.settings.ApplicationLock.AccountRecoveryKey
import com.dashlane.settings.ApplicationLock.AutoLockOnExit
import com.dashlane.settings.ApplicationLock.AutoLockTime
import com.dashlane.settings.ApplicationLock.BiometricRecovery
import com.dashlane.settings.ApplicationLock.BiometricUnlock
import com.dashlane.settings.ApplicationLock.ChangePin
import com.dashlane.settings.ApplicationLock.DisableLocal2FA
import com.dashlane.settings.ApplicationLock.PinLock
import com.dashlane.settings.ApplicationLock.TwoFA
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.DialogDismissed
import com.dashlane.settings.SettingsEvent.DialogNegative
import com.dashlane.settings.SettingsEvent.DialogPositive
import com.dashlane.settings.SettingsEvent.DisplayDialog
import com.dashlane.settings.SettingsEvent.ItemCheckChanged
import com.dashlane.settings.SettingsEvent.ItemClicked
import com.dashlane.settings.SettingsEvent.Search
import com.dashlane.settings.SettingsEvent.SettingsLoaded
import com.dashlane.settings.SettingsEvent.SettingsRemoved
import com.dashlane.settings.SettingsEvent.SettingsUpdated
import com.dashlane.settings.SettingsEvent.Unlocked
import com.dashlane.settings.removeSetting
import com.dashlane.settings.search
import com.dashlane.settings.security.SettingsDialog
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.SideEffect
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.SideEffect.DisplayLock
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.SideEffect.GoToARK
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.SideEffect.GoToBiometricLock
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.SideEffect.GoToDisable2FA
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.SideEffect.GoToPinLock
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.View
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.View.AutoLockTimeDialogConfirmed
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.View.DialogConfirmed
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.View.DisplaySettings
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.View.Initial
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.View.UpdateAutoLockOnExit
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.View.UpdateAutoLockTime
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.View.UpdateBiometricLock
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.View.UpdateBiometricRecovery
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.View.UpdateDisableLocal2FA
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.View.UpdatePinLock
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.View.WaitingForLock
import com.dashlane.settings.updateSetting
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class SettingsApplicationLockViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, SettingsEvent<ApplicationLock>>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<View, SideEffect, SettingsEvent<ApplicationLock>>(
        useCases = useCases,
        defaultDispatcher = defaultDispatcher,
    ) {
    init {
        start(createStateMachine())
    }

    @Suppress("LongMethod")
    internal fun createStateMachine() = StateMachine.create<View, SettingsEvent<ApplicationLock>, SideEffect> {
        initialState(Initial(SettingsApplicationLockData()))
        state<Initial> {
            on<SettingsLoaded<ApplicationLock>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(securitySettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<DisplaySettings> {
            on<SettingsLoaded<ApplicationLock>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(securitySettings = it.settings, filteredSettings = filteredSettings)))
            }
            on<DialogDismissed<ApplicationLock>> {
                transitionTo(state = DisplaySettings(data.copy(dialog = null)))
            }
            on<DialogPositive<ApplicationLock>> {
                when (it.dialog) {
                    is SettingsDialog.AutoLockTimeSelector -> transitionTo(AutoLockTimeDialogConfirmed(data, it.dialog.selected))
                    is SettingsDialog.Disable2FA -> transitionTo(DisplaySettings(data.copy(dialog = null)), GoToDisable2FA)

                    else -> transitionTo(state = DialogConfirmed(data))
                }
            }
            on<DialogNegative<ApplicationLock>> {
                when (it.dialog) {
                    is SettingsDialog.WeakBiometricWarning -> {
                        transitionTo(state = DisplaySettings(data.copy(dialog = null)), GoToBiometricLock)
                    }
                    else -> transitionTo(state = DisplaySettings(data.copy(dialog = null)))
                }
            }
            on<ItemClicked<ApplicationLock>> {
                val (state, sideEffect) = reduceItemClicked(it)
                transitionTo(state, sideEffect)
            }
            on<ItemCheckChanged<ApplicationLock>> {
                val (state, sideEffect) = reduceItemCheckChanged(it)
                transitionTo(state, sideEffect)
            }
            on<SettingsUpdated<ApplicationLock>> {
                val (state, sideEffect) = reduceSecuritySettingsUpdated(it)
                transitionTo(state, sideEffect)
            }
            on<SettingsRemoved<ApplicationLock>> {
                val (state, sideEffect) = reduceSecuritySettingsRemoved(it)
                transitionTo(state, sideEffect)
            }
            on<Search<ApplicationLock>> { event ->
                val filteredSettings = data.securitySettings.search(event.searchQuery)
                transitionTo(DisplaySettings(data.copy(filteredSettings = filteredSettings, searchQuery = event.searchQuery)))
            }
        }
        state<DialogConfirmed> {
            on<Unlocked<ApplicationLock, SideEffect>> {
                transitionTo(DisplaySettings(data.copy(dialog = null)), it.sideEffect)
            }
            on<SettingsUpdated<ApplicationLock>> {
                val (state, sideEffect) = reduceSecuritySettingsUpdated(it)
                transitionTo(state, sideEffect)
            }
        }
        state<WaitingForLock> {
            on<Unlocked<ApplicationLock, SideEffect>> {
                transitionTo(DisplaySettings(data), sideEffect)
            }
            on<SettingsLoaded<ApplicationLock>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(securitySettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<UpdatePinLock> {
            on<Unlocked<ApplicationLock, SideEffect>> {
                transitionTo(DisplaySettings(data), it.sideEffect)
            }
            on<SettingsLoaded<ApplicationLock>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(securitySettings = it.settings, filteredSettings = filteredSettings)))
            }
            on<SettingsUpdated<ApplicationLock>> {
                val (state, sideEffect) = reduceSecuritySettingsUpdated(it)
                transitionTo(state, sideEffect)
            }
        }
        state<UpdateBiometricLock> {
            on<Unlocked<ApplicationLock, SideEffect>> {
                transitionTo(DisplaySettings(data), it.sideEffect)
            }
            on<DisplayDialog<ApplicationLock>> {
                transitionTo(DisplaySettings(data.copy(dialog = it.dialog)))
            }
            on<SettingsLoaded<ApplicationLock>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(securitySettings = it.settings, filteredSettings = filteredSettings)))
            }
            on<SettingsUpdated<ApplicationLock>> {
                val (state, sideEffect) = reduceSecuritySettingsUpdated(it)
                transitionTo(state, sideEffect)
            }
        }
        state<View.Update2FA> {
            on<Unlocked<ApplicationLock, SideEffect>> {
                transitionTo(DisplaySettings(data), it.sideEffect)
            }
        }
        state<UpdateBiometricRecovery> {
            on<Unlocked<ApplicationLock, SideEffect>> {
                transitionTo(DisplaySettings(data), it.sideEffect)
            }
            on<DialogPositive<ApplicationLock>> {
                transitionTo(DisplaySettings(data.copy(dialog = it.dialog)))
            }
            on<SettingsLoaded<ApplicationLock>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(securitySettings = it.settings, filteredSettings = filteredSettings)))
            }
            on<SettingsUpdated<ApplicationLock>> {
                val (state, sideEffect) = reduceSecuritySettingsUpdated(it)
                transitionTo(state, sideEffect)
            }
        }
        state<UpdateDisableLocal2FA> {
            on<SettingsLoaded<ApplicationLock>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(securitySettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<UpdateAutoLockOnExit> {
            on<SettingsLoaded<ApplicationLock>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(securitySettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<UpdateAutoLockTime> {
            on<DisplayDialog<ApplicationLock>> {
                transitionTo(DisplaySettings(data.copy(dialog = it.dialog)))
            }
            on<SettingsLoaded<ApplicationLock>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(securitySettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<AutoLockTimeDialogConfirmed> {
            on<SettingsLoaded<ApplicationLock>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(securitySettings = it.settings, dialog = null, filteredSettings = filteredSettings)))
            }
        }
    }

    private fun View.reduceItemCheckChanged(event: ItemCheckChanged<ApplicationLock>): Pair<View, SideEffect?> {
        return when (event.id) {
            PinLock -> UpdatePinLock(data, event.checked) to DisplayLock(forceMasterPassword = true)
            BiometricUnlock -> UpdateBiometricLock(data, event.checked) to DisplayLock(forceMasterPassword = true)
            TwoFA -> DisplaySettings(data.copy(dialog = SettingsDialog.Disable2FA)) to null

            BiometricRecovery -> {
                if (!event.checked) {
                    DisplaySettings(data.copy(dialog = SettingsDialog.DisableBiometricRecovery)) to null
                } else {
                    UpdateBiometricRecovery(data, event.checked) to DisplayLock(forceMasterPassword = true)
                }
            }
            DisableLocal2FA -> {
                if (event.checked) {
                    UpdateDisableLocal2FA(data, event.checked) to DisplayLock(forceMasterPassword = false)
                } else {
                    UpdateDisableLocal2FA(data, event.checked) to null
                }
            }

            AutoLockOnExit -> UpdateAutoLockOnExit(data, event.checked) to DisplayLock(forceMasterPassword = false)
            else -> this to null
        }
    }

    private fun View.reduceItemClicked(event: ItemClicked<ApplicationLock>): Pair<View, SideEffect?> {
        return when (event.id) {
            AccountRecoveryKey -> WaitingForLock(data, sideEffect = GoToARK) to DisplayLock(forceMasterPassword = true)
            AutoLockTime -> UpdateAutoLockTime(data) to DisplayLock(forceMasterPassword = false)
            ChangePin -> WaitingForLock(data, sideEffect = GoToPinLock) to DisplayLock(forceMasterPassword = true)
            else -> this to null
        }
    }

    private fun View.reduceSecuritySettingsUpdated(event: SettingsUpdated<ApplicationLock>): Pair<View, SideEffect?> {
        val newSettings = data.securitySettings.toMutableList().updateSetting(event.setting.id) { event.setting }.toList()
        val filteredSettings = newSettings.search(data.searchQuery)
        return DisplaySettings(data.copy(securitySettings = newSettings, dialog = null, filteredSettings = filteredSettings)) to null
    }

    private fun View.reduceSecuritySettingsRemoved(event: SettingsRemoved<ApplicationLock>): Pair<View, SideEffect?> {
        val newSettings = data.securitySettings.toMutableList()
        event.settings.forEach { setting -> newSettings.removeSetting(setting) }
        val filteredSettings = newSettings.search(data.searchQuery)
        return DisplaySettings(data.copy(securitySettings = newSettings, dialog = null, filteredSettings = filteredSettings)) to null
    }
}
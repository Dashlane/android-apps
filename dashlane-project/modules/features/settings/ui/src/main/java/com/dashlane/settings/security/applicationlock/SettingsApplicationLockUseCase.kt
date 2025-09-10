package com.dashlane.settings.security.applicationlock

import android.content.Context
import android.content.res.Resources
import arrow.optics.copy
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.accountstatus.getB2bStatus
import com.dashlane.authentication.twofa.Use2faSettingStateHolder
import com.dashlane.biometric.BiometricSetupRepository
import com.dashlane.crypto.keys.serverKeyUtf8Bytes
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.dashlane.hardwaresecurity.BiometricRecovery
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockType
import com.dashlane.pin.PinSetupRepository
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType
import com.dashlane.server.api.endpoints.authentication.Get2FAStatusService
import com.dashlane.session.Session
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.authorization
import com.dashlane.settings.ApplicationLock
import com.dashlane.settings.ApplicationLock.AccountRecoveryKey
import com.dashlane.settings.ApplicationLock.AutoLockOnExit
import com.dashlane.settings.ApplicationLock.AutoLockTime
import com.dashlane.settings.ApplicationLock.BiometricUnlock
import com.dashlane.settings.ApplicationLock.ChangePin
import com.dashlane.settings.ApplicationLock.DisableLocal2FA
import com.dashlane.settings.ApplicationLock.PinLock
import com.dashlane.settings.ApplicationLock.TwoFA
import com.dashlane.settings.SettingsError
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.DisplayDialog
import com.dashlane.settings.SettingsEvent.SettingsLoaded
import com.dashlane.settings.SettingsEvent.Unlocked
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.checked
import com.dashlane.settings.description
import com.dashlane.settings.enabled
import com.dashlane.settings.isLoading
import com.dashlane.settings.security.SettingsDialog
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.SideEffect.GoToBiometricLock
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.SideEffect.GoToBiometricRecovery
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.SideEffect.GoToDisable2FA
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.SideEffect.GoToDisable2FAEnforced
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState.SideEffect.GoToPinLock
import com.dashlane.settings.toggle
import com.dashlane.settings.ui.R
import com.dashlane.settings.updateSetting
import com.dashlane.settings.visible
import com.dashlane.teamspaces.manager.getCurrentTeam
import com.dashlane.teamspaces.manager.is2FAEnforced
import com.dashlane.user.UserAccountInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import java.time.Duration
import java.util.Arrays


@Suppress("kotlin:S3776", "kotlin:S107", "LongMethod")
fun loadApplicationLockSecuritySettings(
    @ApplicationContext context: Context,
    sessionManager: SessionManager,
    sessionCredentialsSaver: SessionCredentialsSaver,
    userAccountStorage: UserAccountStorage,
    lockManager: LockManager,
    biometricAuthModule: BiometricAuthModule,
    biometricRecovery: BiometricRecovery,
    accountStatusRepository: AccountStatusRepository,
    accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
    get2FAStatusService: Get2FAStatusService,
    state: SettingsApplicationLockState.View.Initial,
): Flow<SettingsEvent<ApplicationLock>> {
    return flow {

        val session = sessionManager.session ?: run {
            emit(SettingsEvent.Error(SettingsError.Generic))
            return@flow
        }
        val userAccountInfo = userAccountStorage[session.username] ?: run {
            emit(SettingsEvent.Error(SettingsError.Generic))
            return@flow
        }
        val locks = lockManager.getLocks(session.username)
        val accountStatus = accountStatusRepository[session]
        val currentTeam = accountStatus?.getB2bStatus()?.getCurrentTeam()
        val hasPin = LockType.PinCode in locks
        val hasBiometric = LockType.Biometric in locks

        val securitySettings = mutableListOf<SettingsItemData>()

        
        if (userAccountInfo.accountType is UserAccountInfo.AccountType.MasterPassword) {
            securitySettings.add(
                SettingsItemData.Toggle(
                    id = PinLock,
                    title = context.getString(R.string.settings_use_pincode),
                    description = context.getString(R.string.setting_use_pincode_description),
                    enabled = true,
                    checked = hasPin,
                ),
            )
        }

        
        securitySettings.add(
            SettingsItemData.Click(
                id = ChangePin,
                title = context.getString(R.string.settings_change_pincode),
                description = context.getString(R.string.settings_change_pincode_description),
                enabled = true,
                visible = hasPin,
            ),
        )

        
        if (biometricAuthModule.isHardwareSupported()) {
            securitySettings.add(
                SettingsItemData.Toggle(
                    id = BiometricUnlock,
                    title = context.getString(R.string.setting_use_google_fingerprint_title),
                    description = context.getString(R.string.setting_use_google_fingerprint_description),
                    enabled = true,
                    checked = hasBiometric,
                ),
            )
        }

        
        val userSecuritySettings = userAccountInfo.securitySettings
        val has2FA = userSecuritySettings?.isTotp == true ||
            userSecuritySettings?.isDuoEnabled == true ||
            userSecuritySettings?.isU2fEnabled == true

        securitySettings.add(
            SettingsItemData.Toggle(
                id = TwoFA,
                title = context.getString(R.string.settings_use_2fa),
                description = context.getString(R.string.settings_use_2fa_description),
                enabled = false,
                checked = false,
                visible = has2FA,
                isLoading = has2FA,
            ),
        )

        
        if (biometricRecovery.isFeatureAvailable()) {
            securitySettings.add(
                SettingsItemData.Toggle(
                    id = ApplicationLock.BiometricRecovery,
                    title = context.getString(R.string.account_recovery_setting_title),
                    description = context.getString(R.string.account_recovery_setting_description),
                    enabled = true,
                    checked = hasBiometric && biometricRecovery.isFeatureEnabled(),
                ),
            )
        }

        
        securitySettings.add(
            SettingsItemData.Click(
                id = AccountRecoveryKey,
                title = context.getString(R.string.account_recovery_key_setting_title),
                description = "",
                enabled = false,
                isLoading = true,
            ),
        )

        
        if (userAccountInfo.otp2 && (LockType.PinCode in locks || LockType.Biometric in locks)) {
            securitySettings.add(
                SettingsItemData.Toggle(
                    id = DisableLocal2FA,
                    title = context.getString(R.string.settings_disable_totp2_for_device),
                    description = context.getString(R.string.settings_disable_totp2_for_device_description),
                    enabled = true,
                    checked = sessionCredentialsSaver.hasServerKey(session.username),
                ),
            )
        }

        
        securitySettings.add(
            SettingsItemData.Toggle(
                id = AutoLockOnExit,
                title = context.getString(R.string.settings_lock_on_exit),
                description = context.getString(R.string.setting_lock_on_exit_description),
                enabled = currentTeam == null || !currentTeam.isLockOnExitEnabled,
                checked = lockManager.isLockOnExit(),
            ),
        )

        
        securitySettings.add(
            SettingsItemData.Click(
                id = AutoLockTime,
                title = context.getString(R.string.settings_use_lock_timeout),
                description = getCurrentTimeoutArrayPosition(context.resources, lockManager.lockTimeout)
                    .let { pos ->
                        if (pos <= 0) {
                            context.getString(R.string.settings_use_lock_timeout_description_never_lock)
                        } else {
                            context.getString(
                                R.string.settings_use_lock_timeout_description,
                                context.resources.getStringArray(R.array.timeout_strings)[pos],
                            )
                        }
                    },
                enabled = true,
            ),
        )
        emit(SettingsLoaded(securitySettings))

        val updatedSetting = getSettingAsyncStatus(
            settings = mutableListOf<SettingsItemData>().apply { addAll(securitySettings) },
            context = context,
            session = session,
            has2FA = has2FA,
            get2FAStatusService = get2FAStatusService,
            accountRecoveryKeyRepository = accountRecoveryKeyRepository,
        ).toList()
        emit(SettingsLoaded(updatedSetting))
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun listenToSettingsChange(
    @ApplicationContext context: Context,
    pinSetupRepository: PinSetupRepository,
    biometricSetupRepository: BiometricSetupRepository,
    biometricRecovery: BiometricRecovery,
    accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
    use2faSettingStateHolder: Use2faSettingStateHolder,
    state: SettingsApplicationLockState.View.DisplaySettings,
): Flow<SettingsEvent<ApplicationLock>> {
    val pinFlow: Flow<SettingsEvent<ApplicationLock>> = pinSetupRepository.pinStatusFlow
        .flatMapMerge { pinStatus ->
            flow {
                state.data.securitySettings.find { it.id == PinLock }
                    ?.let { it.copy { SettingsItemData.toggle.checked set pinStatus } }
                    ?.let { setting -> emit(SettingsEvent.SettingsUpdated(setting = setting)) }
                state.data.securitySettings.find { it.id == ChangePin }
                    ?.let { it.copy { SettingsItemData.visible set pinStatus } }
                    ?.let { setting -> emit(SettingsEvent.SettingsUpdated(setting = setting)) }
            }
        }
    val biometricFlow: Flow<SettingsEvent<ApplicationLock>> = biometricSetupRepository.biometricStatusFlow
        .mapNotNull { biometricStatus ->
            state.data.securitySettings.find { it.id == BiometricUnlock }
                ?.let { it.copy { SettingsItemData.toggle.checked set biometricStatus } }
                ?.let { setting -> SettingsEvent.SettingsUpdated(setting = setting) }
        }
    val biometricRecoveryFlow: Flow<SettingsEvent<ApplicationLock>> = biometricRecovery.biometricRecoveryStatusFlow
        .mapNotNull { biometricRecoveryStatus ->
            state.data.securitySettings.find { it.id == ApplicationLock.BiometricRecovery }
                ?.let { it.copy { SettingsItemData.toggle.checked set biometricRecoveryStatus } }
                ?.let { setting -> SettingsEvent.SettingsUpdated(setting = setting) }
        }
    val accountRecoveryKeyFlow: Flow<SettingsEvent<ApplicationLock>> = accountRecoveryKeyRepository.arkStatusFlow
        .mapNotNull { accountRecoveryState ->
            val description = if (accountRecoveryState.enabled) {
                context.getString(R.string.account_recovery_key_setting_description_on)
            } else {
                context.getString(R.string.account_recovery_key_setting_description_off)
            }
            state.data.securitySettings.find { it.id == AccountRecoveryKey }
                ?.let {
                    it.copy {
                        SettingsItemData.description set description
                        SettingsItemData.isLoading set false
                        SettingsItemData.enabled set true
                    }
                }
                ?.let { setting -> SettingsEvent.SettingsUpdated(setting = setting) }
        }
    val twoFaStatusFlow: Flow<SettingsEvent<ApplicationLock>> = use2faSettingStateHolder.use2faSettingStateFlow
        .mapNotNull { use2faSettingState ->
            if (use2faSettingState.visible) return@mapNotNull null
            SettingsEvent.SettingsRemoved(listOf(TwoFA, DisableLocal2FA))
        }
    return merge(
        pinFlow,
        biometricFlow,
        biometricRecoveryFlow,
        accountRecoveryKeyFlow,
        twoFaStatusFlow,
    )
}

fun waitForLock(
    lockManager: LockManager,
    state: SettingsApplicationLockState.View.WaitingForLock,
): Flow<SettingsEvent<ApplicationLock>> {
    return lockManager.lockEventFlow
        .take(1)
        .mapNotNull { lockEvent ->
            if (lockEvent is LockEvent.Unlock) {
                Unlocked(state.sideEffect)
            } else {
                SettingsLoaded(state.data.securitySettings)
            }
        }
}

fun dialogPositive(
    sessionManager: SessionManager,
    biometricRecovery: BiometricRecovery,
    biometricSetupRepository: BiometricSetupRepository,
    lockManager: LockManager,
    state: SettingsApplicationLockState.View.DialogConfirmed,
): Flow<SettingsEvent<ApplicationLock>> {
    return flow {
        val session = sessionManager.session ?: return@flow
        when (state.data.dialog) {
            SettingsDialog.BiometricRecoveryAutoDisable -> {
                
                val hasPin = LockType.PinCode in lockManager.getLocks(session.username)
                biometricRecovery.setBiometricRecoveryFeatureEnabledAsync(false)
                biometricSetupRepository.disableBiometric(username = session.username, deleteSavedCredentials = !hasPin)
            }

            SettingsDialog.DisableBiometricRecovery -> {
                
                biometricRecovery.setBiometricRecoveryFeatureEnabledAsync(false)
            }

            is SettingsDialog.WeakBiometricWarning -> {
                
                emit(Unlocked(GoToPinLock))
            }

            SettingsDialog.WeakBiometricRecoveryWarning -> emit(Unlocked(GoToBiometricRecovery))
            else -> Unit
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun updatePinLock(
    lockManager: LockManager,
    sessionManager: SessionManager,
    pinSetupRepository: PinSetupRepository,
    state: SettingsApplicationLockState.View.UpdatePinLock,
): Flow<SettingsEvent<ApplicationLock>> {
    return lockManager.lockEventFlow
        .take(1)
        .flatMapMerge { lockEvent ->
            flow {
                if (lockEvent !is LockEvent.Unlock) {
                    emit(SettingsLoaded(state.data.securitySettings))
                    return@flow
                }
                if (state.checked) {
                    emit(Unlocked(GoToPinLock))
                } else {
                    emit(SettingsLoaded(state.data.securitySettings))
                    sessionManager.session?.let { pinSetupRepository.disablePin(it) }
                }
            }
        }
}

fun updateBiometricLock(
    lockManager: LockManager,
    sessionManager: SessionManager,
    biometricAuthModule: BiometricAuthModule,
    biometricRecovery: BiometricRecovery,
    biometricSetupRepository: BiometricSetupRepository,
    state: SettingsApplicationLockState.View.UpdateBiometricLock,
): Flow<SettingsEvent<ApplicationLock>> {
    return lockManager.lockEventFlow
        .take(1)
        .mapNotNull { lockEvent ->
            val session = sessionManager.session
            if (session == null || lockEvent !is LockEvent.Unlock) {
                return@mapNotNull SettingsLoaded(state.data.securitySettings)
            }
            val hasPin = LockType.PinCode in lockManager.getLocks(session.username)
            if (state.checked) {
                if (biometricAuthModule.isOnlyWeakSupported()) {
                    
                    DisplayDialog(dialog = SettingsDialog.WeakBiometricWarning(hasPin))
                } else {
                    Unlocked(sideEffect = GoToBiometricLock)
                }
            } else {
                if (biometricRecovery.isFeatureEnabled()) {
                    DisplayDialog(dialog = SettingsDialog.BiometricRecoveryAutoDisable)
                } else {
                    biometricSetupRepository.disableBiometric(
                        username = session.username,
                        deleteSavedCredentials = !hasPin,
                    )
                    null
                }
            }
        }
}

fun update2FA(
    sessionManager: SessionManager,
    accountStatusRepository: AccountStatusRepository,
    state: SettingsApplicationLockState.View.Update2FA,
): Flow<SettingsEvent<ApplicationLock>> {
    return flow {
        val session = sessionManager.session ?: run {
            emit(SettingsEvent.Error(SettingsError.Generic))
            return@flow
        }

        if (accountStatusRepository[session]?.is2FAEnforced() == true) {
            emit(Unlocked(GoToDisable2FAEnforced))
        } else {
            emit(Unlocked(GoToDisable2FA))
        }
    }
}

fun updateBiometricRecovery(
    lockManager: LockManager,
    sessionManager: SessionManager,
    biometricAuthModule: BiometricAuthModule,
    biometricRecovery: BiometricRecovery,
    state: SettingsApplicationLockState.View.UpdateBiometricRecovery,
): Flow<SettingsEvent<ApplicationLock>> {
    return lockManager.lockEventFlow
        .take(1)
        .mapNotNull { lockEvent ->
            val session = sessionManager.session ?: return@mapNotNull null
            if (lockEvent !is LockEvent.Unlock) return@mapNotNull SettingsLoaded<ApplicationLock>(state.data.securitySettings)
            if (state.checked) {
                if (biometricAuthModule.isOnlyWeakSupported()) {
                    DisplayDialog(dialog = SettingsDialog.WeakBiometricRecoveryWarning)
                } else {
                    val hasBiometric = LockType.Biometric in lockManager.getLocks(session.username)
                    if (!hasBiometric) {
                        Unlocked(GoToBiometricRecovery)
                    } else {
                        biometricRecovery.setBiometricRecoveryFeatureEnabledAsync(true)
                        null
                    }
                }
            } else {
                biometricRecovery.setBiometricRecoveryFeatureEnabledAsync(false)
                null
            }
        }
        .onStart {
            biometricRecovery.isFeatureKnown = true
        }
}

fun updateDisableLocal2FA(
    lockManager: LockManager,
    sessionManager: SessionManager,
    sessionCredentialsSaver: SessionCredentialsSaver,
    state: SettingsApplicationLockState.View.UpdateDisableLocal2FA,
): Flow<SettingsEvent<ApplicationLock>> {
    return flow {
        val session = sessionManager.session ?: run {
            emit(SettingsLoaded(state.data.securitySettings))
            return@flow
        }

        val checked = state.checked
        if (checked) {
            val lockEvent = lockManager.lockEventFlow.first()
            if (lockEvent !is LockEvent.Unlock) {
                emit(SettingsLoaded(state.data.securitySettings))
                return@flow
            }
            sessionCredentialsSaver.saveServerKey(
                serverKey = session.appKey.serverKeyUtf8Bytes?.use(ObfuscatedByteArray::toByteArray),
                localKey = session.localKey,
                username = session.username,
            )
        } else {
            sessionCredentialsSaver.removeServerKey(session.username)
        }
        val securitySettings = state.data.securitySettings
            .toMutableList()
            .updateSetting(id = DisableLocal2FA, checked = checked)
        emit(SettingsLoaded(securitySettings))
    }
}

fun updateAutoLockOnExit(
    lockManager: LockManager,
    state: SettingsApplicationLockState.View.UpdateAutoLockOnExit,
): Flow<SettingsEvent<ApplicationLock>> {
    return lockManager.lockEventFlow
        .take(1)
        .mapNotNull { lockEvent ->
            if (lockEvent !is LockEvent.Unlock) return@mapNotNull SettingsLoaded(state.data.securitySettings)
            val checked = state.checked

            lockManager.setLockOnExit(checked)
            val securitySettings = state.data.securitySettings
                .toMutableList()
                .updateSetting(id = AutoLockOnExit, checked = checked)
            SettingsLoaded(securitySettings)
        }
}

fun updateAutoLockTime(
    @ApplicationContext context: Context,
    lockManager: LockManager,
    state: SettingsApplicationLockState.View.UpdateAutoLockTime,
): Flow<SettingsEvent<ApplicationLock>> {
    return lockManager.lockEventFlow
        .take(1)
        .mapNotNull { lockEvent ->
            if (lockEvent !is LockEvent.Unlock) return@mapNotNull SettingsLoaded(state.data.securitySettings)

            val selected = getCurrentTimeoutArrayPosition(context.resources, lockManager.lockTimeout)
            DisplayDialog(SettingsDialog.AutoLockTimeSelector(selected))
        }
}

fun autoLockTimeSelectedDialogPositive(
    @ApplicationContext context: Context,
    lockManager: LockManager,
    sessionManager: SessionManager,
    state: SettingsApplicationLockState.View.AutoLockTimeDialogConfirmed,
): Flow<SettingsEvent<ApplicationLock>> {
    return flow {
        val timeOutValue =
            Duration.ofSeconds(context.resources.getIntArray(R.array.timeout_values)[state.selected].toLong())
        val timeoutText = if (state.selected <= 0) {
            context.getString(R.string.settings_use_lock_timeout_description_never_lock)
        } else {
            context.getString(
                R.string.settings_use_lock_timeout_description,
                context.resources.getStringArray(R.array.timeout_strings)[state.selected],
            )
        }
        sessionManager.session?.username?.let { lockManager.updateLockTimeout(timeOutValue, it) }
        val securitySettings = state.data.securitySettings.toMutableList()
        securitySettings
            .find { it.id == AutoLockTime }
            ?.let { setting ->
                val index = securitySettings.indexOf(setting)
                securitySettings[index] = SettingsItemData.description.modify(setting) { timeoutText }
            }
        emit(SettingsLoaded(securitySettings))
    }
}

internal fun getCurrentTimeoutArrayPosition(res: Resources, lockTimeout: Duration): Int {
    return Arrays.binarySearch(res.getIntArray(R.array.timeout_values), lockTimeout.seconds.toInt())
}

private suspend fun getSettingAsyncStatus(
    settings: MutableList<SettingsItemData>,
    context: Context,
    session: Session,
    has2FA: Boolean,
    get2FAStatusService: Get2FAStatusService,
    accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
): MutableList<SettingsItemData> = coroutineScope {
    val twoFaSetting = if (has2FA) {
        settings.find { it.id == TwoFA }
            ?.let { async { update2FAStatus(it, get2FAStatusService, session) } }
    } else {
        null
    }

    val arkSetting = settings.find { it.id == AccountRecoveryKey }
        ?.let { async { updateArkStatus(it, context, accountRecoveryKeyRepository) } }

    val twoFaUpdatedSetting = twoFaSetting?.await()
    val arkUpdatedSetting = arkSetting?.await()

    settings.apply {
        twoFaUpdatedSetting?.let { updatedSetting -> updateSetting(TwoFA) { updatedSetting } }
        arkUpdatedSetting?.let { updatedSetting -> updateSetting(AccountRecoveryKey) { updatedSetting } }
    }
}

private suspend fun update2FAStatus(
    setting: SettingsItemData,
    get2FAStatusService: Get2FAStatusService,
    session: Session,
): SettingsItemData {
    return try {
        val authSecurityType = get2FAStatusService.execute(session.authorization).data.type
        val is2FAEnabled =
            authSecurityType == AuthSecurityType.TOTP_LOGIN || authSecurityType == AuthSecurityType.TOTP_DEVICE_REGISTRATION
        setting.copy {
            SettingsItemData.enabled set is2FAEnabled
            SettingsItemData.isLoading set false
            SettingsItemData.toggle.checked set is2FAEnabled
        }
    } catch (_: Exception) {
        setting.copy {
            SettingsItemData.isLoading set false
        }
    }
}

private suspend fun updateArkStatus(
    setting: SettingsItemData,
    context: Context,
    accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
): SettingsItemData {
    return try {
        val accountRecoveryKeyStatus = accountRecoveryKeyRepository.getAccountRecoveryStatusAsync()
        val description = if (accountRecoveryKeyStatus.enabled) {
            context.getString(R.string.account_recovery_key_setting_description_on)
        } else {
            context.getString(R.string.account_recovery_key_setting_description_off)
        }
        setting.copy {
            SettingsItemData.enabled set true
            SettingsItemData.isLoading set false
            SettingsItemData.description set description
            SettingsItemData.visible set accountRecoveryKeyStatus.visible
        }
    } catch (_: Exception) {
        setting.copy {
            SettingsItemData.enabled set false
            SettingsItemData.isLoading set false
        }
    }
}
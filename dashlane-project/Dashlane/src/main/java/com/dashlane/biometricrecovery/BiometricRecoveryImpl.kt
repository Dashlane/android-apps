package com.dashlane.biometricrecovery

import com.dashlane.account.UserAccountStorage
import com.dashlane.device.DeviceUpdateManager
import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.dashlane.hardwaresecurity.BiometricRecovery
import com.dashlane.masterpassword.ChangeMasterPasswordFeatureAccessChecker
import com.dashlane.preference.PreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.user.UserAccountInfo
import dagger.Lazy
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Singleton
class BiometricRecoveryImpl @Inject constructor(
    private val masterPasswordFeatureAccessChecker: ChangeMasterPasswordFeatureAccessChecker,
    private val userAccountStorage: UserAccountStorage,
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager,
    private val biometricAuthModule: BiometricAuthModule,
    private val deviceUpdateManager: Lazy<DeviceUpdateManager>,
) : BiometricRecovery {
    private val biometricRecoveryStatusMutableFlow: MutableSharedFlow<Boolean> = MutableSharedFlow()
    override val biometricRecoveryStatusFlow: SharedFlow<Boolean> = biometricRecoveryStatusMutableFlow.asSharedFlow()

    private val userPreferencesManager: UserPreferencesManager
        get() = preferencesManager[sessionManager.session?.username]

    override fun isFeatureAvailable(): Boolean {
        sessionManager.session?.username
            ?.let { username -> userAccountStorage[username]?.accountType }
            ?.let { accountType ->
                return accountType is UserAccountInfo.AccountType.MasterPassword &&
                    biometricAuthModule.isHardwareSupported() &&
                    masterPasswordFeatureAccessChecker.canAccessFeature()
            } ?: return false
    }

    override fun isFeatureEnabled(): Boolean = userPreferencesManager.getBoolean(PREF_ENABLED)

    @Deprecated("Use suspend setBiometricRecoveryFeatureEnabledAsync instead")
    override fun setBiometricRecoveryFeatureEnabled(enabled: Boolean) {
        userPreferencesManager.putBoolean(PREF_ENABLED, enabled)
        biometricRecoveryStatusMutableFlow.tryEmit(enabled)
        deviceUpdateManager.get().updateIfNeeded()
    }

    override suspend fun setBiometricRecoveryFeatureEnabledAsync(enabled: Boolean) {
        userPreferencesManager.putBoolean(PREF_ENABLED, enabled)
        biometricRecoveryStatusMutableFlow.emit(enabled)
        deviceUpdateManager.get().updateIfNeeded()
    }

    override var isFeatureKnown by booleanPref(PREF_KNOWN)

    override fun isSetUpForUser(username: String): Boolean {
        return preferencesManager[username].getBoolean(PREF_ENABLED) &&
            biometricAuthModule.isFeatureEnabled(username)
    }

    private fun booleanPref(name: String) = object : ReadWriteProperty<Any, Boolean> {
        override fun getValue(thisRef: Any, property: KProperty<*>) = userPreferencesManager.getBoolean(name)

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
            userPreferencesManager.putBoolean(name, value)
        }
    }

    companion object {
        private const val PREF_ENABLED = "isAccountRecoveryEnabled"
        private const val PREF_KNOWN = "isAccountRecoveryKnown"
    }
}
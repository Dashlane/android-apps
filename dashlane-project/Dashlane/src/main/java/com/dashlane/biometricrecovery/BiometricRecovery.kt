package com.dashlane.biometricrecovery

import com.dashlane.user.UserAccountInfo
import com.dashlane.account.UserAccountStorage
import com.dashlane.device.DeviceUpdateManager
import com.dashlane.masterpassword.ChangeMasterPasswordFeatureAccessChecker
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Singleton
class BiometricRecovery @Inject constructor(
    private val masterPasswordFeatureAccessChecker: ChangeMasterPasswordFeatureAccessChecker,
    private val userAccountStorage: UserAccountStorage,
    private val userPreferencesManager: UserPreferencesManager,
    private val sessionManager: SessionManager,
    private val biometricAuthModule: BiometricAuthModule,
    private val deviceUpdateManager: Lazy<DeviceUpdateManager>,
) {
    fun isFeatureAvailable(): Boolean {
        sessionManager.session?.username
            ?.let { username -> userAccountStorage[username]?.accountType }
            ?.let { accountType ->
                return accountType is UserAccountInfo.AccountType.MasterPassword &&
                    biometricAuthModule.isHardwareSupported() &&
                    masterPasswordFeatureAccessChecker.canAccessFeature()
            } ?: return false
    }

    var isFeatureEnabled by booleanPref(PREF_ENABLED)
        private set

    fun setBiometricRecoveryFeatureEnabled(enabled: Boolean) {
        isFeatureEnabled = enabled
        deviceUpdateManager.get().updateIfNeeded()
    }

    var isFeatureKnown by booleanPref(PREF_KNOWN)

    fun isSetUpForUser(username: String) = userPreferencesManager.preferencesFor(username).getBoolean(PREF_ENABLED) &&
        biometricAuthModule.isFeatureEnabled(username)

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

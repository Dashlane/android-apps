package com.dashlane.accountrecovery

import com.dashlane.device.DeviceUpdateManager
import com.dashlane.masterpassword.ChangeMasterPasswordFeatureAccessChecker
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import dagger.Lazy
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AccountRecovery @Inject constructor(
    private val masterPasswordFeatureAccessChecker: ChangeMasterPasswordFeatureAccessChecker,
    private val userPreferencesManager: UserPreferencesManager,
    private val biometricAuthModule: BiometricAuthModule,
    private val deviceUpdateManager: Lazy<DeviceUpdateManager>,
    val logger: AccountRecoveryLogger
) {
    val isFeatureAvailable: Boolean
        get() = biometricAuthModule.isHardwareSupported() &&
                masterPasswordFeatureAccessChecker.canAccessFeature()

    var isFeatureEnabled by booleanPref(PREF_ENABLED)
        private set

    fun setFeatureEnabled(enabled: Boolean, originViewType: String?) {
        isFeatureEnabled = enabled
        logger.logAccountRecoveryActivation(enabled, originViewType)
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

package com.dashlane.security

import android.app.Activity
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import com.dashlane.R
import com.dashlane.debug.DeveloperUtilities
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.session.SessionManager
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.queryIntentActivitiesCompat
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Provider

class SecurityHelper @Inject constructor(
    private val lockManager: Lazy<LockManager>,
    private val keyguardManagerProvider: Provider<KeyguardManager>,
    private val sessionManager: SessionManager,
    packageManagerProvider: Provider<PackageManager>
) {
    private val keyguardManager
        get() = keyguardManagerProvider.get()

    private val intentHelper = IntentHelper(packageManagerProvider)

    fun isDeviceSecured(): Boolean {
        
        if (DeveloperUtilities.isSmoketestAccount(sessionManager.session)) {
            return true
        }
        return keyguardManager.isDeviceSecure
    }

    fun allowedToUsePin(): Boolean =
        isDeviceSecured()

    fun showPopupPinCodeDisableIfWasEnable(activity: Activity) {
        if (isDeviceSecured()) return

        val lockType = lockManager.get().getLockType(false)

        if (lockType != LockTypeManager.LOCK_TYPE_PIN_CODE && lockType != LockTypeManager.LOCK_TYPE_BIOMETRIC) {
            
            return
        }
        disableAllWeakLock()
        showPopupPinCodeDisable(activity)
    }

    fun showPopupPinCodeDisable(activity: Context?) {
        if (activity == null) {
            return
        }
        val builder = DialogHelper().builder(activity)
        builder.setTitle(R.string.settings_use_pincode_need_screen_lock_title)
            .setMessage(R.string.settings_use_pincode_need_screen_lock_description)
            .setNegativeButton(R.string.settings_use_pincode_need_screen_lock_action_dontuse, null)
            .setPositiveButtonStartSecuritySettingsIfAvailable(
                activity,
                R.string.settings_use_pincode_need_screen_lock_action_settings
            )
            .show()
    }

    fun showPopupBiometricsRequired(activity: Context, onCancel: () -> Unit) {
        DialogHelper()
            .builder(activity)
            .setTitle(R.string.biometric_prompt_registration_required_title)
            .setMessage(R.string.biometric_prompt_registration_required_message)
            .setNegativeButton(R.string.biometric_prompt_registration_required_button_cancel) { _, _ -> onCancel() }
            .setPositiveButtonRegisterBiometricsIfAvailable(
                activity,
                R.string.biometric_prompt_registration_required_button_continue
            )
            .setOnCancelListener { onCancel() }
            .show()
    }

    fun showPopupStrongerBiometricsRequired(activity: Context, onCancel: () -> Unit) {
        DialogHelper()
            .builder(activity)
            .setTitle(R.string.biometric_prompt_strong_registration_required_title)
            .setMessage(R.string.biometric_prompt_strong_registration_required_message)
            .setNegativeButton(R.string.biometric_prompt_strong_registration_required_button_cancel) { _, _ -> onCancel() }
            .setPositiveButtonRegisterBiometricsIfAvailable(
                activity,
                R.string.biometric_prompt_strong_registration_required_button_continue
            )
            .setOnDismissListener { onCancel() }
            .show()
    }

    private fun AlertDialog.Builder.setPositiveButtonStartSecuritySettingsIfAvailable(
        activity: Context,
        @StringRes
        textId: Int
    ) = apply {
        val intent = intentHelper.findEnableDeviceLockIntent()
        setPositiveButtonIfIntentNotNull(activity, textId, intent)
    }

    private fun AlertDialog.Builder.setPositiveButtonRegisterBiometricsIfAvailable(
        activity: Context,
        @StringRes
        textId: Int
    ) = apply {
        val intent = intentHelper.findEnableBiometricsIntent()
        setPositiveButtonIfIntentNotNull(activity, textId, intent)
    }

    private fun AlertDialog.Builder.setPositiveButtonIfIntentNotNull(
        activity: Context,
        textId: Int,
        intent: Intent?
    ) {
        if (intent != null) {
            setPositiveButton(textId) { dialog, _ ->
                dialog.dismiss()
                activity.startActivity(intent)
            }
        }
    }

    private fun disableAllWeakLock() {
        lockManager.get().setLockType(LockTypeManager.LOCK_TYPE_MASTER_PASSWORD)
    }

    private class IntentHelper(
        private val packageManagerProvider: Provider<PackageManager>
    ) {
        private val packageManager
            get() = packageManagerProvider.get()

        fun findEnableDeviceLockIntent(): Intent? =
            IntentRegistry.enableDeviceLockIntentSequence.find(::isAvailableIntent)

        fun findEnableBiometricsIntent(): Intent? =
            IntentRegistry.enableBiometricsIntentSequence.find(::isAvailableIntent)

        private fun isAvailableIntent(intent: Intent): Boolean =
            packageManager.queryIntentActivitiesCompat(intent, 0).size > 0
    }

    private object IntentRegistry {

        val enableDeviceLockIntentSequence
            get() = sequence {
                yield(intentSettingsEnableLockScreen)
                yield(intentSettingsSecurity)
            }

        val enableBiometricsIntentSequence
            get() = sequence {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    yield(intentSettingsBiometricsEnrollStrong)
                    yield(intentSettingsBiometricsEnroll)
                }
                yield(intentSettingsSecurity)
            }

        private val intentSettingsSecurity: Intent
            get() = createSettingsIntent(Settings.ACTION_SECURITY_SETTINGS)

        private val intentSettingsEnableLockScreen: Intent
            get() = createSettingsIntent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)

        @get:RequiresApi(Build.VERSION_CODES.R)
        private val intentSettingsBiometricsEnroll: Intent
            get() = createSettingsIntent(Settings.ACTION_BIOMETRIC_ENROLL)

        @get:RequiresApi(Build.VERSION_CODES.R)
        private val intentSettingsBiometricsEnrollStrong: Intent
            get() = createSettingsIntent(Settings.ACTION_BIOMETRIC_ENROLL).putExtra(
                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            )

        private fun createSettingsIntent(settingsAction: String): Intent =
            Intent(settingsAction).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
    }
}

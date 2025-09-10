package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class UserSettings(
    val hasAuthenticationWithRememberMe: Boolean? = null,
    val hasAuthenticationWithWebauthn: Boolean? = null,
    val hasLockOnExit: Boolean? = null,
    val hasAuthenticationWithBiometrics: Boolean? = null,
    val hasClearClipboard: Boolean? = null,
    val hasAutomaticTwoFactorAuthenticationTokenCopy: Boolean? = null,
    val hasMasterPasswordBiometricReset: Boolean? = null,
    val hasIdsProtectWithMasterPassword: Boolean? = null,
    val hasAuthenticationWithPin: Boolean? = null,
    val lockAutoTimeout: Int? = null,
    val hasPaymentsProtectWithMasterPassword: Boolean? = null,
    val hasAutofillActivated: Boolean? = null,
    val hasUnlockItemWithBiometric: Boolean? = null,
    val hasSecureNotesProtectWithMasterPassword: Boolean? = null,
    val hasCredentialsProtectWithMasterPassword: Boolean? = null,
) : TrackingLog {
    override val name
        get() = "user_settings"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = true

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["has_authentication_with_remember_me"] = hasAuthenticationWithRememberMe
        collector["has_authentication_with_webauthn"] = hasAuthenticationWithWebauthn
        collector["has_lock_on_exit"] = hasLockOnExit
        collector["has_authentication_with_biometrics"] = hasAuthenticationWithBiometrics
        collector["has_clear_clipboard"] = hasClearClipboard
        collector["has_automatic_two_factor_authentication_token_copy"] = hasAutomaticTwoFactorAuthenticationTokenCopy
        collector["has_master_password_biometric_reset"] = hasMasterPasswordBiometricReset
        collector["has_ids_protect_with_master_password"] = hasIdsProtectWithMasterPassword
        collector["has_authentication_with_pin"] = hasAuthenticationWithPin
        collector["lock_auto_timeout"] = lockAutoTimeout
        collector["has_payments_protect_with_master_password"] = hasPaymentsProtectWithMasterPassword
        collector["has_autofill_activated"] = hasAutofillActivated
        collector["has_unlock_item_with_biometric"] = hasUnlockItemWithBiometric
        collector["has_secure_notes_protect_with_master_password"] = hasSecureNotesProtectWithMasterPassword
        collector["has_credentials_protect_with_master_password"] = hasCredentialsProtectWithMasterPassword
    }
}
package com.dashlane.login

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Mode
import com.dashlane.hermes.generated.definitions.Reason
import com.dashlane.hermes.generated.definitions.Status
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.hermes.generated.events.user.AskAuthentication
import com.dashlane.hermes.generated.events.user.AskUseOtherAuthentication
import com.dashlane.hermes.generated.events.user.ForgetMasterPassword
import com.dashlane.hermes.generated.events.user.Login
import com.dashlane.hermes.generated.events.user.Logout
import com.dashlane.hermes.generated.events.user.ResendToken
import com.dashlane.hermes.generated.events.user.UseAnotherAccount
import com.dashlane.lock.UnlockEvent

interface LoginLogger {
    

    fun logSuccess(isFirstLogin: Boolean = false, loginMode: LoginMode)

    

    fun logWrongPassword(verification: VerificationMode)
    fun logWrongEmail()
    fun logWrongOtp(verification: VerificationMode)
    fun logWrongBiometric()
    fun logWrongPin()
    fun logInvalidSso()

    

    fun logErrorUnknown(loginMode: LoginMode)

    

    fun logResendToken()

    

    fun logAskAuthentication(loginMode: LoginMode)

    

    fun logUseAnotherAccount()

    

    fun logForgetMasterPassword(hasBiometricReset: Boolean)

    

    fun logAskUseMasterPassword(originMode: Mode)

    

    fun logAskUseSso(originMode: Mode)
}

@Suppress("FunctionName")
fun LoginLogger(
    logRepository: LogRepository,
    unlockEventReason: UnlockEvent.Reason? = null
): LoginLogger = LoginLoggerImpl(logRepository, unlockEventReason)

private class LoginLoggerImpl(
    private val logRepository: LogRepository,
    private val unlockEventReason: UnlockEvent.Reason?
) : LoginLogger {
    override fun logSuccess(isFirstLogin: Boolean, loginMode: LoginMode) {
        val mode = loginMode.toMode()

        logEvent(
            Login(
                isFirstLogin = isFirstLogin,
                status = Status.SUCCESS,
                mode = mode ?: return, 
                verificationMode = loginMode.verification,
                isBackupCode = false 
            )
        )
    }

    override fun logWrongPassword(verification: VerificationMode) {
        logEvent(
            Login(
                verificationMode = verification,
                status = Status.ERROR_WRONG_PASSWORD,
                mode = Mode.MASTER_PASSWORD,
                isBackupCode = false,
                isFirstLogin = when (verification) {
                    VerificationMode.OTP1,
                    VerificationMode.EMAIL_TOKEN,
                    VerificationMode.AUTHENTICATOR_APP -> true
                    VerificationMode.OTP2,
                    VerificationMode.NONE -> false
                }
            )
        )
    }

    override fun logWrongEmail() {
        logEvent(
            Login(
                status = Status.ERROR_WRONG_EMAIL,
                mode = Mode.MASTER_PASSWORD
            )
        )
    }

    override fun logWrongOtp(verification: VerificationMode) {
        logEvent(
            Login(
                status = Status.ERROR_WRONG_OTP,
                mode = Mode.MASTER_PASSWORD,
                verificationMode = verification,
                isBackupCode = false,
                isFirstLogin = verification != VerificationMode.OTP2
            )
        )
    }

    override fun logWrongBiometric() {
        logEvent(
            Login(
                status = Status.ERROR_WRONG_BIOMETRIC,
                mode = Mode.BIOMETRIC,
                isBackupCode = false,
                verificationMode = VerificationMode.NONE
            )
        )
    }

    override fun logWrongPin() {
        logEvent(
            Login(
                status = Status.ERROR_WRONG_PIN,
                mode = Mode.PIN,
                isBackupCode = false,
                verificationMode = VerificationMode.NONE
            )
        )
    }

    override fun logInvalidSso() {
        logEvent(
            Login(
                status = Status.ERROR_INVALID_SSO,
                mode = Mode.SSO,
                isBackupCode = false
            )
        )
    }

    override fun logErrorUnknown(loginMode: LoginMode) {
        val mode = loginMode.toMode()

        logEvent(
            Login(
                mode = mode,
                isBackupCode = false,
                verificationMode = loginMode.verification,
                status = Status.ERROR_UNKNOWN
            )
        )
    }

    override fun logResendToken() {
        logEvent(ResendToken())
    }

    override fun logUseAnotherAccount() {
        logEvent(UseAnotherAccount())
        logEvent(Logout())
    }

    override fun logForgetMasterPassword(hasBiometricReset: Boolean) {
        logEvent(
            ForgetMasterPassword(
                hasBiometricReset = hasBiometricReset,
                hasTeamAccountRecovery = false
            )
        )
    }

    override fun logAskAuthentication(loginMode: LoginMode) {
        val mode = loginMode.toMode()

        logEvent(
            AskAuthentication(
                mode = mode ?: return,
                reason = when (unlockEventReason) {
                    is UnlockEvent.Reason.WithCode -> when (unlockEventReason.origin) {
                        UnlockEvent.Reason.WithCode.Origin.EDIT_SETTINGS -> Reason.EDIT_SETTINGS
                        UnlockEvent.Reason.WithCode.Origin.CHANGE_MASTER_PASSWORD -> Reason.CHANGE_MASTER_PASSWORD
                        null -> Reason.UNLOCK_APP
                    }
                    is UnlockEvent.Reason.OpenItem -> Reason.UNLOCK_ITEM
                    is UnlockEvent.Reason.AppAccess,
                    is UnlockEvent.Reason.AccessFromAutofillApi,
                    is UnlockEvent.Reason.AccessFromExternalComponent -> Reason.UNLOCK_APP
                    else -> Reason.LOGIN
                },
                verificationMode = loginMode.verification
            )
        )
    }

    override fun logAskUseMasterPassword(originMode: Mode) {
        logAskUseOtherAuthentication(originMode, Mode.MASTER_PASSWORD)
    }

    override fun logAskUseSso(originMode: Mode) {
        logAskUseOtherAuthentication(originMode, Mode.SSO)
    }

    private fun logAskUseOtherAuthentication(previous: Mode, next: Mode) {
        logEvent(
            AskUseOtherAuthentication(
                previous = previous,
                next = next
            )
        )
    }

    private fun logEvent(event: TrackingLog) {
        logRepository.queueEvent(event)
    }
}

private val LoginMode.verification get() = if (this is LoginMode.MasterPassword) verification else null

private fun LoginMode.toMode(): Mode? = when (this) {
    LoginMode.Biometric -> Mode.BIOMETRIC
    LoginMode.Pin -> Mode.PIN
    is LoginMode.MasterPassword -> Mode.MASTER_PASSWORD
    LoginMode.Sso -> Mode.SSO
    LoginMode.SessionRestore,
    LoginMode.MasterPasswordChanger -> null
}
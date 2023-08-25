package com.dashlane.masterpassword.logger

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.ChangeMasterPasswordError
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.events.user.ChangeMasterPassword
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository

class ChangeMasterPasswordLogger(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val sender: String? = null,
    private val logRepository: LogRepository
) {

    fun logChangeMasterPasswordStart() {
        logRepository.queueEvent(ChangeMasterPassword(flowStep = FlowStep.START))
    }

    fun logChangeMasterPasswordCancel() {
        logRepository.queueEvent(ChangeMasterPassword(flowStep = FlowStep.CANCEL))
    }

    fun logChangeMasterPasswordComplete() {
        logRepository.queueEvent(ChangeMasterPassword(flowStep = FlowStep.COMPLETE))
    }

    fun logChangeMasterPasswordError(error: ChangeMasterPasswordError) {
        logRepository.queueEvent(
            ChangeMasterPassword(
                flowStep = FlowStep.ERROR,
                errorName = error
            )
        )

        
        when (error) {
            ChangeMasterPasswordError.CIPHER_ERROR -> logError(ERROR_CIPHER)
            ChangeMasterPasswordError.CONFIRMATION_ERROR -> logError(ERROR_CONFIRMATION)
            ChangeMasterPasswordError.DECIPHER_ERROR -> logError(ERROR_DECIPHER)
            ChangeMasterPasswordError.DOWNLOAD_ERROR -> logError(ERROR_DOWNLOAD)
            ChangeMasterPasswordError.LOGIN_ERROR -> logError(ERROR_LOGIN)
            ChangeMasterPasswordError.SYNC_FAILED_ERROR -> logError(ERROR_SYNC_FAILED)
            ChangeMasterPasswordError.UNKNOWN_ERROR -> logError(ERROR_UNKNOWN)
            ChangeMasterPasswordError.UPLOAD_ERROR -> logError(ERROR_UPLOAD)
            ChangeMasterPasswordError.WEAK_PASSWORD_ERROR,
            ChangeMasterPasswordError.SAME_PASSWORD_ERROR,
            ChangeMasterPasswordError.WRONG_PASSWORD_ERROR,
            ChangeMasterPasswordError.PASSWORDS_DONT_MATCH -> {
                
            }
        }
    }

    fun logDisplayEnterPasswordStep() = logEnterPassword("display")
    fun logClickShowTips() = logEnterPassword("show_tips")
    fun logDisplayConfirmPasswordStep() = logConfirmPassword("display")
    fun logClickConfirmPasswordStep() = logConfirmPassword("confirm")
    fun logDisplayPasswordChanged() = log("display", subType = "password_changed")

    private fun logError(subAction: String) {
        log("display", subType = "error_prompt", subAction = subAction)
    }

    private fun logEnterPassword(action: String) {
        log(action, subType = "enter_new_password")
    }

    private fun logConfirmPassword(action: String) {
        log(action, subType = "confirm_password")
    }

    private fun log(action: String, subAction: String? = null, subType: String? = null) {
        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(
                UsageLogCode75(
                    type = "change_master_password",
                    action = action,
                    subaction = subAction,
                    subtype = subType,
                    originStr = sender
                )
            )
    }

    companion object {
        const val ERROR_DOWNLOAD = "download"

        const val ERROR_DECIPHER = "decipher"

        const val ERROR_CIPHER = "cipher"

        const val ERROR_UPLOAD = "upload"

        const val ERROR_CONFIRMATION = "confirm_failed"

        const val ERROR_LOGIN = "login"

        const val ERROR_SYNC_FAILED = "sync_failed"

        const val ERROR_UNKNOWN = "unknown"
    }
}
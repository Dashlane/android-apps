package com.dashlane.masterpassword.logger

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.ChangeMasterPasswordError
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.events.user.ChangeMasterPassword

class ChangeMasterPasswordLogger(
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
    }
}
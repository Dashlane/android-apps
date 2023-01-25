package com.dashlane.activatetotp

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.definitions.FlowType
import com.dashlane.hermes.generated.definitions.TwoFactorAuthenticationError
import com.dashlane.hermes.generated.events.user.ChangeTwoFactorAuthenticationSetting
import dagger.Reusable
import javax.inject.Inject

@Reusable
class ActivateTotpLogger @Inject constructor(
    private val logRepository: LogRepository
) {
    fun logActivationStart() {
        logRepository.queueEvent(
            ChangeTwoFactorAuthenticationSetting(
                flowType = FlowType.ACTIVATION,
                flowStep = FlowStep.START
            )
        )
    }

    fun logActivationCancel() {
        logRepository.queueEvent(
            ChangeTwoFactorAuthenticationSetting(
                flowType = FlowType.ACTIVATION,
                flowStep = FlowStep.CANCEL
            )
        )
    }

    fun logActivationComplete() {
        logRepository.queueEvent(
            ChangeTwoFactorAuthenticationSetting(
                flowType = FlowType.ACTIVATION,
                flowStep = FlowStep.COMPLETE
            )
        )
    }

    fun logActivationError(error: TwoFactorAuthenticationError) {
        logRepository.queueEvent(
            ChangeTwoFactorAuthenticationSetting(
                flowType = FlowType.ACTIVATION,
                flowStep = FlowStep.ERROR,
                errorName = error
            )
        )
    }
}
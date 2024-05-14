package com.dashlane.limitations

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.Button
import com.dashlane.hermes.generated.definitions.CallToAction.BUY_DASHLANE
import com.dashlane.hermes.generated.definitions.CallToAction.DISMISS
import com.dashlane.hermes.generated.definitions.ClickOrigin
import com.dashlane.hermes.generated.events.user.CallToAction
import com.dashlane.hermes.generated.events.user.Click
import javax.inject.Inject

class PasswordLimitationLogger @Inject constructor(private val logRepository: LogRepository) {
    fun upgradeFromBanner() {
        logRepository.queueEvent(
            Click(
                button = Button.BUY_DASHLANE,
                clickOrigin = ClickOrigin.BANNER_PASSWORD_LIMIT_REACHED
            )
        )
    }

    fun upgradeFromBottomSheetPaywall() {
        logRepository.queueEvent(
            CallToAction(
                callToActionList = listOf(DISMISS, BUY_DASHLANE),
                chosenAction = BUY_DASHLANE,
                hasChosenNoAction = false,
            )
        )
    }

    fun dismissBottomSheetPaywall() {
        logRepository.queueEvent(
            CallToAction(
                callToActionList = listOf(DISMISS, BUY_DASHLANE),
                chosenAction = DISMISS,
                hasChosenNoAction = false,
            )
        )
    }

    fun cancelBottomSheetPaywall() {
        logRepository.queueEvent(
            CallToAction(
                callToActionList = listOf(DISMISS, BUY_DASHLANE),
                hasChosenNoAction = true,
            )
        )
    }
}
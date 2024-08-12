package com.dashlane.frozenaccount.tracking

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.Button
import com.dashlane.hermes.generated.definitions.CallToAction.NOT_NOW
import com.dashlane.hermes.generated.definitions.CallToAction.UNFREEZE_ACCOUNT
import com.dashlane.hermes.generated.definitions.ClickOrigin
import com.dashlane.hermes.generated.events.user.CallToAction
import com.dashlane.hermes.generated.events.user.Click
import javax.inject.Inject

class FrozenStateLogger @Inject constructor(private val logRepository: LogRepository) {
    
    fun logVaultBannerClicked() {
        logRepository.queueEvent(
            Click(
                Button.UNFREEZE_ACCOUNT,
                clickOrigin = ClickOrigin.BANNER_FROZEN_ACCOUNT
            )
        )
    }

    
    fun logAutofillPaywallDisplayed() {
        logRepository.queuePageView(
            component = BrowseComponent.OS_AUTOFILL,
            page = AnyPage.AUTOFILL_NOTIFICATION_FROZEN_ACCOUNT
        )
    }

    fun logNotNowClicked() {
        logRepository.queueEvent(
            CallToAction(
                callToActionList = listOf(UNFREEZE_ACCOUNT, NOT_NOW),
                chosenAction = NOT_NOW,
                hasChosenNoAction = false
            )
        )
    }

    fun logUnfreezeAccountClicked() {
        logRepository.queueEvent(
            CallToAction(
                callToActionList = listOf(UNFREEZE_ACCOUNT, NOT_NOW),
                chosenAction = UNFREEZE_ACCOUNT,
                hasChosenNoAction = false
            )
        )
    }
}
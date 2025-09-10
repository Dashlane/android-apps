package com.dashlane.autofill.core

import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.securitywarnings.AutofillSecurityWarningsLogger
import com.dashlane.autofill.securitywarnings.data.getDomain
import com.dashlane.autofill.unlockfill.UnlockedAuthentifiant
import com.dashlane.core.helpers.SignatureVerification
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.AutofillMessageType
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.Button
import com.dashlane.hermes.generated.definitions.DismissType
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.events.anonymous.AutofillAcceptAnonymous
import com.dashlane.hermes.generated.events.anonymous.AutofillDismissAnonymous
import com.dashlane.hermes.generated.events.user.AutofillAccept
import com.dashlane.hermes.generated.events.user.AutofillDismiss
import com.dashlane.hermes.generated.events.user.AutofillSuggest
import com.dashlane.hermes.generated.events.user.Click
import javax.inject.Inject

class AutofillSecurityWarningsLoggerImpl @Inject constructor(
    private val logRepository: LogRepository,
) : AutofillSecurityWarningsLogger {
    override fun onDisplayedIncorrectWarning(
        security: SignatureVerification.Incorrect,
        fill: UnlockedAuthentifiant,
    ) {
        logDisplayAutofillWarning(
            formSource = fill.formSource,
            warningMessageType = AutofillMessageType.KNOWN_INCORRECT_SIGNATURE,
        )
    }

    override fun onDisplayedMismatchWarning(
        security: SignatureVerification.UnknownWithSignature,
        fill: UnlockedAuthentifiant,
    ) {
        logDisplayAutofillWarning(
            formSource = fill.formSource,
            warningMessageType = AutofillMessageType.KNOWN_SOURCE_ACCOUNT_FILL_MISMATCH,
        )
    }

    override fun onDisplayedUnknownWarning(
        security: SignatureVerification,
        fill: UnlockedAuthentifiant,
    ) {
        logDisplayAutofillWarning(
            formSource = fill.formSource,
            warningMessageType = AutofillMessageType.UNKNOWN_SOURCE_ACCOUNT_FILL_MISMATCH,
        )
    }

    override fun onApprovedWarning(
        fill: UnlockedAuthentifiant,
        neverShowAgain: Boolean,
    ) {
        logAcceptWarning(fill, neverShowAgain)
    }

    override fun onDeclinedWarning(
        fill: UnlockedAuthentifiant,
    ) {
        logDismissAutofill(fill)
    }

    private fun logDisplayAutofillWarning(
        formSource: AutoFillFormSource,
        warningMessageType: AutofillMessageType,
    ) {
        logRepository.queuePageView(
            component = BrowseComponent.OS_AUTOFILL,
            page = AnyPage.AUTOFILL_WARNING,
        )
        logRepository.queueEvent(
            AutofillSuggest(
                isNativeApp = formSource is ApplicationFormSource,
                autofillMessageTypeList = listOf(warningMessageType),
            ),
        )
    }

    private fun logAcceptWarning(
        fill: UnlockedAuthentifiant,
        neverShowAgain: Boolean,
    ) {
        if (neverShowAgain) {
            logRepository.queueEvent(
                Click(Button.OK),
            )
        }
        logRepository.queueEvent(
            AutofillAccept(dataTypeList = listOf(ItemType.CREDENTIAL)),
        )
        logRepository.queueEvent(
            AutofillAcceptAnonymous(domain = fill.formSource.getDomain()),
        )
    }

    private fun logDismissAutofill(fill: UnlockedAuthentifiant) {
        logRepository.queueEvent(
            AutofillDismiss(
                DismissType.CLOSE,
            ),
        )

        logRepository.queueEvent(
            AutofillDismissAnonymous(
                dismissType = DismissType.CLOSE,
                domain = fill.formSource.getDomain(),
                isNativeApp = fill.formSource is ApplicationFormSource,
            ),
        )
    }
}
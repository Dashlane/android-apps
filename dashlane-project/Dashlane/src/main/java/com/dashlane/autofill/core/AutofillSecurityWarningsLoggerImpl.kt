package com.dashlane.autofill.core

import com.dashlane.autofill.api.securitywarnings.AutofillSecurityWarningsLogger
import com.dashlane.autofill.api.unlockfill.UnlockedAuthentifiant
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.core.helpers.SignatureVerification
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.AutofillMessageType
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.Button
import com.dashlane.hermes.generated.definitions.DismissType
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.events.anonymous.AutofillAcceptAnonymous
import com.dashlane.hermes.generated.events.anonymous.AutofillDismissAnonymous
import com.dashlane.hermes.generated.events.user.AutofillAccept
import com.dashlane.hermes.generated.events.user.AutofillDismiss
import com.dashlane.hermes.generated.events.user.AutofillSuggest
import com.dashlane.hermes.generated.events.user.Click
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class AutofillSecurityWarningsLoggerImpl @Inject constructor(
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val logRepository: LogRepository
) : AutofillSecurityWarningsLogger, AutofillLegacyLogger(sessionManager, bySessionUsageLogRepository) {

    override fun onDisplayedIncorrectWarning(
        security: SignatureVerification.Incorrect,
        fill: UnlockedAuthentifiant
    ) {
        logDisplayAutofillWarning(fill.formSource, AutofillMessageType.KNOWN_INCORRECT_SIGNATURE)
    }

    override fun onDisplayedMismatchWarning(
        security: SignatureVerification.UnknownWithSignature,
        fill: UnlockedAuthentifiant
    ) {
        logDisplayAutofillWarning(
            fill.formSource,
            AutofillMessageType.KNOWN_SOURCE_ACCOUNT_FILL_MISMATCH
        )
    }

    override fun onDisplayedUnknownWarning(
        security: SignatureVerification,
        fill: UnlockedAuthentifiant
    ) {
        logDisplayAutofillWarning(
            fill.formSource,
            AutofillMessageType.UNKNOWN_SOURCE_ACCOUNT_FILL_MISMATCH
        )
    }

    override fun onApprovedIncorrectWarning(
        security: SignatureVerification.Incorrect,
        fill: UnlockedAuthentifiant,
        neverShowAgain: Boolean,
        domain: Domain
    ) {
        logAcceptWarning(neverShowAgain, domain)
    }

    override fun onApprovedMismatchWarning(
        security: SignatureVerification.UnknownWithSignature,
        fill: UnlockedAuthentifiant,
        neverShowAgain: Boolean,
        domain: Domain
    ) {
        logAcceptWarning(neverShowAgain, domain)
    }

    override fun onApprovedUnknownWarning(
        security: SignatureVerification,
        fill: UnlockedAuthentifiant,
        neverShowAgain: Boolean,
        domain: Domain
    ) {
        logAcceptWarning(neverShowAgain, domain)
    }

    override fun onDeclinedIncorrectWarning(
        security: SignatureVerification.Incorrect,
        fill: UnlockedAuthentifiant,
        domain: Domain
    ) {
        logDismissAutofill(fill, domain)
    }

    override fun onDeclinedMismatchWarning(
        security: SignatureVerification.UnknownWithSignature,
        fill: UnlockedAuthentifiant,
        domain: Domain
    ) {
        logDismissAutofill(fill, domain)
    }

    override fun onDeclinedUnknownWarning(
        security: SignatureVerification,
        fill: UnlockedAuthentifiant,
        domain: Domain
    ) {
        logDismissAutofill(fill, domain)
    }

    private fun logDisplayAutofillWarning(
        formSource: AutoFillFormSource,
        warningMessageType: AutofillMessageType
    ) {
        logRepository.queuePageView(
            component = BrowseComponent.OS_AUTOFILL,
            page = AnyPage.AUTOFILL_WARNING
        )
        logRepository.queueEvent(
            AutofillSuggest(
                isNativeApp = formSource is ApplicationFormSource,
                autofillMessageTypeList = listOf(warningMessageType)
            )
        )
    }

    private fun logAcceptWarning(neverShowAgain: Boolean, domain: Domain) {
        if (neverShowAgain) {
            logRepository.queueEvent(
                Click(Button.OK)
            )
        }
        logRepository.queueEvent(
            AutofillAccept(dataTypeList = listOf(ItemType.CREDENTIAL))
        )
        logRepository.queueEvent(
            AutofillAcceptAnonymous(domain = domain)
        )
    }

    private fun logDismissAutofill(fill: UnlockedAuthentifiant, domain: Domain) {
        logRepository.queueEvent(
            AutofillDismiss(
                DismissType.CLOSE
            )
        )

        logRepository.queueEvent(
            AutofillDismissAnonymous(
                dismissType = DismissType.CLOSE,
                domain = domain,
                isNativeApp = fill.formSource is ApplicationFormSource
            )
        )
    }
}

package com.dashlane.autofill.core

import com.dashlane.autofill.api.securitywarnings.AutofillSecurityWarningsLogger
import com.dashlane.autofill.api.unlockfill.UnlockedAuthentifiant
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.core.helpers.AppSignature
import com.dashlane.core.helpers.SignatureVerification
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.Button
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.AutofillMessageType
import com.dashlane.hermes.generated.definitions.DismissType
import com.dashlane.hermes.generated.events.anonymous.AutofillAcceptAnonymous
import com.dashlane.hermes.generated.events.anonymous.AutofillDismissAnonymous
import com.dashlane.hermes.generated.events.user.AutofillAccept
import com.dashlane.hermes.generated.events.user.AutofillDismiss
import com.dashlane.hermes.generated.events.user.AutofillSuggest
import com.dashlane.hermes.generated.events.user.Click
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.useractivity.log.usage.UsageLogCode136
import com.dashlane.useractivity.log.usage.UsageLogCode136.SecureResult
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.vault.model.urlForUsageLog
import com.dashlane.vault.summary.SummaryObject
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

        
        onWarningAutofillAction(
            UsageLogCode136.Action.AUTOFILL_WARNING_DISPLAYED,
            security,
            fill.authentifiantSummary,
            fill.formSource,
            security.toIncorrectSecureResult()
        )
    }

    override fun onDisplayedMismatchWarning(
        security: SignatureVerification.UnknownWithSignature,
        fill: UnlockedAuthentifiant
    ) {
        logDisplayAutofillWarning(
            fill.formSource,
            AutofillMessageType.KNOWN_SOURCE_ACCOUNT_FILL_MISMATCH
        )

        
        onWarningAutofillAction(
            UsageLogCode136.Action.AUTOFILL_WARNING_DISPLAYED,
            security,
            fill.authentifiantSummary,
            fill.formSource,
            security.toMismatchSecureResult()
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

        
        onWarningAutofillAction(
            UsageLogCode136.Action.AUTOFILL_WARNING_DISPLAYED,
            security,
            fill.authentifiantSummary,
            fill.formSource,
            security.toUnknownSecureResult()
        )
    }

    override fun onApprovedIncorrectWarning(
        security: SignatureVerification.Incorrect,
        fill: UnlockedAuthentifiant,
        neverShowAgain: Boolean,
        domain: Domain
    ) {
        logAcceptWarning(neverShowAgain, domain)

        
        onWarningAutofillAction(
            UsageLogCode136.Action.AUTOFILL_WARNING_APPROVED,
            security,
            fill.authentifiantSummary,
            fill.formSource,
            security.toIncorrectSecureResult(),
            neverShowAgain
        )
    }

    override fun onApprovedMismatchWarning(
        security: SignatureVerification.UnknownWithSignature,
        fill: UnlockedAuthentifiant,
        neverShowAgain: Boolean,
        domain: Domain
    ) {
        logAcceptWarning(neverShowAgain, domain)

        
        onWarningAutofillAction(
            UsageLogCode136.Action.AUTOFILL_WARNING_APPROVED,
            security,
            fill.authentifiantSummary,
            fill.formSource,
            security.toMismatchSecureResult(),
            neverShowAgain
        )
    }

    override fun onApprovedUnknownWarning(
        security: SignatureVerification,
        fill: UnlockedAuthentifiant,
        neverShowAgain: Boolean,
        domain: Domain
    ) {
        logAcceptWarning(neverShowAgain, domain)

        
        onWarningAutofillAction(
            UsageLogCode136.Action.AUTOFILL_WARNING_APPROVED,
            security,
            fill.authentifiantSummary,
            fill.formSource,
            security.toUnknownSecureResult(),
            neverShowAgain
        )
    }

    override fun onDeclinedIncorrectWarning(
        security: SignatureVerification.Incorrect,
        fill: UnlockedAuthentifiant,
        domain: Domain
    ) {
        logDismissAutofill(fill, domain)

        
        onWarningAutofillAction(
            UsageLogCode136.Action.AUTOFILL_WARNING_DECLINED,
            security,
            fill.authentifiantSummary,
            fill.formSource,
            security.toIncorrectSecureResult()
        )
    }

    override fun onDeclinedMismatchWarning(
        security: SignatureVerification.UnknownWithSignature,
        fill: UnlockedAuthentifiant,
        domain: Domain
    ) {
        logDismissAutofill(fill, domain)

        
        onWarningAutofillAction(
            UsageLogCode136.Action.AUTOFILL_WARNING_DECLINED,
            security,
            fill.authentifiantSummary,
            fill.formSource,
            security.toMismatchSecureResult()
        )
    }

    override fun onDeclinedUnknownWarning(
        security: SignatureVerification,
        fill: UnlockedAuthentifiant,
        domain: Domain
    ) {
        logDismissAutofill(fill, domain)

        
        onWarningAutofillAction(
            UsageLogCode136.Action.AUTOFILL_WARNING_DECLINED,
            security,
            fill.authentifiantSummary,
            fill.formSource,
            security.toUnknownSecureResult()
        )
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

    private fun SignatureVerification.Incorrect.toIncorrectSecureResult(): SecureResult {
        return if (this.signatureKnown != null) {
            SecureResult.INCORRECT_SIGNATURE_IN_KNOWN
        } else {
            SecureResult.INCORRECT_SIGNATURE_IN_APP_META
        }
    }

    private fun SignatureVerification.UnknownWithSignature.toMismatchSecureResult(): SecureResult {
        return if (this.knownApplication != null) {
            SecureResult.MISMATCH_FOR_KNOWN_SOURCE_ACCOUNT_FILL
        } else {
            SecureResult.MISMATCH_FOR_UNKNOWN_SOURCE_ACCOUNT_FILL
        }
    }

    private fun SignatureVerification.toUnknownSecureResult(): SecureResult {
        return if (this.signatureInstalled == null) {
            SecureResult.UNKNOWN_NO_SIGNATURES
        } else {
            SecureResult.UNKNOWN_ONE_SIGNATURE
        }
    }

    private fun onWarningAutofillAction(
        action: UsageLogCode136.Action,
        signatureVerification: SignatureVerification,
        authentifiant: SummaryObject.Authentifiant,
        autoFillFormSource: AutoFillFormSource,
        secureResult: SecureResult,
        neverShowAgain: Boolean? = null
    ) {
        val (appId, sourceWebsite) = when (autoFillFormSource) {
            is ApplicationFormSource ->
                autoFillFormSource.packageName to null
            is WebDomainFormSource ->
                autoFillFormSource.packageName to autoFillFormSource.webDomain.toUrlDomainOrNull()?.root?.value
        }
        val (sha128signatures, sha256signatures) = signatureVerification.toInstalledLogSignatures()
        val (sha128signaturesExpected, sha256signaturesExpected) = signatureVerification.toExpectedLogSignatures()

        log(
            UsageLogCode136(
                appId = appId,
                website = authentifiant.urlForUsageLog.toUrlDomainOrNull()?.root?.value,
                action = action,
                
                sha128signatures = sha128signatures,
                
                sha256signatures = sha256signatures,
                
                sha128signaturesExpected = sha128signaturesExpected,
                
                sha256signaturesExpected = sha256signaturesExpected,
                sourceWebsite = sourceWebsite,
                neverShowAgain = neverShowAgain,
                secureResult = secureResult
            )
        )
    }

    private fun SignatureVerification.toInstalledLogSignatures(): Pair<String?, String?> =
        this.signatureInstalled.toLogSignatures()

    private fun SignatureVerification.toExpectedLogSignatures(): Pair<String?, String?> {
        return when (this) {
            is SignatureVerification.VaultLinkedApps -> signatureInMeta.toLogSignatures()
            is SignatureVerification.Known -> signatureKnown.toLogSignatures()
            else -> null to null
        }
    }

    private fun AppSignature?.toLogSignatures(): Pair<String?, String?> {
        val sha128 = this?.sha256Signatures?.take(3)?.joinToString()
        val sha256 = this?.sha512Signatures?.take(3)?.joinToString()

        return sha128 to sha256
    }
}

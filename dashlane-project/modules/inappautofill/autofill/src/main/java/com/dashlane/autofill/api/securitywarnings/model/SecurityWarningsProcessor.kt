package com.dashlane.autofill.api.securitywarnings.model

import android.content.Context
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.api.securitywarnings.AutofillSecurityWarningsLogger
import com.dashlane.autofill.api.unlockfill.UnlockedAuthentifiant
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.core.helpers.SignatureVerification
import com.dashlane.hermes.generated.definitions.Domain
import dagger.hilt.android.qualifiers.ApplicationContext



internal class SecurityWarningsProcessor(
    private val securityWarningsView: SecurityWarningsView,
    @ApplicationContext
    private val context: Context,
    private val authentifiantResult: AutofillAnalyzerDef.IAutofillSecurityApplication,
    private val warningsLogger: AutofillSecurityWarningsLogger,
    private val rememberSecurityWarningsService: RememberSecurityWarningsService
) {

    private var securityWarning: Pair<UnlockedAuthentifiant, SignatureVerification>? = null

    fun dealWithSecurityBeforeFill(unlockedAuthentifiant: UnlockedAuthentifiant) {
        val signatureVerification: SignatureVerification = getSignatureVerification(unlockedAuthentifiant)

        securityWarning = Pair(unlockedAuthentifiant, signatureVerification)

        if (isVerifiedOrRemembered(signatureVerification, unlockedAuthentifiant)) {
            securityWarningsView.finishWithResult(
                unlockedAuthentifiant,
                showWarningRemembered = false,
                warningShown = false
            )
        } else {
            showSecurityWarning(signatureVerification, unlockedAuthentifiant)
        }
    }

    private fun getSignatureVerification(unlockedAuthentifiant: UnlockedAuthentifiant) =
        authentifiantResult.getSignatureVerification(
            context,
            unlockedAuthentifiant.packageName,
            unlockedAuthentifiant.authentifiantSummary
        )

    private fun isVerifiedOrRemembered(
        signatureVerification: SignatureVerification,
        unlockedAuthentifiant: UnlockedAuthentifiant
    ) = signatureVerification.isVerified() ||
            rememberSecurityWarningsService.isItemSourceRemembered(
                unlockedAuthentifiant,
                signatureVerification
            )

    private fun showSecurityWarning(
        signatureVerification: SignatureVerification,
        unlockedAuthentifiant: UnlockedAuthentifiant
    ) {
        when (signatureVerification) {
            is SignatureVerification.Incorrect -> showIncorrect(
                unlockedAuthentifiant,
                signatureVerification
            )
            is SignatureVerification.MismatchUnknown ->
                showMismatch(unlockedAuthentifiant, signatureVerification)
            is SignatureVerification.WithSignatureUnknown ->
                showUnknownOrMismatch(unlockedAuthentifiant, signatureVerification)
            else -> showUnknown(unlockedAuthentifiant, signatureVerification)
        }
    }

    private fun showIncorrect(
        unlockedAuthentifiant: UnlockedAuthentifiant,
        signatureVerification: SignatureVerification.Incorrect
    ) {
        warningsLogger.onDisplayedIncorrectWarning(
            signatureVerification,
            unlockedAuthentifiant
        )
        securityWarningsView.showIncorrectWarning(unlockedAuthentifiant)
    }

    private fun showUnknownOrMismatch(
        unlockedAuthentifiant: UnlockedAuthentifiant,
        signatureVerification: SignatureVerification.WithSignatureUnknown
    ) {
        if (signatureVerification.isKnownUnknownSource(unlockedAuthentifiant)) {
            showMismatch(unlockedAuthentifiant, signatureVerification)
        } else {
            showUnknown(unlockedAuthentifiant, signatureVerification)
        }
    }

    private fun showMismatch(
        unlockedAuthentifiant: UnlockedAuthentifiant,
        signatureVerification: SignatureVerification.UnknownWithSignature
    ) {
        if (securityWarningsView.autoAcceptMismatch()) {
            rememberMismatchWarning(unlockedAuthentifiant, signatureVerification)
            securityWarningsView.finishWithResult(
                unlockedAuthentifiant,
                showWarningRemembered = false,
                warningShown = false
            )
        } else {
            warningsLogger.onDisplayedMismatchWarning(
                signatureVerification,
                unlockedAuthentifiant
            )
            securityWarningsView.showMismatchWarning(unlockedAuthentifiant)
        }
    }

    private fun showUnknown(
        unlockedAuthentifiant: UnlockedAuthentifiant,
        signatureVerification: SignatureVerification
    ) {
        if (securityWarningsView.autoAcceptUnknown()) {
            rememberUnknownWarning(unlockedAuthentifiant, signatureVerification)
            securityWarningsView.finishWithResult(
                unlockedAuthentifiant,
                showWarningRemembered = false,
                warningShown = false
            )
        } else {
            warningsLogger.onDisplayedUnknownWarning(
                signatureVerification,
                unlockedAuthentifiant
            )
            securityWarningsView.showUnknownWarning(unlockedAuthentifiant)
        }
    }

    private fun SignatureVerification.WithSignatureUnknown.isKnownUnknownSource(
        unlockedAuthentifiant: UnlockedAuthentifiant
    ): Boolean =
        rememberSecurityWarningsService.isSourceRemembered(unlockedAuthentifiant, this)

    fun incorrectWarningPositiveClick(doNotShowAgain: Boolean, domain: Domain) {
        val unlockedAuthentifiant = securityWarning?.first ?: return
        val signatureVerification =
            securityWarning?.second as? SignatureVerification.Incorrect ?: return

        warningsLogger.onApprovedIncorrectWarning(
            signatureVerification,
            unlockedAuthentifiant,
            doNotShowAgain,
            domain
        )

        val securityWarningRemembered = doNotShowAgain && rememberIncorrectWarning(
            unlockedAuthentifiant,
            signatureVerification
        )
        securityWarningsView.finishWithResult(unlockedAuthentifiant, securityWarningRemembered, true)
    }

    private fun rememberIncorrectWarning(
        unlockedAuthentifiant: UnlockedAuthentifiant,
        signatureVerification: SignatureVerification.Incorrect
    ): Boolean {
        if (!isPackageNameMatch(unlockedAuthentifiant.formSource, signatureVerification)) {
            return false
        }
        return rememberSecurityWarningsService.remember(
            unlockedAuthentifiant,
            signatureVerification
        )
    }

    fun incorrectWarningNegativeClick(domain: Domain) {
        val unlockedAuthentifiant = securityWarning?.first ?: return
        val signatureVerification =
            securityWarning?.second as? SignatureVerification.Incorrect ?: return

        warningsLogger.onDeclinedIncorrectWarning(
            signatureVerification,
            unlockedAuthentifiant,
            domain
        )
        securityWarningsView.finish()
    }

    fun incorrectWarningCancelClick(domain: Domain) {
        val unlockedAuthentifiant = securityWarning?.first ?: return
        val signatureVerification =
            securityWarning?.second as? SignatureVerification.UnknownWithSignature ?: return

        logDismiss(signatureVerification, unlockedAuthentifiant, domain)
        securityWarningsView.finish()
    }

    fun mismatchWarningPositiveClick(doNotShowAgain: Boolean, domain: Domain) {
        val unlockedAuthentifiant = securityWarning?.first ?: return
        val signatureVerification =
            securityWarning?.second as? SignatureVerification.UnknownWithSignature ?: return

        logDismiss(signatureVerification, unlockedAuthentifiant, domain, doNotShowAgain)

        val securityWarningRemembered = doNotShowAgain && rememberMismatchWarning(
            unlockedAuthentifiant,
            signatureVerification
        )
        securityWarningsView.finishWithResult(unlockedAuthentifiant, securityWarningRemembered, true)
    }

    private fun logDismiss(
        signatureVerification: SignatureVerification.UnknownWithSignature,
        unlockedAuthentifiant: UnlockedAuthentifiant,
        domain: Domain,
        doNotShowAgain: Boolean = false
    ) {
        warningsLogger.onApprovedMismatchWarning(
            signatureVerification,
            unlockedAuthentifiant,
            doNotShowAgain,
            domain
        )
    }

    private fun rememberMismatchWarning(
        unlockedAuthentifiant: UnlockedAuthentifiant,
        signatureVerification: SignatureVerification.UnknownWithSignature
    ): Boolean {
        if (!isPackageNameMatch(unlockedAuthentifiant.formSource, signatureVerification)) {
            return false
        }
        return rememberSecurityWarningsService.remember(
            unlockedAuthentifiant,
            signatureVerification
        )
    }

    fun mismatchWarningCancelClick(domain: Domain) {
        logDecline(domain)
        securityWarningsView.finish()
    }

    private fun logDecline(domain: Domain) {
        val unlockedAuthentifiant = securityWarning?.first ?: return
        val signatureVerification =
            securityWarning?.second as? SignatureVerification.UnknownWithSignature ?: return

        warningsLogger.onDeclinedMismatchWarning(
            signatureVerification,
            unlockedAuthentifiant,
            domain
        )
        return
    }

    fun unknownWarningPositiveClick(domain: Domain) {
        val unlockedAuthentifiant = securityWarning?.first ?: return
        val signatureVerification =
            securityWarning?.second as? SignatureVerification.Unknown ?: return

        warningsLogger.onApprovedUnknownWarning(
            signatureVerification,
            unlockedAuthentifiant,
            true,
            domain
        )
        rememberUnknownWarning(unlockedAuthentifiant, signatureVerification)
        securityWarningsView.finishWithResult(unlockedAuthentifiant, showWarningRemembered = false, warningShown = true)
    }

    private fun rememberUnknownWarning(
        unlockedAuthentifiant: UnlockedAuthentifiant,
        signatureVerification: SignatureVerification
    ): Boolean {
        if (!isPackageNameMatch(unlockedAuthentifiant.formSource, signatureVerification)) {
            return false
        }
        if (signatureVerification !is SignatureVerification.WithSignatureUnknown) {
            return false
        }

        return rememberSecurityWarningsService.remember(
            unlockedAuthentifiant,
            signatureVerification
        )
    }

    fun onCloseSecurityWarningWithDeny(domain: Domain) {
        logDeclineWarning(domain)
        securityWarningsView.finish()
    }

    private fun logDeclineWarning(domain: Domain) {
        val unlockedAuthentifiant = securityWarning?.first ?: return
        val signatureVerification =
            securityWarning?.second as? SignatureVerification.Unknown ?: return

        warningsLogger.onDeclinedUnknownWarning(
            signatureVerification,
            unlockedAuthentifiant,
            domain
        )
        return
    }

    private fun isPackageNameMatch(
        formSource: AutoFillFormSource,
        signatureVerification: SignatureVerification
    ): Boolean {
        return when (signatureVerification) {
            is SignatureVerification.VaultLinkedAppsIncorrect ->
                formSource.getPackageName() == signatureVerification.signatureInMeta.packageName
            is SignatureVerification.KnownApplicationIncorrect ->
                formSource.getPackageName() == signatureVerification.knownApplication.packageName
            is SignatureVerification.MismatchUnknown ->
                formSource.getPackageName() == signatureVerification.knownApplication.packageName
            is SignatureVerification.WithSignatureUnknown ->
                formSource.getPackageName() == signatureVerification.signatureInstalled.packageName
            else -> false
        }
    }

    private fun AutoFillFormSource.getPackageName(): String {
        return when (this) {
            is ApplicationFormSource -> this.packageName
            is WebDomainFormSource -> this.packageName
        }
    }
}

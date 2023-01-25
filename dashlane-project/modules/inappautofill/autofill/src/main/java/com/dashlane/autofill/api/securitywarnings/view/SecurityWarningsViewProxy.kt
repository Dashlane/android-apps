package com.dashlane.autofill.api.securitywarnings.view

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.internal.AutofillFormSourcesStrings
import com.dashlane.autofill.api.securitywarnings.AutofillSecurityWarningsLogger
import com.dashlane.autofill.api.securitywarnings.model.RememberSecurityWarningsService
import com.dashlane.autofill.api.securitywarnings.model.SecurityWarningsProcessor
import com.dashlane.autofill.api.securitywarnings.model.SecurityWarningsView
import com.dashlane.autofill.api.ui.AutoFillResponseActivity
import com.dashlane.autofill.api.ui.AutofillFeature
import com.dashlane.autofill.api.unlockfill.UnlockedAuthentifiant
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.DomainType
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.Toaster
import dagger.hilt.android.qualifiers.ApplicationContext



internal open class SecurityWarningsViewProxy(
    private val autoFillResponseActivity: AutoFillResponseActivity,
    @ApplicationContext
    private val applicationContext: Context,
    private val authentifiantResult: AutofillAnalyzerDef.IAutofillSecurityApplication,
    private val securityWarningsLogger: AutofillSecurityWarningsLogger,
    private val rememberSecurityWarningsService: RememberSecurityWarningsService,
    private val autofillFormSourcesStrings: AutofillFormSourcesStrings,
    protected val toaster: Toaster,
    private val matchType: MatchType
) : SecurityWarningsView,
    BottomSheetMismatchSecurityWarningDialogFragment.Actions,
    BottomSheetUnknownSecurityWarningDialogFragment.Actions,
    IncorrectSecurityWarningDialogFragment.Actions {

    private val securityWarningsProcessor: SecurityWarningsProcessor by lazy(LazyThreadSafetyMode.NONE) {
        SecurityWarningsProcessor(
            this,
            applicationContext,
            authentifiantResult,
            securityWarningsLogger,
            rememberSecurityWarningsService
        )
    }

    fun dealWithSecurityBeforeFill(unlockedAuthentifiant: UnlockedAuthentifiant) {
        securityWarningsProcessor.dealWithSecurityBeforeFill(unlockedAuthentifiant)
    }

    override fun finish() = autoFillResponseActivity.finish()

    override fun autoAcceptUnknown(): Boolean = false

    override fun autoAcceptMismatch(): Boolean = false

    override fun finishWithResult(
        unlockedAuthentifiant: UnlockedAuthentifiant,
        showWarningRemembered: Boolean,
        warningShown: Boolean
    ) {
        if (showWarningRemembered) {
            toaster.show(R.string.autofill_warning_we_will_remember, Toast.LENGTH_SHORT)
        }
        autoFillResponseActivity.finishWithResult(
            itemToFill = unlockedAuthentifiant.itemToFill,
            autofillFeature = AutofillFeature.SUGGESTION,
            matchType = matchType
        )
    }

    override fun showIncorrectWarning(unlockedAuthentifiant: UnlockedAuthentifiant) {
        showDialog(IncorrectSecurityWarningDialogFragment.create(unlockedAuthentifiant.formSource))
    }

    override fun showMismatchWarning(unlockedAuthentifiant: UnlockedAuthentifiant) {
        val applicationFormSource = unlockedAuthentifiant.formSource
            .takeIf { it is ApplicationFormSource } as? ApplicationFormSource
        val applicationName = applicationFormSource?.let {
            autofillFormSourcesStrings.getApplicationString(it)
        } ?: ""
        val authentifiantLabel = unlockedAuthentifiant.authentifiantSummary.title ?: ""

        showDialog(
            BottomSheetMismatchSecurityWarningDialogFragment.create(
                authentifiantLabel,
                applicationName,
                applicationFormSource
            )
        )
    }

    override fun showUnknownWarning(unlockedAuthentifiant: UnlockedAuthentifiant) {
        showDialog(
            BottomSheetUnknownSecurityWarningDialogFragment.create(unlockedAuthentifiant.formSource)
        )
    }

    private fun showDialog(dialogFragment: DialogFragment) {
        val supportFragmentManager = autoFillResponseActivity.supportFragmentManager
        var dialog =
            supportFragmentManager.findFragmentByTag(AutoFillResponseActivity.SECURITY_WARNING_DIALOG_TAG)
        if (dialog == null) {
            dialog = dialogFragment
            dialog.show(
                supportFragmentManager,
                AutoFillResponseActivity.SECURITY_WARNING_DIALOG_TAG
            )
        }
    }

    override fun mismatchDialogPositiveAction(doNotShowAgainChecked: Boolean, domain: Domain) =
        securityWarningsProcessor.mismatchWarningPositiveClick(doNotShowAgainChecked, domain)

    override fun mismatchDialogNegativeAction(domain: Domain) =
        securityWarningsProcessor.onCloseSecurityWarningWithDeny(domain)

    override fun mismatchDialogCancelAction(domain: Domain) =
        securityWarningsProcessor.mismatchWarningCancelClick(domain)

    override fun unknownDialogPositiveAction(domain: Domain) =
        securityWarningsProcessor.unknownWarningPositiveClick(domain)

    override fun unknownDialogNegativeAction(domain: Domain) =
        securityWarningsProcessor.onCloseSecurityWarningWithDeny(domain)

    override fun unknownDialogCancelAction(domain: Domain) =
        securityWarningsProcessor.onCloseSecurityWarningWithDeny(domain)

    override fun incorrectDialogPositiveAction(doNotShowAgainChecked: Boolean, domain: Domain) =
        securityWarningsProcessor.incorrectWarningPositiveClick(doNotShowAgainChecked, domain)

    override fun incorrectDialogNegativeAction(domain: Domain) =
        securityWarningsProcessor.incorrectWarningNegativeClick(domain)

    override fun incorrectDialogCancelAction(domain: Domain) =
        securityWarningsProcessor.incorrectWarningCancelClick(domain)
}

internal fun AutoFillFormSource?.getDomain(): Domain = when (this) {
    is ApplicationFormSource -> Domain(Sha256Hash.of(packageName), DomainType.APP)
    is WebDomainFormSource -> Domain(
        id = webDomain.toUrlDomainOrNull()?.root?.value?.let {
            Sha256Hash.of(it)
        },
        type = DomainType.WEB
    )
    else -> Domain(type = DomainType.APP)
}
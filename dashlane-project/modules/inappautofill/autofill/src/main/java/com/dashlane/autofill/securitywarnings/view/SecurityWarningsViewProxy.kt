package com.dashlane.autofill.securitywarnings.view

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.api.R
import com.dashlane.autofill.internal.AutofillFormSourcesStrings
import com.dashlane.autofill.securitywarnings.AutofillSecurityWarningsLogger
import com.dashlane.autofill.securitywarnings.data.SecurityWarningAction
import com.dashlane.autofill.securitywarnings.data.SecurityWarningType
import com.dashlane.autofill.securitywarnings.data.getDomain
import com.dashlane.autofill.securitywarnings.model.RememberSecurityWarningsService
import com.dashlane.autofill.securitywarnings.model.SecurityWarningsProcessor
import com.dashlane.autofill.securitywarnings.model.SecurityWarningsView
import com.dashlane.autofill.ui.AutoFillResponseActivity
import com.dashlane.autofill.ui.AutofillFeature
import com.dashlane.autofill.unlockfill.UnlockedAuthentifiant
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.util.Toaster
import com.dashlane.util.getParcelableCompat
import com.dashlane.util.getSerializableCompat
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
) : SecurityWarningsView {

    private val securityWarningsProcessor: SecurityWarningsProcessor by lazy(LazyThreadSafetyMode.NONE) {
        SecurityWarningsProcessor(
            this,
            applicationContext,
            authentifiantResult,
            securityWarningsLogger,
            rememberSecurityWarningsService
        )
    }

    init {
        autoFillResponseActivity.supportFragmentManager.setFragmentResultListener(
            SECURITY_WARNING_ACTION_RESULT,
            autoFillResponseActivity
        ) { _, bundle ->
            val securityWarningType = bundle.getParcelableCompat<SecurityWarningType>(PARAMS_WARNING_TYPE)!!
            val action = bundle.getSerializableCompat<SecurityWarningAction>(PARAMS_ACTION)!!
            when (securityWarningType) {
                is SecurityWarningType.Incorrect -> incorrectAction(securityWarningType, action)
                is SecurityWarningType.Mismatch -> mismatchAction(securityWarningType, action)
                is SecurityWarningType.Unknown -> unknownAction(securityWarningType, action)
            }
        }
    }

    private fun incorrectAction(securityWarningType: SecurityWarningType, action: SecurityWarningAction) {
        when (action) {
            SecurityWarningAction.POSITIVE -> securityWarningsProcessor.incorrectWarningPositiveClick(
                securityWarningType.doNotShowAgainChecked,
                securityWarningType.formSource.getDomain()
            )
            SecurityWarningAction.NEGATIVE -> securityWarningsProcessor.incorrectWarningNegativeClick(
                securityWarningType.formSource.getDomain()
            )
            SecurityWarningAction.CANCEL -> securityWarningsProcessor.incorrectWarningCancelClick(
                securityWarningType.formSource.getDomain()
            )
        }
    }

    private fun mismatchAction(securityWarningType: SecurityWarningType, action: SecurityWarningAction) {
        when (action) {
            SecurityWarningAction.POSITIVE -> securityWarningsProcessor.mismatchWarningPositiveClick(
                securityWarningType.doNotShowAgainChecked,
                securityWarningType.formSource.getDomain()
            )
            SecurityWarningAction.NEGATIVE -> securityWarningsProcessor.onCloseSecurityWarningWithDeny(
                securityWarningType.formSource.getDomain()
            )
            SecurityWarningAction.CANCEL -> securityWarningsProcessor.mismatchWarningCancelClick(
                securityWarningType.formSource.getDomain()
            )
        }
    }

    private fun unknownAction(securityWarningType: SecurityWarningType, action: SecurityWarningAction) {
        when (action) {
            SecurityWarningAction.POSITIVE -> securityWarningsProcessor.unknownWarningPositiveClick(
                securityWarningType.formSource.getDomain()
            )
            SecurityWarningAction.NEGATIVE, SecurityWarningAction.CANCEL -> securityWarningsProcessor.onCloseSecurityWarningWithDeny(
                securityWarningType.formSource.getDomain()
            )
        }
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

    companion object {
        const val SECURITY_WARNING_ACTION_RESULT = "security_warning_action_result"
        const val PARAMS_ACTION = "params_action"
        const val PARAMS_WARNING_TYPE = "params_warning_type"
    }
}
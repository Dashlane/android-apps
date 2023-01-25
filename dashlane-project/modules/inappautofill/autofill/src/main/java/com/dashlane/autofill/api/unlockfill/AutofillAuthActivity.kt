package com.dashlane.autofill.api.unlockfill

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.dashlane.autofill.api.changepassword.view.AskChangePasswordViewProxy
import com.dashlane.autofill.api.changepassword.view.AutofillChangePasswordActivity
import com.dashlane.autofill.api.request.autofill.logger.getAutofillApiOrigin
import com.dashlane.autofill.api.securitywarnings.view.BottomSheetMismatchSecurityWarningDialogFragment
import com.dashlane.autofill.api.securitywarnings.view.BottomSheetUnknownSecurityWarningDialogFragment
import com.dashlane.autofill.api.securitywarnings.view.IncorrectSecurityWarningDialogFragment
import com.dashlane.autofill.api.securitywarnings.view.SecurityWarningsViewProxy
import com.dashlane.autofill.api.ui.AutoFillResponseActivity
import com.dashlane.autofill.api.ui.AutofillFeature
import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.followupnotification.FollowUpNotificationComponent
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.util.desktopId
import com.dashlane.xml.domain.SyncObjectType



class AutofillAuthActivity :
    BottomSheetUnknownSecurityWarningDialogFragment.Actions,
    BottomSheetMismatchSecurityWarningDialogFragment.Actions,
    IncorrectSecurityWarningDialogFragment.Actions,
    AutoFillResponseActivity() {

    private val supportedAutofillsDetector: SupportedAutofillsDetector =
        SupportedAutofillsDetector()

    private val authentifiantUnlocker: AuthentifiantUnlocker by lazy(LazyThreadSafetyMode.NONE) {
        AuthentifiantUnlocker(
            AutofillAuthActivityUnlockAuthentifiantView(this),
            componentInternal.itemLoader,
            lockManager,
            autofillUsageLog,
            forKeyboardAutofill
        )
    }

    private val securityWarningsViewProxy: SecurityWarningsViewProxy by lazy(LazyThreadSafetyMode.NONE) {
        SecurityWarningsViewProxy(
            this,
            applicationContext,
            component.authentifiantResult,
            component.securityWarningsLogger,
            component.rememberSecurityWarningsService,
            component.autofillFormSourcesStrings,
            component.toaster,
            matchType
        )
    }

    private val changePasswordActivityResultLauncher =
        registerForActivityResult(
            object : ActivityResultContract<Pair<AutoFillHintSummary, Boolean>, Pair<Int, Intent?>>() {
                override fun createIntent(
                    context: Context,
                    input: Pair<AutoFillHintSummary, Boolean>
                ): Intent =
                    AutofillChangePasswordActivity.buildIntent(context, input.first, input.second)

                override fun parseResult(resultCode: Int, intent: Intent?): Pair<Int, Intent?> =
                    resultCode to intent
            }
        ) { (resultCode, intent) ->
            onChangePasswordActivityResult(resultCode, intent)
        }

    private val askChangePasswordViewProxy: AskChangePasswordViewProxy by lazy(LazyThreadSafetyMode.NONE) {
        AskChangePasswordViewProxy(
            this,
            summary!!,
            forKeyboardAutofill,
            changePasswordActivityResultLauncher
        ) {
            securityWarningsViewProxy.dealWithSecurityBeforeFill(it)
        }
    }

    private var itemId: String? = null

    private var isLockScreenCalled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        itemId = intent.getStringExtra(EXTRA_ITEM_ID)
    }

    

    override fun onAutofillResponseCreated() {
        if (intent.hasExtra(EXTRA_ITEM_ID)) {
            autofillUsageLog.onClickToAutoFillSuggestion(
                getAutofillApiOrigin(forKeyboardAutofill),
                packageName,
                summary?.webDomain?.toUrlDomainOrNull(),
                supportedAutofillsDetector.detectResponse(getDesktopId(), getFormType())
            )
        }
    }

    override fun onResume() {
        super.onResume()

        if (summary == null) {
            finish()
            return
        }

        
        if (isLoggedIn && summary?.formType == AutoFillFormType.EMAIL_ONLY) {
            onUserLoggedIn()
        } else {
            performLoginAndUnlock {
                onUserLoggedIn()
            }
        }
    }

    private fun onChangePasswordActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) finish() else finishAndTransferResult(data!!)
    }

    private fun canRequestLockScreen() = isFirstRun && !isLockScreenCalled

    private fun onUserLoggedIn() {
        
        when (supportedAutofillsDetector.detectResponse(getDesktopId(), getFormType())) {
            null -> finish()
            SupportedAutofills.AUTHENTIFIANT -> respondAuthentifiant()
            SupportedAutofills.CREDIT_CARD -> respondCreditCard()
            SupportedAutofills.EMAIL -> respondEmail()
        }
    }

    private fun getDesktopId(): Int? = intent?.getIntExtra(EXTRA_ITEM_DATA_TYPE, -1)

    private fun getFormType(): Int? {
        return summary?.formType
    }

    private fun respondCreditCard() {
        val autofillSummary = summary!!
        if (lockManager.isInAppLoginLocked) {
            if (canRequestLockScreen()) {
                startLockActivity()
                autofillUsageLog.onClickToAutoFillCreditCardButLock(
                    getAutofillApiOrigin(forKeyboardAutofill)
                )
            } else {
                finish()
            }
            return
        } else if (isFirstRun) {
            autofillUsageLog.onClickToAutoFillCreditCardNotLock(
                getAutofillApiOrigin(forKeyboardAutofill),
                autofillSummary.packageName
            )
        }
        val itemToFill = componentInternal.itemLoader.loadCreditCard(itemId)

        
        if (itemToFill != null && isCreditCardWithMissingField(autofillSummary)) {
            FollowUpNotificationComponent(this)
                .followUpNotificationApiProvider
                .getFollowUpNotificationApi()
                .startFollowUpNotification(itemToFill.primaryItem.toSummary(), null)
        }

        finishWithResult(
            itemToFill = itemToFill,
            autofillFeature = AutofillFeature.SUGGESTION,
            matchType = matchType
        )
    }

    

    private fun isCreditCardWithMissingField(summary: AutoFillHintSummary): Boolean {
        return summary.formType == AutoFillFormType.CREDIT_CARD && !summary.entries.flatMap { it.autoFillHints.getHints() }
            .let {
                it.contains(AutoFillHint.CREDIT_CARD_NUMBER) && it.contains(AutoFillHint.CREDIT_CARD_SECURITY_CODE) &&
                        (it.contains(AutoFillHint.CREDIT_CARD_EXPIRATION_DATE) ||
                                it.contains(AutoFillHint.CREDIT_CARD_EXPIRATION_DAY) ||
                                it.contains(AutoFillHint.CREDIT_CARD_EXPIRATION_MONTH) ||
                                it.contains(AutoFillHint.CREDIT_CARD_EXPIRATION_YEAR))
            }
    }

    private fun startLockActivity() {
        lockManager.showLockActivityForAutofillApi(this)
        isLockScreenCalled = true
    }

    private fun respondEmail() {
        val itemToFill = componentInternal.itemLoader.loadEmail(itemId)
        finishWithResult(
            itemToFill = itemToFill,
            autofillFeature = AutofillFeature.SUGGESTION,
            matchType = matchType
        )
    }

    private fun respondAuthentifiant() {
        authentifiantUnlocker.unlockAuthentifiant(itemId, summary!!.formSource)
    }

    private fun authentifiantItemUnlocked(unlockedAuthentifiant: UnlockedAuthentifiant) {
        if (isChangePassword) {
            
            askChangePasswordViewProxy.showDialog(unlockedAuthentifiant)
        } else {
            securityWarningsViewProxy.dealWithSecurityBeforeFill(unlockedAuthentifiant)
        }
    }

    internal class AutofillAuthActivityUnlockAuthentifiantView(
        private val autofillAuthActivity: AutofillAuthActivity
    ) : UnlockAuthentifiantView {
        override fun isFirstRun(): Boolean = autofillAuthActivity.isFirstRun

        override fun canRequestLockScreen(): Boolean = autofillAuthActivity.canRequestLockScreen()

        override fun finishWithAutoFillSuggestions() =
            autofillAuthActivity.finishWithAutoFillSuggestions()

        override fun startLockActivity() = autofillAuthActivity.startLockActivity()

        override fun finish() = autofillAuthActivity.finish()

        override fun authentifiantItemUnlocked(unlockedAuthentifiant: UnlockedAuthentifiant) =
            autofillAuthActivity.authentifiantItemUnlocked(unlockedAuthentifiant)
    }

    override fun unknownDialogPositiveAction(domain: Domain) =
        securityWarningsViewProxy.unknownDialogPositiveAction(domain)

    override fun unknownDialogNegativeAction(domain: Domain) =
        securityWarningsViewProxy.unknownDialogNegativeAction(domain)

    override fun unknownDialogCancelAction(domain: Domain) =
        securityWarningsViewProxy.unknownDialogCancelAction(domain)

    override fun mismatchDialogPositiveAction(doNotShowAgainChecked: Boolean, domain: Domain) =
        securityWarningsViewProxy.mismatchDialogPositiveAction(doNotShowAgainChecked, domain)

    override fun mismatchDialogNegativeAction(domain: Domain) =
        securityWarningsViewProxy.mismatchDialogNegativeAction(domain)

    override fun mismatchDialogCancelAction(domain: Domain) =
        securityWarningsViewProxy.mismatchDialogCancelAction(domain)

    override fun incorrectDialogPositiveAction(doNotShowAgainChecked: Boolean, domain: Domain) =
        securityWarningsViewProxy.incorrectDialogPositiveAction(doNotShowAgainChecked, domain)

    override fun incorrectDialogNegativeAction(domain: Domain) =
        securityWarningsViewProxy.incorrectDialogNegativeAction(domain)

    override fun incorrectDialogCancelAction(domain: Domain) =
        securityWarningsViewProxy.incorrectDialogCancelAction(domain)

    companion object {

        private const val EXTRA_IS_TO_JUST_LOGIN = "extra_is_to_just_login"
        private const val EXTRA_ITEM_ID = "extra_item_id"
        private const val EXTRA_ITEM_DATA_TYPE = "extra_data_type"

        internal fun getAuthIntentSenderForLoggedOutDataset(
            context: Context,
            summary: AutoFillHintSummary,
            forKeyboard: Boolean
        ): IntentSender {
            val intent = createIntent(context, summary, AutofillAuthActivity::class)
            intent.putExtra(EXTRA_IS_TO_JUST_LOGIN, true)
            intent.putExtra(EXTRA_FOR_KEYBOARD_AUTOFILL, forKeyboard)

            return createIntentSender(context, intent)
        }

        internal fun getAuthIntentSenderForDataset(
            context: Context,
            itemId: String,
            dataType: SyncObjectType,
            summary: AutoFillHintSummary,
            forKeyboard: Boolean,
            isChangePassword: Boolean,
            matchType: MatchType?
        ): IntentSender {
            val intent = createIntent(context, summary, AutofillAuthActivity::class)
            intent.putExtra(EXTRA_IS_TO_JUST_LOGIN, false)
            intent.putExtra(EXTRA_ITEM_ID, itemId)
            intent.putExtra(EXTRA_ITEM_DATA_TYPE, dataType.desktopId)
            intent.putExtra(EXTRA_FOR_KEYBOARD_AUTOFILL, forKeyboard)
            intent.putExtra(EXTRA_IS_CHANGE_PASSWORD, isChangePassword)
            intent.putExtra(EXTRA_MATCH_TYPE, matchType?.code)

            return createIntentSender(context, intent)
        }
    }
}
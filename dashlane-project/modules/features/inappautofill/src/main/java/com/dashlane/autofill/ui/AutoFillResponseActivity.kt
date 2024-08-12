package com.dashlane.autofill.ui

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Parcelable
import android.view.autofill.AutofillManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.dagger.AutofillApiInternalEntryPoint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.internal.AutofillApiEntryPoint
import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.autofill.model.CreditCardItemToFill
import com.dashlane.autofill.model.EmailItemToFill
import com.dashlane.autofill.model.ItemToFill
import com.dashlane.autofill.phishing.PhishingAttemptLevel
import com.dashlane.autofill.phishing.PhishingWarningDataProvider
import com.dashlane.autofill.request.autofill.logger.getAutofillApiOrigin
import com.dashlane.autofill.request.autofill.logger.getHermesAutofillApiOrigin
import com.dashlane.autofill.viewallaccounts.AutofillViewAllAccountsLogger
import com.dashlane.crashreport.CrashReporter
import com.dashlane.hermes.generated.definitions.AutofillMechanism
import com.dashlane.hermes.generated.definitions.AutofillOrigin
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.session.SessionManager
import com.dashlane.url.UrlDomain
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.CurrentPageViewLogger
import com.dashlane.util.getParcelableCompat
import com.dashlane.util.getSerializableExtraCompat
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlin.reflect.KClass

@AndroidEntryPoint
abstract class AutoFillResponseActivity : AppCompatActivity(), CurrentPageViewLogger.Owner {

    protected val component: AutofillApiEntryPoint by lazy(LazyThreadSafetyMode.NONE) {
        AutofillApiEntryPoint(this)
    }

    internal val componentInternal: AutofillApiInternalEntryPoint by lazy(LazyThreadSafetyMode.NONE) {
        AutofillApiInternalEntryPoint(this)
    }

    @Inject
    lateinit var phishingWarningDataProvider: PhishingWarningDataProvider

    protected val autofillUsageLog: AutofillAnalyzerDef.IAutofillUsageLog
        get() = component.autoFillApiUsageLog

    protected val viewAllAccountsLogger: AutofillViewAllAccountsLogger
        get() = component.viewAllAccountsLogger

    protected val crashReporter: CrashReporter
        get() = component.crashReporter

    protected val sessionManager: SessionManager
        get() = component.sessionManager

    protected val lockManager: AutofillAnalyzerDef.ILockManager
        get() = component.lockManager

    protected val isLoggedIn: Boolean
        get() = sessionManager.session != null

    private val autofillPerformedCallback: AutofillPerformedCallback
        get() = component.autofillPerformedCallback

    private var replyIntent: Intent? = null

    internal var summary: AutoFillHintSummary? = null

    internal val forKeyboardAutofill: Boolean
        get() = intent?.getBooleanExtra(EXTRA_FOR_KEYBOARD_AUTOFILL, false) ?: false

    internal val isGuidedChangePassword: Boolean
        get() = intent?.getBooleanExtra(EXTRA_IS_GUIDED_CHANGE_PASSWORD, false) ?: false

    internal val phishingAttemptLevel: PhishingAttemptLevel
        get() = intent?.getSerializableExtraCompat<PhishingAttemptLevel>(EXTRA_PHISHING_ATTEMPT_LEVEL)
            ?: PhishingAttemptLevel.NONE
    internal var phishingAttemptTrustedByUser = false

    internal var matchType: MatchType = MatchType.REGULAR

    internal var isFirstRun: Boolean = false
        private set

    private var isHandlingConfigurationChange: Boolean = false

    override val currentPageViewLogger by lazy { CurrentPageViewLogger(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
        val authenticationKey = intent.getStringExtra(EXTRA_AUTHENTICATION_KEY)
        if (authenticationKey == null || !latestAuthenticationKey.contains(authenticationKey)) {
            latestAuthenticationKey.clear() 
            finish()
            return
        }

        isFirstRun = savedInstanceState == null
        isHandlingConfigurationChange = savedInstanceState?.getBoolean(EXTRA_IS_HANDLING_CONFIGURATION_CHANGE) ?: false
        summary = intent.getBundleExtra(EXTRA_SUB_BUNDLE)?.getParcelableCompat(EXTRA_AUTOFILL_HINT_SUMMARY)
        matchType = intent.getStringExtra(EXTRA_MATCH_TYPE)?.let { matchType ->
            MatchType.values().firstOrNull { it.code == matchType }
        } ?: MatchType.REGULAR
    }

    internal fun performLoginAndUnlock(onUnlocked: () -> Unit) {
        val isLocked = lockManager.isInAppLoginLocked
        when {
            isLoggedIn && !isLocked -> onUnlocked()
            isLoggedIn && isLocked && isFirstRun -> {
                startLockActivity()
                isFirstRun = false
            }
            !isLoggedIn && isFirstRun -> {
                startNotLoggedInActivity()
                isFirstRun = false
            }
            
            isHandlingConfigurationChange -> {
                isHandlingConfigurationChange = false
                return
            }
            else -> finish()
        }
    }

    private fun startLockActivity() {
        lockManager.showLockActivityForAutofillApi(this)
    }

    private fun startNotLoggedInActivity() {
        lockManager.logoutAndCallLoginScreenForInAppLogin(this)
        isFirstRun = false
    }

    override fun finish() {
        if (replyIntent == null) {
            setResult(Activity.RESULT_CANCELED)
        } else {
            setResult(Activity.RESULT_OK, replyIntent)
        }
        super.finish()
    }

    internal fun finishWithAutoFillSuggestions() {
        val summary = summary
        if (summary == null) {
            finish()
            return
        }
        val fillRequestHandler = componentInternal.fillRequestHandler

        FillSuggestionToActivityResult(fillRequestHandler, lifecycleScope).finishWithRequestResult(this, summary)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EXTRA_IS_HANDLING_CONFIGURATION_CHANGE, isChangingConfigurations)
        super.onSaveInstanceState(outState)
    }

    internal fun finishWithResult(itemToFill: ItemToFill?, matchType: MatchType) {
        if (itemToFill == null) {
            finishWithAutoFillSuggestions()
            return
        }
        val summary = summary
        if (summary == null) {
            finish()
            return
        }

        val packageName = summary.packageName
        val website = summary.webDomain?.toUrlDomainOrNull()
        val origin = getAutofillApiOrigin(forKeyboardAutofill)
        val autofillOrigin = getHermesAutofillApiOrigin(forKeyboardAutofill)

        sendAutofillLog(
            itemToFill = itemToFill,
            origin = origin,
            packageName = packageName,
            website = website,
            matchType = matchType,
            autofillOrigin = autofillOrigin
        )

        val dataset = componentInternal.dataSetCreator.create(
            summary,
            itemToFill,
            false,
            isGuidedChangePassword,
            null,
            PhishingAttemptLevel.NONE
        )
        updateItemLastViewDate(itemToFill.itemId)
        autofillPerformedCallback.onAutofillPerformed(itemToFill, summary)
        finishWithResultIntentResult(dataset?.build()?.toAndroidDataset())
    }

    fun shouldDisplayPhishingWarning(syncObject: SyncObject.Authentifiant?): Boolean =
        phishingWarningDataProvider.shouldDisplayPhishingWarning(phishingAttemptLevel, summary, syncObject) &&
            !phishingAttemptTrustedByUser

    @OptIn(DelicateCoroutinesApi::class)
    internal fun updateItemLastViewDate(itemId: String?) {
        itemId ?: return
        GlobalScope.launch(Dispatchers.IO) {
            component.databaseAccess.updateLastViewDate(itemId = itemId, instant = Instant.now())
        }
    }

    private fun sendAutofillLog(
        itemToFill: ItemToFill,
        origin: Int,
        packageName: String,
        website: UrlDomain?,
        matchType: MatchType,
        autofillOrigin: AutofillOrigin
    ) {
        when (itemToFill) {
            is AuthentifiantItemToFill -> autofillUsageLog.onAutoFillCredentialDone(
                origin = origin,
                packageName = packageName,
                websiteUrlDomain = website,
                itemUrlDomain = itemToFill.url.toUrlDomainOrNull(),
                matchType = matchType,
                autofillOrigin = autofillOrigin,
                autofillMechanism = AutofillMechanism.ANDROID_AUTOFILL_API,
                credentialId = itemToFill.itemId
            )
            is CreditCardItemToFill -> autofillUsageLog.onAutoFillCreditCardDone(
                origin = origin,
                packageName = packageName,
                websiteUrlDomain = website,
                matchType = matchType,
                autofillOrigin = autofillOrigin,
                autofillMechanism = AutofillMechanism.ANDROID_AUTOFILL_API,
                credentialId = itemToFill.itemId
            )
            is EmailItemToFill -> autofillUsageLog.onAutoFillEmailDone(
                origin = origin,
                packageName = packageName,
                websiteUrlDomain = website,
                matchType = matchType,
                autofillOrigin = autofillOrigin,
                autofillMechanism = AutofillMechanism.ANDROID_AUTOFILL_API,
                credentialId = itemToFill.itemId
            )
            else -> Unit
        }
    }

    internal fun finishWithResultIntentResult(response: Parcelable?) {
        if (response != null) {
            replyIntent = Intent()
            replyIntent!!.putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, response)
        }
        finish()
    }

    internal fun finishAndTransferResult(data: Intent) {
        replyIntent = data
        finish()
    }

    companion object {
        const val SECURITY_WARNING_DIALOG_TAG = "security_warning_dialog"
        private const val EXTRA_SUB_BUNDLE = "extra_sub_bundle"
        private const val EXTRA_AUTOFILL_HINT_SUMMARY = "extra_autofill_hint_summary"
        private const val EXTRA_AUTHENTICATION_KEY = "extra_authentication_key"
        const val EXTRA_FOR_KEYBOARD_AUTOFILL = "for_keyboard_autofill"
        const val EXTRA_IS_GUIDED_CHANGE_PASSWORD = "is_guided_change_password"
        const val EXTRA_MATCH_TYPE = "extra_match_type"
        const val EXTRA_PHISHING_ATTEMPT_LEVEL = "extra_phishing_attempt_level"

        const val EXTRA_IS_HANDLING_CONFIGURATION_CHANGE = "is_handling_configuration_change"

        
        private var datasetPendingIntentId = 0

        private val latestAuthenticationKey = HashSet<String>()

        internal fun createIntent(
            context: Context,
            summary: AutoFillHintSummary,
            klass: KClass<out AutoFillResponseActivity>
        ): Intent {
            val summaryContainerBundle = Bundle()
            summaryContainerBundle.putParcelable(EXTRA_AUTOFILL_HINT_SUMMARY, summary)

            val intent = Intent(context, klass.java)
            
            
            intent.putExtra(EXTRA_SUB_BUNDLE, summaryContainerBundle)
            val authenticationKey = UUID.randomUUID().toString()
            latestAuthenticationKey.add(authenticationKey)
            intent.putExtra(EXTRA_AUTHENTICATION_KEY, authenticationKey)
            return intent
        }

        internal fun createIntentSender(context: Context, intent: Intent): IntentSender {
            return PendingIntent.getActivity(
                context,
                ++datasetPendingIntentId,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            ).intentSender
        }
    }
}
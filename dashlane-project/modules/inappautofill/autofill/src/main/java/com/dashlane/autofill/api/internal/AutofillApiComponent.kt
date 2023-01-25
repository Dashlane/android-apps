package com.dashlane.autofill.api.internal

import android.content.Context
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.announcement.KeyboardAutofillService
import com.dashlane.autofill.api.changepassword.AutoFillChangePasswordConfiguration
import com.dashlane.autofill.api.emptywebsitewarning.AutofillEmptyWebsiteWarningService
import com.dashlane.autofill.api.emptywebsitewarning.EmptyWebsiteWarningLogger
import com.dashlane.autofill.api.fillresponse.ChangePasswordActionIntentProvider
import com.dashlane.autofill.api.fillresponse.CreateAccountActionIntentProvider
import com.dashlane.autofill.api.fillresponse.EmptyWebsiteWarningIntentProvider
import com.dashlane.autofill.api.fillresponse.PauseActionIntentProvider
import com.dashlane.autofill.api.fillresponse.RememberedAccountsService
import com.dashlane.autofill.api.fillresponse.ViewAllAccountsActionIntentProvider
import com.dashlane.autofill.api.request.save.AutofillSaveRequestLogger
import com.dashlane.autofill.api.securitywarnings.AutofillSecurityWarningsLogger
import com.dashlane.autofill.api.securitywarnings.model.RememberSecurityWarningsService
import com.dashlane.autofill.api.ui.AutofillPerformedCallback
import com.dashlane.autofill.api.util.AutofillNavigationService
import com.dashlane.autofill.api.viewallaccounts.AutofillViewAllAccountsLogger
import com.dashlane.crashreport.CrashReporter
import com.dashlane.performancelogger.TimeToAutofillLogger
import com.dashlane.performancelogger.TimeToLoadLocalLogger
import com.dashlane.session.SessionManager
import com.dashlane.util.Toaster
import com.dashlane.util.userfeatures.UserFeaturesChecker



interface AutofillApiComponent {
    val authentifiantResult: AutofillAnalyzerDef.IAutofillSecurityApplication
    val autoFillchangePasswordConfiguration: AutoFillChangePasswordConfiguration
    val crashReporter: CrashReporter
    val databaseAccess: AutofillAnalyzerDef.DatabaseAccess
    val autoFillApiUsageLog: AutofillAnalyzerDef.IAutofillUsageLog
    val viewAllAccountsActionIntentProvider: ViewAllAccountsActionIntentProvider
    val pauseActionIntentProvider: PauseActionIntentProvider
    val autofillLimiter: AutofillLimiter
    val createAccountActionIntentProvider: CreateAccountActionIntentProvider
    val changePasswordActionIntentProvider: ChangePasswordActionIntentProvider
    val autofillPerformedCallback: AutofillPerformedCallback
    val rememberedAccountsService: RememberedAccountsService
    val autofillFormSourcesStrings: AutofillFormSourcesStrings
    val fetchAccounts: FetchAccounts
    val applicationFormSourceDeviceStatus: ApplicationFormSourceDeviceStatus
    val userPreferencesAccess: AutofillAnalyzerDef.IUserPreferencesAccess
    val emptyWebsiteWarningIntentProvider: EmptyWebsiteWarningIntentProvider
    val userFeaturesChecker: UserFeaturesChecker
    val toaster: Toaster
    val sessionManager: SessionManager
    val lockManager: AutofillAnalyzerDef.ILockManager
    val autofillSaveRequestLogger: AutofillSaveRequestLogger
    val navigationService: AutofillNavigationService
    val keyboardAutofillService: KeyboardAutofillService
    val timeToAutofillLogger: TimeToAutofillLogger
    val timeToLoadLocalLogger: TimeToLoadLocalLogger
    val emptyWebsiteWarningService: AutofillEmptyWebsiteWarningService
    val emptyWebsiteWarningLogger: EmptyWebsiteWarningLogger
    val rememberSecurityWarningsService: RememberSecurityWarningsService
    val securityWarningsLogger: AutofillSecurityWarningsLogger
    val viewAllAccountsLogger: AutofillViewAllAccountsLogger

    companion object {
        operator fun invoke(context: Context) =
            (context.applicationContext as AutofillApiApplication).component
    }
}

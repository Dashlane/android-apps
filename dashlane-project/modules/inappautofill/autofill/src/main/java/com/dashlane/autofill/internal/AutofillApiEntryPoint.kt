package com.dashlane.autofill.internal

import android.content.Context
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.announcement.KeyboardAutofillService
import com.dashlane.autofill.changepassword.AutoFillChangePasswordConfiguration
import com.dashlane.autofill.emptywebsitewarning.AutofillEmptyWebsiteWarningService
import com.dashlane.autofill.fillresponse.ChangePasswordActionIntentProvider
import com.dashlane.autofill.fillresponse.CreateAccountActionIntentProvider
import com.dashlane.autofill.fillresponse.EmptyWebsiteWarningIntentProvider
import com.dashlane.autofill.fillresponse.PauseActionIntentProvider
import com.dashlane.autofill.fillresponse.ViewAllAccountsActionIntentProvider
import com.dashlane.autofill.request.save.AutofillSaveRequestLogger
import com.dashlane.autofill.securitywarnings.AutofillSecurityWarningsLogger
import com.dashlane.autofill.securitywarnings.model.RememberSecurityWarningsService
import com.dashlane.autofill.ui.AutofillPerformedCallback
import com.dashlane.autofill.util.AutofillNavigationService
import com.dashlane.autofill.viewallaccounts.AutofillViewAllAccountsLogger
import com.dashlane.crashreport.CrashReporter
import com.dashlane.session.SessionManager
import com.dashlane.util.Toaster
import com.dashlane.util.userfeatures.UserFeaturesChecker
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AutofillApiEntryPoint {
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
    val autofillFormSourcesStrings: AutofillFormSourcesStrings
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
    val emptyWebsiteWarningService: AutofillEmptyWebsiteWarningService
    val rememberSecurityWarningsService: RememberSecurityWarningsService
    val securityWarningsLogger: AutofillSecurityWarningsLogger
    val viewAllAccountsLogger: AutofillViewAllAccountsLogger

    companion object {
        operator fun invoke(context: Context) = EntryPointAccessors.fromApplication(
            context,
            AutofillApiEntryPoint::class.java
        )
    }
}

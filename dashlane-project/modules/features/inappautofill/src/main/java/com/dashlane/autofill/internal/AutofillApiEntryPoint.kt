package com.dashlane.autofill.internal

import android.content.Context
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.logs.AutofillUsageLog
import com.dashlane.autofill.securitywarnings.AutofillSecurityWarningsLogger
import com.dashlane.autofill.securitywarnings.model.RememberSecurityWarningsService
import com.dashlane.autofill.ui.AutofillPerformedCallback
import com.dashlane.autofill.util.AutofillNavigationService
import com.dashlane.autofill.viewallaccounts.AutofillViewAllAccountsLogger
import com.dashlane.crashreport.CrashReporter
import com.dashlane.lock.LockHelper
import com.dashlane.session.SessionManager
import com.dashlane.util.Toaster
import com.dashlane.util.inject.OptionalProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AutofillApiEntryPoint {
    val authentifiantResult: AutofillAnalyzerDef.AutofillSecurityApplication
    val crashReporter: CrashReporter
    val databaseAccess: AutofillAnalyzerDef.DatabaseAccess
    val autoFillApiUsageLog: AutofillUsageLog
    val autofillPerformedCallback: AutofillPerformedCallback
    val autofillFormSourcesStrings: AutofillFormSourcesStrings
    val toaster: Toaster
    val sessionManager: SessionManager
    val lockHelper: LockHelper
    val navigationService: AutofillNavigationService
    val rememberSecurityWarningsService: RememberSecurityWarningsService
    val securityWarningsLogger: AutofillSecurityWarningsLogger
    val viewAllAccountsLogger: AutofillViewAllAccountsLogger
    val sessionCoroutineScopeProvider: OptionalProvider<CoroutineScope>

    companion object {
        operator fun invoke(context: Context) = EntryPointAccessors.fromApplication(
            context,
            AutofillApiEntryPoint::class.java,
        )
    }
}
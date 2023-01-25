package com.dashlane.autofill.api.dagger

import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.AutofillAnalyzerDef.DatabaseAccess
import com.dashlane.autofill.AutofillAnalyzerDef.IAutofillUsageLog
import com.dashlane.autofill.api.ApplicationFormSourceDeviceStatusFromContext
import com.dashlane.autofill.api.AutofillEmptyWebsiteWarningServiceImpl
import com.dashlane.autofill.api.AutofillFormSourcesStringsFromContext
import com.dashlane.autofill.api.FetchAccountsUsingLoader
import com.dashlane.autofill.api.changepassword.AutofillChangePasswordActivityIntentProvider
import com.dashlane.autofill.api.createaccount.AutofillCreateAccountActivityIntentProvider
import com.dashlane.autofill.api.emptywebsitewarning.AutofillEmptyWebsiteWarningService
import com.dashlane.autofill.api.emptywebsitewarning.EmptyWebsiteWarningActivityIntentProvider
import com.dashlane.autofill.api.emptywebsitewarning.EmptyWebsiteWarningLogger
import com.dashlane.autofill.api.fillresponse.ChangePasswordActionIntentProvider
import com.dashlane.autofill.api.fillresponse.CreateAccountActionIntentProvider
import com.dashlane.autofill.api.fillresponse.EmptyWebsiteWarningIntentProvider
import com.dashlane.autofill.api.fillresponse.PauseActionIntentProvider
import com.dashlane.autofill.api.fillresponse.RememberedAccountsService
import com.dashlane.autofill.api.fillresponse.ViewAllAccountsActionIntentProvider
import com.dashlane.autofill.api.internal.ApplicationFormSourceDeviceStatus
import com.dashlane.autofill.api.internal.AutofillFormSourcesStrings
import com.dashlane.autofill.api.internal.FetchAccounts
import com.dashlane.autofill.api.internal.AutofillLimiter
import com.dashlane.autofill.api.pause.AutofillPauseActivityIntentProvider
import com.dashlane.autofill.api.pause.services.PausedAutofillLimiter
import com.dashlane.autofill.api.rememberaccount.RememberedAccountsServiceUsingLoader
import com.dashlane.autofill.api.ui.AutofillPerformedCallback
import com.dashlane.autofill.api.ui.AutofillPerformedCallbackImpl
import com.dashlane.autofill.api.viewallaccounts.AutofillViewAllItemsActivityIntentProvider
import com.dashlane.autofill.core.AutoFillDataBaseAccess
import com.dashlane.autofill.core.AutofillEmptyWebsiteWarningLoggerImpl
import com.dashlane.autofill.core.AutofillUsageLog
import com.dashlane.autofill.core.domain.AutofillSecurityApplication
import dagger.Binds
import dagger.Module
import javax.inject.Singleton



@Module
interface AutofillApiComponentBindModule {

    @Singleton
    @Binds
    fun bindsAutofillUsageLog(impl: AutofillUsageLog): IAutofillUsageLog

    @Singleton
    @Binds
    fun bindsAutoFillDataBaseAccess(impl: AutoFillDataBaseAccess): DatabaseAccess

    @Singleton
    @Binds
    fun bindsAutofillSecurityApplication(impl: AutofillSecurityApplication): AutofillAnalyzerDef.IAutofillSecurityApplication

    @Singleton
    @Binds
    fun bindsViewAllAccountsActionIntentProvider(impl: AutofillViewAllItemsActivityIntentProvider): ViewAllAccountsActionIntentProvider

    @Singleton
    @Binds
    fun bindsPauseActionIntentProvider(impl: AutofillPauseActivityIntentProvider): PauseActionIntentProvider

    @Singleton
    @Binds
    fun bindsCreateAccountActionIntentProvider(impl: AutofillCreateAccountActivityIntentProvider): CreateAccountActionIntentProvider

    @Singleton
    @Binds
    fun bindsChangePasswordActionIntentProvider(impl: AutofillChangePasswordActivityIntentProvider): ChangePasswordActionIntentProvider

    @Singleton
    @Binds
    fun bindsEmptyWebsiteWarningIntentProvider(impl: EmptyWebsiteWarningActivityIntentProvider): EmptyWebsiteWarningIntentProvider

    @Singleton
    @Binds
    fun bindsAutofillLimiter(impl: PausedAutofillLimiter): AutofillLimiter

    @Singleton
    @Binds
    fun bindsAutofillPerformedCallback(impl: AutofillPerformedCallbackImpl): AutofillPerformedCallback

    @Singleton
    @Binds
    fun bindsFetchAccounts(impl: FetchAccountsUsingLoader): FetchAccounts

    @Singleton
    @Binds
    fun bindsRememberedAccountsService(impl: RememberedAccountsServiceUsingLoader): RememberedAccountsService

    @Binds
    fun bindsAutofillFormSourcesStrings(impl: AutofillFormSourcesStringsFromContext): AutofillFormSourcesStrings

    @Binds
    fun bindsApplicationFormSourceDeviceStatus(impl: ApplicationFormSourceDeviceStatusFromContext): ApplicationFormSourceDeviceStatus

    @Binds
    fun bindAutofillEmptyWebsiteWarningService(impl: AutofillEmptyWebsiteWarningServiceImpl): AutofillEmptyWebsiteWarningService

    @Binds
    fun bindsEmptyWebsiteWarningLogger(impl: AutofillEmptyWebsiteWarningLoggerImpl): EmptyWebsiteWarningLogger
}
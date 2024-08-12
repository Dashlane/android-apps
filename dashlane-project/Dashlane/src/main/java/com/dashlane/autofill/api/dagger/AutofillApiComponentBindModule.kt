package com.dashlane.autofill.api.dagger

import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.AutofillAnalyzerDef.DatabaseAccess
import com.dashlane.autofill.AutofillAnalyzerDef.IAutofillUsageLog
import com.dashlane.autofill.api.ApplicationFormSourceDeviceStatusFromContext
import com.dashlane.autofill.api.AutofillEmptyWebsiteWarningServiceImpl
import com.dashlane.autofill.api.AutofillFormSourcesStringsFromContext
import com.dashlane.autofill.changepassword.AutofillChangePasswordActivityIntentProvider
import com.dashlane.autofill.core.AutoFillDataBaseAccess
import com.dashlane.autofill.core.AutofillPhishingLoggerImpl
import com.dashlane.autofill.core.AutofillUsageLog
import com.dashlane.autofill.core.domain.AutofillSecurityApplication
import com.dashlane.autofill.createaccount.AutofillCreateAccountActivityIntentProvider
import com.dashlane.autofill.emptywebsitewarning.AutofillEmptyWebsiteWarningService
import com.dashlane.autofill.emptywebsitewarning.EmptyWebsiteWarningActivityIntentProvider
import com.dashlane.autofill.fillresponse.ChangePasswordActionIntentProvider
import com.dashlane.autofill.fillresponse.CreateAccountActionIntentProvider
import com.dashlane.autofill.fillresponse.EmptyWebsiteWarningIntentProvider
import com.dashlane.autofill.fillresponse.PauseActionIntentProvider
import com.dashlane.autofill.fillresponse.PhishingWarningIntentProvider
import com.dashlane.autofill.fillresponse.ViewAllAccountsActionIntentProvider
import com.dashlane.autofill.internal.ApplicationFormSourceDeviceStatus
import com.dashlane.autofill.internal.AutofillFormSourcesStrings
import com.dashlane.autofill.internal.AutofillLimiter
import com.dashlane.autofill.pause.AutofillPauseActivityIntentProvider
import com.dashlane.autofill.pause.services.PausedAutofillLimiter
import com.dashlane.autofill.phishing.AutofillPhishingActivityIntentProvider
import com.dashlane.autofill.phishing.AutofillPhishingLogger
import com.dashlane.autofill.phishing.PhishingWarningDataProvider
import com.dashlane.autofill.phishing.PhishingWarningDataProviderImpl
import com.dashlane.autofill.ui.AutofillPerformedCallback
import com.dashlane.autofill.ui.AutofillPerformedCallbackImpl
import com.dashlane.autofill.viewallaccounts.AutofillViewAllItemsActivityIntentProvider
import com.dashlane.login.lock.LockManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AutofillApiComponentBindModule {

    @Binds
    fun bindAutofillLockManager(lockManager: LockManager): AutofillAnalyzerDef.ILockManager

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

    @Binds
    fun bindsAutofillFormSourcesStrings(impl: AutofillFormSourcesStringsFromContext): AutofillFormSourcesStrings

    @Binds
    fun bindsApplicationFormSourceDeviceStatus(impl: ApplicationFormSourceDeviceStatusFromContext): ApplicationFormSourceDeviceStatus

    @Binds
    fun bindAutofillEmptyWebsiteWarningService(impl: AutofillEmptyWebsiteWarningServiceImpl): AutofillEmptyWebsiteWarningService

    @Binds
    fun bindPhishingWarningIntent(impl: AutofillPhishingActivityIntentProvider): PhishingWarningIntentProvider

    @Binds
    fun bindPhishingWarningDataProvider(impl: PhishingWarningDataProviderImpl): PhishingWarningDataProvider

    @Singleton
    @Binds
    fun bindsAutofillPhishingLogger(impl: AutofillPhishingLoggerImpl): AutofillPhishingLogger
}
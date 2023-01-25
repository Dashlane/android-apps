package com.dashlane.autofill.api.dagger

import com.dashlane.autofill.api.totp.AutofillApiTotpServiceImpl
import com.dashlane.autofill.api.totp.TotpNotificationClipboardServiceImpl
import com.dashlane.autofill.api.totp.TotpNotificationUpdateServiceImpl
import com.dashlane.autofill.api.totp.repository.TotpNotificationRepository
import com.dashlane.autofill.api.totp.repository.TotpNotificationRepositoryImpl
import com.dashlane.autofill.api.totp.services.AutofillApiTotpNotificationLogger
import com.dashlane.autofill.api.totp.AutofillApiTotpNotificationLoggerImpl
import com.dashlane.autofill.api.totp.services.AutofillApiTotpService
import com.dashlane.autofill.api.totp.services.TotpNotificationClipboardService
import com.dashlane.autofill.api.totp.services.TotpNotificationDisplayService
import com.dashlane.autofill.api.totp.services.TotpNotificationDisplayServiceImpl
import com.dashlane.autofill.api.totp.services.TotpNotificationUpdateService
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
internal abstract class AutofillApiTotpComponentModule {

    @Singleton
    @Binds
    abstract fun bindsTotpNotificationRepository(impl: TotpNotificationRepositoryImpl): TotpNotificationRepository

    @Singleton
    @Binds
    abstract fun bindsAutofillApiTotpService(impl: AutofillApiTotpServiceImpl): AutofillApiTotpService

    @Singleton
    @Binds
    abstract fun bindsTotpNotificationClipboardService(impl: TotpNotificationClipboardServiceImpl): TotpNotificationClipboardService

    @Singleton
    @Binds
    abstract fun bindsTotpNotificationUpdateService(impl: TotpNotificationUpdateServiceImpl): TotpNotificationUpdateService

    @Singleton
    @Binds
    abstract fun bindsTotpNotificationDisplayService(impl: TotpNotificationDisplayServiceImpl): TotpNotificationDisplayService

    @Singleton
    @Binds
    abstract fun bindsAutofillApiTotpNotificationLogger(impl: AutofillApiTotpNotificationLoggerImpl): AutofillApiTotpNotificationLogger
}
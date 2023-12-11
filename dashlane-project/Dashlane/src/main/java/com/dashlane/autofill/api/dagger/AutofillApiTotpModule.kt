package com.dashlane.autofill.api.dagger

import com.dashlane.autofill.totp.AutofillTotpCopyService
import com.dashlane.autofill.totp.AutofillTotpCopyServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface AutofillApiTotpModule {
    @Binds
    fun bindsAutofillApiTotp(impl: AutofillTotpCopyServiceImpl): AutofillTotpCopyService
}
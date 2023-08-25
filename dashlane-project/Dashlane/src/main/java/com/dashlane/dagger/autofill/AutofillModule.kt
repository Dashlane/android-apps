package com.dashlane.dagger.autofill

import com.dashlane.autofill.AutofillAnalyzerDef.IAutofillSecurityApplication
import com.dashlane.autofill.core.domain.AutofillSecurityApplication
import com.dashlane.dagger.singleton.BinderModule
import dagger.Binds
import dagger.Module

@Module(includes = [BinderModule::class])
interface AutofillModule {
    @Binds
    fun bindAuthentifiantResult(authentifiantResult: AutofillSecurityApplication): IAutofillSecurityApplication
}
package com.dashlane.dagger.autofill

import com.dashlane.autofill.AutofillAnalyzerDef.IAutofillSecurityApplication
import com.dashlane.autofill.core.domain.AutofillSecurityApplication
import com.dashlane.dagger.singleton.BinderModule
import com.dashlane.storage.userdata.accessor.dagger.UserDataAccessorModule
import dagger.Binds
import dagger.Module

@Module(includes = [BinderModule::class, UserDataAccessorModule::class])
interface AutofillModule {
    @Binds
    fun bindAuthentifiantResult(authentifiantResult: AutofillSecurityApplication): IAutofillSecurityApplication
}
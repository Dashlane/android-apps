package com.dashlane.autofill.api.dagger

import com.dashlane.autofill.api.changepause.AutofillApiChangePauseLogger
import com.dashlane.autofill.api.changepause.ChangePauseViewTypeProviderFactoryImpl
import com.dashlane.autofill.api.changepause.services.ChangePauseStrings
import com.dashlane.autofill.api.changepause.services.ChangePauseStringsFromContext
import com.dashlane.autofill.api.changepause.view.ChangePauseViewTypeProviderFactory
import com.dashlane.autofill.core.AutofillApiApiChangePauseLoggerImpl
import dagger.Binds
import dagger.Module



@Module
internal abstract class AutofillApiChangePauseComponentModule {
    @Binds
    abstract fun bindsAutofillApiChangePauseLogger(impl: AutofillApiApiChangePauseLoggerImpl): AutofillApiChangePauseLogger

    @Binds
    abstract fun bindsChangePauseViewTypeProviderFactory(impl: ChangePauseViewTypeProviderFactoryImpl): ChangePauseViewTypeProviderFactory

    @Binds
    abstract fun bindsChangePauseStrings(impl: ChangePauseStringsFromContext): ChangePauseStrings
}

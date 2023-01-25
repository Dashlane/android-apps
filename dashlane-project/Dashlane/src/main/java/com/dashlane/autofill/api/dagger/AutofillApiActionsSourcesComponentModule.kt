package com.dashlane.autofill.api.dagger

import com.dashlane.autofill.api.actionssources.AutofillActionsSourcesLogger
import com.dashlane.autofill.api.actionssources.view.AutofillFormSourceViewTypeProviderFactoryImpl
import com.dashlane.autofill.api.actionssources.view.AutofillFormSourceViewTypeProviderFactory
import com.dashlane.autofill.core.AutofillApiActionsSourcesLoggerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent



@Module
@InstallIn(ViewModelComponent::class)
internal abstract class AutofillApiActionsSourcesComponentModule {
    @Binds
    abstract fun bindsAutofillActionsSourcesLogger(impl: AutofillApiActionsSourcesLoggerImpl): AutofillActionsSourcesLogger

    @Binds
    abstract fun bindsAutofillFormSourceViewTypeProviderFactory(
        impl: AutofillFormSourceViewTypeProviderFactoryImpl
    ): AutofillFormSourceViewTypeProviderFactory
}

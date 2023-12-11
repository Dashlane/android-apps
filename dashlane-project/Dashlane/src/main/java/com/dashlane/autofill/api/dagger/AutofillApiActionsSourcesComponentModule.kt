package com.dashlane.autofill.api.dagger

import com.dashlane.autofill.actionssources.view.AutofillFormSourceViewTypeProviderFactoryImpl
import com.dashlane.autofill.actionssources.view.AutofillFormSourceViewTypeProviderFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class AutofillApiActionsSourcesComponentModule {

    @Binds
    abstract fun bindsAutofillFormSourceViewTypeProviderFactory(
        impl: AutofillFormSourceViewTypeProviderFactoryImpl
    ): AutofillFormSourceViewTypeProviderFactory
}

package com.dashlane.autofill.api.dagger

import com.dashlane.autofill.api.changepause.ChangePauseViewTypeProviderFactoryImpl
import com.dashlane.autofill.changepause.services.ChangePauseStrings
import com.dashlane.autofill.changepause.services.ChangePauseStringsFromContext
import com.dashlane.autofill.changepause.view.ChangePauseViewTypeProviderFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
internal abstract class AutofillApiChangePauseComponentModule {
    @Binds
    abstract fun bindsChangePauseViewTypeProviderFactory(impl: ChangePauseViewTypeProviderFactoryImpl): ChangePauseViewTypeProviderFactory

    @Binds
    abstract fun bindsChangePauseStrings(impl: ChangePauseStringsFromContext): ChangePauseStrings
}

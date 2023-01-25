package com.dashlane.autofill.api.dagger

import com.dashlane.autofill.api.unlinkaccount.LinkedAccountViewTypeProviderFactoryImpl
import com.dashlane.autofill.api.unlinkaccount.view.LinkedAccountViewTypeProviderFactory
import dagger.Binds
import dagger.Module
import javax.inject.Singleton



@Module
internal abstract class AutofillApiUnlinkAccountsComponentModule {
    @Singleton
    @Binds
    abstract fun bindsLinkedAccountViewTypeProviderFactory(impl: LinkedAccountViewTypeProviderFactoryImpl): LinkedAccountViewTypeProviderFactory
}

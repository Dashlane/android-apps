package com.dashlane.autofill.api.dagger

import com.dashlane.autofill.api.AuthentifiantSearchViewTypeProviderFactoryImpl
import com.dashlane.autofill.api.AutofillSearchUsingLoader
import com.dashlane.autofill.linkservices.services.TriggerRememberViewAllAccount
import com.dashlane.autofill.viewallaccounts.AutofillSearch
import com.dashlane.autofill.viewallaccounts.services.ViewAllAccountSelectionNotifier
import com.dashlane.autofill.viewallaccounts.view.AuthentifiantSearchViewTypeProviderFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AutofillApiViewAllAccountsComponentModule {
    @Binds
    abstract fun bindsAutofillSearch(impl: AutofillSearchUsingLoader): AutofillSearch

    @Binds
    abstract fun bindsAuthentifiantSearchViewTypeProviderFactory(
        impl: AuthentifiantSearchViewTypeProviderFactoryImpl,
    ): AuthentifiantSearchViewTypeProviderFactory

    @Binds
    abstract fun bindsViewAllAccountSelectionNotifier(impl: TriggerRememberViewAllAccount): ViewAllAccountSelectionNotifier
}
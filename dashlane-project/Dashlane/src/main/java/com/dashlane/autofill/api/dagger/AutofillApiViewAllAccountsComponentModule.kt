package com.dashlane.autofill.api.dagger

import com.dashlane.autofill.api.AuthentifiantSearchViewTypeProviderFactoryImpl
import com.dashlane.autofill.api.AutofillSearchUsingLoader
import com.dashlane.autofill.api.rememberaccount.services.TriggerRememberViewAllAccount
import com.dashlane.autofill.api.viewallaccounts.services.ViewAllAccountSelectionNotifier
import com.dashlane.autofill.api.viewallaccounts.model.AutofillSearch
import com.dashlane.autofill.api.viewallaccounts.view.AuthentifiantSearchViewTypeProviderFactory
import dagger.Binds
import dagger.Module
import javax.inject.Singleton



@Module
abstract class AutofillApiViewAllAccountsComponentModule {
    @Singleton
    @Binds
    abstract fun bindsAutofillSearch(impl: AutofillSearchUsingLoader): AutofillSearch

    @Singleton
    @Binds
    abstract fun bindsAuthentifiantSearchViewTypeProviderFactory(impl: AuthentifiantSearchViewTypeProviderFactoryImpl): AuthentifiantSearchViewTypeProviderFactory

    @Singleton
    @Binds
    abstract fun bindsViewAllAccountSelectionNotifier(impl: TriggerRememberViewAllAccount): ViewAllAccountSelectionNotifier
}
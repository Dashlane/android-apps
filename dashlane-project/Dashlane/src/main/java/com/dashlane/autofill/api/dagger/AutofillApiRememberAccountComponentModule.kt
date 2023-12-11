package com.dashlane.autofill.api.dagger

import com.dashlane.autofill.api.rememberaccount.ApplicationFormSourceAuthentifiantPreferencesLinker
import com.dashlane.autofill.api.rememberaccount.AutofillApiRememberAccountToasterImpl
import com.dashlane.autofill.rememberaccount.AutofillApiRememberedAccountToaster
import com.dashlane.autofill.api.rememberaccount.WebDomainFormSourceAuthentifiantPreferencesLinker
import com.dashlane.autofill.api.rememberaccount.linkedservices.ApplicationFormSourceAuthentifiantLinker
import com.dashlane.autofill.api.rememberaccount.linkedservices.WebDomainFormSourceAuthentifiantLinker
import com.dashlane.autofill.rememberaccount.model.FormSourcesDataProvider
import com.dashlane.autofill.rememberaccount.model.FormSourcesDataProviderImpl
import com.dashlane.autofill.rememberaccount.services.FormSourceAuthentifiantLinker
import dagger.Binds
import dagger.Module
import javax.inject.Named
import javax.inject.Singleton

@Module
internal abstract class AutofillApiRememberAccountComponentModule {
    @Singleton
    @Binds
    @Named("ApplicationLinkedPreference")
    abstract fun bindsApplicationFormSourceAuthentifiantPreferencesDataLinker(impl: ApplicationFormSourceAuthentifiantPreferencesLinker): FormSourceAuthentifiantLinker

    @Singleton
    @Binds
    @Named("WebDomainLinkedPreference")
    abstract fun bindsWebDomainFormSourceAuthentifiantPreferencesDataLinker(impl: WebDomainFormSourceAuthentifiantPreferencesLinker): FormSourceAuthentifiantLinker

    @Singleton
    @Binds
    @Named("ApplicationDataLinked")
    abstract fun bindsApplicationFormSourceAuthentifiantDataLinker(impl: ApplicationFormSourceAuthentifiantLinker): FormSourceAuthentifiantLinker

    @Singleton
    @Binds
    @Named("WebDomainDataLinked")
    abstract fun bindsWebDomainFormSourceAuthentifiantDataLinker(impl: WebDomainFormSourceAuthentifiantLinker): FormSourceAuthentifiantLinker

    @Singleton
    @Binds
    abstract fun bindsFormSourcesDataProvider(impl: FormSourcesDataProviderImpl): FormSourcesDataProvider

    @Singleton
    @Binds
    abstract fun bindsAutofillApiRememberedAccountToaster(impl: AutofillApiRememberAccountToasterImpl): AutofillApiRememberedAccountToaster
}

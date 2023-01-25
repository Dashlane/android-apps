package com.dashlane.autofill.api.unlinkaccount.dagger

import com.dashlane.autofill.api.unlinkaccount.AutofillApiUnlinkAccountsComponent
import com.dashlane.autofill.api.internal.AutofillApiComponent
import com.dashlane.autofill.api.unlinkaccount.view.UnlinkAccountsFragment
import dagger.BindsInstance
import dagger.Component



@UnlinkAccountsFragmentScope
@Component(
    modules = [
        UnlinkAccountsFragmentModule::class
    ],
    dependencies = [
        AutofillApiComponent::class,
        UnlinkAccountsViewModelComponent::class,
        AutofillApiUnlinkAccountsComponent::class
    ]
)
interface UnlinkAccountsFragmentComponent {
    @Component.Factory
    interface Factory {
        fun create(
            autofillApiComponent: AutofillApiComponent,
            autofillApiUnlinkAccountsComponent: AutofillApiUnlinkAccountsComponent,
            unlinkAccountsViewModelComponent: UnlinkAccountsViewModelComponent,
            @BindsInstance fragment: UnlinkAccountsFragment
        ): UnlinkAccountsFragmentComponent
    }

    fun inject(fragment: UnlinkAccountsFragment)
}

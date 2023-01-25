package com.dashlane.autofill.api.unlinkaccount.dagger

import com.dashlane.autofill.api.internal.AutofillApiComponent
import com.dashlane.autofill.api.unlinkaccount.AutofillApiUnlinkAccountsComponent
import com.dashlane.autofill.api.rememberaccount.AutofillApiRememberAccountComponent
import com.dashlane.autofill.api.unlinkaccount.UnlinkAccountsContract
import com.dashlane.autofill.api.unlinkaccount.view.UnlinkAccountsFragment
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext



@UnlinkAccountsViewModelScope
@Component(
    modules = [
        UnlinkAccountsViewModelModule::class
    ],
    dependencies = [
        AutofillApiComponent::class,
        AutofillApiUnlinkAccountsComponent::class,
        AutofillApiRememberAccountComponent::class
    ]
)
interface UnlinkAccountsViewModelComponent {
    val presenter: UnlinkAccountsContract.Presenter

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance autoFillFormSource: AutoFillFormSource,
            autofillApiComponent: AutofillApiComponent,
            autofillApiUnlinkAccountsComponent: AutofillApiUnlinkAccountsComponent,
            autofillApiRememberAccountComponent: AutofillApiRememberAccountComponent,
            @BindsInstance @ViewModel viewModelScope: CoroutineScope,
            @BindsInstance @Data backgroundCoroutineContext: CoroutineContext = Dispatchers.IO
        ): UnlinkAccountsViewModelComponent
    }
}

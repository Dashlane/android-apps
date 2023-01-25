package com.dashlane.autofill.api.changepause.dagger

import com.dashlane.autofill.api.changepause.AutofillApiChangePauseComponent
import com.dashlane.autofill.api.internal.AutofillApiComponent
import com.dashlane.autofill.api.changepause.view.ChangePauseFragment
import dagger.BindsInstance
import dagger.Component



@ChangePauseFragmentScope
@Component(
    modules = [
        ChangePauseFragmentModule::class
    ],
    dependencies = [
        AutofillApiComponent::class,
        ChangePauseViewModelComponent::class,
        AutofillApiChangePauseComponent::class
    ]
)
interface ChangePauseFragmentComponent {
    @Component.Factory
    interface Factory {
        fun create(
            autofillApiComponent: AutofillApiComponent,
            autofillApiChangePauseComponent: AutofillApiChangePauseComponent,
            changePauseViewModelComponent: ChangePauseViewModelComponent,
            @BindsInstance fragment: ChangePauseFragment
        ): ChangePauseFragmentComponent
    }

    fun inject(fragment: ChangePauseFragment)
}

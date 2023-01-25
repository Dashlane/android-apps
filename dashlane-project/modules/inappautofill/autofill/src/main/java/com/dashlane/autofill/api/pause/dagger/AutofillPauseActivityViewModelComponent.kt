package com.dashlane.autofill.api.pause.dagger

import com.dashlane.autofill.api.pause.AutofillApiPauseComponent
import com.dashlane.autofill.api.pause.AskPauseContract
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named



@AutofillPauseActivityViewModelScope
@Component(
    modules = [
        AutofillPauseActivityViewModelModule::class
    ],
    dependencies = [
        AutofillApiPauseComponent::class
    ]
)
interface AutofillPauseActivityViewModelComponent {
    val askPausePresenter: AskPauseContract.Presenter

    @Component.Factory
    interface Factory {
        fun create(
            autofillApiPauseComponent: AutofillApiPauseComponent,
            @BindsInstance @Named("openInDashlane") openInDashlane: Boolean = false
        ): AutofillPauseActivityViewModelComponent
    }
}

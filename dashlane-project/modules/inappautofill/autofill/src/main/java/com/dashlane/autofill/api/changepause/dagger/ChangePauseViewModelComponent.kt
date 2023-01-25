package com.dashlane.autofill.api.changepause.dagger

import com.dashlane.autofill.api.changepause.AutofillApiChangePauseComponent
import com.dashlane.autofill.api.changepause.ChangePauseContract
import com.dashlane.autofill.api.pause.AutofillApiPauseComponent
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext



@ChangePauseViewModelScope
@Component(
    modules = [
        ChangePauseViewModelModule::class
    ],
    dependencies = [
        AutofillApiChangePauseComponent::class,
        AutofillApiPauseComponent::class
    ]
)
interface ChangePauseViewModelComponent {
    val presenter: ChangePauseContract.Presenter

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance autoFillFormSource: AutoFillFormSource,
            autofillApiChangePauseComponent: AutofillApiChangePauseComponent,
            autofillApiPauseComponent: AutofillApiPauseComponent,
            @BindsInstance @ViewModel viewModelScope: CoroutineScope,
            @BindsInstance @Data backgroundCoroutineContext: CoroutineContext = Dispatchers.IO
        ): ChangePauseViewModelComponent
    }
}

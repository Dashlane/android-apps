package com.dashlane.autofill.api.pause.dagger

import com.dashlane.autofill.api.internal.AutofillApiComponent
import com.dashlane.autofill.api.pause.AutofillApiPauseComponent
import com.dashlane.autofill.api.pause.view.BottomSheetAskPauseDialogFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named



@BottomSheetAskPauseDialogFragmentScope
@Component(
    modules = [
        BottomSheetAskPauseDialogFragmentModule::class
    ],
    dependencies = [
        AutofillApiComponent::class,
        AutofillPauseActivityViewModelComponent::class,
        AutofillApiPauseComponent::class
    ]
)
interface BottomSheetAskPauseDialogFragmentComponent {
    @Component.Factory
    interface Factory {
        fun create(
            autofillApiComponent: AutofillApiComponent,
            autofillApiPauseComponent: AutofillApiPauseComponent,
            autofillPauseActivityViewModelComponent: AutofillPauseActivityViewModelComponent,
            @BindsInstance bottomSheetAskPauseDialogFragment: BottomSheetAskPauseDialogFragment,
            @BindsInstance @Named("openInDashlane") openInDashlane: Boolean = false
        ): BottomSheetAskPauseDialogFragmentComponent
    }

    fun inject(bottomSheetAskPauseDialogFragment: BottomSheetAskPauseDialogFragment)
}

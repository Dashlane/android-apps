package com.dashlane.autofill.api.emptywebsitewarning.dagger

import com.dashlane.autofill.api.emptywebsitewarning.view.BottomSheetEmptyWebsiteWarningDialogFragment
import com.dashlane.autofill.api.emptywebsitewarning.view.EmptyWebsiteWarningActivity
import com.dashlane.autofill.api.internal.AutofillApiComponent
import dagger.BindsInstance
import dagger.Component
import javax.inject.Scope

@EmptyWebsiteWarningScope
@Component(
    modules = [
        AutofillEmptyWebsiteWarningModule::class
    ],
    dependencies = [
        AutofillApiComponent::class
    ]
)
interface EmptyWebsiteWarningComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance emptyWebsiteWarningActivity: EmptyWebsiteWarningActivity,
            autofillApiComponent: AutofillApiComponent
        ): EmptyWebsiteWarningComponent
    }

    fun inject(bottomSheetEmptyWebsiteWarningDialogFragment: BottomSheetEmptyWebsiteWarningDialogFragment)
}

@Scope
annotation class EmptyWebsiteWarningScope
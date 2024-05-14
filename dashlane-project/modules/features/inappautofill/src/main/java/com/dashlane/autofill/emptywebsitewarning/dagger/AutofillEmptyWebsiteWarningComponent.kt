package com.dashlane.autofill.emptywebsitewarning.dagger

import com.dashlane.autofill.emptywebsitewarning.view.BottomSheetEmptyWebsiteWarningDialogFragment
import com.dashlane.autofill.emptywebsitewarning.view.EmptyWebsiteWarningActivity
import com.dashlane.autofill.internal.AutofillApiEntryPoint
import dagger.BindsInstance
import dagger.Component
import javax.inject.Scope

@EmptyWebsiteWarningScope
@Component(
    modules = [
        AutofillEmptyWebsiteWarningModule::class
    ],
    dependencies = [
        AutofillApiEntryPoint::class
    ]
)
interface EmptyWebsiteWarningComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance emptyWebsiteWarningActivity: EmptyWebsiteWarningActivity,
            autofillApiEntryPoint: AutofillApiEntryPoint
        ): EmptyWebsiteWarningComponent
    }

    fun inject(bottomSheetEmptyWebsiteWarningDialogFragment: BottomSheetEmptyWebsiteWarningDialogFragment)
}

@Scope
annotation class EmptyWebsiteWarningScope
package com.dashlane.autofill.api.viewallaccounts.dagger

import com.dashlane.autofill.api.viewallaccounts.AutofillApiViewAllAccountsComponent
import com.dashlane.autofill.api.viewallaccounts.view.BottomSheetAuthentifiantsSearchAndFilterDialogFragment
import com.dashlane.autofill.api.viewallaccounts.view.AutofillViewAllItemsActivity
import dagger.BindsInstance
import dagger.Component



@BottomSheetViewAllItemsScope
@Component(
    modules = [
        BottomSheetViewAllItemsModule::class
    ],
    dependencies = [
        AutofillApiViewAllAccountsComponent::class
    ]
)
interface BottomSheetViewAllItemsComponent {
    @Component.Factory
    interface Factory {
        fun create(
            autofillApiViewAllAccountsComponent: AutofillApiViewAllAccountsComponent,
            @BindsInstance bottomSheetDialogFragment: BottomSheetAuthentifiantsSearchAndFilterDialogFragment
        ): BottomSheetViewAllItemsComponent
    }

    fun inject(bottomSheetAuthentifiantsSearchAndFilterDialogFragment: BottomSheetAuthentifiantsSearchAndFilterDialogFragment)
}

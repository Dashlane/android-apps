package com.dashlane.autofill.api.createaccount

import android.content.Context
import com.dashlane.autofill.api.createaccount.domain.AutofillCreateAccountService
import com.dashlane.util.Toaster

interface AutofillApiCreateAccountComponent {
    val autofillAccountCreationService: AutofillCreateAccountService
    val createAccountLogger: AutofillCreateAccountLogger
    val toaster: Toaster

    companion object {
        operator fun invoke(context: Context) =
            (context.applicationContext as AutofillApiCreateAccountApplication).component
    }
}

package com.dashlane.autofill.api.rememberaccount

import android.content.Context
import com.dashlane.autofill.api.rememberaccount.model.FormSourcesDataProvider



interface AutofillApiRememberAccountComponent {
    val autofillApiRememberedAccountToaster: AutofillApiRememberedAccountToaster
    val formSourcesDataProvider: FormSourcesDataProvider

    companion object {
        operator fun invoke(context: Context) =
            (context.applicationContext as AutofillApiRememberAccountApplication).component
    }
}

package com.dashlane.autofill.api.changepause

import android.content.Context
import com.dashlane.autofill.api.changepause.services.ChangePauseStrings
import com.dashlane.autofill.api.changepause.view.ChangePauseViewTypeProviderFactory



interface AutofillApiChangePauseComponent {
    val autofillApiChangePauseLogger: AutofillApiChangePauseLogger
    val changePauseViewTypeProviderFactory: ChangePauseViewTypeProviderFactory
    val changePauseStrings: ChangePauseStrings

    companion object {
        operator fun invoke(context: Context) =
            (context.applicationContext as AutofillApiChangePauseApplication).component
    }
}

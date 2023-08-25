package com.dashlane.autofill.api.unlinkaccount

import android.content.Context
import com.dashlane.autofill.api.unlinkaccount.view.LinkedAccountViewTypeProviderFactory

interface AutofillApiUnlinkAccountsComponent {
    val linkedAccountViewTypeProviderFactory: LinkedAccountViewTypeProviderFactory

    companion object {
        operator fun invoke(context: Context) =
            (context.applicationContext as AutofillApiUnlinkAccountsApplication).component
    }
}

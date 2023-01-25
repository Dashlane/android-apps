package com.dashlane.autofill.api.viewallaccounts

import android.content.Context
import com.dashlane.autofill.api.viewallaccounts.model.AutofillSearch
import com.dashlane.autofill.api.viewallaccounts.services.ViewAllAccountSelectionNotifier
import com.dashlane.autofill.api.viewallaccounts.view.AuthentifiantSearchViewTypeProviderFactory
import com.dashlane.util.userfeatures.UserFeaturesChecker



interface AutofillApiViewAllAccountsComponent {
    val autofillSearch: AutofillSearch
    val authentifiantSearchViewTypeProviderFactory: AuthentifiantSearchViewTypeProviderFactory
    val viewAllAccountSelectionNotifier: ViewAllAccountSelectionNotifier
    val userFeaturesChecker: UserFeaturesChecker

    companion object {
        operator fun invoke(context: Context) =
            (context.applicationContext as AutofillApiViewAllAccountsApplication).component
    }
}

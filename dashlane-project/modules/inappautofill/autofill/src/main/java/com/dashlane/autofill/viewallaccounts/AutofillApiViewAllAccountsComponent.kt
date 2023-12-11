package com.dashlane.autofill.viewallaccounts

import com.dashlane.autofill.viewallaccounts.services.ViewAllAccountSelectionNotifier
import com.dashlane.autofill.viewallaccounts.view.AuthentifiantSearchViewTypeProviderFactory
import com.dashlane.util.userfeatures.UserFeaturesChecker

interface AutofillApiViewAllAccountsComponent {
    val autofillSearch: AutofillSearch
    val authentifiantSearchViewTypeProviderFactory: AuthentifiantSearchViewTypeProviderFactory
    val viewAllAccountSelectionNotifier: ViewAllAccountSelectionNotifier
    val userFeaturesChecker: UserFeaturesChecker
}

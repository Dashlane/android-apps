package com.dashlane.autofill.api.viewallaccounts.view

import com.dashlane.autofill.api.navigation.AutofillBottomSheetNavigator
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject



interface SearchAuthentifiantDialogResponse {
    fun onResultsLoaded()
    fun onNavigateToCreateAuthentifiant(autofillBottomSheetNavigator: AutofillBottomSheetNavigator)
    fun onNavigateToLinkService(
        formSource: AutoFillFormSource,
        itemId: String
    )

    fun onAuthentifiantDialogResponse(
        authentifiant: VaultItem<SyncObject.Authentifiant>?,
        itemListContext: ItemListContext?,
        searchQuery: String
    )
}
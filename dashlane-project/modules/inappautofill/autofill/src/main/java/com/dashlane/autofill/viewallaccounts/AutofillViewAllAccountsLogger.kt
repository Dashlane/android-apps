package com.dashlane.autofill.viewallaccounts

import com.dashlane.autofill.AutofillOrigin
import com.dashlane.ui.adapter.ItemListContext

interface AutofillViewAllAccountsLogger {

    fun onResultsLoaded()

    fun onSelectFromViewAllAccount(
        @AutofillOrigin origin: Int,
        packageName: String,
        webappDomain: String,
        itemUrl: String?,
        itemId: String?,
        itemListContext: ItemListContext?
    )

    fun onViewAllAccountOver(totalCount: Int, hasInteracted: Boolean, charactersTypedCount: Int)
}
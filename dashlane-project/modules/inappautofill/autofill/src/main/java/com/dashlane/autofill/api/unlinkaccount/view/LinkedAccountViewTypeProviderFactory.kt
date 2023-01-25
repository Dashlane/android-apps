package com.dashlane.autofill.api.unlinkaccount.view

import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.vault.summary.SummaryObject



interface LinkedAccountViewTypeProviderFactory {
    fun create(authentifiant: SummaryObject.Authentifiant): AuthentifiantWrapperItem

    interface AuthentifiantWrapperItem : DashlaneRecyclerAdapter.ViewTypeProvider {
        val title: String?
        val subtitle: String?
    }
}

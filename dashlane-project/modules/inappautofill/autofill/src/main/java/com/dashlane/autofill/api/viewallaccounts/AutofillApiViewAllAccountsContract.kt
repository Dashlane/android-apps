package com.dashlane.autofill.api.viewallaccounts

import com.dashlane.autofill.api.viewallaccounts.view.AuthentifiantSearchViewTypeProviderFactory
import com.dashlane.search.MatchedSearchResult
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import kotlinx.coroutines.CoroutineScope

interface AutofillApiViewAllAccountsContract {
    

    interface View {
        fun onSelected(authentifiant: VaultItem<SyncObject.Authentifiant>, itemListContext: ItemListContext)
        fun onNothingSelected()
        fun getQuery(): String
        fun onUpdateAuthentifiants(authentifiants: List<MatchedSearchResult>, query: String?)
        fun onError()
        fun showLoading()
        fun hideLoading()
    }

    

    interface Presenter {
        fun setView(view: View, viewCoroutineScope: CoroutineScope)
        fun filterAuthentifiants(query: String)
        fun selectedAuthentifiant(wrapperItem: AuthentifiantSearchViewTypeProviderFactory.AuthentifiantWrapperItem)
        fun noSelection()
    }

    

    interface DataProvider {
        fun getAuthentifiant(authentifiantId: String): VaultItem<SyncObject.Authentifiant>
        fun getMatchedAuthentifiantsFromQuery(query: String?): List<MatchedSearchResult>
    }
}

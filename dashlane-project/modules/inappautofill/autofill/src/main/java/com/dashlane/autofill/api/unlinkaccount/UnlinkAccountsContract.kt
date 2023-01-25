package com.dashlane.autofill.api.unlinkaccount

import com.dashlane.autofill.api.unlinkaccount.model.LinkedAccountsModel
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineScope

interface UnlinkAccountsContract {

    

    interface View {
        fun unlinkAccount(account: SummaryObject.Authentifiant, formSourceTitle: String)
        fun updateLinkedAccounts(items: List<SummaryObject.Authentifiant>)
        fun showErrorOnLoadLinkedAccounts()
        fun showErrorOnUnlinkAccount()

        fun startLoading()
        fun stopLoading()
    }

    

    interface Presenter {
        fun setView(view: View, viewCoroutineScope: CoroutineScope)
        fun onResume()
        fun onRefresh()
        fun onLinkedAccountsItemClick(position: Int)
    }

    

    interface DataProvider {
        fun bindResponses(responses: Responses)
        fun currentState(): LinkedAccountsModel?
        fun loadLinkedAccounts()
        fun unlinkAccount(index: Int)

        interface Responses {
            fun unlinkedAccount(account: SummaryObject.Authentifiant, linkedAccountsModel: LinkedAccountsModel)
            fun updateLinkedAccounts(linkedAccountsModel: LinkedAccountsModel)
            fun errorOnLoadLinkedAccounts()
            fun errorOnUnlinkAccount()
        }
    }
}

package com.dashlane.autofill.api.emptywebsitewarning

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dashlane.vault.summary.SummaryObject



interface EmptyWebsiteWarningContract {

    interface Presenter {

        

        suspend fun getAccountWrapper(uid: String, currentUrl: String): AccountWrapper?

        

        suspend fun fetchAccount(uid: String): SummaryObject.Authentifiant?

        

        suspend fun updateAccountWithNewUrl(uid: String, website: String): SummaryObject.Authentifiant?

        fun onCancel(website: String)

        fun onDisplay(website: String)
    }

    interface ViewProxy {
        fun createView(inflater: LayoutInflater, container: ViewGroup?): View?

        fun updateView(context: Context, website: String, itemId: String)

        fun onCancel()
    }

    interface DataProvider {

        

        suspend fun fetchAccount(uid: String): SummaryObject.Authentifiant?

        

        suspend fun updateAccountWithNewUrl(uid: String, newUrl: String): SummaryObject.Authentifiant?
    }

    data class AccountWrapper(val itemId: String, val title: String, val login: String, val websiteSuggestion: String)
}
package com.dashlane.autofill.api.unlinkaccount

import com.dashlane.R
import com.dashlane.autofill.api.unlinkaccount.view.LinkedAccountViewTypeProviderFactory
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class LinkedAccountViewTypeProviderFactoryImpl @Inject constructor() : LinkedAccountViewTypeProviderFactory {

    override fun create(authentifiant: SummaryObject.Authentifiant): LinkedAccountViewTypeProviderFactory.AuthentifiantWrapperItem {
        return Wrapper(authentifiant)
    }

    class Wrapper(val authentifiant: SummaryObject.Authentifiant) :
        LinkedAccountViewTypeProviderFactory.AuthentifiantWrapperItem {
        override val title: String?
            get() = authentifiant.loginForUi
        override val subtitle: String?
            get() = authentifiant.title

        override fun getViewType(): DashlaneRecyclerAdapter.ViewType<*> {
            return DashlaneRecyclerAdapter.ViewType(
                R.layout.list_linked_accounts_item,
                LinkedAccountHolder::class.java
            )
        }
    }
}

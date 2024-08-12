package com.dashlane.search.textfactory

import com.dashlane.search.SearchField
import com.dashlane.search.fields.CredentialField
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.StatusText
import com.dashlane.vault.textfactory.list.toStatusText

class AuthentifiantSearchListTextFactory(
    private val item: SummaryObject.Authentifiant
) : DataIdentifierSearchListTextFactory {

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is CredentialField) return null

        val text = when (field) {
            CredentialField.EMAIL -> item.email
            CredentialField.LOGIN -> item.login
            CredentialField.NOTE -> item.note
            CredentialField.SECONDARY_LOGIN -> item.secondaryLogin
            CredentialField.URL -> item.url?.toUrlDomainOrNull()?.value ?: item.url
            CredentialField.USER_SELECTED_URL -> item.userSelectedUrl
            
            else -> null
        }
        return text?.toStatusText()
    }
}
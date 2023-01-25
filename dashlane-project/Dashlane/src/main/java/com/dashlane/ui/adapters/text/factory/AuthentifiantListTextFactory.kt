package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.R
import com.dashlane.search.fields.CredentialField
import com.dashlane.search.SearchField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.model.titleForList
import com.dashlane.vault.summary.SummaryObject

class AuthentifiantListTextFactory(
    private val context: Context,
    private val item: SummaryObject.Authentifiant
) : DataIdentifierListTextFactory {
    override fun getLine1() = StatusText(item.titleForList.orEmpty())

    override fun getLine2(default: StatusText): StatusText {
        return when {
            item.isPasswordEmpty -> StatusText(
                context.getString(R.string.incomplete_reason_missing_password_list_line_2),
                true
            )
            item.login.isSemanticallyNull() && item.email.isSemanticallyNull() -> default
            else -> {
                val email = item.email
                val login = item.login
                StatusText(if (login.isNotSemanticallyNull()) login!! else email!!)
            }
        }
    }

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

    companion object {
        const val ITEM_TYPE_NAME_ID = R.string.datatype_authentifiant
    }
}

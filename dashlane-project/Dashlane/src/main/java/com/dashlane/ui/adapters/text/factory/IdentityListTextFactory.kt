package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.R
import com.dashlane.search.SearchField
import com.dashlane.search.fields.IdentityField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.identityPartialOrFullNameNoLogin
import com.dashlane.vault.summary.SummaryObject

class IdentityListTextFactory(
    private val context: Context,
    private val item: SummaryObject.Identity
) : DataIdentifierListTextFactory {

    override fun getLine1(): StatusText {
        val defaultString = context.getString(ITEM_TYPE_NAME_ID)
        val name = item.identityPartialOrFullNameNoLogin
        return StatusText(if (name.isNotSemanticallyNull()) name!! else defaultString)
    }

    override fun getLine2(default: StatusText): StatusText {
        return item.birthDate?.toIdentityFormat()?.let { StatusText(it) } ?: default
    }

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is IdentityField) return null
        val text = when (field) {
            IdentityField.MIDDLE_NAME -> item.middleName
            IdentityField.LAST_NAME -> item.lastName
            IdentityField.PSEUDO -> item.pseudo
            else -> null
        }
        return text?.toStatusText()
    }

    companion object {
        const val ITEM_TYPE_NAME_ID = R.string.identity
    }
}
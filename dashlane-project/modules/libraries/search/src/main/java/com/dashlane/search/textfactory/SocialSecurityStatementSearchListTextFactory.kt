package com.dashlane.search.textfactory

import android.content.Context
import com.dashlane.search.R
import com.dashlane.search.SearchField
import com.dashlane.search.fields.SocialSecurityStatementField
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.StatusText
import com.dashlane.vault.textfactory.list.toStatusText

class SocialSecurityStatementSearchListTextFactory(
    private val context: Context,
    private val item: SummaryObject.SocialSecurityStatement,
) : DataIdentifierSearchListTextFactory {

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is SocialSecurityStatementField) return null
        val text = when (field) {
            SocialSecurityStatementField.ITEM_TYPE_NAME -> context.getString(R.string.social_security)
            SocialSecurityStatementField.SOCIAL_SECURITY_FULL_NAME -> item.socialSecurityFullname
            else -> null
        }
        return text?.toStatusText()
    }
}
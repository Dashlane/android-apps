package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.R
import com.dashlane.search.SearchField
import com.dashlane.search.fields.SocialSecurityStatementField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.IdentityNameHolderService

class SocialSecurityStatementListTextFactory(
    private val context: Context,
    private val item: SummaryObject.SocialSecurityStatement,
    private val identityNameHolderService: IdentityNameHolderService
) : DataIdentifierListTextFactory {

    override fun getLine1(): StatusText {
        return StatusText(context.getString(ITEM_TYPE_NAME_ID))
    }

    override fun getLine2(default: StatusText): StatusText {
        val hasNoIdentity = item.linkedIdentity.isSemanticallyNull() &&
            (item.socialSecurityFullname.isSemanticallyNull() || item.dateOfBirth == null)
        if (hasNoIdentity || item.isSocialSecurityNumberEmpty) {
            return default
        }
        return StatusText(identityNameHolderService.getOwner(item))
    }

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is SocialSecurityStatementField) return null
        val text = when (field) {
            SocialSecurityStatementField.ITEM_TYPE_NAME -> context.getString(ITEM_TYPE_NAME_ID)
            SocialSecurityStatementField.SOCIAL_SECURITY_FULL_NAME -> item.socialSecurityFullname
            else -> null
        }
        return text?.toStatusText()
    }

    companion object {
        const val ITEM_TYPE_NAME_ID = R.string.social_security
    }
}
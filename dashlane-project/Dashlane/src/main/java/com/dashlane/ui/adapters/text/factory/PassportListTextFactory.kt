package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.R
import com.dashlane.search.SearchField
import com.dashlane.search.fields.PassportField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.IdentityNameHolderService

class PassportListTextFactory(
    private val context: Context,
    private val item: SummaryObject.Passport,
    private val identityNameHolderService: IdentityNameHolderService
) : DataIdentifierListTextFactory {

    override fun getLine1(): StatusText {
        val title = identityNameHolderService.getOwner(item)
        val incomplete = context.getString(ITEM_TYPE_NAME_ID)
        return StatusText(if (title.isNotSemanticallyNull()) title else incomplete)
    }

    override fun getLine2(default: StatusText): StatusText =
        item.expireDate?.getIdentityStatusText(context, item.number, default) ?: default

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is PassportField) return null
        val text = when (field) {
            PassportField.FULL_NAME -> item.fullname
            else -> null
        }
        return text?.toStatusText()
    }

    companion object {
        const val ITEM_TYPE_NAME_ID = R.string.passport
    }
}
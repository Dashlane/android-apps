package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.R
import com.dashlane.search.SearchField
import com.dashlane.search.fields.IdCardField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.IdentityNameHolderService

class IdCardListTextFactory(
    private val context: Context,
    private val item: SummaryObject.IdCard,
    private val identityNameHolderService: IdentityNameHolderService
) : DataIdentifierListTextFactory {

    override fun getLine1(): StatusText {
        val title = identityNameHolderService.getOwner(item)
        val incomplete = context.getString(ITEM_TYPE_NAME_ID)
        if (title.isSemanticallyNull()) {
            return StatusText(incomplete)
        }
        return StatusText(title)
    }

    override fun getLine2(default: StatusText): StatusText =
        item.expireDate?.getIdentityStatusText(context, item.number, default) ?: default

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is IdCardField) return null
        val text = when (field) {
            IdCardField.FULL_NAME -> item.fullname
            else -> null
        }
        return text?.toStatusText()
    }

    companion object {
        const val ITEM_TYPE_NAME_ID = R.string.id_card
    }
}
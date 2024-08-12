package com.dashlane.vault.textfactory.list

import android.content.res.Resources
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import javax.inject.Inject

class BankStatementListTextFactory @Inject constructor(
    private val resources: Resources,
) : DataIdentifierListTextFactory<SummaryObject.BankStatement> {

    override fun getTitle(item: SummaryObject.BankStatement): StatusText {
        val name = item.bankAccountName
        return StatusText(if (name.isNotSemanticallyNull()) name!! else resources.getString(R.string.bank_statement))
    }

    override fun getDescription(item: SummaryObject.BankStatement, default: StatusText): StatusText {
        if (item.bankAccountOwner.isSemanticallyNull() ||
            item.isIBANEmpty ||
            item.isBICEmpty
        ) {
            return default
        }
        return StatusText(item.bankAccountOwner!!)
    }
}
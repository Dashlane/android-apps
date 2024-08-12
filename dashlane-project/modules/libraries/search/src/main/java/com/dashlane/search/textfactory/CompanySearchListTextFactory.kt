package com.dashlane.search.textfactory

import com.dashlane.search.SearchField
import com.dashlane.search.fields.CompanyField
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.StatusText
import com.dashlane.vault.textfactory.list.toStatusText

class CompanySearchListTextFactory(
    private val item: SummaryObject.Company
) : DataIdentifierSearchListTextFactory {

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is CompanyField) return null
        val text = when (field) {
            CompanyField.JOB_TITLE -> item.jobTitle
            else -> null
        }
        return text?.toStatusText()
    }
}
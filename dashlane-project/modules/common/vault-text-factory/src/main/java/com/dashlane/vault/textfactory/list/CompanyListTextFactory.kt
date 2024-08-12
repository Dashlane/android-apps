package com.dashlane.vault.textfactory.list

import android.content.res.Resources
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import javax.inject.Inject

class CompanyListTextFactory @Inject constructor(
    private val resources: Resources,
) : DataIdentifierListTextFactory<SummaryObject.Company> {

    override fun getTitle(item: SummaryObject.Company): StatusText {
        return StatusText(if (item.name.isSemanticallyNull()) resources.getString(R.string.company) else item.name!!)
    }

    override fun getDescription(item: SummaryObject.Company, default: StatusText): StatusText {
        if (item.name.isSemanticallyNull() || item.jobTitle.isSemanticallyNull()) {
            return default
        }
        return StatusText(item.jobTitle!!)
    }
}
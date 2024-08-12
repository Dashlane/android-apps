package com.dashlane.vault.textfactory.list

import android.content.res.Resources
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import javax.inject.Inject

class PersonalWebsiteListTextFactory @Inject constructor(
    private val resources: Resources,
) : DataIdentifierListTextFactory<SummaryObject.PersonalWebsite> {

    override fun getTitle(item: SummaryObject.PersonalWebsite): StatusText {
        return StatusText(
            if (item.name.isNotSemanticallyNull()) {
                item.name!!
            } else {
                resources.getString(R.string.personal_website)
            }
        )
    }

    override fun getDescription(item: SummaryObject.PersonalWebsite, default: StatusText): StatusText {
        if (item.name.isSemanticallyNull() || item.website.isSemanticallyNull()) {
            return default
        }
        return StatusText(item.website!!)
    }
}
package com.dashlane.vault.textfactory.list

import android.content.res.Resources
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import com.dashlane.vault.textfactory.identity.IdentityNameHolderService
import com.dashlane.vault.textfactory.list.utils.getIdentityStatusText
import java.time.Clock
import javax.inject.Inject

class PassportListTextFactory @Inject constructor(
    private val resources: Resources,
    private val identityNameHolderService: IdentityNameHolderService,
    private val clock: Clock,
) : DataIdentifierListTextFactory<SummaryObject.Passport> {

    override fun getTitle(item: SummaryObject.Passport): StatusText {
        val title = identityNameHolderService.getOwner(item)
        return StatusText(if (title.isNotSemanticallyNull()) title else resources.getString(R.string.passport))
    }

    override fun getDescription(item: SummaryObject.Passport, default: StatusText): StatusText =
        item.expireDate?.getIdentityStatusText(
            resources = resources,
            number = item.number,
            default = default,
            clock = clock
        ) ?: default
}
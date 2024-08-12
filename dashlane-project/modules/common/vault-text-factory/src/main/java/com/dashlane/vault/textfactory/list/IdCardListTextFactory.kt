package com.dashlane.vault.textfactory.list

import android.content.res.Resources
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import com.dashlane.vault.textfactory.identity.IdentityNameHolderService
import com.dashlane.vault.textfactory.list.utils.getIdentityStatusText
import java.time.Clock
import javax.inject.Inject

class IdCardListTextFactory @Inject constructor(
    private val resources: Resources,
    private val identityNameHolderService: IdentityNameHolderService,
    private val clock: Clock,
) : DataIdentifierListTextFactory<SummaryObject.IdCard> {

    override fun getTitle(item: SummaryObject.IdCard): StatusText {
        val title = identityNameHolderService.getOwner(item)
        if (title.isSemanticallyNull()) {
            return StatusText(resources.getString(R.string.id_card))
        }
        return StatusText(title)
    }

    override fun getDescription(item: SummaryObject.IdCard, default: StatusText): StatusText =
        item.expireDate?.getIdentityStatusText(
            resources = resources,
            number = item.number,
            default = default,
            clock = clock
        ) ?: default
}
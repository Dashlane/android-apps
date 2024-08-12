package com.dashlane.vault.textfactory.list

import com.dashlane.vault.model.title
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class PasskeyListTextFactory @Inject constructor() : DataIdentifierListTextFactory<SummaryObject.Passkey> {
    override fun getTitle(item: SummaryObject.Passkey): StatusText {
        return StatusText(item.title.orEmpty())
    }

    override fun getDescription(item: SummaryObject.Passkey, default: StatusText): StatusText {
        return StatusText(item.userDisplayName.orEmpty())
    }
}
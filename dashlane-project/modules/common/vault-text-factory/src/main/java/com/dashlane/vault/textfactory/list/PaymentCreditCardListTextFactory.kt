package com.dashlane.vault.textfactory.list

import android.content.res.Resources
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.expireDate
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import com.dashlane.vault.textfactory.list.utils.getIdentityStatusText
import java.time.Clock
import javax.inject.Inject

class PaymentCreditCardListTextFactory @Inject constructor(
    private val resources: Resources,
    private val clock: Clock,
) : DataIdentifierListTextFactory<SummaryObject.PaymentCreditCard> {

    override fun getTitle(item: SummaryObject.PaymentCreditCard): StatusText {
        val name = item.name
        return StatusText(if (name.isNotSemanticallyNull()) name!! else resources.getString(R.string.creditcard))
    }

    override fun getDescription(item: SummaryObject.PaymentCreditCard, default: StatusText): StatusText {
        val incomplete = item.isCardNumberEmpty || item.isSecurityCodeEmpty
        
        val number = if (incomplete) null else item.cardNumberObfuscate
        return item.expireDate?.getIdentityStatusText(resources, number, default, clock) ?: default
    }
}
package com.dashlane.autofill.api.fillresponse.filler

import com.dashlane.autofill.api.fillresponse.DatasetWrapperBuilder
import com.dashlane.autofill.api.model.CreditCardItemToFill
import com.dashlane.autofill.api.model.ItemToFill
import com.dashlane.autofill.api.util.AutofillValueFactory
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.vault.summary.toSummary



internal class CreditCardSyncObjectFiller(autofillValueFactory: AutofillValueFactory) :
    CreditCardFiller(autofillValueFactory) {

    @Suppress("UNCHECKED_CAST")
    override fun fill(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: ItemToFill,
        requireLock: Boolean
    ): Boolean {
        val creditCard = (item as? CreditCardItemToFill)?.primaryItem?.syncObject ?: return false
        val address = item.optional
        val cardNumberFieldFound = setCardNumber(dataSetBuilder, summary, creditCard, requireLock)
        val securityCodeFieldFound = setSecurityCode(dataSetBuilder, summary, creditCard, requireLock)
        val expirationDateFieldFound = setExpirationDate(dataSetBuilder, summary, creditCard.toSummary(), requireLock)
        val postalCodeFieldFound = setPostalCode(dataSetBuilder, summary, address, requireLock)
        return cardNumberFieldFound || securityCodeFieldFound || expirationDateFieldFound || postalCodeFieldFound
    }
}
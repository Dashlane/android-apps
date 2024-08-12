package com.dashlane.item.v3.builders

import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.toCreditCardFormData
import com.dashlane.item.v3.viewmodels.State
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class CreditCardBuilder @Inject constructor(
    private val frozenStateManager: FrozenStateManager,
) : FormData.Builder() {
    override fun build(
        initialSummaryObject: SummaryObject,
        state: State
    ): FormData {
        isCopyActionAllowed = !frozenStateManager.isAccountFrozen
        return (initialSummaryObject as SummaryObject.PaymentCreditCard).toCreditCardFormData(isCopyActionAllowed = isCopyActionAllowed)
    }
}
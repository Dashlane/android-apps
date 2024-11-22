package com.dashlane.item.v3.builders

import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.v3.data.CreditCardFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.toCreditCardFormData
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.item.v3.viewmodels.ItemEditState
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class CreditCardBuilder @Inject constructor(
    private val frozenStateManager: FrozenStateManager,
) : FormData.Builder<CreditCardFormData>() {
    override fun build(
        initialSummaryObject: SummaryObject,
        state: ItemEditState<CreditCardFormData>
    ): Data<CreditCardFormData> {
        isCopyActionAllowed = !frozenStateManager.isAccountFrozen
        return (initialSummaryObject as SummaryObject.PaymentCreditCard).toCreditCardFormData(isCopyActionAllowed = isCopyActionAllowed)
    }
}
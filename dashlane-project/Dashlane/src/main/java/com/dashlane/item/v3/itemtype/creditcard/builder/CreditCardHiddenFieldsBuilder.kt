package com.dashlane.item.v3.itemtype.creditcard.builder

import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.itemtype.creditcard.CreditCardFormData
import com.dashlane.item.v3.util.Field
import javax.inject.Inject

class CreditCardHiddenFieldsBuilder @Inject constructor() {
    operator fun invoke(data: CreditCardFormData, commonData: CommonData): Set<Field> =
        setOf(
            Field.CREDIT_CARD_OWNER_NAME.takeIf { data.cardHolder == null },
            Field.CREDIT_CARD_SECURITY_CODE.takeIf { data.isSecurityCodeEmpty },
            Field.CREDIT_CARD_EXPIRATION_DATE.takeIf {
                data.expirationYear == null || data.expirationMonth == null
            },
            Field.CREDIT_CARD_NOTE.takeIf { data.isNoteEmpty },
            Field.CREDIT_CARD_ITEM_NAME.takeIf { commonData.name == null },
        ).filterNotNull().toSet()
}
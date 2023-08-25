package com.dashlane.storage.userdata.accessor.filter.datatype

import com.dashlane.xml.domain.SyncObjectType

object PaymentDataTypeFilter : DataTypeFilter {
    override val dataTypes = arrayOf(
        SyncObjectType.PAYMENT_CREDIT_CARD,
        SyncObjectType.PAYMENT_PAYPAL,
        SyncObjectType.BANK_STATEMENT
    )
}
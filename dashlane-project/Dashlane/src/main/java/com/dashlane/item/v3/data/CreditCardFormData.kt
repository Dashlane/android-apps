package com.dashlane.item.v3.data

import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.securefile.extensions.attachmentsCount
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

data class CreditCardFormData(
    val lastDigits: String? = null,
    val color: SyncObject.PaymentCreditCard.Color? = null
) : FormData

internal fun SummaryObject.PaymentCreditCard.toCreditCardFormData(
    isCopyActionAllowed: Boolean,
) = Data(
    commonData = CommonData(
        id = this.id,
        name = this.name ?: "",
        isShared = this.isShared,
        isCopyActionAllowed = isCopyActionAllowed,
        created = this.creationDatetime,
        updated = this.userModificationDatetime,
        attachmentCount = attachmentsCount()
    ),
    formData = CreditCardFormData(
        lastDigits = this.cardNumberLastFourDigits,
        color = this.color
    )
)
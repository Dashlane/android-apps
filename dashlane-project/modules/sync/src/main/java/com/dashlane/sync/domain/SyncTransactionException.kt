package com.dashlane.sync.domain

import androidx.annotation.Keep
import com.dashlane.sync.treat.SyncSummaryItem
import com.dashlane.util.SecureException
import com.dashlane.xml.domain.SyncObjectType

@Keep
class SyncTransactionException(
    val transaction: Transaction,
    val syncObjectType: SyncObjectType,
    message: String? = null,
    cause: Throwable? = null
) : SecureException(message, cause) {

    val summary
        get() = SyncSummaryItem(
            transaction.identifier,
            transaction.date,
            syncObjectType
        )
}
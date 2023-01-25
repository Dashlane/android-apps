package com.dashlane.util.clipboard.vault

import com.dashlane.hermes.generated.definitions.Highlight
import com.dashlane.vault.summary.SummaryObject



interface VaultItemCopyListener {
    fun onCopyFromVault(summaryObject: SummaryObject, copyField: CopyField, highlight: Highlight? = null, index: Double? = null, totalCount: Int? = null)
    fun onCopyFromFollowUpNotification(notificationId: String, copyField: CopyField)
}
package com.dashlane.followupnotification.services

import android.content.Context
import com.dashlane.followupnotification.FollowUpNotificationComponent
import com.dashlane.hermes.generated.definitions.Highlight
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyListener
import com.dashlane.vault.summary.SummaryObject
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FollowUpNotificationVaultItemCopyListenerImpl @Inject constructor(
    @ApplicationContext
    private val context: Context
) : VaultItemCopyListener {

    override fun onCopyFromVault(
        summaryObject: SummaryObject,
        copyField: CopyField,
        highlight: Highlight?,
        index: Double?,
        totalCount: Int?
    ) {
        FollowUpNotificationComponent(context)
            .followUpNotificationApiProvider
            .getFollowUpNotificationApi()
            .startFollowUpNotification(summaryObject, copyField)
    }

    override fun onCopyFromFollowUpNotification(notificationId: String, copyField: CopyField) {
        FollowUpNotificationComponent(context)
            .followUpNotificationApiProvider
            .getFollowUpNotificationApi()
            .refreshExistingFollowUpNotification(notificationId)
    }
}
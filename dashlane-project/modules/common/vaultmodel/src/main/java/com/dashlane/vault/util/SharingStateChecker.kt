package com.dashlane.vault.util

import com.dashlane.sharing.UserPermission
import com.dashlane.vault.summary.SummaryObject

object SharingStateChecker {
    fun hasLimitedSharingRights(summaryObject: SummaryObject) = summaryObject.isShared &&
            UserPermission.LIMITED == summaryObject.sharingPermission
}
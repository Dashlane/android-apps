package com.dashlane.followupnotification.domain

import com.dashlane.followupnotification.api.FollowUpNotificationLockManager
import com.dashlane.followupnotification.services.FollowUpNotificationDiscoveryService
import com.dashlane.followupnotification.services.FollowUpNotificationLogger
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemClipboard
import com.dashlane.util.tryOrNull
import javax.inject.Inject



class CopyFollowUpNotificationToClipboardImpl @Inject constructor(
    private val vaultItemClipboard: VaultItemClipboard,
    private val followUpNotificationLogger: FollowUpNotificationLogger,
    private val discoveryService: FollowUpNotificationDiscoveryService,
    private val lockManager: FollowUpNotificationLockManager
) : CopyFollowUpNotificationToClipboard {

    override fun copy(followUpNotification: FollowUpNotification, copyFieldIndex: Int): Boolean {
        val followUpNotificationField = tryOrNull { followUpNotification.fields[copyFieldIndex] } ?: return false
        val copyField = tryOrNull { CopyField.valueOf(followUpNotificationField.name) } ?: return false

        return if (!followUpNotificationField.content.needsUnlock) {
            if (lockManager.isAccountLocked()) {
                
                copyFromNotification(followUpNotificationField, followUpNotification, copyField)
            } else {
                copyFromVault(followUpNotification, copyField)
            }
        } else {
            
            lockManager.askForUnlockAndExecute {
                copyFromVault(followUpNotification, copyField)
            }
            true
        }
    }

    private fun copyFromNotification(
        followUpNotificationField: FollowUpNotification.Field,
        followUpNotification: FollowUpNotification,
        copyField: CopyField
    ): Boolean {
        val result = vaultItemClipboard.handleCopy(
            notificationId = followUpNotification.id,
            content = followUpNotificationField.content.displayValue,
            copyField = copyField
        )
        return onFieldCopied(result, followUpNotification, copyField)
    }

    private fun copyFromVault(followUpNotification: FollowUpNotification, copyField: CopyField): Boolean {
        val result = vaultItemClipboard.handleCopy(
            followUpNotification.vaultItemId,
            copyField,
            followUpNotification.type.syncObjectType
        )
        return onFieldCopied(result, followUpNotification, copyField)
    }

    private fun onFieldCopied(
        result: Boolean,
        followUpNotification: FollowUpNotification,
        copyField: CopyField
    ): Boolean {
        if (result) {
            onFieldCopied(followUpNotification, copyField)
        }
        return result
    }

    private fun onFieldCopied(
        followUpNotification: FollowUpNotification,
        copyField: CopyField
    ) {
        followUpNotificationLogger.copyFieldFromFollowUp(
            followUpNotification.type,
            copyField,
            followUpNotification.vaultItemId,
            followUpNotification.isItemProtected,
            followUpNotification.itemDomain
        )
        discoveryService.updateLastNotificationItem(
            followUpNotification.vaultItemId, followUpNotification.id, true
        )
    }
}
package com.dashlane.followupnotification.api

import com.dashlane.followupnotification.data.FollowUpNotificationRepository
import com.dashlane.followupnotification.domain.CopyFollowUpNotificationToClipboard
import com.dashlane.followupnotification.domain.CreateFollowUpNotification
import com.dashlane.followupnotification.domain.FollowUpNotification
import com.dashlane.followupnotification.services.FollowUpAutoRemovalService
import com.dashlane.followupnotification.services.FollowUpNotificationDisplayService
import com.dashlane.followupnotification.services.FollowUpNotificationLogger
import com.dashlane.logger.Log
import com.dashlane.logger.v
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject



class FollowUpNotificationApiImpl @Inject constructor(
    private val createFollowUpNotification: CreateFollowUpNotification,
    private val copyFollowUpNotificationToClipboard: CopyFollowUpNotificationToClipboard,
    private val followUpNotificationRepository: FollowUpNotificationRepository,
    private val followUpNotificationDisplayService: FollowUpNotificationDisplayService,
    private val followUpNotificationLogger: FollowUpNotificationLogger,
    private val followUpAutoRemovalService: FollowUpAutoRemovalService
) : FollowUpNotificationApi {

    override fun startFollowUpNotification(summaryObject: SummaryObject, copyField: CopyField?) {
        try {
            createFollowUpNotification.createFollowUpNotification(summaryObject, copyField)?.let {
                val newFollowUpNotification = followUpNotificationRepository.notificationsCount() == 0
                if (newFollowUpNotification) {
                    followUpNotificationLogger.showFollowUp(it.type, copyField)
                } else {
                    followUpNotificationRepository.removeAll()
                }
                startFollowUpNotification(it)
            }
        } catch (e: Exception) {
            Log.v(e)
        }
    }

    override fun refreshExistingFollowUpNotification(followUpNotificationId: String) {
        val currentNotification = followUpNotificationRepository.get(followUpNotificationId) ?: return
        createFollowUpNotification.refreshExistingNotification(currentNotification)
        startFollowUpNotification(currentNotification)
    }

    override fun dismissFollowUpNotifications(followUpNotificationId: String, autoDismiss: Boolean) {
        if (autoDismiss) {
            followUpNotificationDisplayService.dismiss(followUpNotificationId)
        } else {
            followUpNotificationRepository.get(followUpNotificationId)?.let {
                followUpNotificationLogger.dismissFollowUp(it.type)
            }
        }
        followUpNotificationRepository.remove(followUpNotificationId)
    }

    override fun copyToClipboard(followUpNotificationId: String, copyFieldIndex: Int) {
        val followUpNotification = followUpNotificationRepository.get(followUpNotificationId) ?: return
        val validCopyFieldIndex = copyFieldIndex.takeIf {
            it > -1 && it < followUpNotification.fields.size
        } ?: return
        val copiedToClipboard = copyFollowUpNotificationToClipboard.copy(followUpNotification, validCopyFieldIndex)

        if (!copiedToClipboard) {
            followUpNotificationRepository.removeAll()
        }
    }

    private fun startFollowUpNotification(it: FollowUpNotification) {
        followUpNotificationRepository.add(it)
        followUpNotificationDisplayService.displayNotification(it)
        followUpAutoRemovalService.registerToRemove(it.id)
    }
}
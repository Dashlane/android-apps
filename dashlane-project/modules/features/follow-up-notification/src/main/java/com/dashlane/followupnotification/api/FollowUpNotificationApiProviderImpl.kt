package com.dashlane.followupnotification.api

import com.dashlane.followupnotification.domain.FollowUpNotificationSettings
import com.dashlane.followupnotification.services.FollowUpNotificationFlags
import javax.inject.Inject
import javax.inject.Named

class FollowUpNotificationApiProviderImpl @Inject constructor(
    private val followUpNotificationFlags: FollowUpNotificationFlags,
    private val followUpNotificationSettings: FollowUpNotificationSettings,
    @Named("activeFollowUp") private val activeFollowUpNotificationApi: FollowUpNotificationApi,
    @Named("noOperationFollowUp") private val noOperationFollowUpNotificationApi: FollowUpNotificationApi
) : FollowUpNotificationApiProvider {

    override fun getFollowUpNotificationApi(): FollowUpNotificationApi {
        return if (followUpNotificationFlags.canUseFollowUpNotification() && isActive()) {
            activeFollowUpNotificationApi
        } else {
            noOperationFollowUpNotificationApi
        }
    }

    private fun isActive() = followUpNotificationSettings.isActive()
}
package com.dashlane.notificationcenter

import com.dashlane.notificationcenter.view.ActionItemSection
import com.dashlane.notificationcenter.view.NotificationItem
import java.time.Instant



interface NotificationCenterRepository {
    

    fun markDismissed(item: NotificationItem, dismissed: Boolean)

    

    fun markAsRead(item: NotificationItem)

    

    fun isRead(item: NotificationItem): Boolean

    

    fun getOrInitCreationDate(item: NotificationItem): Instant

    

    suspend fun loadAll(): List<NotificationItem>

    

    suspend fun load(section: ActionItemSection, limit: Int? = null): List<NotificationItem>

    

    suspend fun hasAtLeastOneUnRead(): Boolean
}
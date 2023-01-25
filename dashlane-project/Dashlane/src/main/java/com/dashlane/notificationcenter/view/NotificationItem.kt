package com.dashlane.notificationcenter.view

import com.dashlane.notificationcenter.NotificationCenterDef
import com.dashlane.notificationcenter.NotificationCenterRepository
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter



interface NotificationItem : DashlaneRecyclerAdapter.MultiColumnViewTypeProvider {
    val actionItemsRepository: NotificationCenterRepository
    val type: ActionItemType
    val section: ActionItemSection
    val trackingKey: String
    val action: NotificationCenterDef.Presenter.() -> Unit
}
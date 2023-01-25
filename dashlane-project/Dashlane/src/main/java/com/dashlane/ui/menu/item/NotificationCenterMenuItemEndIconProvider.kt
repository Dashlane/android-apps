package com.dashlane.ui.menu.item

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.notification.badge.NotificationBadgeActor

class NotificationCenterMenuItemEndIconProvider(
    notificationBadgeActorLazy: Lazy<NotificationBadgeActor>
) : MenuItemEndIconProvider {
    private val notificationBadgeActor by notificationBadgeActorLazy

    constructor() : this(lazy { SingletonProvider.getNotificationBadgeActor() })

    override fun getEndIcon(context: Context): Drawable? =
        if (notificationBadgeActor.hasUnReadActionItems) {
            AppCompatResources.getDrawable(context, R.drawable.badge_menu_item_notif)
        } else {
            null
        }

    override fun getEndIconDescription(context: Context): String? =
        context.getString(R.string.and_accessibility_notification)
}
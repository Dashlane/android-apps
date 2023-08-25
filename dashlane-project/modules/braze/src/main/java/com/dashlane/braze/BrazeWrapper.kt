package com.dashlane.braze

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import com.braze.Braze
import com.braze.Constants
import com.braze.IBrazeNotificationFactory
import com.braze.events.IEventSubscriber
import com.braze.events.InAppMessageEvent
import com.braze.models.push.BrazeNotificationPayload
import com.braze.push.BrazeNotificationFactory
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import com.dashlane.util.notification.NotificationHelper

class BrazeWrapper(
    private val braze: Braze,
    private val inAppManager: BrazeInAppMessageManager,
    inAppMessageSubscriber: BrazeInAppMessageSubscriber
) {
    private var userId: String? = null

    init {
        
        subscribeToInAppMessageUpdates(inAppMessageSubscriber)
    }

    fun requestImmediateDataFlush() {
        braze.requestImmediateDataFlush()
    }

    fun openSession(activity: Activity) {
        braze.openSession(activity)
        inAppManager.requestDisplayInAppMessage()
    }

    fun setUserId(userId: String?) {
        if (userId == null) {
            return 
        }
        if (userId != this.userId) {
            
            this.userId = userId
            braze.changeUser(userId)
        }
    }

    fun configureBrazeNotificationFactory() {
        Braze.customBrazeNotificationFactory = NotificationFactory
    }

    private fun subscribeToInAppMessageUpdates(listener: IEventSubscriber<InAppMessageEvent>) {
        braze.apply {
            subscribeToNewInAppMessages(listener)
        }
    }

    private object NotificationFactory : IBrazeNotificationFactory {

        override fun createNotification(payload: BrazeNotificationPayload): Notification? {
            val notificationExtras = payload.notificationExtras

            
            val channelExists =
                notificationExtras.getString(Constants.BRAZE_PUSH_NOTIFICATION_CHANNEL_ID_KEY)?.let { channelId ->
                    val notificationManager =
                        payload.context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

                    notificationManager?.getNotificationChannel(channelId)
                } != null

            if (!channelExists) {
                notificationExtras.putString(
                    Constants.BRAZE_PUSH_NOTIFICATION_CHANNEL_ID_KEY,
                    defaultChannel.id
                )
            }

            return BrazeNotificationFactory.populateNotificationBuilder(
                payload = BrazeNotificationPayload(
                    context = payload.context,
                    configurationProvider = payload.configurationProvider,
                    notificationExtras = payload.notificationExtras,
                    brazeExtras = payload.brazeExtras
                )
            )?.build()
        }
    }

    companion object {
        private val defaultChannel: NotificationHelper.Channel = NotificationHelper.Channel.MARKETING
    }
}

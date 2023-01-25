package com.dashlane.util.notification

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dashlane.useractivity.log.install.InstallLogCode30
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.util.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton



@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val installLogRepository: InstallLogRepository,
    private val notificationManager: NotificationManager?,
    private val activityManagerProvider: Provider<ActivityManager?>
) {

    enum class Channel(
        val id: String,
        val importance: Int,
        val priority: Int,
        val title: Int
    ) {
        SECURITY(
            SECURITY_CHANNEL,
            NotificationManagerCompat.IMPORTANCE_HIGH,
            NotificationCompat.PRIORITY_HIGH,
            R.string.notification_channel_security_title
        ),
        TOKEN(
            TOKEN_CHANNEL,
            NotificationManagerCompat.IMPORTANCE_HIGH,
            NotificationCompat.PRIORITY_HIGH,
            R.string.notification_channel_token_title
        ),
        VPN(
            VPN_CHANNEL,
            NotificationManagerCompat.IMPORTANCE_HIGH,
            NotificationCompat.PRIORITY_HIGH,
            R.string.notification_channel_vpn_title
        ),
        PASSIVE(
            PASSIVE_CHANNEL,
            NotificationManagerCompat.IMPORTANCE_LOW,
            NotificationCompat.PRIORITY_LOW,
            R.string.notification_channel_passive_title
        ),
        MARKETING(
            MARKETING_CHANNEL,
            NotificationManagerCompat.IMPORTANCE_DEFAULT,
            NotificationCompat.PRIORITY_DEFAULT,
            R.string.notification_channel_marketing_title
        ),
        OTP(
            OTP_CHANNEL,
            NotificationManagerCompat.IMPORTANCE_HIGH,
            NotificationCompat.PRIORITY_HIGH,
            R.string.notification_channel_otp_title
        ),
        FOLLOW_UP_NOTIFICATION(
            FOLLOW_UP_NOTIFICATION_HIGH_CHANNEL,
            NotificationManagerCompat.IMPORTANCE_HIGH,
            NotificationCompat.PRIORITY_HIGH,
            R.string.notification_channel_follow_up_notification_title
        )
    }

    companion object {
        
        const val DEFAULT_CHANNEL = "default"
        private const val HIGH_PRIORITY_CHANNEL = "priority"
        private const val LOW_PRIORITY_CHANNEL = "lowPriority"
        private val LEGACY_CHANNELS = arrayOf(DEFAULT_CHANNEL, HIGH_PRIORITY_CHANNEL, LOW_PRIORITY_CHANNEL)

        
        private const val SECURITY_CHANNEL = "security_channel"
        private const val TOKEN_CHANNEL = "token_channel"
        private const val VPN_CHANNEL = "vpn_channel"
        private const val PASSIVE_CHANNEL = "passive_channel"
        private const val MARKETING_CHANNEL = "marketing_channel"
        private const val OTP_CHANNEL = "otp_channel"
        private const val FOLLOW_UP_NOTIFICATION_CHANNEL = "follow_up_notification_channel"
        private const val FOLLOW_UP_NOTIFICATION_HIGH_CHANNEL = "follow_up_notification_high_channel"
    }

    private val activityManager: ActivityManager?
        get() = activityManagerProvider.get()

    fun initChannels() {
        notificationManager ?: return

        LEGACY_CHANNELS.forEach {
            notificationManager.deleteNotificationChannel(it)
        }

        notificationManager.apply {
            createNotificationChannel(Channel.SECURITY.toNotificationChannel(context))
            createNotificationChannel(Channel.TOKEN.toNotificationChannel(context))
            createNotificationChannel(Channel.VPN.toNotificationChannel(context))
            createNotificationChannel(Channel.PASSIVE.toNotificationChannel(context))
            createNotificationChannel(Channel.MARKETING.toNotificationChannel(context))
            createNotificationChannel(Channel.OTP.toNotificationChannel(context))
            createNotificationChannel(Channel.FOLLOW_UP_NOTIFICATION.toNotificationChannel(context))

            
            
            deleteNotificationChannel(FOLLOW_UP_NOTIFICATION_CHANNEL)
        }

        logConfiguration()
    }

    private fun logConfiguration() {
        notificationManager ?: return

        
        val isBackgroundRestricted =
            if (Build.VERSION.SDK_INT >= 28) {
                activityManager?.isBackgroundRestricted
                    ?: false
            } else {
                false
            }

        val log = InstallLogCode30(
            isNotificationEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled(),
            isBackgroundRestricted = isBackgroundRestricted
        ).copy(
            notificationChannelImportanceToken = getNotificationImportance(Channel.TOKEN),
            notificationChannelImportanceSecurity = getNotificationImportance(Channel.SECURITY),
            notificationChannelImportanceMarketing = getNotificationImportance(Channel.MARKETING),
            notificationChannelImportanceVpn = getNotificationImportance(Channel.VPN),
            notificationChannelImportancePassive = getNotificationImportance(Channel.PASSIVE)
        )
        installLogRepository.enqueue(log)
    }

    private fun getNotificationImportance(channel: Channel): Int =
        notificationManager?.getNotificationChannel(channel.id)?.importance ?: channel.importance

    private fun Channel.toNotificationChannel(context: Context) =
        NotificationChannel(id, context.getString(title), importance)
}

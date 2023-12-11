package com.dashlane.dagger.singleton

import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.DownloadManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.hardware.usb.UsbManager
import android.net.ConnectivityManager
import android.os.PowerManager
import android.telephony.TelephonyManager
import android.view.accessibility.AccessibilityManager
import android.view.inputmethod.InputMethodManager
import androidx.biometric.BiometricManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.WorkManager
import com.dashlane.util.notification.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ApplicationContextModule {

    @Provides
    fun provideAssetsManager(@ApplicationContext context: Context): AssetManager =
        context.assets

    @Provides
    fun provideResources(@ApplicationContext context: Context): Resources =
        context.resources

    @Provides
    fun providePackageManager(@ApplicationContext context: Context): PackageManager =
        context.packageManager

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver =
        context.contentResolver

    @Provides
    fun provideLocalBroadcastManager(@ApplicationContext context: Context): LocalBroadcastManager =
        LocalBroadcastManager.getInstance(context)

    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

    @Provides
    fun providerAccessibilityManager(@ApplicationContext context: Context): AccessibilityManager? =
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?

    @Provides
    fun provideActivityManager(@ApplicationContext context: Context): ActivityManager? =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?

    @Provides
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager? =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

    @Provides
    fun provideNotificationHelper(
        @ApplicationContext context: Context,
        notificationManager: NotificationManager?
    ): NotificationHelper = NotificationHelper(context, notificationManager)

    @Provides
    fun provideClipboardManager(@ApplicationContext context: Context): ClipboardManager? =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?

    @Provides
    fun provideBiometricManager(@ApplicationContext context: Context): BiometricManager =
        BiometricManager.from(context)

    @Provides
    fun provideTelephonyManager(@ApplicationContext context: Context): TelephonyManager? =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?

    @Provides
    fun providePowerManager(@ApplicationContext context: Context): PowerManager? =
        context.getSystemService(AccessibilityService.POWER_SERVICE) as PowerManager?

    @Provides
    fun provideInputMethodManager(@ApplicationContext context: Context): InputMethodManager? =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?

    @Provides
    fun provideDownloadManager(@ApplicationContext context: Context): DownloadManager? =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?

    @Provides
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager? =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

    @Provides
    fun provideKeyguardManager(@ApplicationContext context: Context): KeyguardManager? =
        context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?

    @Provides
    fun provideUsbManager(@ApplicationContext context: Context): UsbManager? =
        context.getSystemService(Context.USB_SERVICE) as UsbManager?
}
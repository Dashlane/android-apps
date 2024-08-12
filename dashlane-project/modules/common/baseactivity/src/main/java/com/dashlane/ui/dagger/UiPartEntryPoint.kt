package com.dashlane.ui.dagger

import com.dashlane.lock.LockHelper
import com.dashlane.permission.PermissionsManager
import com.dashlane.ui.ActivityLifecycleListener
import com.dashlane.ui.ScreenshotPolicy
import dagger.hilt.InstallIn
import dagger.hilt.android.EarlyEntryPoint
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@EarlyEntryPoint
interface UiPartEntryPoint {
    val screenshotPolicy: ScreenshotPolicy
    val lockHelper: LockHelper
    val permissionsManager: PermissionsManager
    val activityLifecycleListener: ActivityLifecycleListener
}
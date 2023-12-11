package com.dashlane.ui.component

import com.dashlane.lock.LockHelper
import com.dashlane.navigation.Navigator
import com.dashlane.permission.PermissionsManager
import com.dashlane.ui.ActivityLifecycleListener
import com.dashlane.ui.ScreenshotPolicy
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@EntryPoint
interface UiPartEntryPoint {
    val screenshotPolicy: ScreenshotPolicy
    val lockHelper: LockHelper
    val navigator: Navigator
    val permissionsManager: PermissionsManager
    val activityLifecycleListener: ActivityLifecycleListener
}
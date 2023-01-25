package com.dashlane.ui.component

import android.content.Context
import com.dashlane.lock.LockHelper
import com.dashlane.navigation.Navigator
import com.dashlane.permission.PermissionsManager
import com.dashlane.ui.ActivityLifecycleListener
import com.dashlane.ui.ScreenshotPolicy



interface UiPartComponent {

    val screenshotPolicy: ScreenshotPolicy
    val lockHelper: LockHelper
    val navigator: Navigator
    val permissionsManager: PermissionsManager
    val activityLifecycleListener: ActivityLifecycleListener

    companion object {
        operator fun invoke(context: Context) =
            (context.applicationContext as UiPartApplication).component
    }
}
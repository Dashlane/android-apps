package com.dashlane.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dashlane.lock.LockHelper
import com.dashlane.navigation.Navigator
import com.dashlane.ui.ActivityLifecycleListener
import com.dashlane.ui.applyScreenshotAllowedFlag
import com.dashlane.ui.component.UiPartComponent
import com.dashlane.ui.disableAutoFill
import com.dashlane.ui.util.ActionBarUtil
import com.dashlane.util.CurrentPageViewLogger
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext



abstract class DashlaneActivity : AppCompatActivity(), CoroutineScope, CurrentPageViewLogger.Owner {

    override val coroutineContext: CoroutineContext
        get() = lifecycleScope.coroutineContext

    

    open var requireUserUnlock: Boolean = true

    open val applicationLocked: Boolean
        get() = lockHelper.isLocked

    open val applicationLockedOrLogout: Boolean
        get() = lockHelper.isLockedOrLogout()

    lateinit var uiPartComponent: UiPartComponent
        private set

    val navigator: Navigator
        get() = uiPartComponent.navigator

    val lockHelper: LockHelper
        get() = uiPartComponent.lockHelper

    private val lifecycleListener: ActivityLifecycleListener
        get() = uiPartComponent.activityLifecycleListener

    private var wasLock = false

    val actionBarUtil: ActionBarUtil by lazy { ActionBarUtil(this) }

    override val currentPageViewLogger by lazy { CurrentPageViewLogger(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        uiPartComponent = UiPartComponent.invoke(this)
        window?.applyScreenshotAllowedFlag(uiPartComponent.screenshotPolicy)
        disableAutoFill()
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        verifyApplicationUnlock()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        lifecycleListener.onActivityUserInteraction(this)
    }

    @Suppress("DEPRECATION")
    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        lifecycleListener.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        uiPartComponent.permissionsManager.onRequestPermissionResult(this, permissions, requestCode, grantResults)
    }

    

    open fun onApplicationUnlocked() {
        
    }

    private fun verifyApplicationUnlock() {
        val applicationLockedNow = applicationLocked
        if (wasLock != applicationLockedNow) {
            wasLock = applicationLockedNow
            if (!applicationLockedNow) {
                onApplicationUnlocked()
            }
        }
    }
}
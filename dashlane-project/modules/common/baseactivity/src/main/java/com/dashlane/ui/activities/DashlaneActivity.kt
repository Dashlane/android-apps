package com.dashlane.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.dashlane.lock.LockHelper
import com.dashlane.permission.PermissionsManager
import com.dashlane.ui.ActivityLifecycleListener
import com.dashlane.ui.ScreenshotPolicy
import com.dashlane.ui.applyScreenshotAllowedFlag
import com.dashlane.ui.dagger.UiPartEntryPoint
import com.dashlane.ui.disableAutoFill
import com.dashlane.ui.fragments.BaseDialogFragment
import com.dashlane.ui.util.ActionBarUtil
import com.dashlane.util.CurrentPageViewLogger
import dagger.hilt.android.EarlyEntryPoints
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

    val lockHelper: LockHelper
        get() = uiPartEntryPoint.lockHelper

    val permissionsManager: PermissionsManager
        get() = uiPartEntryPoint.permissionsManager

    val screenshotPolicy: ScreenshotPolicy
        get() = uiPartEntryPoint.screenshotPolicy

    private val lifecycleListener: ActivityLifecycleListener
        get() = uiPartEntryPoint.activityLifecycleListener

    private val uiPartEntryPoint: UiPartEntryPoint
        get() = EarlyEntryPoints.get(
            this.application,
            UiPartEntryPoint::class.java
        )
    private var wasLock = false

    val actionBarUtil: ActionBarUtil by lazy { ActionBarUtil(this) }

    override val currentPageViewLogger by lazy { CurrentPageViewLogger(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.applyScreenshotAllowedFlag(screenshotPolicy)
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
        permissionsManager.onRequestPermissionResult(this, permissions, requestCode, grantResults)
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

fun FragmentActivity.hideDialogs() {
    
    val fragments = supportFragmentManager.fragments
    for (fragment in fragments) {
        if (fragment is BaseDialogFragment && fragment.showsDialog) {
            fragment.dismiss()
        }
    }
}
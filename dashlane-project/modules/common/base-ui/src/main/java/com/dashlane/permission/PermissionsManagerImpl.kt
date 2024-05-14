package com.dashlane.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.SparseArray
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import com.dashlane.lock.LockHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionsManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lockHelper: LockHelper
) : PermissionsManager {
    private val handlers = SparseArray<PermissionsManager.OnPermissionResponseHandler?>()

    override fun requestPermission(
        activity: Activity,
        permissionCode: Int,
        handler: PermissionsManager.OnPermissionResponseHandler?,
        permission: String
    ) {
        handlers.put(permissionCode, handler)
        if (checkSelfPermission(activity, permission) != PermissionChecker.PERMISSION_GRANTED) {
            lockHelper.startAutoLockGracePeriod(LOCK_GRACE_PERIOD)
            requestPermissions(activity, arrayOf(permission), permissionCode)
        }
    }

    private fun getHandlerForRequestCode(code: Int): PermissionsManager.OnPermissionResponseHandler? {
        val handler = handlers[code]
        handlers.remove(code)
        return handler
    }

    override fun isAllowedToWriteToPublicFolder(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                isAllowed(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun isAllowed(permission: String): Boolean {
        return checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED
    }

    override fun onRequestPermissionResult(
        activity: Activity,
        permissions: Array<out String>,
        requestCode: Int,
        grantResults: IntArray
    ) {
        val handler = getHandlerForRequestCode(requestCode)
        if (handler != null) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handler.onApproval()
            } else {
                if (permissions.isNotEmpty() && shouldShowRequestPermissionRationale(activity, permissions[0])) {
                    handler.onDisapproval()
                } else {
                    
                    handler.onAlwaysDisapproved()
                }
            }
        }
    }

    companion object {
        private val LOCK_GRACE_PERIOD = Duration.ofSeconds(10)
    }
}
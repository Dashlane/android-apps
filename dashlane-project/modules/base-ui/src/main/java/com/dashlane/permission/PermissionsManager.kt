package com.dashlane.permission

import android.Manifest
import android.app.Activity

interface PermissionsManager {
    fun requestPermission(
        activity: Activity,
        permissionCode: Int,
        handler: OnPermissionResponseHandler?,
        permission: String
    )

    fun isAllowedToWriteToPublicFolder(): Boolean

    fun isAllowed(permission: String): Boolean

    fun onRequestPermissionResult(
        activity: Activity,
        permissions: Array<out String>,
        requestCode: Int,
        grantResults: IntArray
    )

    interface OnPermissionResponseHandler {
        fun onApproval()
        fun onAlwaysDisapproved()
        fun onDisapproval()
    }

    companion object {
        const val PERMISSION_CONTACTS = 0
        const val PERMISSION_CAMERA = 1
        const val PERMISSION_SDCARD = 3
        const val PERMISSION_NOTIFICATION = 4
    }
}
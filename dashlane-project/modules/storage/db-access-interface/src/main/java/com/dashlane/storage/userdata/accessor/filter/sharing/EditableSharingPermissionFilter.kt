package com.dashlane.storage.userdata.accessor.filter.sharing

import android.annotation.SuppressLint
import com.dashlane.sharing.UserPermission

interface EditableSharingPermissionFilter : SharingFilter {

    var sharingFilter: SharingFilter

    fun noSharingFilter() {
        sharingFilter = NoSharingFilter
    }

    fun onlyNotShared() {
        specificSharingPermission(arrayOf(UserPermission.UNDEFINED))
    }

    @SuppressLint("WrongConstant") 
    fun onlyShared() {
        specificSharingPermission(arrayOf(UserPermission.ADMIN, UserPermission.LIMITED))
    }

    @SuppressLint("WrongConstant") 
    fun onlyShareable() {
        specificSharingPermission(arrayOf(UserPermission.UNDEFINED, UserPermission.ADMIN))
    }

    fun specificSharingPermission(@UserPermission permission: Array<String>?) {
        @SuppressLint("WrongConstant") 
        sharingFilter = SpecificSharingFilter(sharingPermissions = permission)
    }
}
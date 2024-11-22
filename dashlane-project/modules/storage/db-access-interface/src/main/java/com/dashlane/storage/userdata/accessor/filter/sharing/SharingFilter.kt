package com.dashlane.storage.userdata.accessor.filter.sharing

import com.dashlane.sharing.UserPermission

interface SharingFilter {

    @UserPermission
    val sharingPermissions: Array<out String>?
}
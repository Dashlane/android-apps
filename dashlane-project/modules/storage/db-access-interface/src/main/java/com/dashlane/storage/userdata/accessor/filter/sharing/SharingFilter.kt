package com.dashlane.storage.userdata.accessor.filter.sharing

import com.dashlane.util.model.UserPermission

interface SharingFilter {

    @UserPermission
    val sharingPermissions: Array<out String>?
}
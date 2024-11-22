package com.dashlane.storage.userdata.accessor.filter.sharing

import com.dashlane.sharing.UserPermission

class SpecificSharingFilter(
    @UserPermission override val sharingPermissions: Array<String>?
) : SharingFilter
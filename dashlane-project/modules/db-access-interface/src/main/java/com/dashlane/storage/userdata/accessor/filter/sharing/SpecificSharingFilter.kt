package com.dashlane.storage.userdata.accessor.filter.sharing

import com.dashlane.util.model.UserPermission

class SpecificSharingFilter(
    @UserPermission override val sharingPermissions: Array<String>?
) : SharingFilter
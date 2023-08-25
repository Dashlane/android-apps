package com.dashlane.core.domain.sharing

import com.dashlane.R
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission

enum class SharingPermission(val stringResource: Int) {
    ADMIN(R.string.enum_sharing_permission_admin),
    LIMITED(R.string.enum_sharing_permission_limited),
    REVOKED(R.string.enum_sharing_permission_revoked);
}

fun SharingPermission.toUserPermission(): Permission = when (this) {
    SharingPermission.ADMIN -> Permission.ADMIN
    else -> Permission.LIMITED
}

fun Permission.toSharingPermission(): SharingPermission = when (this) {
    Permission.ADMIN -> SharingPermission.ADMIN
    else -> SharingPermission.LIMITED
}
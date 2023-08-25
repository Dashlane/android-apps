package com.dashlane.ui.screens.fragments.userdata.sharing

import com.dashlane.R
import com.dashlane.core.domain.sharing.SharingPermission
import com.dashlane.server.api.endpoints.sharinguserdevice.GetUsersPublicKeyService
import com.dashlane.server.api.endpoints.sharinguserdevice.UserDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroupMember
import com.dashlane.sharing.internal.model.UserToUpdate
import com.dashlane.sharing.model.isAccepted
import com.dashlane.sharing.model.isAdmin
import com.dashlane.sharing.model.isPending

fun UserDownload.getSharingStatusResource(): Int {
    return if (isPending) {
        R.string.sharing_status_pending
    } else if (isAccepted) {
        if (isAdmin) {
            SharingPermission.ADMIN.stringResource
        } else {
            SharingPermission.LIMITED.stringResource
        }
    } else {
        0
    }
}

fun UserDownload.getSharingStatusResourceShort(): Int {
    return if (isPending) {
        R.string.sharing_status_pending_short
    } else if (isAccepted) {
        if (isAdmin) {
            SharingPermission.ADMIN.stringResource
        } else {
            SharingPermission.LIMITED.stringResource
        }
    } else {
        0
    }
}

fun UserGroupMember.getSharingStatusResource(): Int {
    return if (isPending) {
        R.string.sharing_status_pending
    } else if (isAccepted) {
        if (isAdmin) {
            SharingPermission.ADMIN.stringResource
        } else {
            SharingPermission.LIMITED.stringResource
        }
    } else {
        0
    }
}

fun List<UserDownload>.getUsersToUpdate(
    users: List<GetUsersPublicKeyService.Data.Data>
) = mapNotNull { userDownload ->
    users.find { it.publicKey != null && it.login != null && it.login == userDownload.userId }
        ?.let {
            UserToUpdate(
                userId = it.login!!,
                permission = userDownload.permission,
                publicKey = it.publicKey!!
            )
        }
}
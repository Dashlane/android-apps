package com.dashlane.core.sharing

import com.dashlane.server.api.Response
import com.dashlane.sharing.exception.SharingResponseException

@Throws(SharingResponseException::class)
suspend fun SharingItemUpdater.handleServerResponse(
    response: Response<com.dashlane.server.api.endpoints.sharinguserdevice.SharingServerResponse>,
    onConflict: suspend () -> Unit = {}
) {
    onConflict.toString()
    val content = response.data
    val itemGroup = content.itemGroups?.firstOrNull()
    val userGroup = content.userGroups?.firstOrNull()
    if (itemGroup == null && userGroup == null) {
        throw SharingResponseException("The response doesn't contain one itemGroup or userGroup")
    }
    itemGroup?.also {
        update(SharingItemUpdaterRequest.toSaveItemGroup(it, null))
    }
    userGroup?.also {
        update(SharingItemUpdaterRequest.toSaveUserGroup(it))
    }
}

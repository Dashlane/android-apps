package com.dashlane.core.sharing

import com.dashlane.server.api.Response
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.SharingServerResponse
import com.dashlane.sharing.exception.SharingResponseException

@Throws(SharingResponseException::class)
suspend fun SharingItemUpdater.handleServerResponse(
    response: Response<SharingServerResponse>,
    onConflict: suspend () -> Unit = {}
) {
    onConflict.toString()
    val content = response.data
    val itemGroup = content.itemGroups?.firstOrNull()
    val userGroup = content.userGroups?.firstOrNull()
    if (itemGroup == null && userGroup == null) {
        throw SharingResponseException("The response doesn't contain one itemGroup or userGroup")
    }
    update(
        SharingItemUpdaterRequest(
            itemGroupUpdates = listOfNotNull(itemGroup),
            userGroupUpdates = listOfNotNull(userGroup)
        )
    )
}

suspend fun SharingItemUpdater.handleCollectionSharingResult(
    collections: List<Collection>,
    updatedItemGroups: List<ItemGroup>? = null
) = update(
    SharingItemUpdaterRequest(
        itemGroupUpdates = updatedItemGroups ?: emptyList(),
        collectionUpdates = collections
    )
)
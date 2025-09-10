package com.dashlane.core.sharing

import com.dashlane.server.api.Response
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.SharingServerResponse
import com.dashlane.sharing.SharingItemUpdaterRequest
import com.dashlane.sharing.exception.SharingResponseException

@Throws(SharingResponseException::class)
suspend fun SharingItemUpdater.handleServerResponse(
    response: Response<SharingServerResponse>,
) {
    val content = response.data
    val itemGroup = content.itemGroups?.firstOrNull()
    val userGroup = content.userGroups?.firstOrNull()
    val itemContent = content.items?.firstOrNull()
    if (itemGroup == null && userGroup == null) {
        throw SharingResponseException("The response doesn't contain one itemGroup or userGroup")
    }
    update(
        SharingItemUpdaterRequest(
            itemGroupUpdates = listOfNotNull(itemGroup),
            userGroupUpdates = listOfNotNull(userGroup),
            itemContentUpdates = listOfNotNull(itemContent),
        ),
    )
    val errors = (content.itemGroupErrors?.map { it.message } ?: emptyList()) +
        (content.userGroupErrors?.map { it.message } ?: emptyList()) +
        (content.itemErrors?.map { it.message } ?: emptyList())

    if (errors.isNotEmpty()) {
        throw SharingResponseException("The response contains ${errors.size} error(s): " + errors.joinToString())
    }
}

suspend fun SharingItemUpdater.handleCollectionSharingResult(
    collections: List<Collection>? = null,
    updatedItemGroups: List<ItemGroup>? = null,
) = update(
    SharingItemUpdaterRequest(
        itemGroupUpdates = updatedItemGroups ?: emptyList(),
        collectionUpdates = collections ?: emptyList(),
    ),
)
package com.dashlane.sync.sharing

import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sync.SyncDownloadService

interface SharingSync {

    suspend fun syncSharing(
        session: Authorization.User,
        sharingSummary: SyncDownloadService.Data.SharingSummary
    )

    data class IdCollection(
        val items: List<String>,
        val itemGroups: List<String>,
        val userGroups: List<String>,
        val collections: List<String>
    ) {
        val isEmpty: Boolean
            get() = items.isEmpty() && itemGroups.isEmpty() && userGroups.isEmpty() && collections.isEmpty()

        fun chunked(): List<IdCollection> {
            val itemGroupsChunked: List<List<String>> =
                itemGroups.chunked(GET_BATCH_SIZE)
            val userGroupsChunked = userGroups.chunked(GET_BATCH_SIZE)
            val collectionsChunked = collections.chunked(GET_BATCH_SIZE)
            val itemsChunked = items.chunked(GET_BATCH_SIZE)
            return (
                0..maxOf(
                    itemGroupsChunked.lastIndex,
                    userGroupsChunked.lastIndex,
                    collectionsChunked.lastIndex,
                    itemsChunked.lastIndex
                )
                ).map { index ->
                    IdCollection(
                        itemGroups = itemGroupsChunked.getOrNull(index) ?: emptyList(),
                        userGroups = userGroupsChunked.getOrNull(index) ?: emptyList(),
                        collections = collectionsChunked.getOrNull(index) ?: emptyList(),
                        items = itemsChunked.getOrNull(index) ?: emptyList()
                    )
                }
        }

        companion object {
            private const val GET_BATCH_SIZE = 100
        }
    }
}

interface SharingComponent {
    val sharingSync: SharingSync
}
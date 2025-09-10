package com.dashlane.database

import com.dashlane.sharing.model.CollectionRaclette
import com.dashlane.sharing.model.ItemContentRaclette
import com.dashlane.sharing.model.ItemGroupRaclette
import com.dashlane.sharing.model.UserGroupRaclette

interface SharingRepository {
    suspend fun transaction(
        timeoutMillis: Long = VaultObjectRepository.DEFAULT_TIMEOUT_MILLIS,
        block: suspend Transaction.() -> Unit,
    )

    suspend fun loadItemGroups(): List<ItemGroupRaclette>

    suspend fun loadUserGroups(): List<UserGroupRaclette>

    suspend fun loadItemContents(): List<ItemContentRaclette>

    suspend fun loadCollections(): List<CollectionRaclette>

    interface Transaction {
        fun deleteItemGroups(ids: Set<String>)

        fun deleteUserGroups(ids: Set<String>)

        fun deleteItemContents(ids: Set<String>)

        fun deleteCollections(ids: Set<String>)

        fun updateItemGroups(itemGroups: List<ItemGroupRaclette>)

        fun updateItemContents(itemContents: List<ItemContentRaclette>)

        fun updateUserGroups(userGroups: List<UserGroupRaclette>)

        fun updateCollections(collections: List<CollectionRaclette>)
    }
}
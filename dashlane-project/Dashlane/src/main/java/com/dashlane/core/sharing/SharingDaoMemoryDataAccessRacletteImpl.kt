package com.dashlane.core.sharing

import com.dashlane.database.SharingRepository
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.sharing.model.ItemContentRaclette
import com.dashlane.sharing.model.toItemGroupRaclettes
import com.dashlane.sharing.model.toItemGroups
import com.dashlane.sharing.model.toUserGroupRaclettes
import com.dashlane.sharing.model.toUserGroups
import com.dashlane.storage.userdata.dao.ItemContentDB

class SharingDaoMemoryDataAccessRacletteImpl(
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository,
    private val delegate: SharingDaoMemoryDataAccessImpl
) : SharingDaoMemoryDataAccess by delegate {

    override val itemGroups: List<ItemGroup>
        get() = delegate.itemGroups
    override val userGroups: List<UserGroup>
        get() = delegate.userGroups
    override val itemContentsDB: List<ItemContentDB>
        get() = delegate.itemContentsDB

    private val sharingRepository: SharingRepository?
        get() {
            val database = sessionManager.session?.let { userDataRepository.getRacletteDatabase(it) }
            return database?.sharingRepository
        }

    override suspend fun init() {
        val sharingRepository = sharingRepository ?: return
        delegate.itemGroups = sharingRepository.loadItemGroups().toItemGroups().toMutableList()
        delegate.userGroups = sharingRepository.loadUserGroups().toUserGroups().toMutableList()
        delegate.itemContentsDB = sharingRepository.loadItemContents().toItemContentDBs().toMutableList()
    }

    override suspend fun close() {
        val sharingRepository = sharingRepository ?: return
        sharingRepository.transaction {
            this.updateItemGroups(delegate.itemGroups.toItemGroupRaclettes())
            this.deleteItemGroups(delegate.itemGroupsToDelete.toSet())
            this.updateUserGroups(delegate.userGroups.toUserGroupRaclettes())
            this.deleteUserGroups(delegate.userGroupsToDelete.toSet())
            this.updateItemContents(delegate.itemContentsDB.toItemContentRaclettes())
            this.deleteItemContents(delegate.itemContentsDBToDelete.toSet())
        }
    }
}

private fun List<ItemContentRaclette>.toItemContentDBs(): List<ItemContentDB> = map { it.toItemContentDB() }
private fun ItemContentRaclette.toItemContentDB() =
    ItemContentDB(
        id = itemId,
        timestamp = timestamp,
        extraData = extraData,
        itemKeyBase64 = itemKeyBase64
    )

private fun List<ItemContentDB>.toItemContentRaclettes(): List<ItemContentRaclette> = map { it.toItemContentRaclette() }
private fun ItemContentDB.toItemContentRaclette() = ItemContentRaclette(
    itemId = id,
    timestamp = timestamp,
    extraData = extraData,
    itemKeyBase64 = itemKeyBase64
)

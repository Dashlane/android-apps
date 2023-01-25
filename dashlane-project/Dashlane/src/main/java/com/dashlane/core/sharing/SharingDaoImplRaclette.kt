package com.dashlane.core.sharing

import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.database.BackupRepository
import com.dashlane.database.Database
import com.dashlane.database.Id
import com.dashlane.database.MemorySummaryRepository
import com.dashlane.database.SharingRepository
import com.dashlane.database.VaultObjectRepository
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.sharing.model.isAcceptedOrPending
import com.dashlane.sharing.model.toItemGroup
import com.dashlane.sharing.model.toItemGroups
import com.dashlane.sharing.model.toUserGroups
import com.dashlane.storage.userdata.DataSyncDaoRaclette
import com.dashlane.storage.userdata.DatabaseItemSaverRaclette
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQueryImplRaclette
import com.dashlane.storage.userdata.dao.SharingDataType
import com.dashlane.sync.DataIdentifierExtraDataWrapper
import com.dashlane.sync.VaultItemBackupWrapper
import com.dashlane.sync.toDataIdentifierExtraDataWrapper
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.manager.getSuggestedTeamspace
import com.dashlane.useractivity.RacletteLogger
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.util.SyncObjectTypeUtils
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.serializer.XmlDeserializer
import com.dashlane.xml.serializer.XmlSerializer
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SharingDaoImplRaclette @Inject constructor(
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository,
    private val databaseItemSaver: DatabaseItemSaverRaclette,
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
    private val xmlConverterLazy: Lazy<DataIdentifierSharingXmlConverter>,
    override val vaultDataQuery: VaultDataQueryImplRaclette,
    override val dataSaver: Lazy<DataSaver>,
    private val dataSyncDaoRaclette: DataSyncDaoRaclette,
    private val racletteLogger: RacletteLogger
) : SharingDao {

    private val database: Database?
        get() = sessionManager.session?.let { userDataRepository.getRacletteDatabase(it) }

    private val sharingRepository: SharingRepository?
        get() = database?.sharingRepository

    private val vaultObjectRepository: VaultObjectRepository?
        get() = database?.vaultObjectRepository

    private val memorySummaryRepository: MemorySummaryRepository?
        get() = database?.memorySummaryRepository

    private val backupRepository: BackupRepository?
        get() {
            val database =
                sessionManager.session?.let { userDataRepository.getRacletteDatabase(it) }
            return database?.backupRepository
        }
    override val databaseOpen: Boolean
        get() = sharingRepository != null

    override suspend fun getItemsSummary(dataType: SharingDataType): List<Pair<String, Long>> =
        withContext(Dispatchers.IO) {
            val sharingRepository = sharingRepository ?: return@withContext emptyList()
            when (dataType) {
                SharingDataType.ITEM_GROUP -> sharingRepository.loadItemGroups()
                    .map { it.groupId to it.revision.toLong() }
                SharingDataType.USER_GROUP -> sharingRepository.loadUserGroups()
                    .map { it.groupId to it.revision.toLong() }
                SharingDataType.ITEM -> sharingRepository.loadItemContents()
                    .map { it.itemId to it.timestamp }
            }
        }

    override suspend fun updateItemTimestamp(uid: String, timestamp: Long) {
        val sharingRepository = sharingRepository ?: return
        val list = sharingRepository.loadItemContents()
        val updated = list.find { it.itemId == uid }?.copy(
            timestamp = timestamp
        ) ?: return
        sharingRepository.transaction { updateItemContents(listOf(updated)) }
    }

    override suspend fun getItemKeyTimestamp(uid: String): Pair<String, Long>? {
        val sharingRepository = sharingRepository ?: return null
        val itemContent = sharingRepository.loadItemContents().find { it.itemId == uid }
            ?: return null
        return itemContent.itemKeyBase64 to itemContent.timestamp
    }

    override fun isDirtyForSharing(id: String, type: SyncObjectType): Boolean {
        val vaultObjectRepository = vaultObjectRepository ?: return false
        return vaultObjectRepository[Id.of(id)]?.hasDirtySharedField ?: false
    }

    override suspend fun getDirtyForSharing(): List<DataIdentifierExtraDataWrapper<out SyncObject>> {
        val vaultObjectRepository = vaultObjectRepository ?: return emptyList()

        val ids = getDirtyItemIds() ?: return emptyList()
        return ids.mapNotNull {
            val id = Id.of(it)
            val vaultItem = vaultObjectRepository[id] ?: return@mapNotNull null
            val extraData = backupRepository?.load(id)
                ?.let { backup -> XmlSerializer.serializeTransaction(backup) }
            DataIdentifierExtraDataWrapper(
                vaultItem = vaultItem,
                extraData = extraData,
                backupDate = vaultItem.backupDate
            )
        }
    }

    override suspend fun markAsShared(uids: List<String>) {
        val vaultObjectRepository = vaultObjectRepository ?: return

        val ids = getDirtyItemIds() ?: return
        runCatching {
            val updated = ids.mapNotNull {
                val id = Id.of(it)
                val vaultItem = vaultObjectRepository[id] ?: return@mapNotNull null
                vaultItem.copy(
                    hasDirtySharedField = false
                )
            }
            vaultObjectRepository.transaction {
                updated.forEach { update(it) }
            }
        }.onFailure { racletteLogger.exception(it) }.getOrThrow()
    }

    override fun loadItemGroup(itemGroupUid: String): ItemGroup? {
        val sharingRepository = sharingRepository ?: return null
        return sharingRepository.loadItemGroups().find { it.groupId == itemGroupUid }?.toItemGroup()
    }

    override fun loadAllItemGroup(): List<ItemGroup> {
        val sharingRepository = sharingRepository ?: return emptyList()
        return sharingRepository.loadItemGroups().toItemGroups()
    }

    override fun loadAllUserGroup(): List<UserGroup> {
        val sharingRepository = sharingRepository ?: return emptyList()
        return sharingRepository.loadUserGroups().toUserGroups()
    }

    override fun loadUserGroupsAcceptedOrPending(userId: String): List<UserGroup> {
        val sharingRepository = sharingRepository ?: return emptyList()
        return sharingRepository.loadUserGroups().toUserGroups().filter {
            val found = it.users.find { user -> user.userId == userId } ?: return@filter false
            found.isAcceptedOrPending
        }
    }

    override suspend fun deleteItemGroups(
        memory: SharingDaoMemoryDataAccess,
        itemGroupsUid: List<String>
    ) {
        memory.itemGroups.filter {
            it.groupId in itemGroupsUid
        }.forEach {
            deleteLocallyItemGroupAndItems(memory, it)
        }
    }

    override suspend fun deleteLocallyItemGroupAndItems(
        memory: SharingDaoMemoryDataAccess,
        itemGroup: ItemGroup
    ) {
        itemGroup.items?.forEach { item ->
            val itemId = item.itemId
            
            memory.deleteItemContent(itemId)
            
            deleteDataIdentifier(itemId)
        }
        memory.delete(itemGroup)
    }

    override suspend fun saveAsLocalItem(
        identifier: String,
        extraData: String,
        userPermission: String
    ) = saveAsLocalItem(
        xmlConverterLazy.get().fromXml(identifier, extraData),
        userPermission
    )

    override suspend fun saveAsLocalItem(
        objectToSave: DataIdentifierExtraDataWrapper<out SyncObject>?,
        userPermission: String
    ) {
        objectToSave ?: return
        val teamspaceAccessor = teamspaceAccessorProvider.get() ?: return
        val vaultItem = objectToSave.vaultItem.let {
            it.copyWithAttrs { teamSpaceId = it.getSuggestedTeamspace(teamspaceAccessor)?.teamId }
        }
        val newObjectToSave = VaultItemBackupWrapper(
            vaultItem = vaultItem.copyWithAttrs {
                sharingPermission = userPermission
            },
            backup = objectToSave.extraData?.let { XmlDeserializer.deserializeTransaction(it) }
        )
        databaseItemSaver.saveItemFromSharingSync(newObjectToSave)
    }

    override fun loadItemContentExtraData(itemUid: String): String? {
        val sharingRepository = sharingRepository ?: return null
        return sharingRepository.loadItemContents().find { it.itemId == itemUid }?.extraData
    }

    override fun getExtraData(uid: String): String? = loadItemContentExtraData(uid)

    override fun loadItemGroupForItem(itemUID: String): ItemGroup? {
        val sharingRepository = sharingRepository ?: return null
        return sharingRepository.loadItemGroups().find { itemGroup ->
            itemGroup.items.any { it.itemId == itemUID }
        }?.toItemGroup()
    }

    override fun loadUserGroupsAccepted(userId: String): List<UserGroup>? {
        val sharingRepository = sharingRepository ?: return null
        return sharingRepository.loadUserGroups().filter { userGroup ->
            userGroup.users.any { it.userId == userId }
        }.toUserGroups()
    }

    override fun loadUserGroup(userGroupId: String): UserGroup? {
        return loadAllUserGroup().find { it.groupId == userGroupId }
    }

    override fun getItemWithExtraData(
        id: String,
        dataType: SyncObjectType
    ): DataIdentifierExtraDataWrapper<SyncObject>? =
        dataSyncDaoRaclette.getItemWithExtraData(id)?.toDataIdentifierExtraDataWrapper()

    private fun getDirtyItemIds(): List<String>? {
        val memorySummaryRepository = memorySummaryRepository ?: return null

        return memorySummaryRepository.databaseSyncSummary
            ?.items
            ?.filter { it.hasDirtySharedField && it.type in SyncObjectTypeUtils.SHAREABLE }
            ?.map { it.id }
    }
}
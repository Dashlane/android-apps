package com.dashlane.teamspaces.db

import com.dashlane.core.sharing.SharingDao
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.isAdmin
import com.dashlane.sharing.model.isLimited
import com.dashlane.sharing.model.isUserSolitaryAdmin
import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.space.NoRestrictionSpaceFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.db.TeamspaceDbUtils.prepareItemForDuplication
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingRevokeAllMembers
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingRevokeMe
import com.dashlane.vault.model.VaultItem
import javax.inject.Inject

open class TeamspaceForceDeletionSharingWorker @Inject constructor(
    private val sessionManager: SessionManager,
    private val sharingRevokeAllMembers: SharingRevokeAllMembers,
    private val sharingRevokeMe: SharingRevokeMe,
    private val mainDataAccessor: MainDataAccessor,
    private val dataStorageProvider: DataStorageProvider
) {
    private val vaultDataQuery: VaultDataQuery
        get() = mainDataAccessor.getVaultDataQuery()

    private val dataSaver: DataSaver
        get() = mainDataAccessor.getDataSaver()

    private val sharingDao: SharingDao
        get() = dataStorageProvider.sharingDao

    private val session: Session?
        get() = sessionManager.session

    open suspend fun revokeAll(sharedForcedItems: List<String>) {
        if (sharedForcedItems.isEmpty()) return
        val session = session ?: return
        val vaultItems = vaultDataQuery.queryAll(
            vaultFilter {
                ignoreUserLock()
                specificUid(sharedForcedItems)
                allStatusFilter()
                spaceFilter = NoRestrictionSpaceFilter
            }
        )
        val username = session.userId
        val itemGroups = getItemGroups(sharedForcedItems)
        itemGroups.forEach { itemGroup ->
            val me = itemGroup.getUser(username) ?: return@forEach
            val vaultItem = getItem(vaultItems, itemGroup) ?: return@forEach
            if (me.isLimited) {
                sharingRevokeMe.execute(itemGroup, vaultItem)
            } else if (me.isAdmin) {
                if (itemGroup.isUserSolitaryAdmin(username)) {
                    sharingRevokeAllMembers.execute(session, itemGroup)
                    
                } else {
                    sharingRevokeMe.execute(itemGroup, vaultItem)
                }
                duplicateItem(vaultItem)
            }
        }
    }

    private suspend fun duplicateItem(vaultItem: VaultItem<*>) {
        val updated = prepareItemForDuplication(vaultItem)
        dataSaver.save(updated)
    }

    private fun getItemGroups(
        itemsUidToRevoke: List<String>
    ): List<ItemGroup> {
        val allItemGroups = sharingDao.loadAllItemGroup()
        return allItemGroups.filter { it.items?.firstOrNull()?.itemId in itemsUidToRevoke }
    }

    private fun getItem(vaultItems: List<VaultItem<*>>, itemGroup: ItemGroup): VaultItem<*>? {
        val item = itemGroup.items?.first() ?: return null
        return vaultItems.find { item.itemId == it.uid }
    }
}
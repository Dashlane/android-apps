package com.dashlane.ui.screens.fragments

import com.dashlane.core.sharing.SharingDao
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.model.canLostAccess
import com.dashlane.sharing.model.isAcceptedOrPending
import com.dashlane.sharing.model.toPermission
import com.dashlane.storage.DataStorageProvider
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectTypeUtils.SHAREABLE
import javax.inject.Inject

class SharingPolicyDataProvider @Inject constructor(
    private val sessionManager: SessionManager,
    private val dataStorageProvider: DataStorageProvider,
    private val sharingDataProvider: SharingDataProvider
) {
    private val sharingDao: SharingDao
        get() = dataStorageProvider.sharingDao

    private fun getSharingPolicy(summaryObject: SummaryObject): Permission? {
        return if (summaryObject.isShared) {
            summaryObject.sharingPermission?.toPermission()
        } else {
            null
        }
    }

    fun canShareItem(summaryObject: SummaryObject): Boolean {
        if (summaryObject.syncObjectType !in SHAREABLE) return false
        val permission = getSharingPolicy(summaryObject)
        return permission == null || permission == Permission.ADMIN
    }

    fun canEditItem(summaryObject: SummaryObject, itemIsNew: Boolean): Boolean {
        return itemIsNew || getSharingPolicy(summaryObject).canEdit
    }

    private val Permission?.canEdit: Boolean
        get() = this == null || this == Permission.ADMIN

    fun getSharingCount(uid: String): Pair<Int, Int> {
        val itemGroup: ItemGroup = sharingDao.loadItemGroupForItem(uid) ?: return 0 to 0
        return getSharingCountUserAndUserGroup(itemGroup)
    }

    fun getSharingCountUserAndUserGroup(itemGroup: ItemGroup): Pair<Int, Int> {
        val c1 = itemGroup.users?.count { it.isAcceptedOrPending } ?: 0
        val c2 = itemGroup.groups?.count { it.isAcceptedOrPending } ?: 0
        return c1 to c2
    }

    fun isDeleteAllowed(uid: String, isNewItem: Boolean, isShared: Boolean): Boolean {
        if (isNewItem) {
            return false
        }
        if (!isShared) {
            return true
        }
        val session: Session = sessionManager.session ?: return false
        val username = session.userId
        val userGroups: List<UserGroup> = sharingDao.loadUserGroupsAcceptedOrPending(username)
        val itemGroup: ItemGroup = sharingDao.loadItemGroupForItem(uid) ?: return false
        return itemGroup.canLostAccess(username, userGroups)
    }

    fun isDeleteAllowed(isNewItem: Boolean, item: VaultItem<SyncObject>?): Boolean {
        item ?: return false
        return isDeleteAllowed(item.uid, isNewItem, item.isShared())
    }

    suspend fun doCancelSharingFor(
        item: VaultItem<SyncObject>
    ): Boolean {
        val itemGroup: ItemGroup = sharingDao.loadItemGroupForItem(item.uid)
            ?: return false
        sharingDataProvider.declineItemGroupInvite(itemGroup, item.toSummary(), true)
        return true
    }
}
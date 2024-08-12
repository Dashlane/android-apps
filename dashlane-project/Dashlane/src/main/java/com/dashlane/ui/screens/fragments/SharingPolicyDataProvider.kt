package com.dashlane.ui.screens.fragments

import com.dashlane.core.sharing.SharingDao
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.model.canLostAccess
import com.dashlane.sharing.model.isAccepted
import com.dashlane.sharing.model.isAcceptedOrPending
import com.dashlane.sharing.model.toPermission
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectTypeUtils.SHAREABLE
import javax.inject.Inject

class SharingPolicyDataProvider @Inject constructor(
    private val sessionManager: SessionManager,
    private val sharingDao: SharingDao,
    private val sharingDataProvider: SharingDataProvider
) {
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

    fun canEditItem(
        summaryObject: SummaryObject,
        itemIsNew: Boolean,
        isAccountFrozen: Boolean = false
    ): Boolean {
        return (itemIsNew || getSharingPolicy(summaryObject).canEdit) && !isAccountFrozen
    }

    private val Permission?.canEdit: Boolean
        get() = this == null || this == Permission.ADMIN

    fun getSharingCount(uid: String): Pair<Int, Int> {
        val itemGroup: ItemGroup = sharingDao.loadItemGroupForItemLegacy(uid) ?: return 0 to 0
        return getSharingCountUserAndUserGroup(itemGroup)
    }

    fun getSharingCountUserAndUserGroup(itemGroup: ItemGroup): Pair<Int, Int> {
        val collections = sharingDao.loadAllCollectionLegacy().filter { collection ->
            itemGroup.collections?.any { collection.uuid == it.uuid && it.isAccepted } == true
        }
        val collectionUsers = collections.mapNotNull {
            it.users?.filter { user -> user.isAcceptedOrPending }
        }.flatten().distinctBy { it.login }
        val c1 = itemGroup.users?.distinctBy { it.userId }?.count {
            it.isAcceptedOrPending && !collectionUsers.any { cUser ->
                cUser.login == it.userId || cUser.login == it.alias
            }
        } ?: 0
        val collectionGroups = collections.mapNotNull {
            it.userGroups?.filter { group -> group.isAcceptedOrPending }
        }.flatten().distinctBy { it.uuid }
        val c2 = itemGroup.groups?.distinctBy { it.groupId }?.count {
            it.isAcceptedOrPending && !collectionGroups.any { cGroup -> cGroup.uuid == it.groupId }
        } ?: 0
        return c1 + collectionUsers.size to c2 + collectionGroups.size
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
        val myUserGroups: List<UserGroup> = sharingDao.loadUserGroupsAcceptedOrPendingLegacy(username)
        val myCollections: List<Collection> =
            sharingDao.loadCollectionsAcceptedOrPending(username, myUserGroups)

        val itemGroup: ItemGroup = sharingDao.loadItemGroupForItemLegacy(uid) ?: return false
        return itemGroup.canLostAccess(username, myUserGroups, myCollections)
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
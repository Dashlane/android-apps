package com.dashlane.ui.screens.fragments.userdata.sharing.group

import com.dashlane.core.sharing.SharingDao
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.getUserGroup
import com.dashlane.sharing.model.getUserGroupMember
import com.dashlane.sharing.model.isAccepted
import com.dashlane.sharing.model.isAdmin
import com.dashlane.sharing.model.isPending
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.datatype.ShareableDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingUserGroupUser
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.ui.screens.fragments.userdata.sharing.getSharingStatusResource
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserGroupDataProvider @Inject constructor(
    private val sharingDao: SharingDao,
    private val genericDataQuery: GenericDataQuery,
    private val sharingDataProvider: SharingDataProvider,
    @IoCoroutineDispatcher
    private val ioCoroutineDispatcher: CoroutineDispatcher
) {
    suspend fun getItemsForUserGroup(userGroupId: String):
        List<SharingModels.ItemUserGroup> {
        return withContext(ioCoroutineDispatcher) {
            val acceptedCollections = sharingDataProvider.getAcceptedCollectionsForGroup(userGroupId)
            val itemGroups = sharingDao.loadAllItemGroup()
                .filter {
                    it.getUserGroupMember(userGroupId)?.isAccepted == true ||
                        acceptedCollections.any { collection ->
                            collection.getUserGroup(userGroupId)?.isAccepted == true
                        }
                }
            val result = arrayListOf<SharingModels.ItemUserGroup>()
            itemGroups.forEach { itemGroup ->
                val collectionGroup = acceptedCollections.filter { collection ->
                    itemGroup.collections?.any {
                        it.uuid == collection.uuid && it.isAccepted &&
                            collection.getUserGroup(userGroupId)?.isAccepted == true
                    } == true
                }.takeIf { it.isNotEmpty() }?.firstOrNull()?.getUserGroup(userGroupId)
                val items = itemGroup.items ?: return@forEach
                val vaultItems = genericDataQuery.queryAll(
                    genericFilter {
                        dataTypeFilter = ShareableDataTypeFilter
                        specificUid(items.map { it.itemId })
                    }
                )
                val userGroup = itemGroup.getUserGroupMember(userGroupId)
                if (userGroup == null && collectionGroup == null) return@forEach
                vaultItems.forEach {
                    result.add(
                        SharingModels.ItemUserGroup(
                            groupId = userGroup?.groupId ?: collectionGroup!!.uuid,
                            name = userGroup?.name ?: collectionGroup!!.name,
                            isAccepted = userGroup?.isAccepted ?: collectionGroup!!.isAccepted,
                            isPending = userGroup?.isPending ?: collectionGroup!!.isPending,
                            isMemberAdmin = userGroup?.isAdmin ?: collectionGroup!!.isAdmin,
                            sharingStatusResource = userGroup?.getSharingStatusResource()
                                ?: collectionGroup!!.getSharingStatusResource(),
                            itemGroup = itemGroup,
                            item = it,
                            isItemInCollection = collectionGroup != null
                        )
                    )
                }
            }
            result
        }
    }

    suspend fun getMembersForUserGroup(
        userGroupId: String,
        userId: String
    ): List<SharingUserGroupUser> {
        return withContext(ioCoroutineDispatcher) {
            val userGroup = sharingDao.loadUserGroup(userGroupId)?.takeIf {
                it.getUser(userId)?.isAccepted == true
            } ?: return@withContext emptyList()
            userGroup.users.map {
                SharingUserGroupUser(userGroup = userGroup, user = it)
            }
        }
    }
}

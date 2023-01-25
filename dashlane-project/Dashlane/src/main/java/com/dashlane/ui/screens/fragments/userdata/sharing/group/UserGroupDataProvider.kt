package com.dashlane.ui.screens.fragments.userdata.sharing.group

import com.dashlane.core.sharing.SharingDao
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.getUserGroupMember
import com.dashlane.sharing.model.isAccepted
import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.datatype.ShareableDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingUserGroupUser
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject



class UserGroupDataProvider @Inject constructor(
    private val dataStorageProvider: DataStorageProvider,
    private val mainDataAccessor: MainDataAccessor,
    @IoCoroutineDispatcher
    private val ioCoroutineDispatcher: CoroutineDispatcher
) {
    private val genericDataQuery: GenericDataQuery
        get() = mainDataAccessor.getGenericDataQuery()

    private val sharingDao: SharingDao
        get() = dataStorageProvider.sharingDao

    suspend fun getItemsForUserGroup(userGroupId: String):
            List<SharingModels.ItemUserGroup> {
        return withContext(ioCoroutineDispatcher) {
            val itemGroups = sharingDao.loadAllItemGroup()
                .filter { it.getUserGroupMember(userGroupId)?.isAccepted == true }

            val result = arrayListOf<SharingModels.ItemUserGroup>()
            itemGroups.forEach { itemGroup ->
                val items = itemGroup.items ?: return@forEach
                val vaultItems = genericDataQuery.queryAll(
                    genericFilter {
                        dataTypeFilter = ShareableDataTypeFilter
                        specificUid(items.map { it.itemId })
                    }
                )
                vaultItems.forEach {
                    result.add(
                        SharingModels.ItemUserGroup(
                            userGroup = checkNotNull(itemGroup.getUserGroupMember(userGroupId)),
                            itemGroup = itemGroup,
                            item = it
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

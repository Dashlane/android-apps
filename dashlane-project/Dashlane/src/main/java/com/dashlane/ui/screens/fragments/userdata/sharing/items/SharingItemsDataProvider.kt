package com.dashlane.ui.screens.fragments.userdata.sharing.items

import com.dashlane.core.sharing.SharingDao
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.hasUserGroupsAccepted
import com.dashlane.sharing.model.isAcceptedOrPending
import com.dashlane.sharing.model.isUserAccepted
import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.datatype.ShareableDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject



class SharingItemsDataProvider @Inject constructor(
    private val dataStorageProvider: DataStorageProvider,
    private val sessionManager: SessionManager,
    private val mainDataAccessor: MainDataAccessor,
    @IoCoroutineDispatcher
    private val ioCoroutineDispatcher: CoroutineDispatcher
) {
    private val genericDataQuery: GenericDataQuery
        get() = mainDataAccessor.getGenericDataQuery()

    private val sharingDao: SharingDao
        get() = dataStorageProvider.sharingDao

    private val session: Session?
        get() = sessionManager.session

    suspend fun getItemsForUser(username: String, memberLogin: String):
            List<SharingModels.ItemUser> {
        val login: String = session?.userId ?: return emptyList()
        return withContext(ioCoroutineDispatcher) {
            val itemGroups = sharingDao.loadAllItemGroup()
            val myUserGroupsAccepted = sharingDao.loadUserGroupsAccepted(username) ?: emptyList()
            val result = arrayListOf<SharingModels.ItemUser>()
            itemGroups.forEach { itemGroup ->
                val contactUser = itemGroup.getUser(memberLogin)
                val isSharedWithMe: Boolean = itemGroup.isUserAccepted(login)
                val isSharedWithMyUserGroups: Boolean =
                    itemGroup.hasUserGroupsAccepted(myUserGroupsAccepted)
                if (!isSharedWithMe && !isSharedWithMyUserGroups) return@forEach
                if (contactUser?.isAcceptedOrPending != true) return@forEach
                val items = itemGroup.items ?: return@forEach
                val vaultItems = genericDataQuery.queryAll(
                    genericFilter {
                        dataTypeFilter = ShareableDataTypeFilter
                        specificUid(items.map { it.itemId })
                    }
                )
                vaultItems.forEach {
                    result.add(
                        SharingModels.ItemUser(
                            user = contactUser,
                            itemGroup = itemGroup,
                            item = it
                        )
                    )
                }
            }
            result
        }
    }
}

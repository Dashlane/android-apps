package com.dashlane.ui.screens.fragments.userdata.sharing.items

import com.dashlane.core.sharing.SharingDao
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.hasUserGroupsAccepted
import com.dashlane.sharing.model.isAccepted
import com.dashlane.sharing.model.isAcceptedOrPending
import com.dashlane.sharing.model.isAdmin
import com.dashlane.sharing.model.isPending
import com.dashlane.sharing.model.isUserAccepted
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.datatype.ShareableDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.ui.screens.fragments.userdata.sharing.getSharingStatusResource
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SharingItemsDataProvider @Inject constructor(
    private val sharingDao: SharingDao,
    private val sharingDataProvider: SharingDataProvider,
    private val sessionManager: SessionManager,
    private val genericDataQuery: GenericDataQuery,
    @IoCoroutineDispatcher
    private val ioCoroutineDispatcher: CoroutineDispatcher
) {
    private val session: Session?
        get() = sessionManager.session

    @Suppress("ComplexMethod")
    suspend fun getItemsForUser(username: String, memberLogin: String):
        List<SharingModels.ItemUser> {
        val login: String = session?.userId ?: return emptyList()
        return withContext(ioCoroutineDispatcher) {
            val itemGroups = sharingDao.loadAllItemGroup()
            val myUserGroupsAccepted = sharingDao.loadUserGroupsAccepted(username) ?: emptyList()
            val myCollectionsAccepted = sharingDataProvider.getAcceptedCollections(username, needsAdminRights = false)
            val result = arrayListOf<SharingModels.ItemUser>()
            itemGroups.forEach { itemGroup ->
                val contactUser = itemGroup.getUser(memberLogin)
                val isSharedWithMe: Boolean = itemGroup.isUserAccepted(login)
                val isSharedWithMyUserGroups: Boolean =
                    itemGroup.hasUserGroupsAccepted(myUserGroupsAccepted)
                val collectionsShared =
                    myCollectionsAccepted.filter { collection ->
                        itemGroup.collections?.any {
                            it.uuid == collection.uuid && it.isAccepted
                        } == true
                    }
                val collectionUser = collectionsShared.mapNotNull {
                    val user = it.getUser(memberLogin)
                    if (user == null || !user.isAcceptedOrPending) return@mapNotNull null
                    return@mapNotNull user
                }.firstOrNull()
                val isInCollection = collectionsShared.isNotEmpty()
                if (!isSharedWithMe && !isSharedWithMyUserGroups && !isInCollection) return@forEach
                if (contactUser?.isAcceptedOrPending != true && collectionUser == null) return@forEach
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
                            userId = contactUser?.userId ?: collectionUser!!.login,
                            isAccepted = contactUser?.isAccepted ?: collectionUser!!.isAccepted,
                            isPending = contactUser?.isPending ?: collectionUser!!.isPending,
                            isMemberAdmin = contactUser?.isAdmin ?: collectionUser!!.isAdmin,
                            sharingStatusResource = contactUser?.getSharingStatusResource()
                                ?: collectionUser!!.getSharingStatusResource(),
                            itemGroup = itemGroup,
                            item = it,
                            isItemInCollection = isInCollection
                        )
                    )
                }
            }
            result
        }
    }
}

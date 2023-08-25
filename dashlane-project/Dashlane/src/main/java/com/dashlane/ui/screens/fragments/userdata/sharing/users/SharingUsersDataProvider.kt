package com.dashlane.ui.screens.fragments.userdata.sharing.users

import com.dashlane.core.sharing.SharingDao
import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SharingUsersDataProvider @Inject constructor(
    private val dataStorageProvider: DataStorageProvider,
    private val mainDataAccessor: MainDataAccessor,
    @IoCoroutineDispatcher
    private val ioCoroutineDispatcher: CoroutineDispatcher
) {
    private val genericDataQuery: GenericDataQuery
        get() = mainDataAccessor.getGenericDataQuery()

    private val sharingDao: SharingDao
        get() = dataStorageProvider.sharingDao

    suspend fun getContactsForItem(itemId: String):
            List<SharingModels> {
        return withContext(ioCoroutineDispatcher) {
            val itemGroup = sharingDao.loadItemGroupForItem(itemId)
                ?: return@withContext emptyList<SharingModels>()
            val summaryObject = genericDataQuery.queryFirst(
                genericFilter {
                specificUid(itemId)
            }
            ) ?: return@withContext emptyList<SharingModels>()

            val groups = itemGroup.groups?.map {
                SharingModels.ItemUserGroup(
                    itemGroup = itemGroup,
                    userGroup = it,
                    item = summaryObject
                )
            } ?: emptyList()

            val users = itemGroup.users?.map {
                SharingModels.ItemUser(
                    itemGroup = itemGroup,
                    user = it,
                    item = summaryObject
                )
            } ?: emptyList()
            groups + users
        }
    }
}

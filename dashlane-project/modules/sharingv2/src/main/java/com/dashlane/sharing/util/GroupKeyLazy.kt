package com.dashlane.sharing.util

import com.dashlane.cryptography.CryptographyKey
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.sharing.model.hasCollectionsAcceptedOrPending
import com.dashlane.sharing.model.hasUserGroupsAcceptedOrPending
import com.dashlane.sharing.model.isUserAcceptedOrPending

class GroupKeyLazy private constructor(
    private val itemGroup: ItemGroup,
    private val userId: String,
    private val myUserGroupsAcceptedOrPending: List<UserGroup>,
    private val myCollectionsAcceptedOrPending: List<Collection>,
    private val sharingCryptography: SharingCryptographyHelper
) {
    private var groupKey: CryptographyKey.Raw32? = null
    fun get(): CryptographyKey.Raw32? {
        if (groupKey == null) {
            groupKey = sharingCryptography.getItemGroupKey(
                itemGroup = itemGroup,
                userId = userId,
                myUserGroups = myUserGroupsAcceptedOrPending,
                myCollections = myCollectionsAcceptedOrPending
            )
        }
        return groupKey
    }

    companion object {
        fun newInstance(
            itemGroup: ItemGroup,
            userId: String,
            myUserGroupsAcceptedOrPending: List<UserGroup>,
            myCollectionsAcceptedOrPending: List<Collection>,
            sharingCryptography: SharingCryptographyHelper
        ): GroupKeyLazy? {
            return if (itemGroup.isUserAcceptedOrPending(userId) ||
                itemGroup.hasUserGroupsAcceptedOrPending(myUserGroupsAcceptedOrPending) ||
                itemGroup.hasCollectionsAcceptedOrPending(myCollectionsAcceptedOrPending)
            ) {
                GroupKeyLazy(
                    itemGroup = itemGroup,
                    userId = userId,
                    myUserGroupsAcceptedOrPending = myUserGroupsAcceptedOrPending,
                    myCollectionsAcceptedOrPending = myCollectionsAcceptedOrPending,
                    sharingCryptography = sharingCryptography
                )
            } else {
                null
            }
        }
    }
}
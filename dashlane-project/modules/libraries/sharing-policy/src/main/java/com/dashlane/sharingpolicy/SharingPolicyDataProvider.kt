package com.dashlane.sharingpolicy

import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

interface SharingPolicyDataProvider {
    fun canShareItem(summaryObject: SummaryObject): Boolean
    fun canEditItem(summaryObject: SummaryObject, itemIsNew: Boolean, isAccountFrozen: Boolean = false): Boolean
    fun getSharingCount(uid: String): Pair<Int, Int>
    fun getSharingCountUserAndUserGroup(itemGroup: ItemGroup): Pair<Int, Int>
    fun isDeleteAllowed(uid: String, isNewItem: Boolean, isShared: Boolean): Boolean
    fun isDeleteAllowed(isNewItem: Boolean, item: VaultItem<SyncObject>?): Boolean
    suspend fun doCancelSharingFor(item: VaultItem<SyncObject>): Boolean
}
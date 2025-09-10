package com.dashlane.vault.summary

import com.dashlane.vault.model.VaultItem

fun VaultItem<*>.toSyncSummary(): SyncSummaryItem =
    SyncSummaryItem(
        id = this.uid,
        type = this.syncObjectType,
        syncState = this.syncState,
        backupDate = this.backupDate,
        hasDirtySharedField = this.hasDirtySharedField,
        userContentData = this.syncObject.userContentData.hashCode(),
    )

fun List<SyncSummaryItem>.groupBySyncObjectType() = groupBy { it.type }
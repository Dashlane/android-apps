package com.dashlane.item.logger

import com.dashlane.item.subview.edit.ItemEditSpaceSubView
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObjectType

interface Logger {

    

    fun log(log: UsageLog)

    

    fun logItemAdded(
        vaultItem: VaultItem<*>,
        dataType: SyncObjectType,
        categorizationMethod: ItemEditSpaceSubView.CategorizationMethod?
    )

    

    fun logItemModified(
        vaultItem: VaultItem<*>,
        dataType: SyncObjectType,
        categorizationMethod: ItemEditSpaceSubView.CategorizationMethod?
    )

    

    fun logItemDeleted(vaultItem: VaultItem<*>, dataType: SyncObjectType)

    

    fun logDisplay(dataType: SyncObjectType)
}
package com.dashlane.attachment

import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.toItemTypeOrNull

class VaultItemLogAttachmentHelper(
    private val vaultItemLogger: VaultItemLogger,
    item: VaultItem<*>
) {
    private val itemIdForLog = item.uid
    private val itemType = item.syncObjectType.toItemTypeOrNull()

    private fun onLogConditionsFulfilled(block: (itemId: String, itemType: ItemType) -> Unit) {
        if (itemType == null) return
        block(itemIdForLog, itemType)
    }

    fun logDownload() {
        onLogConditionsFulfilled { itemId, itemType ->
            vaultItemLogger.logAttachmentDownload(
                itemId = itemId,
                itemType = itemType
            )
        }
    }

    fun logUpdate(action: Action) {
        onLogConditionsFulfilled { itemId, itemType ->
            vaultItemLogger.logAttachmentUpdate(
                action = action,
                itemId = itemId,
                itemType = itemType
            )
        }
    }

    fun logView() {
        onLogConditionsFulfilled { itemId, itemType ->
            vaultItemLogger.logAttachmentView(
                itemId = itemId,
                itemType = itemType
            )
        }
    }
}
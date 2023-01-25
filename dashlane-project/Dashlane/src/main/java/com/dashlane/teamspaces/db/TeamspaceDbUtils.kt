package com.dashlane.teamspaces.db

import com.dashlane.util.generateUniqueIdentifier
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem

object TeamspaceDbUtils {

    @JvmStatic
    fun prepareItemForDuplication(item: VaultItem<*>): VaultItem<*> {
        
        return item.copyWithAttrs {
            id = 0 
            uid = generateUniqueIdentifier()
            
            syncState = SyncState.MODIFIED
            sharingPermission = null
        }
    }
}
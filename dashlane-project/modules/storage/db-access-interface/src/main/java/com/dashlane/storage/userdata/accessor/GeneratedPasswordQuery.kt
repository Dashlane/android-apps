package com.dashlane.storage.userdata.accessor

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

interface GeneratedPasswordQuery {
    fun queryAllNotRevoked(): List<VaultItem<SyncObject.GeneratedPassword>>
}
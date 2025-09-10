package com.dashlane.sync

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.XmlTransaction
import com.dashlane.xml.domain.SyncObject

data class VaultItemBackupWrapper<T : SyncObject>(
    val vaultItem: VaultItem<T>,
    val backup: XmlTransaction?,
)
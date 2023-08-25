package com.dashlane.sync

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.serializer.XmlSerializer
import java.time.Instant

data class DataIdentifierExtraDataWrapper<T : SyncObject>(
    val vaultItem: VaultItem<T>,
    val extraData: String?,
    val backupDate: Instant? = null
)

fun VaultItemBackupWrapper<SyncObject>.toDataIdentifierExtraDataWrapper(): DataIdentifierExtraDataWrapper<SyncObject> =
    DataIdentifierExtraDataWrapper(
        vaultItem = this.vaultItem,
        extraData = this.backup?.let { XmlSerializer.serializeTransaction(it) },
        backupDate = this.vaultItem.backupDate
    )
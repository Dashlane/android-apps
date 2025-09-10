package com.dashlane.item.v3.util

import android.content.Context
import com.dashlane.storage.userdata.accessor.copyWithDefaultValue
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.hasBeenSaved
import java.time.Instant

internal fun VaultItem<*>.fillDefaultValue(context: Context): VaultItem<*> {
    val itemToSave = if (!hasBeenSaved) {
        copyWithDefaultValue(context)
    } else {
        this
    }
    return itemToSave.copyWithAttrs {
        creationDate = itemToSave.syncObject.creationDatetime ?: Instant.now()
        userModificationDate = Instant.now()
        setStateModifiedIfNotDeleted()
    }
}
package com.dashlane.item.v3.util

import android.content.Context
import com.dashlane.session.Session
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.hasBeenSaved
import com.dashlane.vault.util.copyWithDefaultValue
import java.time.Instant

internal fun VaultItem<*>.fillDefaultValue(context: Context, session: Session?): VaultItem<*> {
    val itemToSave = if (hasBeenSaved) {
        copyWithDefaultValue(context, session)
    } else {
        this
    }
    return itemToSave.copyWithAttrs {
        creationDate = itemToSave.syncObject.creationDatetime ?: Instant.now()
        userModificationDate = Instant.now()
        setStateModifiedIfNotDeleted()
    }
}
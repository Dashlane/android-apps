package com.dashlane.database.converter

import android.database.Cursor
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObjectType



fun Cursor.getDataIdentifiers(type: SyncObjectType) =
    DbConverter.fromCursorToList(this, type)



fun VaultItem<*>.toContentValues() =
    DbConverter.toContentValues(this)
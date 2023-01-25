package com.dashlane.sync

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.converter.DbConverter
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.util.getLong
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import java.time.Instant



object WithExtraDataDbConverter {

    @JvmStatic
    fun <T : SyncObject> cursorToItem(c: Cursor, vaultItem: VaultItem<T>): DataIdentifierExtraDataWrapper<T> {
        val extraData = c.getString(DataIdentifierSql.FIELD_EXTRA)
        val backupDate = Instant.ofEpochSecond(c.getLong(DataIdentifierSql.FIELD_BACKUP_DATE))
        return DataIdentifierExtraDataWrapper(vaultItem, extraData, backupDate)
    }

    fun toContentValues(vaultItem: VaultItem<*>, extraData: String?, backupDate: Instant?): ContentValues? {
        return DbConverter.toContentValues(vaultItem)?.apply {
            addSyncInformation(this, extraData, backupDate)
        }
    }

    fun addSyncInformation(contentValues: ContentValues, extraData: String?, backupDate: Instant?) {
        extraData?.also { contentValues.put(DataIdentifierSql.FIELD_EXTRA, extraData) }
        backupDate?.epochSecond?.takeIf { it > 0L }?.let { contentValues.put(DataIdentifierSql.FIELD_BACKUP_DATE, it) }
    }
}
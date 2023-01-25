package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.SecureNoteCategorySql
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createSecureNoteCategory
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object SecureNoteCategoryDbConverter : DbConverter.Delegate<SyncObject.SecureNoteCategory> {
    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.SecureNoteCategory> {
        return createSecureNoteCategory(
            dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            title = c.getString(SecureNoteCategorySql.FIELD_TITLE)
        )
    }

    override fun syncObjectType() = SyncObjectType.SECURE_NOTE_CATEGORY

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.SecureNoteCategory>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.SecureNoteCategory>): ContentValues? {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        cv.put(SecureNoteCategorySql.FIELD_TITLE, item.categoryName)
        return cv
    }
}

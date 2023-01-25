package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.database.sql.SecureNoteSql
import com.dashlane.util.getBoolean
import com.dashlane.util.getString
import com.dashlane.util.tryOrNull
import com.dashlane.vault.model.DataIdentifierAttrsMutable
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createSecureNote
import com.dashlane.vault.model.getSecureNoteTypeDeprecatedDatabaseOrder
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object SecureNoteDbConverter : DbConverter.Delegate<SyncObject.SecureNote> {
    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.SecureNote> {
        val dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType())
        val typeInDb = c.getString(SecureNoteSql.FIELD_TYPE)
        val type = tryOrNull { getSecureNoteTypeDeprecatedDatabaseOrder()[Integer.valueOf(typeInDb!!)] }
            ?: SyncObject.SecureNoteType.NO_TYPE

        return createSecureNote(
            dataIdentifier = DataIdentifierAttrsMutable.with(dataIdentifier) {
                sharingPermission = c.getString(DataIdentifierSql.FIELD_SHARING_PERMISSION)
                hasDirtySharedField = c.getBoolean(DataIdentifierSql.FIELD_HAS_DIRTY_SHARED_FIELD)
            },
            title = c.getString(SecureNoteSql.FIELD_TITLE),
            type = type,
            category = c.getString(SecureNoteSql.FIELD_CATEGORY),
            content = c.getString(SecureNoteSql.FIELD_CONTENT),
            isSecured = Integer.valueOf(c.getString(SecureNoteSql.FIELD_SECURED)!!) == 1
        )
    }

    override fun syncObjectType() = SyncObjectType.SECURE_NOTE

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.SecureNote>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.SecureNote>): ContentValues {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        cv.put(SecureNoteSql.FIELD_TYPE, getSecureNoteTypeDeprecatedDatabaseOrder().indexOf(item.type).toString())
        cv.put(SecureNoteSql.FIELD_TITLE, item.title)
        cv.put(SecureNoteSql.FIELD_CATEGORY, item.category)
        cv.put(SecureNoteSql.FIELD_CONTENT, item.content)
        cv.put(SecureNoteSql.FIELD_SECURED, if (item.secured == true) 1 else 0)
        cv.put(DataIdentifierSql.FIELD_SHARING_PERMISSION, vaultItem.sharingPermission)
        cv.put(DataIdentifierSql.FIELD_HAS_DIRTY_SHARED_FIELD, vaultItem.hasDirtySharedField)
        return cv
    }
}
package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.EmailSql
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createEmail
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object EmailDbConverter : DbConverter.Delegate<SyncObject.Email> {
    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.Email> {
        val emailFromOldValue = when (c.getString(EmailSql.FIELD_EMAIL_TYPE)?.toIntOrNull()) {
            101 -> SyncObject.Email.Type.PERSO
            102 -> SyncObject.Email.Type.PRO
            else -> null
        }

        return createEmail(
            dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            emailAddress = c.getString(EmailSql.FIELD_EMAIL_ADDRESS),
            emailName = c.getString(EmailSql.FIELD_EMAIL_NAME),
            type = emailFromOldValue
        )
    }

    override fun syncObjectType() = SyncObjectType.EMAIL

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.Email>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.Email>): ContentValues {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        val emailTypeOldValue = when (item.type) {
            SyncObject.Email.Type.PERSO -> "101"
            SyncObject.Email.Type.PRO -> "102"
            else -> ""
        }
        cv.put(EmailSql.FIELD_EMAIL_TYPE, emailTypeOldValue)
        cv.put(EmailSql.FIELD_EMAIL_NAME, item.emailName ?: "")
        cv.put(EmailSql.FIELD_EMAIL_ADDRESS, item.email ?: "")
        return cv
    }
}

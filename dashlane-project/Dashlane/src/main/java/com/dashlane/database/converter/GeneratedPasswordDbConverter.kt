package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.GeneratedPasswordSql
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createGeneratedPassword
import com.dashlane.xml.SyncObjectEnum
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object GeneratedPasswordDbConverter : DbConverter.Delegate<SyncObject.GeneratedPassword> {
    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.GeneratedPassword> {
        return createGeneratedPassword(
            dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            authDomain = c.getString(GeneratedPasswordSql.FIELD_AUTH_DOMAIN),
            generatedDate = c.getString(GeneratedPasswordSql.FIELD_GENERATED_DATE),
            password = c.getString(GeneratedPasswordSql.FIELD_PASSWORD),
            authId = c.getString(GeneratedPasswordSql.FIELD_AUTH_ID),
            platform = c.getString(GeneratedPasswordSql.FIELD_PLATFORM)?.let { SyncObjectEnum.getEnumForValue(it) }
        )
    }

    override fun syncObjectType() = SyncObjectType.GENERATED_PASSWORD

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.GeneratedPassword>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.GeneratedPassword>): ContentValues {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        cv.put(GeneratedPasswordSql.FIELD_AUTH_DOMAIN, item.domain ?: "")
        cv.put(GeneratedPasswordSql.FIELD_GENERATED_DATE, item.generatedDate?.toString() ?: "")
        cv.put(GeneratedPasswordSql.FIELD_PASSWORD, item.password?.toString() ?: "")
        cv.put(GeneratedPasswordSql.FIELD_AUTH_ID, item.authId ?: "")
        cv.put(GeneratedPasswordSql.FIELD_PLATFORM, item.platform?.value)
        return cv
    }
}

package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.database.sql.PhoneSql
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createPhone
import com.dashlane.vault.model.signature
import com.dashlane.xml.SyncObjectEnum
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object PhoneDbConverter : DbConverter.Delegate<SyncObject.Phone> {
    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.Phone> {
        return createPhone(
            dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            type = c.getString(PhoneSql.FIELD_TYPE)?.let { SyncObjectEnum.getEnumForValue(it) },
            phoneNumber = c.getString(PhoneSql.FIELD_PHONE_NUMBER),
            phoneName = c.getString(PhoneSql.FIELD_PHONE_NAME)
        )
    }

    override fun syncObjectType() = SyncObjectType.PHONE

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.Phone>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.Phone>): ContentValues? {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        cv.put(PhoneSql.FIELD_TYPE, item.type?.value)
        cv.put(DataIdentifierSql.FIELD_LOCALE_LANG, item.localeFormat.signature)
        cv.put(PhoneSql.FIELD_PHONE_NUMBER, item.number)
        cv.put(PhoneSql.FIELD_PHONE_NAME, item.phoneName)
        return cv
    }
}

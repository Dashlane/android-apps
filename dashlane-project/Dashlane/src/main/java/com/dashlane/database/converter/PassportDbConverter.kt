package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.PassportSql
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createPassport
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object PassportDbConverter : DbConverter.Delegate<SyncObject.Passport> {
    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.Passport> {
        val sex = c.getString(PassportSql.FIELD_SEX)?.let {
            when (it.toIntOrNull()) {
                0 -> SyncObject.Gender.MALE
                1 -> SyncObject.Gender.FEMALE
                else -> null
            }
        }
        return createPassport(
            dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            number = c.getString(PassportSql.FIELD_NUMBER),
            fullname = c.getString(PassportSql.FIELD_FULLNAME),
            sex = sex,
            dateOfBirth = TimedDocumentDbConverter.getLocalDate(c, PassportSql.FIELD_DATE_OF_BIRTH),
            deliveryPlace = c.getString(PassportSql.FIELD_DELIVERY_PLACE),
            linkedIdentity = c.getString(PassportSql.FIELD_LINKED_IDENTITY),
            deliveryDate = c.getDeliveryDateField(),
            expireDate = c.getExpireDateField()
        )
    }

    override fun syncObjectType() = SyncObjectType.PASSPORT

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.Passport>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.Passport>): ContentValues? {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)
        val genderValue = when (item.sex) {
            SyncObject.Gender.MALE -> "0"
            SyncObject.Gender.FEMALE -> "1"
            else -> null
        }

        TimedDocumentDbConverter.putDeliveryDate(cv, item.deliveryDate)
        TimedDocumentDbConverter.putExpireDate(cv, item.expireDate)
        cv.put(PassportSql.FIELD_NUMBER, item.number)
        cv.put(PassportSql.FIELD_FULLNAME, item.fullname)
        cv.put(PassportSql.FIELD_SEX, genderValue)
        TimedDocumentDbConverter.putLocalDate(cv, PassportSql.FIELD_DATE_OF_BIRTH, item.dateOfBirth)
        cv.put(PassportSql.FIELD_DELIVERY_PLACE, item.deliveryPlace)
        cv.put(PassportSql.FIELD_LINKED_IDENTITY, item.linkedIdentity)

        return cv
    }
}

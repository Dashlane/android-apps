package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.database.sql.DriverLicenceSql
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createDriverLicence
import com.dashlane.vault.model.signature
import com.dashlane.xml.SyncObjectEnum
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object DriverLicenceDbConverter : DbConverter.Delegate<SyncObject.DriverLicence> {
    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.DriverLicence> {
        return createDriverLicence(
            dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            dateOfBirth = TimedDocumentDbConverter.getLocalDate(c, DriverLicenceSql.FIELD_DATE_OF_BIRTH),
            fullName = c.getString(DriverLicenceSql.FIELD_FULLNAME),
            number = c.getString(DriverLicenceSql.FIELD_NUMBER),
            sex = c.getString(DriverLicenceSql.FIELD_SEX)?.let { SyncObjectEnum.getEnumForValue(it.uppercase()) },
            state = c.getString(DriverLicenceSql.FIELD_STATE),
            linkedIdentity = c.getString(DriverLicenceSql.FIELD_LINKED_IDENTITY),
            deliveryDate = c.getDeliveryDateField(),
            expireDate = c.getExpireDateField()
        )
    }

    override fun syncObjectType() = SyncObjectType.DRIVER_LICENCE

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.DriverLicence>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.DriverLicence>): ContentValues {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        TimedDocumentDbConverter.putDeliveryDate(cv, item.deliveryDate)
        TimedDocumentDbConverter.putExpireDate(cv, item.expireDate)
        TimedDocumentDbConverter.putLocalDate(cv, DriverLicenceSql.FIELD_DATE_OF_BIRTH, item.dateOfBirth)
        cv.put(DriverLicenceSql.FIELD_FULLNAME, item.fullname)
        cv.put(DataIdentifierSql.FIELD_LOCALE_LANG, item.localeFormat.signature)
        cv.put(DriverLicenceSql.FIELD_NUMBER, item.number)
        cv.put(DriverLicenceSql.FIELD_SEX, item.sex?.value)
        cv.put(DriverLicenceSql.FIELD_STATE, item.state)
        cv.put(DriverLicenceSql.FIELD_LINKED_IDENTITY, item.linkedIdentity)
        return cv
    }
}
